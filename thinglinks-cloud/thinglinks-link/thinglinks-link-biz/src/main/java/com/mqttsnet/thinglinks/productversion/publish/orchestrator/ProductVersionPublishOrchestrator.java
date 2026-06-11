package com.mqttsnet.thinglinks.productversion.publish.orchestrator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import cn.hutool.core.collection.CollUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.tds.constant.TdsConstants;
import com.mqttsnet.basic.tds.enumeration.TdDataTypeEnum;
import com.mqttsnet.basic.tds.model.FieldsVO;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.thinglinks.cache.product.ProductModelCacheService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.product.enumeration.ProductTypeEnum;
import com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord;
import com.mqttsnet.thinglinks.productpublishrecord.enumeration.ProductPublishRecordIntentEnum;
import com.mqttsnet.thinglinks.productpublishrecord.enumeration.ProductPublishRecordStatusEnum;
import com.mqttsnet.thinglinks.productpublishrecord.enumeration.PublishDdlOperationEnum;
import com.mqttsnet.thinglinks.productpublishrecord.service.ProductPublishRecordService;
import com.mqttsnet.thinglinks.productpublishrecord.vo.ddl.PublishDdlItemVO;
import com.mqttsnet.thinglinks.productversion.converter.ProductSnapshotConverter;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import com.mqttsnet.thinglinks.productversion.event.source.ProductVersionLifecycleEventSource;
import com.mqttsnet.thinglinks.productversion.publish.strategy.DeviceRebindStrategy;
import com.mqttsnet.thinglinks.productversion.publish.util.TdSchemaInspector;
import com.mqttsnet.thinglinks.productversion.publish.util.TdSchemaInspector.SchemaSnapshot;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionService;
import com.mqttsnet.thinglinks.productversion.util.ProductTdsNamer;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotPropertyVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotServiceVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotVO;
import com.mqttsnet.thinglinks.tds.facade.TdsFacade;
import com.mqttsnet.thinglinks.tds.vo.result.SuperTableDescribeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 产品版本发布编排器 ── 把 publish / rollback / purge 三种生命周期动作统一收口:schema sync 编排 /
 * 设备改绑路由({@link DeviceRebindStrategy})/ 记录回写 / retry 兜底,事件入口与定时兜底 job 共享同一份逻辑。
 *
 * <p>幂等性保证(retry 兜底的前提):TD CREATE STABLE if not exists 重复建表无副作用;设备改绑 SET 到同一
 * 目标值幂等;markSuccess / markFailed update 单行无额外副作用。</p>
 *
 * @author mqttsnet
 */
@Slf4j
@Service
public class ProductVersionPublishOrchestrator {

    /**
     * 单字段 NCHAR/BINARY 字符数上限 ── 注意是字符数不是字节数!TDengine 硬上限 16374,但 NCHAR 每字符 4 字节
     * = 65496 字节单字段就吃光行上限,这里 cap 到 5000(= NCHAR 20000 字节)给同表其他字段留 ~45KB 空间。
     */
    private static final int TD_VAR_MAX_CHARS = 5000;
    /** 变长字段缺失 maxlength 时的默认字符数。 */
    private static final int TD_VAR_DEFAULT_CHARS = 255;
    /** retry 兜底扫描时间窗:超过此时长仍 RUNNING 视为永久失败,标 FAILED 不再重试。 */
    private static final int RETRY_TIMEOUT_HOURS = 1;
    /** retry 兜底单次扫描每个状态的记录上限 ── 实际单次最多拉 RETRY_BATCH_LIMIT×2(RUNNING + FAILED 各 100 条)。 */
    private static final int RETRY_BATCH_LIMIT = 100;
    /**
     * 单条 DDL item 的最大执行次数(首次 + 兜底重试)── 超过视为永久失败 Job 不再尝试。常见暂态错误(网络抖动 /
     * TD 短暂不可用)1-3 次内会自愈,设 10 给足重试空间。
     */
    private static final int MAX_ATTEMPT_PER_ITEM = 10;
    /** 时间戳格式化器 ── DDL item.executedAt 字段的统一格式。 */
    private static final java.time.format.DateTimeFormatter EXECUTED_AT_FORMATTER = DateTimeFormatter.ofPattern(DateUtils.CHINESE_DATETIME_FORMAT_LINE);

