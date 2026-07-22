package com.mqttsnet.thinglinks.productversion.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.product.service.ProductQueryService;
import com.mqttsnet.thinglinks.product.service.ProductService;
import com.mqttsnet.thinglinks.product.vo.param.ProductParamVO;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.productpublishrecord.entity.ProductPublishRecord;
import com.mqttsnet.thinglinks.productpublishrecord.service.ProductPublishRecordService;
import com.mqttsnet.thinglinks.productversion.converter.ProductSnapshotConverter;
import com.mqttsnet.thinglinks.productversion.diff.ProductSnapshotDiffCalculator;
import com.mqttsnet.thinglinks.productversion.entity.ProductVersion;
import com.mqttsnet.thinglinks.productversion.enumeration.ProductPublishStrategyEnum;
import com.mqttsnet.thinglinks.productversion.enumeration.ProductVersionStatusEnum;
import com.mqttsnet.thinglinks.productversion.event.publisher.ProductVersionEventPublisher;
import com.mqttsnet.thinglinks.productversion.event.source.ProductVersionLifecycleEventSource;
import com.mqttsnet.thinglinks.productversion.manager.ProductVersionManager;
import com.mqttsnet.thinglinks.productversion.service.ProductVersionService;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionDiffSummaryVO;
import com.mqttsnet.thinglinks.productversion.vo.diff.ProductVersionDiffVO;
import com.mqttsnet.thinglinks.productversion.vo.result.ProductVersionStatisticsResultVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionPublishVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionPurgeVO;
import com.mqttsnet.thinglinks.productversion.vo.save.ProductVersionRollbackVO;
import com.mqttsnet.thinglinks.productversion.vo.snapshot.ProductSnapshotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 产品物模型版本业务实现。
 *
 * @author mqttsnet
 * @see ProductVersionService
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@Service
public class ProductVersionServiceImpl
    extends SuperServiceImpl<ProductVersionManager, Long, ProductVersion>
    implements ProductVersionService {

    private final ProductVersionManager productVersionManager;
    private final ProductQueryService productQueryService;
    /**
     * 跨域产品写入(切换 active_version_no 指针)走 Service 而非直接调 ProductManager,触发 @DS(BASE_TENANT)
     * 切租户库 + 满足"禁止跨层级调用"。@Lazy 必要性:ProductServiceImpl 已依赖 ProductVersionService
     * (产品 CRUD 刷草稿 + 删除级联软删),反向直接注入会形成构造期循环依赖,@Lazy 注入代理按需创建真实 bean。
     * (本类不能用 @RequiredArgsConstructor ── Lombok 默认不复制 @Lazy 到构造器参数。)
     */
    private final ProductService productService;
    private final ProductPublishRecordService productPublishRecordService;
    private final ProductSnapshotConverter productSnapshotConverter;
    private final ProductSnapshotDiffCalculator productSnapshotDiffCalculator;
    private final ProductVersionEventPublisher productVersionEventPublisher;

    @Autowired
    public ProductVersionServiceImpl(ProductVersionManager productVersionManager,
                                     ProductQueryService productQueryService,
                                     @Lazy ProductService productService,
                                     ProductPublishRecordService productPublishRecordService,
                                     ProductSnapshotConverter productSnapshotConverter,
                                     ProductSnapshotDiffCalculator productSnapshotDiffCalculator,
                                     ProductVersionEventPublisher productVersionEventPublisher) {
        this.productVersionManager = productVersionManager;
        this.productQueryService = productQueryService;
        this.productService = productService;
        this.productPublishRecordService = productPublishRecordService;
        this.productSnapshotConverter = productSnapshotConverter;
        this.productSnapshotDiffCalculator = productSnapshotDiffCalculator;
        this.productVersionEventPublisher = productVersionEventPublisher;
    }

    @Override
    public Optional<ProductVersion> findByProductIdentificationAndVersionNo(String productIdentification, String versionNo) {
        return productVersionManager.findByProductIdentificationAndVersionNo(productIdentification, versionNo);
    }

    @Override
    public Optional<ProductVersion> findDraft(String productIdentification) {
        return productVersionManager.findDraft(productIdentification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVersion upsertDraft(String productIdentification) {
        ArgumentAssert.notBlank(productIdentification, "productIdentification must not be blank");

        // 1. 拉当前产品完整树(含停用服务 ── 快照须为完整物模型;services 仍可能为空,产品刚 create 时即如此)
        ProductParamVO fullTree = productQueryService.selectFullProductByProductIdentification(productIdentification, true);
        if (fullTree == null) {
            throw BizException.wrap("Product not found: " + productIdentification);
        }

        // 2. 找当前所有 DRAFT 行 ── 正常只有 1 个;>1 说明历史并发漏网 / 锁失效,做一次自愈。
        //    listByProductIdentificationAndStatus 已按 created_time 倒序,取 [0] 即最新。
        List<ProductVersion> drafts = productVersionManager.listByProductIdentificationAndStatus(
            productIdentification, ProductVersionStatusEnum.DRAFT.getValue());
        ProductVersion draft;
        boolean isNew = drafts.isEmpty();
        if (isNew) {
            draft = ProductVersion.builder()
                .productIdentification(productIdentification)
                .versionNo(nextVersion())
                .versionStatus(ProductVersionStatusEnum.DRAFT.getValue())
                .build();
        } else {
            draft = drafts.get(0);
            // 自愈:多余 DRAFT 软删,保证"每产品只有一个 DRAFT"不变量
            if (drafts.size() > 1) {
                List<Long> dupIds = drafts.stream().skip(1).map(ProductVersion::getId).toList();
                productVersionManager.removeByIds(dupIds);
                log.warn("[ProductVersion] cleaned {} duplicate DRAFT rows productIdentification={} kept={}",
                    dupIds.size(), productIdentification, draft.getVersionNo());
            }
        }

        // 3. 序列化最新 snapshot 写回(snapshot 内部 activeVersionNo 字段 = draft 自己的 version)
        ProductSnapshotVO snapshot = productSnapshotConverter.toSnapshot(fullTree, draft);
        snapshot.setActiveVersionNo(draft.getVersionNo());
        String newSnapshotJson = productSnapshotConverter.serialize(snapshot);
        draft.setProductSnapshotJson(newSnapshotJson);

        if (isNew) {
            productVersionManager.save(draft);
            log.info("[ProductVersion] draft created productIdentification={} version={}",
                productIdentification, draft.getVersionNo());
        } else {
            productVersionManager.updateById(draft);
            log.debug("[ProductVersion] draft refreshed productIdentification={} version={}",
                productIdentification, draft.getVersionNo());
        }
        return draft;
    }

    /**
     * {@inheritDoc}
     *
     * <p>REQUIRES_NEW:本方法由 ProductChangeLogListener 在 AFTER_COMMIT 同步阶段调用,此时原事务已提交;
     * 必须起独立事务,否则草稿创建复用刚提交的连接不会落库。</p>
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public String resolveDraftVersion(String productIdentification) {
        return findDraft(productIdentification)
            .map(ProductVersion::getVersionNo)
            .orElseGet(() -> upsertDraft(productIdentification).getVersionNo());
    }

    /**
     * 版本号生成单一入口 ── 系统接管,16 位短雪花。所有 product_version.version_no 都从这里产出,改策略只动这一处。
     *
     * @return 新生成的版本号
     */
    private String nextVersion() {
        return SnowflakeIdUtil.nextId();
    }

    @Override
    public List<ProductVersion> listByProductIdentification(String productIdentification) {
        return productVersionManager.listByProductIdentification(productIdentification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int softDeleteAllByProductIdentification(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return 0;
        }
        List<ProductVersion> rows = productVersionManager.listByProductIdentification(productIdentification);
        if (rows.isEmpty()) {
            return 0;
        }
        int affected = productVersionManager.removeByIds(
            rows.stream().map(ProductVersion::getId).toList()) ? rows.size() : 0;
        log.info("[ProductVersion] cascade soft-delete productIdentification={} affected={}", productIdentification, affected);
        return affected;
    }

    /**
     * {@inheritDoc}
     *
     * <p>草稿升级模型:发布不新建版本行,而是把当前 DRAFT 升级为目标状态(PUBLISHED/CANARY/SHADOW),再起一个
     * 新 DRAFT 作为下一轮编辑基线(snapshot 拷贝自刚发布的快照)。FULL / CANARY 才把 version 回写
     * product.active_version_no(SHADOW 不切指针);CANARY 还要把切换前的 activeVersionNo 记入
     * product.previous_full_version_no 供回滚 / 灰度路由。落 RUNNING 记录后发事件,异步走 TD DDL + 设备改绑。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVersion publish(ProductVersionPublishVO vo) {
        ArgumentAssert.notNull(vo, "publish VO must not be null");
        String productIdentification = vo.getProductIdentification();

        ProductResultVO product = Optional.ofNullable(
                productQueryService.findOneByProductIdentification(productIdentification))
            .orElseThrow(() -> BizException.wrap("Product not found: " + productIdentification));

        ProductPublishStrategyEnum strategy = ProductPublishStrategyEnum.fromValue(vo.getPublishStrategy())
            .orElse(ProductPublishStrategyEnum.FULL);

        // 1. 发布前再刷一次 DRAFT,保证 snapshot 是最新产品树
        ProductVersion draft = upsertDraft(productIdentification);
        String publishedVersion = draft.getVersionNo();

        // 2. DRAFT 升级为目标状态
        draft.setVersionStatus(resolveStatusByStrategy(strategy).getValue());
        draft.setPublishStrategy(strategy.getValue());
        draft.setCanaryConfigJson(vo.getCanaryConfigJson());
        draft.setRemark(vo.getPublishRemark());
        draft.setPublishTime(LocalDateTime.now());
        productVersionManager.updateById(draft);

        // 3. 切产品指针(SHADOW 不动指针,保留旁路语义)
        // 跨域产品写入走 ProductService(满足"禁止跨层级"+ Service AOP @DS 切租户库),
        // 不直接调 ProductManager;previousVersion 在 service 内部从 DB 读出来更新到 previousFullVersionNo
        String previousVersion = product.getActiveVersionNo();
        boolean shouldSwitchPointer = Optional.ofNullable(strategy)
            .filter(ProductPublishStrategyEnum.SHADOW::equals)
            .isEmpty();
        if (shouldSwitchPointer) {
            boolean isCanary = Optional.ofNullable(strategy)
                .filter(ProductPublishStrategyEnum.CANARY::equals)
                .isPresent();
            productService.switchActiveVersionForPublish(productIdentification, publishedVersion, isCanary);
            // 被取代的上一个 active 版本若仍是 CANARY(瞬态),demote 为 PUBLISHED(历史态):否则灰度晋升 /
            // 放量后版本列表会残留 CANARY 标签,误导"仍在灰度中"(实际 active 指针已指向新版本)。
            demoteSupersededCanary(productIdentification, previousVersion);
        }

        // 4. 起新的 DRAFT 行,snapshot 拷贝自刚发布的版本,后续 CRUD 在此基础上累积
        ProductVersion nextDraft = ProductVersion.builder()
            .productIdentification(productIdentification)
            .versionNo(nextVersion())
            .versionStatus(ProductVersionStatusEnum.DRAFT.getValue())
            .productSnapshotJson(draft.getProductSnapshotJson())
            .build();
        productVersionManager.save(nextDraft);

        // 5. 落发布记录(RUNNING),异步监听器执行 TD DDL 后回写 SUCCESS / FAILED;
        //    最大兜底重试次数取用户配置(clamp 到 1~10,缺省 PUBLISH_RETRY_DEFAULT)
        Integer maxRetry = resolvePublishMaxRetry(vo.getMaxRetryCount());
        ProductPublishRecord record = productPublishRecordService.recordPublish(
            productIdentification, previousVersion, publishedVersion, maxRetry);

        // 6. 发事件,异步执行 TD DDL + 设备改绑 + 缓存刷新
        productVersionEventPublisher.publishPublished(ProductVersionLifecycleEventSource.builder()
            .productIdentification(productIdentification)
            .sourceVersion(previousVersion)
            .targetVersion(publishedVersion)
            .publishStrategy(strategy)
            .publishRecordId(record.getId())
            .build());

        log.info("[ProductVersion] publish accepted productIdentification={} publishedVersion={} nextDraft={} strategy={} switchPointer={} by={} recordId={}",
            productIdentification, publishedVersion, nextDraft.getVersionNo(), strategy,
            shouldSwitchPointer, ContextUtil.getUserId(), record.getId());
        return draft;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVersion rollback(ProductVersionRollbackVO vo) {
        ArgumentAssert.notNull(vo, "rollback VO must not be null");
        String productIdentification = vo.getProductIdentification();
        String targetVersion = vo.getTargetVersion();

        ProductResultVO product = Optional.ofNullable(
                productQueryService.findOneByProductIdentification(productIdentification))
            .orElseThrow(() -> BizException.wrap("Product not found: " + productIdentification));

        ProductVersion targetRow = productVersionManager
            .findByProductIdentificationAndVersionNo(productIdentification, targetVersion)
            .orElseThrow(() -> BizException.wrap("Target version not found: " + targetVersion));

        if (StrUtil.equals(product.getActiveVersionNo(), targetVersion)) {
            throw BizException.wrap("Already on target version, no rollback needed");
        }

        String fromVersion = product.getActiveVersionNo();
        // 跨域产品写入走 ProductService 而非 ProductManager(满足"禁止跨层级" + Service AOP 切库)
        productService.rollbackActiveVersion(productIdentification, targetVersion);

        targetRow.setVersionStatus(ProductVersionStatusEnum.PUBLISHED.getValue());
        targetRow.setRemark(Optional.ofNullable(vo.getRollbackRemark()).orElse(targetRow.getRemark()));
        productVersionManager.updateById(targetRow);

        // 把原 active 版本(被回滚走的)标记为 ROLLED_BACK ── 否则版本列表里它还显示 PUBLISHED
        // 但实际已不是 active,前端 status tab 计数错、最新生效徽章错配,用户感知混乱
        Optional.ofNullable(fromVersion)
            .flatMap(v -> productVersionManager.findByProductIdentificationAndVersionNo(productIdentification, v))
            .ifPresent(fromRow -> {
                fromRow.setVersionStatus(ProductVersionStatusEnum.ROLLED_BACK.getValue());
                productVersionManager.updateById(fromRow);
            });

        ProductPublishRecord record = productPublishRecordService.recordRollback(
            productIdentification, fromVersion, targetVersion);

        productVersionEventPublisher.publishRolledBack(ProductVersionLifecycleEventSource.builder()
            .productIdentification(productIdentification)
            .sourceVersion(fromVersion)
            .targetVersion(targetVersion)
            .publishRecordId(record.getId())
            .build());

        log.info("[ProductVersion] rollback accepted productIdentification={} from={} to={} by={} recordId={}",
            productIdentification, fromVersion, targetVersion, ContextUtil.getUserId(), record.getId());
        return targetRow;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVersion purgeHistory(ProductVersionPurgeVO vo) {
        ArgumentAssert.notNull(vo, "purge VO must not be null");
        String productIdentification = vo.getProductIdentification();
        String version = vo.getVersionNo();

        ProductResultVO product = Optional.ofNullable(
                productQueryService.findOneByProductIdentification(productIdentification))
            .orElseThrow(() -> BizException.wrap("Product not found: " + productIdentification));

        if (StrUtil.equals(product.getActiveVersionNo(), version)) {
            throw BizException.wrap("Cannot purge the current effective version");
        }
        if (StrUtil.equals(product.getPreviousFullVersionNo(), version)) {
            throw BizException.wrap("Cannot purge the canary previous full version");
        }

        ProductVersion versionRow = productVersionManager
            .findByProductIdentificationAndVersionNo(productIdentification, version)
            .orElseThrow(() -> BizException.wrap("Version not found: " + version));

        versionRow.setVersionStatus(ProductVersionStatusEnum.ARCHIVED.getValue());
        versionRow.setRemark(Optional.ofNullable(vo.getPurgeRemark()).orElse(versionRow.getRemark()));
        productVersionManager.updateById(versionRow);

        ProductPublishRecord record = productPublishRecordService.recordPurge(productIdentification, version);

        productVersionEventPublisher.publishPurgeRequested(ProductVersionLifecycleEventSource.builder()
            .productIdentification(productIdentification)
            .sourceVersion(version)
            .targetVersion(version)
            .publishRecordId(record.getId())
            .build());

        log.info("[ProductVersion] purgeHistory accepted productIdentification={} version={} by={} recordId={}",
            productIdentification, version, ContextUtil.getUserId(), record.getId());
        return versionRow;
    }

    /**
     * {@inheritDoc}
     *
     * <p>多个 count 查询拼装,统计粒度按租户(@DS 已切到当前租户库),不跨租户聚合。</p>
     */
    @Override
    public ProductVersionStatisticsResultVO statistics() {
        // 全部 count 走 Service / 本域 Manager 层(满足"禁止跨层级调用"+ @DS 切租户库生效);
        // 每项用 safeCount 兜底:任一查询异常(租户库缺表 / SQL 报错 / DB 抖动)仅该项归 0,
        // 绝不让整个统计接口对前端 500 ── 看板宁可显示 0,也不能整页报错。
        Long total = safeCount(productQueryService::findProductTotal);
        Long published = safeCount(productQueryService::countPublishedProducts);
        Long canary = safeCount(productQueryService::countCanaryInProgressProducts);
        long unpublished = Math.max(0L, total - published);
        Long recent7d = safeCount(() -> productPublishRecordService.countSuccessfulPublishesInLastDays(7));
        // 物模型服务数(建模深度)── 走 ProductQueryService 统计 product_service
        Long thingModelServiceCount = safeCount(productQueryService::countThingModelServices);
        // 发布版本总量 ── 本域 Manager 按 version_status=PUBLISHED 统计 product_version
        Long publishedVersionTotal = safeCount(() ->
            productVersionManager.countByVersionStatus(ProductVersionStatusEnum.PUBLISHED.getValue()));
        return ProductVersionStatisticsResultVO.builder()
            .productTotal(total)
            .publishedProductCount(published)
            .canaryProductCount(canary)
            .unpublishedProductCount(unpublished)
            .recentPublishCount7d(recent7d)
            .thingModelServiceCount(thingModelServiceCount)
            .publishedVersionTotal(publishedVersionTotal)
            .build();
    }

    /**
     * 统计项安全计数 ── 单项查询异常归 0 并告警不向上抛,保证统计接口对前端永远成功返回(看板兜底 0,不整页 500)。
     *
     * @param supplier 计数查询
     * @return 计数结果;查询异常或为 null 时返回 0
     */
    private Long safeCount(java.util.function.Supplier<Long> supplier) {
        try {
            Long v = supplier.get();
            return v == null ? 0L : v;
        } catch (Exception e) {
            log.warn("[ProductVersion.statistics] count failed, fallback 0", e);
            return 0L;
        }
    }

    @Override
    public ProductVersionDiffVO diff(String productIdentification, String sourceVersion, String targetVersion) {
        ArgumentAssert.notNull(productIdentification, "productIdentification must not be null");
        ArgumentAssert.notNull(targetVersion, "targetVersion must not be null");

        // 同号短路 ── 自比时直接返回空 diff,避免反射计算 + 防止 changeType 被默认推为 UPDATE(语义错)
        if (StrUtil.isNotBlank(sourceVersion) && sourceVersion.equals(targetVersion)) {
            return ProductVersionDiffVO.builder()
                .sourceVersion(sourceVersion)
                .targetVersion(targetVersion)
                .summary(new ProductVersionDiffSummaryVO())
                .nodes(List.of())
                .build();
        }

        ProductSnapshotVO targetSnapshot = loadSnapshot(productIdentification, targetVersion)
            .orElseThrow(() -> BizException.wrap("Target version not found: " + targetVersion));

        // source 显式传了但找不到 → 必须抛错,避免静默退化为"全部新增"误导用户(首次发布场景才允许 source 为空)
        ProductSnapshotVO sourceSnapshot = null;
        if (StrUtil.isNotBlank(sourceVersion)) {
            sourceSnapshot = loadSnapshot(productIdentification, sourceVersion)
                .orElseThrow(() -> BizException.wrap("Source version not found: " + sourceVersion));
        }

        return productSnapshotDiffCalculator.diff(sourceSnapshot, targetSnapshot);
    }

    // ────────────── 私有 ──────────────

    private Optional<ProductSnapshotVO> loadSnapshot(String productIdentification, String versionNo) {
        return productVersionManager.findByProductIdentificationAndVersionNo(productIdentification, versionNo)
            .map(ProductVersion::getProductSnapshotJson)
            .flatMap(productSnapshotConverter::deserialize);
    }

    private ProductVersionStatusEnum resolveStatusByStrategy(ProductPublishStrategyEnum strategy) {
        return switch (strategy == null ? ProductPublishStrategyEnum.FULL : strategy) {
            case FULL -> ProductVersionStatusEnum.PUBLISHED;
            case CANARY -> ProductVersionStatusEnum.CANARY;
            case SHADOW -> ProductVersionStatusEnum.SHADOW;
        };
    }

    /** 最大兜底重试次数缺省值(用户未填时)。 */
    private static final int PUBLISH_RETRY_DEFAULT = 3;
    /** 最大兜底重试次数上限(前端 max 与后端 clamp 共用此值)。 */
    private static final int PUBLISH_RETRY_MAX = 10;

    /**
     * 解析用户配置的最大兜底重试次数:null 取缺省 {@value #PUBLISH_RETRY_DEFAULT},否则 clamp 到 [1, {@value #PUBLISH_RETRY_MAX}]。
     * 后端兜底校验(VO 已带 {@code @Min/@Max},此处再 clamp 防直连接口绕过)。
     *
     * @param input 用户输入(可空)
     * @return 1~{@value #PUBLISH_RETRY_MAX} 的有效值
     */
    private Integer resolvePublishMaxRetry(Integer input) {
        if (input == null) {
            return PUBLISH_RETRY_DEFAULT;
        }
        return Math.min(Math.max(input, 1), PUBLISH_RETRY_MAX);
    }

    /**
     * 把被取代的上一个 active 版本从 CANARY(瞬态)demote 为 PUBLISHED(历史态)。
     * 仅 CANARY 需要:灰度版本一旦被新发布取代就不再是"进行中的灰度",残留 CANARY 标签会让版本列表误导;
     * PUBLISHED 历史版本保持原状(本就是合法历史态);DRAFT/ROLLED_BACK/ARCHIVED/SHADOW 不会成为 active,无需考虑。
     * 幂等:仅当前状态确为 CANARY 才改,重跑无副作用。
     *
     * @param productIdentification 产品标识
     * @param supersededVersion     被取代的上一个 active 版本号(空白则跳过,如首次发布)
     */
    private void demoteSupersededCanary(String productIdentification, String supersededVersion) {
        if (StrUtil.isBlank(supersededVersion)) {
            return;
        }
        productVersionManager.findByProductIdentificationAndVersionNo(productIdentification, supersededVersion)
            .filter(v -> ProductVersionStatusEnum.CANARY.getValue().equals(v.getVersionStatus()))
            .ifPresent(v -> {
                v.setVersionStatus(ProductVersionStatusEnum.PUBLISHED.getValue());
                productVersionManager.updateById(v);
                log.info("[ProductVersion] demoted superseded canary version {} -> PUBLISHED, product={}",
                    supersededVersion, productIdentification);
            });
    }
}
