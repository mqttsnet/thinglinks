package com.mqttsnet.thinglinks.service.bridge;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.bridge.SubscriptionSource;
import com.mqttsnet.thinglinks.vo.query.bridge.SubscriptionSourcePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.SubscriptionSourceResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.SubscriptionSourceSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.SubscriptionSourceUpdateVO;

import java.util.List;

/**
 * <p>
 * 业务接口
 * 数据桥接-订阅源
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface SubscriptionSourceService extends SuperService<Long, SubscriptionSource> {

    /**
     * 查询订阅源 VO 列表。
     *
     * @param query 查询参数
     * @return {@link List<SubscriptionSourceResultVO>} 订阅源 VO 列表
     */
    List<SubscriptionSourceResultVO> getSubscriptionSourceResultVOList(SubscriptionSourcePageQuery query);

    /**
     * 批量给 VO 列表反填 dataSourceCode + dataSourceName(供 Controller.handlerResult 钩子用)。
     * <p>{@code PageController.page} 默认实现走 {@code BeanPlusUtil.toBeanPage} 直转 VO,
     * 不会经过 {@link #getSubscriptionSourceResultVOList};Controller 重写 {@code handlerResult}
     * 调本方法,确保列表 / 卡片 / 详情都能展示数据源业务编码 + 名称。
     *
     * @param voList 已查出的 VO 列表(原地修改;空列表 / null 直接返回)
     */
    void attachDataSourceInfo(List<SubscriptionSourceResultVO> voList);

    /**
     * 查询订阅源详情。
     *
     * @param id 订阅源主键 ID
     * @return {@link SubscriptionSourceResultVO} 订阅源详情
     */
    SubscriptionSourceResultVO getSubscriptionSourceDetail(Long id);

    /**
     * 保存订阅源（默认 enable=false）。
     * <p>校验：关联数据源 direction 必须为入站(20)或双向(30)；sourceCode 自动雪花生成（若未传）。</p>
     *
     * @param saveVO 保存参数
     * @return {@link SubscriptionSourceSaveVO} 保存后的 VO
     */
    SubscriptionSourceSaveVO saveSubscriptionSource(SubscriptionSourceSaveVO saveVO);

    /**
     * 更新订阅源（配置变更后 enable 自动重置为 false，需重新启用）。
     *
     * @param updateVO 更新参数
     * @return {@link SubscriptionSourceUpdateVO} 更新后的 VO
     */
    SubscriptionSourceUpdateVO updateSubscriptionSource(SubscriptionSourceUpdateVO updateVO);

    /**
     * 启停订阅源。
     * <p>启用时：发 SubscriptionSourceChangedEvent 给 {@code rule-biz/SubscriptionSourceManager}，
     * 后者启动 KafkaConsumer / MqttClient subscribe / HTTP handler 注册。
     * 禁用时：触发对应 Source.stop() 并保存 last_consume_offset。</p>
     *
     * @param id     订阅源主键 ID
     * @param enable true=启用 / false=禁用
     * @return true=切换成功
     */
    boolean changeStatus(Long id, Boolean enable);

    /**
     * 删除订阅源。
     * <p>启用中的订阅源禁止直接删 ── 后台 KafkaConsumer / MqttClient subscribe 还在拉消息，
     * 直接删 DB 记录会导致拉到的消息无法找到关联配置。先 disable 再删。</p>
     *
     * @param id 订阅源主键 ID
     * @return true=删除成功
     * @throws com.mqttsnet.basic.exception.BizException 订阅源处于启用状态时抛出
     */
    boolean deleteSubscriptionSource(Long id);

    /**
     * 按业务编码查订阅源(入站 HTTP endpoint 用 sourceCode 路由到具体订阅源)。
     *
     * @param sourceCode 业务编码
     * @return 订阅源实体;不存在返回 null
     */
    SubscriptionSource getByCode(String sourceCode);
}