    private final TdsFacade tdsFacade;
    private final ProductVersionService productVersionService;
    private final ProductPublishRecordService productPublishRecordService;
    private final ProductSnapshotConverter productSnapshotConverter;
    private final ProductModelCacheService productModelCacheService;
    private final DeviceService deviceService;
    /** 按 publishStrategy 路由的策略 map ── 构造时把 Spring 注入的 List 收敛为 EnumMap(O(1) 查找)。 */
    private final Map<ProductPublishStrategyEnum, DeviceRebindStrategy> strategyRouter;

    public ProductVersionPublishOrchestrator(TdsFacade tdsFacade,
                                             ProductVersionService productVersionService,
                                             ProductPublishRecordService productPublishRecordService,
                                             ProductSnapshotConverter productSnapshotConverter,
                                             ProductModelCacheService productModelCacheService,
                                             DeviceService deviceService,
                                             List<DeviceRebindStrategy> strategies) {
        this.tdsFacade = tdsFacade;
        this.productVersionService = productVersionService;
        this.productPublishRecordService = productPublishRecordService;
        this.productSnapshotConverter = productSnapshotConverter;
        this.productModelCacheService = productModelCacheService;
        this.deviceService = deviceService;
        // 启动时一次性把 List<策略> 转 EnumMap,运行时按 publishStrategy 直接查
        EnumMap<ProductPublishStrategyEnum, DeviceRebindStrategy> map = new EnumMap<>(ProductPublishStrategyEnum.class);
        for (DeviceRebindStrategy strategy : strategies) {
            DeviceRebindStrategy prev = map.put(strategy.supports(), strategy);
            if (prev != null) {
                log.warn("[publish-orchestrator] duplicate DeviceRebindStrategy for {}: {} overrides {}",
                    strategy.supports(), strategy.getClass().getSimpleName(), prev.getClass().getSimpleName());
            }
        }
        this.strategyRouter = Collections.unmodifiableMap(map);
        log.info("[publish-orchestrator] device rebind strategies registered: {}", strategyRouter.keySet());
    }

    // ────────────── 公开入口:三种生命周期 ──────────────

    /**
     * 执行发布:解快照 → 建 TD super table → 按策略改绑设备 → 刷缓存 → 回写 record。返回是否全流程成功。
     *
     * @param src 版本生命周期事件源
     * @return 全流程成功返 true,任一环节失败返 false
     */
    public boolean runPublish(ProductVersionLifecycleEventSource src) {
        ProductVersion versionRow = productVersionService
            .findByProductIdentificationAndVersionNo(src.getProductIdentification(), src.getTargetVersion())
            .orElse(null);
        ProductSnapshotVO snapshot = Optional.ofNullable(versionRow)
            .map(v -> productSnapshotConverter.deserialize(v.getProductSnapshotJson()).orElse(null))
            .orElse(null);
        if (snapshot == null) {
            String msg = "snapshot not found for version " + src.getTargetVersion();
            log.error("[publish-orchestrator] {}", msg);
            productPublishRecordService.markFailed(src.getPublishRecordId(), msg);
            return false;
        }

        String productType = resolveProductTypeDesc(snapshot);
        // 每个 service 执行结果独立记录:含 success / errorMsg / durationMs,前端弹窗可视化展示
        List<PublishDdlItemVO> ddlItems = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        Optional.ofNullable(snapshot.getServices()).orElse(Collections.emptyList()).forEach(service -> {
            String stableName = ProductTdsNamer.superTableName(
                productType, src.getProductIdentification(), src.getTargetVersion(), service.getServiceCode());
            SuperTableDTO dto = buildSuperTableDTO(stableName, service);
            int columnCount = dto.getSchemaFields() == null ? 0 : dto.getSchemaFields().size();
            long t0 = System.currentTimeMillis();
            R<?> createResult = tdsFacade.createSuperTableAndColumn(dto);
            long durationMs = System.currentTimeMillis() - t0;

            PublishDdlItemVO item = PublishDdlItemVO.builder()
                .operation(PublishDdlOperationEnum.CREATE_STABLE)
                .stableName(stableName)
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .columnCount(columnCount)
                .durationMs(durationMs)
                // 首次执行 attemptCount=1,job 重试时递增;executedAt 记录本次时间
                .attemptCount(1)
                .executedAt(LocalDateTime.now().format(EXECUTED_AT_FORMATTER))
                .build();

            // create + describe 作为整体原子动作:任一步失败整条 item 标 failed
            // (describe 失败也算 item 失败,Job 兜底重试时重新执行 create+describe,自愈)
            String enrichErrorMsg = applyCreateAndDescribe(item, createResult);
            if (enrichErrorMsg != null) {
                failed.add(stableName + ": " + enrichErrorMsg);
                log.error("[publish-orchestrator] CREATE STABLE {} failed (cost={}ms): {}",
                    stableName, durationMs, enrichErrorMsg);
            }
            ddlItems.add(item);
        });

        // 结构化 DDL 明细 typed 写入,无需手动 JSON.toString(由 JacksonTypeHandler 自动序列化)
        productPublishRecordService.attachDdlItems(src.getPublishRecordId(), ddlItems);

        if (!failed.isEmpty()) {
            // DDL 部分失败 ── 不刷缓存 + 不改设备,等 Job 兜底 retryRecordPartial 重试到全部成功才闭环
            // (此前老逻辑在这里也刷缓存,会让 v 新版本 entry 写入,但 TD 还缺 stable → 设备一旦改绑就上报失败)
            productPublishRecordService.markFailed(src.getPublishRecordId(), String.join("; ", failed));
            return false;
        }

        // TD DDL 全部成功 → 先刷缓存(确保 v 新版本 entry 就绪)再按 publishStrategy 改绑设备 → markSuccess
        productModelCacheService.refreshProductModelCache(src.getProductIdentification());
        int affected = rebindDevices(src, versionRow.getCanaryConfigJson());
        productPublishRecordService.markSuccess(src.getPublishRecordId());
        log.info("[publish-orchestrator] publish ok product={} version={} strategy={} stables={} devicesRebound={}",
            src.getProductIdentification(), src.getTargetVersion(), src.getPublishStrategy(), ddlItems.size(), affected);
        return true;
    }

