package com.mqttsnet.thinglinks.service.bridge;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.bridge.DataSource;
import com.mqttsnet.thinglinks.vo.query.bridge.DataSourcePageQuery;
import com.mqttsnet.thinglinks.vo.result.bridge.DataSourceResultVO;
import com.mqttsnet.thinglinks.vo.save.bridge.DataSourceSaveVO;
import com.mqttsnet.thinglinks.vo.update.bridge.DataSourceUpdateVO;

import java.util.List;

/**
 * <p>
 * 业务接口
 * 数据桥接-数据源
 * </p>
 *
 * @author mqttsnet
 * @date 2026-04-28
 */
public interface DataSourceService extends SuperService<Long, DataSource> {

    /**
     * 查询数据源 VO 列表（列表接口屏蔽 connectionJson + credentialJson 敏感字段，防止凭证明文外泄）。
     * <p>详情字段仅在 {@link #getDataSourceDetail(Long)} 中返回。</p>
     *
     * @param query 查询参数
     * @return {@link List<DataSourceResultVO>} 数据源 VO 列表（敏感字段已置 null）
     */
    List<DataSourceResultVO> getDataSourceResultVOList(DataSourcePageQuery query);

    /**
     * 查询数据源详情（返回完整明文，含 EncryptTypeHandler 自动解密的 connectionJson + credentialJson）。
     *
     * @param id 数据源主键 ID
     * @return {@link DataSourceResultVO} 数据源详情，含敏感字段明文
     */
    DataSourceResultVO getDataSourceDetail(Long id);

    /**
     * 查数据源 VO(缓存 miss 回源用,与 {@link #getDataSourceDetail(Long)} 区别：不存在时返回 null,不抛异常)。
     * <p>Service 层完成 entity → VO 转换(BeanPlusUtil),helper / dispatcher 都拿 VO,
     * 不再持有 DataSource entity 引用,避免 ORM 包袱(@TableField/@EncryptTypeHandler)流向缓存层。
     * helper 在写缓存时再把 ResultVO 转成 {@code DataSourceCacheVO}(去 echoMap / lastHealthCheckTime
     * / remark / 审计字段噪音,只留热路径必需字段)。
     *
     * @param id 数据源主键 ID
     * @return {@link DataSourceResultVO} 数据源 VO;不存在时返回 null
     */
    DataSourceResultVO getDataSourceVO(Long id);

    /**
     * 保存数据源（默认 enable=false，必须测试连接成功后手动启用）。
     * <p>校验：协议类型合法、方向合法、业务编码自动雪花生成（若未传）。</p>
     *
     * @param saveVO 保存参数
     * @return {@link DataSourceSaveVO} 保存后的 VO（含自动生成的 dataSourceCode）
     */
    DataSourceSaveVO saveDataSource(DataSourceSaveVO saveVO);

    /**
     * 更新数据源。
     * <p><b>关键</b>：修改任一字段后 enable 自动重置为 false，需用户重新测试连接 + 手动启用。
     * 防止配置错误的数据源持续运行导致桥接消息批量入死信。</p>
     *
     * @param updateVO 更新参数
     * @return {@link DataSourceUpdateVO} 更新后的 VO
     */
    DataSourceUpdateVO updateDataSource(DataSourceUpdateVO updateVO);

    /**
     * 测试连接（基于 DB 中已保存的数据源配置）。
     * <p>调用 thinglinks-util {@code Sink.testConnection()} 或 {@code Source.testConnection()}，
     * 自动按 sourceType + direction 路由到对应实现。</p>
     *
     * @param id 数据源主键 ID
     * @return true=连接成功；false=任意原因失败（网络 / 认证 / topic 不存在等）
     */
    boolean testConnection(Long id);

    /**
     * 测试连接（基于编辑表单当前未保存的值）── 编辑表单"测试连接"按钮用。
     * <p>不读 DB，直接用 formVO 拼 ConnectorConfig 测试；用临时 identifier 不影响 pool 现有缓存。</p>
     *
     * @param formVO 编辑表单当前值
     * @return true=连接成功
     */
    boolean testConnectionByForm(DataSourceSaveVO formVO);

    /**
     * 切换启用状态（前端 toggle on/off 触发）。
     * <p><b>后端兜底校验</b>：当 enable=true 时再次调用 {@link #testConnection(Long)} 兜底，
     * 防绕过前端直接调 API。校验失败抛 BizException。</p>
     *
     * @param id     数据源主键 ID
     * @param enable true=启用 / false=禁用
     * @return true=切换成功；false=数据库更新失败
     */
    boolean changeStatus(Long id, Boolean enable);

    /**
     * 删除数据源（含关联引用检查）。
     * <p>检查以下 4 处引用，任一存在则抛 BizException 阻止删除：</p>
     * <ol>
     *   <li>{@code rule_data_bridge.data_source_id}（被桥接规则引用为出/入站源）</li>
     *   <li>{@code rule_data_bridge.dead_letter_data_source_id}（被桥接规则配为死信目标）</li>
     *   <li>{@code rule_data_source.default_dead_letter_data_source_id}（被其它数据源配为默认死信）</li>
     *   <li>{@code rule_subscription_source.data_source_id}（被订阅源引用）</li>
     * </ol>
     * <p>用户需先解除上述关联（修改对应记录或删除）才能删除数据源。</p>
     *
     * @param id 数据源主键 ID
     * @return true=删除成功
     * @throws com.mqttsnet.basic.exception.BizException 存在引用时抛出，错误信息含引用数量提示
     */
    boolean deleteDataSource(Long id);
}
