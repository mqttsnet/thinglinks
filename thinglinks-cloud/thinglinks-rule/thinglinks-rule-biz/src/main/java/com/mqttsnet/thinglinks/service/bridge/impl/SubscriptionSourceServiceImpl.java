package com.mqttsnet.thinglinks.service.bridge.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.thinglinks.bridge.config.SubscriptionSourcePubSubConfig;
import com.mqttsnet.thinglinks.bridge.enrich.DataSourceInfoAttacher;
import com.mqttsnet.thinglinks.bridge.event.DataSourceDeleteCheckEvent;
import com.mqttsnet.thinglinks.bridge.event.SubscriptionSourceChangedEvent;
import com.mqttsnet.thinglinks.bridge.event.source.SubscriptionSourceChangedEventSource;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import com.mqttsnet.thinglinks.manager.bridge.SubscriptionSourceManager;
import com.mqttsnet.thinglinks.service.bridge.DataSourceService;
import com.mqttsnet.thinglinks.service.bridge.SubscriptionSourceService;
import com.mqttsnet.thinglinks.vo.query.bridge.SubscriptionSourcePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.SubscriptionSourceResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.SubscriptionSourceSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.SubscriptionSourceUpdateVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 数据桥接-订阅源业务实现。
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionSourceServiceImpl
        extends SuperServiceImpl<SubscriptionSourceManager, Long, SubscriptionSource>
        implements SubscriptionSourceService {

    /**
     * 数据源允许的方向:入站(20) / 双向(30)。
     */
    private static final Set<String> ALLOWED_DIRECTIONS = Set.of("20", "30");

    private static final String OP_CREATE = "CREATE";
    private static final String OP_UPDATE = "UPDATE";
    private static final String OP_ENABLE = "ENABLE";
    private static final String OP_DISABLE = "DISABLE";
    private static final String OP_DELETE = "DELETE";

    private final DataSourceInfoAttacher dataSourceInfoAttacher;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 跨域字段注入避循环依赖。
     */
    @Autowired
    private DataSourceService dataSourceService;

    /**
     * Redis Pub/Sub 跨节点失效;未启用时单 JVM 模式 OK。
     */
    @Autowired(required = false)
    private CachePlusOps cachePlusOps;

    @Override
    public List<SubscriptionSourceResultVO> getSubscriptionSourceResultVOList(SubscriptionSourcePageQuery query) {
        List<SubscriptionSourceResultVO> voList = BeanPlusUtil.copyToList(
                superManager.getSubscriptionSourceList(query), SubscriptionSourceResultVO.class);
        attachDataSourceInfo(voList);
        return voList;
    }

    @Override
    public SubscriptionSourceResultVO getSubscriptionSourceDetail(Long id) {
        SubscriptionSource entity = superManager.getById(id);
        ArgumentAssert.notNull(entity, "订阅源不存在或已被删除");
        SubscriptionSourceResultVO vo = new SubscriptionSourceResultVO();
        BeanUtils.copyProperties(entity, vo);
        attachDataSourceInfo(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public void attachDataSourceInfo(List<SubscriptionSourceResultVO> voList) {
        dataSourceInfoAttacher.attach(voList,
                SubscriptionSourceResultVO::getDataSourceId,
                (vo, ds) -> {
                    vo.setDataSourceCode(ds.getDataSourceCode());
                    vo.setDataSourceName(ds.getDataSourceName());
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionSourceSaveVO saveSubscriptionSource(SubscriptionSourceSaveVO saveVO) {
        log.info("保存订阅源: name={}", saveVO.getSourceName());
        DataSource ds = dataSourceService.getById(saveVO.getDataSourceId());
        ArgumentAssert.notNull(ds, "关联数据源不存在");
        if (!ALLOWED_DIRECTIONS.contains(ds.getDirection())) {
            throw BizException.wrap("订阅源关联的数据源方向必须为入站(20)或双向(30)");
        }

        SubscriptionSource entity = new SubscriptionSource();
        BeanUtils.copyProperties(saveVO, entity);
        if (StrUtil.isBlank(entity.getSourceCode())) {
            entity.setSourceCode(SnowflakeIdUtil.nextId());
        }
        entity.setEnable(Boolean.FALSE);

        ArgumentAssert.isTrue(superManager.save(entity), "订阅源保存失败");
        // 默认 enable=false,CREATE 由 LifecycleManager 决定(实际 ENABLE 时才 start)
        publishSubscriptionChanged(OP_CREATE, entity.getSourceCode(), entity);
        return saveVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubscriptionSourceUpdateVO updateSubscriptionSource(SubscriptionSourceUpdateVO updateVO) {
        log.info("更新订阅源: id={}", updateVO.getId());
        ArgumentAssert.notNull(superManager.getById(updateVO.getId()), "订阅源不存在或已被删除");

        SubscriptionSource entity = new SubscriptionSource();
        BeanUtils.copyProperties(updateVO, entity);
        // 配置变更后强制 disable,要求重新测试启用
        entity.setEnable(Boolean.FALSE);

        ArgumentAssert.isTrue(superManager.updateById(entity), "订阅源更新失败");
        // UPDATE → LifecycleManager 会 stopOne(因为 enable=false)
        publishSubscriptionChanged(OP_UPDATE, entity.getSourceCode(), entity);
        return updateVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, Boolean enable) {
        SubscriptionSource src = superManager.getById(id);
        ArgumentAssert.notNull(src, "订阅源不存在或已被删除");
        if (Boolean.TRUE.equals(enable)) {
            DataSource ds = dataSourceService.getById(src.getDataSourceId());
            ArgumentAssert.notNull(ds, "关联数据源不存在");
            if (!Boolean.TRUE.equals(ds.getEnable())) {
                throw BizException.wrap("关联数据源未启用,请先启用数据源");
            }
        }
        src.setEnable(enable);
        boolean ok = superManager.updateById(src);
        publishSubscriptionChanged(
                Boolean.TRUE.equals(enable) ? OP_ENABLE : OP_DISABLE,
                src.getSourceCode(),
                src);
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSubscriptionSource(Long id) {
        SubscriptionSource src = superManager.getById(id);
        ArgumentAssert.notNull(src, "订阅源不存在或已被删除");
        if (Boolean.TRUE.equals(src.getEnable())) {
            throw BizException.wrap("启用中的订阅源不允许删除,请先禁用后再删");
        }
        boolean ok = superManager.removeById(id);
        // 兜底发 DELETE(disable 时已 stop;这里再 stop 一次保证幂等)
        publishSubscriptionChanged(OP_DELETE, src.getSourceCode(), null);
        return ok;
    }

    @Override
    public SubscriptionSource getByCode(String sourceCode) {
        return superManager.getByCode(sourceCode);
    }

    /**
     * 监听 DataSource 待删除事件,持有引用就抛错(切断循环依赖)。
     *
     * @param event 数据源删除校验事件
     */
    @EventListener
    public void onDataSourceDeleteCheck(DataSourceDeleteCheckEvent event) {
        Long dsId = event.getEventSource().getDataSourceId();
        long subCount = superManager.count(Wrappers.<SubscriptionSource>lambdaQuery()
                .eq(SubscriptionSource::getDataSourceId, dsId));
        if (subCount > 0) {
            throw BizException.wrap("此数据源被 " + subCount + " 个订阅源引用,请先删除关联订阅源");
        }
    }

    /**
     * 发"订阅源变更"双通道事件:
     * <ul>
     *   <li>本地 ApplicationEvent → 本节点 LifecycleManager 立即 start/stop</li>
     *   <li>Redis Pub/Sub → 集群其它节点同步处理</li>
     * </ul>
     * 关键:tenantId 写进事件,Redis 订阅线程没 LocalMap,靠事件自带 tenantId 切租户库。
     *
     * @param operation  操作类型(CREATE/UPDATE/ENABLE/DISABLE/DELETE)
     * @param sourceCode 订阅源编码
     * @param source     订阅源快照,DELETE 时为 null
     */
    private void publishSubscriptionChanged(String operation, String sourceCode, SubscriptionSource source) {
        SubscriptionSourceChangedEventSource eventSource = SubscriptionSourceChangedEventSource.builder()
                .operation(operation)
                .sourceCode(sourceCode)
                .snapshot(source)
                .tenantId(ContextUtil.getTenantId())
                .build();
        eventPublisher.publishEvent(new SubscriptionSourceChangedEvent(eventSource));
        if (cachePlusOps != null) {
            try {
                // 跨节点序列化 EventSource(plain POJO),不是 ApplicationEvent
                cachePlusOps.publish(SubscriptionSourcePubSubConfig.CHANNEL, JSON.toJSONString(eventSource));
            } catch (Exception e) {
                log.warn("[SubscriptionSource] redis publish failed (non-blocking) op={} sourceCode={}",
                        operation, sourceCode, e);
            }
        }
    }
}