    /**
     * 执行回滚:TD 不动,把"原绑 sourceVersion"的设备批量改绑到 targetVersion + 刷缓存。把 affected + from/to
     * 版本号写到 record.remark 供前端展示"回滚做了什么"。幂等:bulkRebindByProductAndVersion 底层 SQL WHERE 含
     * bound_product_version_no = fromVersion,重跑时已是 toVersion 匹配 0 行,Job 重试 affected=0 是预期不算失败。
     *
     * @param src 版本生命周期事件源
     * @return 恒返 true(改绑 0 台也算成功)
     */
    public boolean runRollback(ProductVersionLifecycleEventSource src) {
        int affected = deviceService.bulkRebindByProductAndVersion(
            src.getProductIdentification(), src.getSourceVersion(), src.getTargetVersion());
        // 写回滚元信息到 record.remark ── 给前端发布记录详情展示
        String remark = String.format("回滚 %s → %s,改绑设备 %d 台",
            src.getSourceVersion(), src.getTargetVersion(), affected);
        productPublishRecordService.attachRemark(src.getPublishRecordId(), remark);
        productModelCacheService.refreshProductModelCache(src.getProductIdentification());
        productPublishRecordService.markSuccess(src.getPublishRecordId());
        // affected=0 大概率是 Job 重试时的空操作(已改绑过),不是失败但值得 warn 关注
        if (affected == 0) {
            log.warn("[publish-orchestrator] rollback affected=0 product={} from={} to={} "
                    + "(可能 Job 重试空操作 / sourceVersion 当前无设备绑定)",
                src.getProductIdentification(), src.getSourceVersion(), src.getTargetVersion());
        } else {
            log.info("[publish-orchestrator] rollback ok product={} from={} to={} devicesRebound={}",
                src.getProductIdentification(), src.getSourceVersion(), src.getTargetVersion(), affected);
        }
        return true;
    }

