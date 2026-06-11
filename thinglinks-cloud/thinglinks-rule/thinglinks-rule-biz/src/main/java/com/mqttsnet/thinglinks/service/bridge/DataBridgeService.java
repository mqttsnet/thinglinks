package com.mqttsnet.thinglinks.service.bridge;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.bridge.DataBridge;
import com.mqttsnet.thinglinks.vo.query.bridge.DataBridgePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.DataBridgeResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.DataBridgeSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.DataBridgeUpdateVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 业务接口
 * 数据桥接-规则
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface DataBridgeService extends SuperService<Long, DataBridge> {

    /**
     * 查询桥接规则 VO 列表（列表接口屏蔽 actionConfigJson 明文，防止内联 Bearer token 泄漏）。
     *
     * @param query 查询参数
     * @return {@link List<DataBridgeResultVO>} 桥接规则 VO 列表（actionConfigJson 已置 null）
     */
    List<DataBridgeResultVO> getDataBridgeResultVOList(DataBridgePageQuery query);

    /**
     * 批量给 VO 列表反填 dataSourceCode + dataSourceName(供 Controller.handlerResult 钩子用)。
     * <p>{@code PageController.page} 的默认实现走 {@code BeanPlusUtil.toBeanPage} 直转 VO,
     * 不会经过 {@link #getDataBridgeResultVOList};Controller 重写 {@code handlerResult}
     * 时调用本方法,确保列表 / 卡片 / 详情都能展示数据源业务编码 + 名称。
     *
     * @param voList 已查出的 VO 列表(原地修改;空列表 / null 直接返回)
     */
    void attachDataSourceInfo(List<DataBridgeResultVO> voList);

    /**
     * 查询桥接规则详情（返回完整明文，含 EncryptTypeHandler 自动解密的 actionConfigJson）。
     *
     * @param id 桥接规则主键 ID
     * @return {@link DataBridgeResultVO} 桥接规则详情
     */
    DataBridgeResultVO getDataBridgeDetail(Long id);

    /**
     * 保存桥接规则（默认 enable=false，必须测试发送成功后手动启用）。
     * <p>校验：关联数据源存在、ruleCode 自动雪花生成（若未传）。</p>
     *
     * @param saveVO 保存参数
     * @return {@link DataBridgeSaveVO} 保存后的 VO
     */
    DataBridgeSaveVO saveDataBridge(DataBridgeSaveVO saveVO);

    /**
     * 更新桥接规则（配置变更后 enable 自动重置为 false，需重新测试启用）。
     *
     * @param updateVO 更新参数
     * @return {@link DataBridgeUpdateVO} 更新后的 VO
     */
    DataBridgeUpdateVO updateDataBridge(DataBridgeUpdateVO updateVO);

    /**
     * 测试发送（编辑表单"测试发送"按钮用）。
     * <p>用规则当前配置 + 用户提供的 sample envelope，调 thinglinks-util {@code Sink.send()} 实际发送一次。
     * 返回结果含 success / messageId / latencyMs / errorCode / errorMessage / attributes。</p>
     *
     * @param id             桥接规则主键 ID
     * @param sampleEnvelope 样例消息（任意 Map，会被 JSON 序列化成 byte[] 发送）
     * @return {@link Map} 发送结果，键值参考 {@code com.mqttsnet.basic.databridge.model.SendResult} 字段
     */
    Map<String, Object> testSink(Long id, Map<String, Object> sampleEnvelope);

    /**
     * 切换启用状态。
     * <p>启用前后端兜底：要求关联数据源 enable=true。</p>
     *
     * @param id     桥接规则主键 ID
     * @param enable true=启用 / false=禁用
     * @return true=切换成功
     */
    boolean changeStatus(Long id, Boolean enable);

    /**
     * 复制规则（同模板新建一条 disabled 状态规则，名称追加 "(副本)"）。
     *
     * @param id 源规则主键 ID
     * @return 新规则主键 ID
     */
    Long copyRule(Long id);

    /**
     * 删除桥接规则。
     * <p>桥接规则无外部 FK 引用（trace 表是日志不阻塞删除）。删除前要求 enable=false：
     * 启用中的规则禁止直接删，必须先 disable 再删，避免 matcher 缓存还在分发但记录已消失。
     * 删除后应发 BridgeRuleChangedEvent 让 BridgeRuleCache 失效（Phase 7 实施）。</p>
     *
     * @param id 桥接规则主键 ID
     * @return true=删除成功
     * @throws com.mqttsnet.basic.exception.BizException 规则处于启用状态时抛出
     */
    boolean deleteDataBridge(Long id);

    /**
     * 查启用中的桥接规则 VO 列表(matcher 缓存 miss 回源 / 事件刷新桶时调)。
     * <p>Service 层完成 entity → VO 转换(BeanPlusUtil),helper / matcher 拿到的都是 VO,
     * 不再持有 DataBridge entity 引用,避免 ORM 包袱(@TableField/@EncryptTypeHandler)
     * 流向缓存层。helper 在 hMSet 入缓存时再把 VO 转成 {@code DataBridgeCacheVO}
     * (去 echoMap / dataSourceCode / dataSourceName 等列表展示字段,只留热路径必需字段)。
     *
     * @param appId     应用 ID(可空 → 不限,跨应用全部规则)
     * @param direction 方向 10/20
     * @return VO 列表(永不返回 null;actionConfigJson 已含明文,SinkDispatcher 直接用)
     */
    List<DataBridgeResultVO> getEnabledRules(String appId, String direction);
}
