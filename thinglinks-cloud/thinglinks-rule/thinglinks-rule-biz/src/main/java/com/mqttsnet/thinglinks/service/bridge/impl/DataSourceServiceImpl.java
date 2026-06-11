package com.mqttsnet.thinglinks.service.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.databridge.model.ConnectorConfig;
import com.mqttsnet.basic.databridge.model.ConnectorType;
import com.mqttsnet.basic.databridge.registry.ConnectorRegistry;
import com.mqttsnet.basic.databridge.spi.Sink;
import com.mqttsnet.basic.databridge.spi.Source;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.bridge.event.DataSourceDeleteCheckEvent;
import com.mqttsnet.thinglinks.bridge.event.publisher.BridgeEventPublisher;
import com.mqttsnet.thinglinks.bridge.event.source.DataSourceDeleteCheckEventSource;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.enumeration.bridge.BridgeDirectionEnum;
import com.mqttsnet.thinglinks.manager.bridge.DataSourceManager;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.vo.query.bridge.DataSourcePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.DataSourceResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.DataSourceSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.DataSourceUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 数据桥接-数据源业务实现。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class DataSourceServiceImpl
        extends SuperServiceImpl<DataSourceManager, Long, DataSource>
        implements DataSourceService {

    private static final String STATUS_HEALTHY = "HEALTHY";
    private static final String STATUS_DOWN = "DOWN";
    private static final String STATUS_UNKNOWN = "UNKNOWN";
    private static final String SERIALIZATION_DEFAULT = "JSON";

    /**
     * Lazy:业务侧不调测试连接时不强依赖 starter。
     */
    @Autowired(required = false)
    private ConnectorRegistry connectorRegistry;

    /**
     * DataSourceDeleteCheckEvent 跨域校验事件用,变更事件统一走 BridgeEventPublisher。
     */
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BridgeEventPublisher bridgeEventPublisher;

    @Override
    public List<DataSourceResultVO> getDataSourceResultVOList(DataSourcePageQuery query) {
        List<DataSourceResultVO> voList = BeanPlusUtil.copyToList(
                superManager.getDataSourceList(query), DataSourceResultVO.class);
        // 列表屏蔽敏感字段(仅详情接口返回 connectionJson + credentialJson 明文)
        voList.forEach(this::maskSensitiveFields);
        return voList;
    }

    @Override
    public DataSourceResultVO getDataSourceDetail(Long id) {
        DataSource entity = superManager.getById(id);
        ArgumentAssert.notNull(entity, "数据源不存在或已被删除");
        DataSourceResultVO vo = new DataSourceResultVO();
        BeanUtils.copyProperties(entity, vo);
        // connectionJson / credentialJson 已由 EncryptTypeHandler 自动解密
        return vo;
    }

    @Override
    public DataSourceResultVO getDataSourceVO(Long id) {
        if (id == null) {
            return null;
        }
        // cache miss 回源:返 null 让 helper 不缓存 null
        return Optional.ofNullable(superManager.getById(id))
                .map(e -> BeanPlusUtil.toBeanIgnoreError(e, DataSourceResultVO.class))
                .orElse(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataSourceSaveVO saveDataSource(DataSourceSaveVO saveVO) {
        log.info("保存数据源: name={} type={} direction={}",
                saveVO.getDataSourceName(), saveVO.getSourceType(), saveVO.getDirection());
        validateSourceTypeAndDirection(saveVO.getSourceType(), saveVO.getDirection());

        DataSource entity = new DataSource();
        BeanUtils.copyProperties(saveVO, entity);
        if (StrUtil.isBlank(entity.getDataSourceCode())) {
            entity.setDataSourceCode(SnowflakeIdUtil.nextId());
        }
        // 默认禁用 ── 必须测试连接成功后手动启用
        entity.setEnable(Boolean.FALSE);
        entity.setHealthStatus(STATUS_UNKNOWN);

        ArgumentAssert.isTrue(superManager.save(entity), "数据源保存失败");
        bridgeEventPublisher.publishDataSourceChangedEvent(entity.getId());
        return saveVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DataSourceUpdateVO updateDataSource(DataSourceUpdateVO updateVO) {
        log.info("更新数据源: id={} name={}", updateVO.getId(), updateVO.getDataSourceName());
        ArgumentAssert.notNull(superManager.getById(updateVO.getId()), "数据源不存在或已被删除");
        validateSourceTypeAndDirection(updateVO.getSourceType(), updateVO.getDirection());

        DataSource entity = new DataSource();
        BeanUtils.copyProperties(updateVO, entity);
        // 配置变更后 enable 自动重置(用户必须重新测试 + 手动启用)
        entity.setEnable(Boolean.FALSE);
        entity.setHealthStatus(STATUS_UNKNOWN);

        ArgumentAssert.isTrue(superManager.updateById(entity), "数据源更新失败");
        bridgeEventPublisher.publishDataSourceChangedEvent(entity.getId());
        return updateVO;
    }

    @Override
    public boolean testConnection(Long id) {
        DataSource ds = superManager.getById(id);
        ArgumentAssert.notNull(ds, "数据源不存在或已被删除");
        boolean ok = doTestConnection(toConnectorConfig(ds));
        // 立即写回 DB,用户列表页能马上看到健康状态变化(不等 5min 定时检查)
        updateHealthStatus(id, ok);
        return ok;
    }

    @Override
    public boolean testConnectionByForm(DataSourceSaveVO formVO) {
        DataSource tmp = new DataSource();
        BeanUtils.copyProperties(formVO, tmp);
        tmp.setId(0L);
        if (StrUtil.isBlank(tmp.getDataSourceCode())) {
            tmp.setDataSourceCode("test-" + SnowflakeIdUtil.nextId());
        }
        // 表单测试是"未保存"临时对象,不写 DB
        return doTestConnection(toConnectorConfig(tmp));
    }

    /**
     * 把 testConnection 结果立即写回 DB,失败仅 warn 不影响返回值。
     */
    private void updateHealthStatus(Long id, boolean healthy) {
        try {
            DataSource update = new DataSource();
            update.setId(id);
            update.setHealthStatus(healthy ? STATUS_HEALTHY : STATUS_DOWN);
            update.setLastHealthCheckTime(LocalDateTime.now());
            superManager.updateById(update);
        } catch (Exception e) {
            log.warn("[DataSource] update healthStatus failed (non-blocking) id={} healthy={}: {}",
                    id, healthy, e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, Boolean enable) {
        DataSource ds = superManager.getById(id);
        ArgumentAssert.notNull(ds, "数据源不存在或已被删除");
        // 启用前后端兜底再做一次 testConnection,防绕过前端
        if (Boolean.TRUE.equals(enable) && !doTestConnection(toConnectorConfig(ds))) {
            throw BizException.wrap("数据源连接测试失败,请先排查配置后再启用");
        }
        ds.setEnable(enable);
        boolean ok = superManager.updateById(ds);
        if (ok) {
            bridgeEventPublisher.publishDataSourceChangedEvent(id);
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDataSource(Long id) {
        DataSource ds = superManager.getById(id);
        ArgumentAssert.notNull(ds, "数据源不存在或已被删除");

        // 1. 同域校验:被其它数据源配为默认死信目标
        long defaultDlqCount = superManager.count(Wrappers.<DataSource>lambdaQuery()
                .eq(DataSource::getDefaultDeadLetterDataSourceId, id));
        if (defaultDlqCount > 0) {
            throw BizException.wrap("此数据源被 " + defaultDlqCount
                    + " 个其它数据源配为默认死信目标,请先调整");
        }

        // 2. 跨域校验:DataBridge/Subscription 监听并自查,持有引用就抛错(切断循环依赖)
        eventPublisher.publishEvent(new DataSourceDeleteCheckEvent(
                DataSourceDeleteCheckEventSource.builder().dataSourceId(id).build()));

        // 3. 释放底层连接资源(失败不阻断删除)
        releaseConnectorResources(ds);

        boolean removed = superManager.removeById(id);
        if (removed) {
            bridgeEventPublisher.publishDataSourceDeletedEvent(id);
        }
        return removed;
    }

    private void releaseConnectorResources(DataSource ds) {
        if (connectorRegistry == null) {
            return;
        }
        try {
            ConnectorType type = ConnectorType.valueOf(ds.getSourceType());
            Optional.ofNullable(connectorRegistry.getSink(type))
                    .ifPresent(s -> s.close(toConnectorConfig(ds)));
            Optional.ofNullable(connectorRegistry.getSource(type))
                    .ifPresent(s -> s.stop(ds.getDataSourceCode()));
        } catch (Exception e) {
            log.warn("[DataSource] 删除前释放连接资源失败 id={}: {}", ds.getId(), e.getMessage());
        }
    }

    // ============================== 内部 ==============================

    /**
     * 列表屏蔽 connectionJson + credentialJson(仅详情接口返回明文)。
     */
    private void maskSensitiveFields(DataSourceResultVO vo) {
        if (vo == null) {
            return;
        }
        vo.setConnectionJson(null);
        vo.setCredentialJson(null);
    }

    /**
     * entity → ConnectorConfig(pool key 用 dataSourceCode,util 不感知 tenant)。
     */
    private ConnectorConfig toConnectorConfig(DataSource ds) {
        return ConnectorConfig.builder()
                .type(ConnectorType.valueOf(ds.getSourceType()))
                .identifier(StrUtil.nullToDefault(ds.getDataSourceCode(), String.valueOf(ds.getId())))
                .connectionJson(ds.getConnectionJson())
                .credentialJson(ds.getCredentialJson())
                .extraConfigJson(ds.getExtendParams())
                .serialization(StrUtil.nullToDefault(ds.getSerialization(), SERIALIZATION_DEFAULT))
                .build();
    }

    /**
     * 调 Sink/Source.testConnection。绝大多数 Sink;入站才走 Source。
     */
    private boolean doTestConnection(ConnectorConfig cfg) {
        if (connectorRegistry == null) {
            log.warn("[DataSource] ConnectorRegistry not autowired, fallback false");
            return false;
        }
        Sink sink = connectorRegistry.getSink(cfg.getType());
        if (sink != null) {
            return safeTest(() -> sink.testConnection(cfg), "Sink", cfg.getType().name());
        }
        Source source = connectorRegistry.getSource(cfg.getType());
        if (source != null) {
            return safeTest(() -> source.testConnection(cfg), "Source", cfg.getType().name());
        }
        log.warn("[DataSource] no Sink/Source registered for type={}", cfg.getType());
        return false;
    }

    private boolean safeTest(java.util.function.BooleanSupplier task, String role, String type) {
        try {
            return task.getAsBoolean();
        } catch (Exception e) {
            log.warn("[DataSource] {}.testConnection failed type={}: {}", role, type, e.getMessage());
            return false;
        }
    }

    /**
     * 校验 sourceType 是合法 ConnectorType + direction 是合法枚举。
     */
    private void validateSourceTypeAndDirection(String sourceType, String direction) {
        try {
            ConnectorType.valueOf(sourceType);
        } catch (Exception e) {
            throw BizException.wrap("不支持的协议类型: " + sourceType);
        }
        if (BridgeDirectionEnum.fromValue(direction).isEmpty()) {
            throw BizException.wrap("不支持的方向值: " + direction);
        }
    }
}