    /**
     * 执行历史清理:遍历版本 services 执行 {@code DROP STABLE},回写 record。
     *
     * @param src 版本生命周期事件源
     * @return 全部 DROP 成功返 true,任一失败返 false
     */
    public boolean runPurge(ProductVersionLifecycleEventSource src) {
        ProductSnapshotVO snapshot = productVersionService
            .findByProductIdentificationAndVersionNo(src.getProductIdentification(), src.getTargetVersion())
            .map(v -> productSnapshotConverter.deserialize(v.getProductSnapshotJson()).orElse(null))
            .orElse(null);
        if (snapshot == null) {
            String msg = "snapshot not found for version " + src.getTargetVersion();
            log.error("[publish-orchestrator] {}", msg);
            productPublishRecordService.markFailed(src.getPublishRecordId(), msg);
            return false;
        }

        String productType = resolveProductTypeDesc(snapshot);
        List<PublishDdlItemVO> ddlItems = new ArrayList<>();
        List<String> failed = new ArrayList<>();
        Optional.ofNullable(snapshot.getServices()).orElse(Collections.emptyList()).forEach(service -> {
            String stableName = ProductTdsNamer.superTableName(
                productType, src.getProductIdentification(), src.getTargetVersion(), service.getServiceCode());
            long t0 = System.currentTimeMillis();
            R<?> result = tdsFacade.dropSuperTable(stableName);
            long durationMs = System.currentTimeMillis() - t0;
            boolean success = !Boolean.FALSE.equals(result.getIsSuccess());
            ddlItems.add(PublishDdlItemVO.builder()
                .operation(PublishDdlOperationEnum.DROP_STABLE)
                .stableName(stableName)
                .serviceCode(service.getServiceCode())
                .serviceName(service.getServiceName())
                .success(success)
                .errorMsg(success ? null : result.getMsg())
                .durationMs(durationMs)
                .attemptCount(1)
                .executedAt(LocalDateTime.now().format(EXECUTED_AT_FORMATTER))
                .build());
            if (!success) {
                failed.add(stableName + ": " + result.getMsg());
            }
        });

        productPublishRecordService.attachDdlItems(src.getPublishRecordId(), ddlItems);
        if (failed.isEmpty()) {
            productPublishRecordService.markSuccess(src.getPublishRecordId());
            return true;
        }
        productPublishRecordService.markFailed(src.getPublishRecordId(), String.join("; ", failed));
        return false;
    }

    /**
     * retry 兜底:事件监听器 async 执行时遇服务异常 / JVM 重启导致中断、publish_record 卡 RUNNING,job 周期扫描
     * 重试保证最终一致。只拉 {@value #RETRY_TIMEOUT_HOURS} 小时内创建的记录(避免无限重试老记录),超 timeout 的
     * RUNNING 直接 markFailed,每次限 {@value #RETRY_BATCH_LIMIT} 条,按 intent 分发到幂等的 runPublish/runRollback/runPurge。
     *
     * @param tenantId 租户 ID(日志用,实际查询走 @DS(BASE_TENANT) 切租户库)
     * @return 实际重试的记录数
     */
    public int retryRunningRecordsForTenant(Long tenantId) {
        LocalDateTime windowStart = LocalDateTime.now().minusHours(RETRY_TIMEOUT_HOURS);
        // 同时扫 RUNNING(中断卡住) + FAILED(已失败但可能服务抖动一次后再次重试)
        List<ProductPublishRecord> candidates = new ArrayList<>();
        candidates.addAll(productPublishRecordService.listByStatusSince(
            ProductPublishRecordStatusEnum.RUNNING.getValue(), windowStart, RETRY_BATCH_LIMIT));
        candidates.addAll(productPublishRecordService.listByStatusSince(
            ProductPublishRecordStatusEnum.FAILED.getValue(), windowStart, RETRY_BATCH_LIMIT));
        if (candidates.isEmpty()) {
            log.info("[publish-orchestrator-retry] tenantId={} no retry candidates", tenantId);
            return 0;
        }

        AtomicInteger retried = new AtomicInteger();
        AtomicInteger timedOut = new AtomicInteger();
        long now = System.currentTimeMillis();
        for (ProductPublishRecord record : candidates) {
            if (record.getCreatedTime() != null && record.getCreatedTime().plusHours(RETRY_TIMEOUT_HOURS).isBefore(LocalDateTime.now())) {
                if (ProductPublishRecordStatusEnum.RUNNING.getValue().equals(record.getStatus())) {
                    productPublishRecordService.markFailed(record.getId(), "retry timeout: still RUNNING after " + RETRY_TIMEOUT_HOURS + "h, no further retry");
                    timedOut.incrementAndGet();
                }
                continue;
            }
            try {
                if (retryRecordPartial(record)) {
                    retried.incrementAndGet();
                }
            } catch (Exception ex) {
                log.error("[publish-orchestrator-retry] retry failed record id={}", record.getId(), ex);
                productPublishRecordService.markFailed(record.getId(), "retry exception: " + unwrap(ex));
            }
        }

        log.info("[publish-orchestrator-retry] tenantId={} scanned={} retried={} timedOut={} cost={}ms", tenantId, candidates.size(), retried.get(), timedOut.get(), System.currentTimeMillis() - now);
        return retried.get();
    }

    /**
     * 精细化重试:只重试 success=false 的 DDL item,成功的跳过(一次抖动只影响某 1 条 service 建表,无需整个
     * record 全部 service 重做)。遍历每项:success=true 跳过,attemptCount >= {@value #MAX_ATTEMPT_PER_ITEM}
     * 视为永久失败跳过,否则按 operation 调对应 TD API 并 attemptCount++ 更新状态;写回 ddlItems。全部 success 后
     * 仅 PUBLISH 意图按 publishStrategy 改绑设备 + 刷缓存 + markSuccess(从 FAILED 改回 SUCCESS 闭环),仍有失败则
     * 累计 markFailed。TD CREATE/DROP STABLE if (not) exists + 设备 UPDATE 改绑都幂等,Job 与事件流并发触发也安全。
     *
     * @param record 待重试的发布记录
     * @return 是否真正执行了至少一次 DDL 重试(全部 success/permanent_failed 时返 false)
     */
    private boolean retryRecordPartial(ProductPublishRecord record) {
        List<PublishDdlItemVO> ddlItems = record.getDdlItems();
        if (CollUtil.isEmpty(ddlItems)) {
            // 兼容老数据 / record 还没来得及写 ddl_summary 的场景:走完整重跑(等价于首次执行)
            ProductVersionLifecycleEventSource src = reconstructEventSource(record);
            if (src == null) {
                return false;
            }
            ProductPublishRecordIntentEnum intent = ProductPublishRecordIntentEnum.fromValue(record.getIntent())
                .orElse(ProductPublishRecordIntentEnum.PUBLISH);
            // 接 run*** 返回值仅用于本次"是否真重跑了" ── 失败的会被 run*** 内部 markFailed,
            // record 状态保持 FAILED,下次 Job 还会再捞起来,最终一致
            boolean rerun = switch (intent) {
                case PUBLISH -> runPublish(src);
                case ROLLBACK -> runRollback(src);
                case PURGE_HISTORY -> runPurge(src);
            };
            return rerun;
        }

        // 一次性 deserialize snapshot,避免每个 CREATE_STABLE 失败 item 都重复反序列化整段 JSON
        // (snapshot 可能上百 KB,N 个失败 item × N 次 deserialize = O(N²) 浪费)
        // lazy-load:只有真有 CREATE_STABLE 重试时才 fetch,纯 DROP_STABLE 场景零开销
        ProductSnapshotVO sharedSnapshot = null;
        boolean snapshotLoaded = false;

        boolean anyRetried = false;
        List<String> stillFailed = new ArrayList<>();
        for (PublishDdlItemVO item : ddlItems) {
            if (Boolean.TRUE.equals(item.getSuccess())) {
                continue; // 成功的跳过
            }
            int attempts = Optional.ofNullable(item.getAttemptCount()).orElse(0);
            if (attempts >= MAX_ATTEMPT_PER_ITEM) {
                stillFailed.add(item.getStableName() + ": permanent_failed (" + attempts + " attempts)");
                continue;
            }
            // 实际执行重试:CREATE_STABLE 调 createSuperTableAndColumn,DROP_STABLE 调 dropSuperTable
            long t0 = System.currentTimeMillis();
            item.setAttemptCount(attempts + 1);
            item.setExecutedAt(LocalDateTime.now().format(EXECUTED_AT_FORMATTER));
            anyRetried = true;

            if (PublishDdlOperationEnum.DROP_STABLE == item.getOperation()) {
                R<?> result = tdsFacade.dropSuperTable(item.getStableName());
                item.setDurationMs(System.currentTimeMillis() - t0);
                boolean success = !Boolean.FALSE.equals(result.getIsSuccess());
                item.setSuccess(success);
                item.setErrorMsg(success ? null : result.getMsg());
                if (!success) {
                    stillFailed.add(item.getStableName() + ": " + result.getMsg());
                }
                continue;
            }

            // CREATE_STABLE 重试:需要从 snapshot 反查 service 拼 DTO
            if (!snapshotLoaded) {
                sharedSnapshot = loadSnapshot(record);
                snapshotLoaded = true;
            }
            SuperTableDTO dto = rebuildSuperTableDto(sharedSnapshot, item);
            if (dto == null) {
                item.setErrorMsg("cannot rebuild DTO (snapshot or service missing)");
                item.setDurationMs(System.currentTimeMillis() - t0);
                stillFailed.add(item.getStableName() + ": " + item.getErrorMsg());
                continue;
            }
            R<?> createResult = tdsFacade.createSuperTableAndColumn(dto);
            item.setDurationMs(System.currentTimeMillis() - t0);
            // 跟 runPublish 同模式:create + describe 整体原子,describe 失败也算本条失败
            String enrichErrorMsg = applyCreateAndDescribe(item, createResult);
            if (enrichErrorMsg != null) {
                stillFailed.add(item.getStableName() + ": " + enrichErrorMsg);
            }
        }

        // 写回结构化 ddlItems(JacksonTypeHandler 自动序列化到 ddl_summary 列)
        productPublishRecordService.attachDdlItems(record.getId(), ddlItems);

        if (stillFailed.isEmpty()) {
            // 全部 success → 触发改绑设备(PUBLISH 才需要,ROLLBACK/PURGE 不动 device)+ 刷缓存 + markSuccess
            ProductPublishRecordIntentEnum intent = ProductPublishRecordIntentEnum.fromValue(record.getIntent())
                .orElse(ProductPublishRecordIntentEnum.PUBLISH);
            if (intent == ProductPublishRecordIntentEnum.PUBLISH) {
                ProductVersionLifecycleEventSource src = reconstructEventSource(record);
                if (src != null) {
                    String canaryConfig = productVersionService
                        .findByProductIdentificationAndVersionNo(record.getProductIdentification(), record.getTargetVersion())
                        .map(ProductVersion::getCanaryConfigJson)
                        .orElse(null);
                    int affected = rebindDevices(src, canaryConfig);
                    log.info("[publish-orchestrator-retry] devicesRebound={} for record id={} after partial retry succeeded",
                        affected, record.getId());
                }
            }
            productModelCacheService.refreshProductModelCache(record.getProductIdentification());
            productPublishRecordService.markSuccess(record.getId());
            log.info("[publish-orchestrator-retry] record id={} fully recovered after partial retry", record.getId());
        } else if (anyRetried) {
            productPublishRecordService.markFailed(record.getId(),
                "partial retry still failed: " + String.join("; ", stillFailed));
            log.warn("[publish-orchestrator-retry] record id={} still failing after retry: {}",
                record.getId(), String.join("; ", stillFailed));
        }
        return anyRetried;
    }

    /**
     * 按 record + version 反查并反序列化 snapshot ── 与 rebuildSuperTableDto 分离,便于 retry 一次 load 多次复用。
     *
     * @param record 发布记录(提供 productIdentification + targetVersion)
     * @return 产品快照;查不到或反序列化失败返 null
     */
    private ProductSnapshotVO loadSnapshot(ProductPublishRecord record) {
        return productVersionService
            .findByProductIdentificationAndVersionNo(record.getProductIdentification(), record.getTargetVersion())
            .map(v -> productSnapshotConverter.deserialize(v.getProductSnapshotJson()).orElse(null))
            .orElse(null);
    }

    /**
     * 从已 load 好的 snapshot + ddl item 重建 SuperTableDTO ── 用于 retry 时重新执行 CREATE_STABLE。
     *
     * @param snapshot 已加载的产品快照
     * @param item     待重建的 DDL item(提供 serviceCode + stableName)
     * @return 重建的 SuperTableDTO;快照为空或匹配不到 service 返 null
     */
    private SuperTableDTO rebuildSuperTableDto(ProductSnapshotVO snapshot, PublishDdlItemVO item) {
        if (snapshot == null) {
            return null;
        }
        ProductSnapshotServiceVO service = Optional.ofNullable(snapshot.getServices())
            .orElse(Collections.emptyList())
            .stream()
            .filter(s -> s.getServiceCode() != null && s.getServiceCode().equals(item.getServiceCode()))
            .findFirst()
            .orElse(null);
        if (service == null) {
            return null;
        }
        return buildSuperTableDTO(item.getStableName(), service);
    }

    // ────────────── 内部:策略路由 + reconstruct ──────────────

    /**
     * 按 publishStrategy 路由到对应策略实现执行设备改绑;策略不存在时回退到 FULL。
     *
     * @param src             版本生命周期事件源
     * @param canaryConfigJson 灰度配置 JSON(灰度策略用)
     * @return 改绑的设备数量
     */
    private int rebindDevices(ProductVersionLifecycleEventSource src, String canaryConfigJson) {
        ProductPublishStrategyEnum strategy = Optional.ofNullable(src.getPublishStrategy())
            .orElse(ProductPublishStrategyEnum.FULL);
        DeviceRebindStrategy impl = strategyRouter.get(strategy);
        if (impl == null) {
            log.warn("[publish-orchestrator] no DeviceRebindStrategy for {}, fallback to FULL", strategy);
            impl = strategyRouter.get(ProductPublishStrategyEnum.FULL);
        }
        return impl == null ? 0 : impl.rebind(src.getProductIdentification(), src.getTargetVersion(), canaryConfigJson);
    }

    /**
     * 从 publish_record 反向构造 EventSource(retry 兜底用)。publishStrategy 不在 record 表里,从 product_version 反查。
     *
     * @param record 发布记录
     * @return 重建的版本生命周期事件源
     */
    private ProductVersionLifecycleEventSource reconstructEventSource(ProductPublishRecord record) {
        ProductPublishStrategyEnum strategy = productVersionService
            .findByProductIdentificationAndVersionNo(record.getProductIdentification(), record.getTargetVersion())
            .map(ProductVersion::getPublishStrategy)
            .flatMap(ProductPublishStrategyEnum::fromValue)
            .orElse(ProductPublishStrategyEnum.FULL);
        return ProductVersionLifecycleEventSource.builder()
            .productIdentification(record.getProductIdentification())
            .sourceVersion(record.getSourceVersion())
            .targetVersion(record.getTargetVersion())
            .publishStrategy(strategy)
            .publishRecordId(record.getId())
            .build();
    }

    // ────────────── 内部:create + describe 原子动作 ──────────────

    /**
     * 把 create 结果 + 后续 describe 反查的真实 schema 合并写入 item,返回失败原因(成功返 null)。
     * 原子语义:create 与 describe 作为整体 —— create 失败整条 item 失败(不调 describe);create 成功但
     * describe 失败也算失败(Job 重试时重走 create+describe,if not exists 幂等成功,describe 暂态错误自愈);
     * 都成功才 success=true 且填 schemaFields/tagsFields/rowBytes。保证 ddl_summary 里"成功"的 item 必有 TD
     * 真实 schema 快照,前端不需降级到 DTO 兜底。
     *
     * @param item         待回写的 DDL item
     * @param createResult 建表结果
     * @return 失败原因;成功返 null
     */
    private String applyCreateAndDescribe(PublishDdlItemVO item, R<?> createResult) {
        boolean createSuccess = !Boolean.FALSE.equals(createResult.getIsSuccess());
        if (!createSuccess) {
            item.setSuccess(false);
            item.setErrorMsg(createResult.getMsg());
            return createResult.getMsg();
        }
        // create 成功 → describe 拿 TD 真实 schema(单一真相源)
        R<List<SuperTableDescribeVO>> descResult = tdsFacade.describeSuperOrSubTable(item.getStableName());
        if (Boolean.FALSE.equals(descResult.getIsSuccess())) {
            String reason = "create ok but describe failed: " + descResult.getMsg();
            item.setSuccess(false);
            item.setErrorMsg(reason);
            return reason;
        }
        SchemaSnapshot snap = TdSchemaInspector.inspect(descResult.getData());
        item.setSuccess(true);
        item.setErrorMsg(null);
        item.setSchemaFields(snap.getSchemaFields());
        item.setTagsFields(snap.getTagsFields());
        item.setRowBytes(snap.getRowBytes());
        return null;
    }

    // ────────────── 内部:TD DDL 构造 ──────────────

    /**
     * 把快照 service 转成 TD createSuperTable 用的 DTO,末尾做行级字节预校验。单字段 NCHAR/BINARY 字符数
     * cap 到 {@value #TD_VAR_MAX_CHARS}(超出截断 + warn,避免单字段 maxlength 过大让整张 stable 创建失败);
     * 所有 schema 字段累加字节数若超过 {@link TdSchemaInspector#TD_ROW_MAX_BYTES} 直接抛 {@link BizException}
     * 提示哪些字段超量,避免 TDengine 端报 "Row length exceeds max length" 这种难懂错误。
     *
     * @param stableName 超表名
     * @param service    快照 service(提供 properties)
     * @return 用于 createSuperTable 的 DTO
     */
    private SuperTableDTO buildSuperTableDTO(String stableName, ProductSnapshotServiceVO service) {
        SuperTableDTO dto = new SuperTableDTO();
        dto.setSuperTableName(stableName);
        dto.setDataBaseName("");

        List<FieldsVO> schema = new ArrayList<>();
        schema.add(new FieldsVO(TdsConstants.TS, TdDataTypeEnum.TIMESTAMP.getDataType(), null));
        schema.add(new FieldsVO(TdsConstants.EVENT_TIME, TdDataTypeEnum.TIMESTAMP.getDataType(), null));

        if (CollUtil.isNotEmpty(service.getProperties())) {
            for (ProductSnapshotPropertyVO property : service.getProperties()) {
                TdDataTypeEnum tdType = TdDataTypeEnum.valueOfByDataType(property.getDatatype());
                Integer size = resolveSize(property, tdType, stableName);
                schema.add(new FieldsVO(
                    property.getPropertyCode(),
                    tdType.getDataType(),
                    size));
            }
        }

        List<FieldsVO> tags = Collections.singletonList(
            new FieldsVO(TdsConstants.DEVICE_IDENTIFICATION, TdDataTypeEnum.BINARY.getDataType(), 64));

        dto.setSchemaFields(FieldsVO.toFieldsList(schema));
        dto.setTagsFields(FieldsVO.toFieldsList(tags));

        // 行级字节预校验:超过 TD 65531 上限 → 抛业务异常给出可读提示,避免后续 TD 报难懂错误
        validateRowBytes(stableName, service, schema);
        return dto;
    }

    /**
     * 行级字节合计预校验 ── 业务层 fail-fast,把 TDengine 难懂的 "Row length exceeds" 错误
     * 转成中文可读提示,带 top-3 字段占用,引导用户精简哪个字段。
     *
     * @param stableName 超表名(异常文案用)
     * @param service    快照 service(提供 serviceCode)
     * @param schema     schema 字段列表
     */
    private void validateRowBytes(String stableName, ProductSnapshotServiceVO service, List<FieldsVO> schema) {
        int total = schema.stream()
            .mapToInt(f -> Optional.ofNullable(TdSchemaInspector.computeBytes(f.getDataType(), f.getSize()))
                .orElse(0))
            .sum();
        if (total <= TdSchemaInspector.TD_ROW_MAX_BYTES) {
            return;
        }
        // 按字节降序找 top-3 大字段,作为问题文案的"嫌疑字段"
        String topOffenders = schema.stream()
            .map(f -> new Object[]{f.getFieldName(), f.getDataType(), f.getSize(),
                Optional.ofNullable(TdSchemaInspector.computeBytes(f.getDataType(), f.getSize())).orElse(0)})
            .sorted((a, b) -> Integer.compare((int) b[3], (int) a[3]))
            .limit(3)
            .map(a -> String.format("%s(%s%s)=%d字节", a[0], a[1],
                a[2] == null ? "" : "(" + a[2] + ")", a[3]))
            .reduce((x, y) -> x + ", " + y)
            .orElse("");
        throw BizException.wrap(String.format(
            "服务 '%s' 字段总长度 %d 字节,超过 TDengine 单行上限 %d 字节(NCHAR 占 4 字节/字符)。"
                + "请缩短 NCHAR 字段的 maxlength。占用 Top 3:%s",
            service.getServiceCode(), total, TdSchemaInspector.TD_ROW_MAX_BYTES, topOffenders));
    }

    /**
     * 解析属性长度,带 TDengine 上限保护:定长类型(INT/FLOAT/TIMESTAMP 等)返 null(TD 不需要 size);
     * 变长类型缺失 maxlength 返默认 {@value #TD_VAR_DEFAULT_CHARS} 字符,超过 {@value #TD_VAR_MAX_CHARS} 字符 cap 到上限 + warn。
     *
     * @param property   快照属性(提供 maxlength)
     * @param tdType     TD 数据类型
     * @param stableName 超表名(warn 日志用)
     * @return 字段字符数;定长类型返 null
     */
    private Integer resolveSize(ProductSnapshotPropertyVO property, TdDataTypeEnum tdType, String stableName) {
        // tdType.isQuoted() 等价于"变长类型"(NCHAR/BINARY/VARBINARY/JSON):INSERT 时值要加引号,DDL 必须 (size)
        if (tdType == null || !tdType.isQuoted()) {
            return null;
        }
        Integer parsed = parseSize(property.getMaxlength());
        if (parsed == null || parsed <= 0) {
            return TD_VAR_DEFAULT_CHARS;
        }
        if (parsed > TD_VAR_MAX_CHARS) {
            log.warn("[publish-orchestrator] property maxlength {} chars exceeds TDengine var-length cap {} chars, capped. "
                    + "stable={} property={} type={}",
                parsed, TD_VAR_MAX_CHARS, stableName, property.getPropertyCode(), tdType.getDataType());
            return TD_VAR_MAX_CHARS;
        }
        return parsed;
    }

    private Integer parseSize(String maxlength) {
        if (maxlength == null || maxlength.isBlank()) return null;
        try {
            return Integer.parseInt(maxlength.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveProductTypeDesc(ProductSnapshotVO snapshot) {
        return Optional.ofNullable(snapshot.getProductType())
            .map(ProductTypeEnum::valueOf)
            .map(ProductTypeEnum::getDesc)
            .orElse("COMMON");
    }

    /**
     * 提取异常链根因的简短描述(写 failedReason 用)。
     *
     * @param ex 异常
     * @return 根因的"类名: 消息"简短描述
     */
    public String unwrap(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }
}
