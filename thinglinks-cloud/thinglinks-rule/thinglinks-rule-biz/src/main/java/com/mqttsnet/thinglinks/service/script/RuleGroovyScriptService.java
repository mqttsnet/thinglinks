package com.mqttsnet.thinglinks.service.script;

import com.mqttsnet.basic.base.service.SuperService;
import com.mqttsnet.thinglinks.entity.script.RuleGroovyScript;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptDirectCompileParam;
import com.mqttsnet.thinglinks.vo.param.script.RuleGroovyScriptExecuteScriptParam;
import com.mqttsnet.thinglinks.vo.query.script.RuleGroovyScriptPageQuery;
import com.mqttsnet.thinglinks.vo.result.script.GroovyScriptEngineExecutorResultVO;
import com.mqttsnet.thinglinks.vo.result.script.RuleGroovyScriptResultVO;
import com.mqttsnet.thinglinks.vo.result.script.RuleScriptExecStatVO;
import com.mqttsnet.thinglinks.vo.save.script.RuleGroovyScriptSaveVO;
import com.mqttsnet.thinglinks.vo.update.script.RuleGroovyScriptUpdateVO;

import java.util.List;


/**
 * <p>
 * 业务接口
 * 规则脚本表
 * </p>
 *
 * @author mqttsnet
 * @date 2025-03-24 09:54:10
 * @create [2025-03-24 09:54:10] [mqttsnet]
 */
public interface RuleGroovyScriptService extends SuperService<Long, RuleGroovyScript> {


    /**
     * 查询规则脚本VO列表
     *
     * @param query 查询参数
     * @return {@link List<RuleGroovyScriptResultVO>} 规则脚本VO列表
     */
    List<RuleGroovyScriptResultVO> getRuleGroovyScriptResultVOList(RuleGroovyScriptPageQuery query);


    /**
     * 查脚本累计执行统计(按主键 id 取实体 → 拼脚本唯一键 → 读 Redis 计数)。
     *
     * @param id 脚本主键
     * @return {@link RuleScriptExecStatVO} total / success / fail(读不到全 0)
     */
    RuleScriptExecStatVO getExecStat(Long id);


    /**
     * 刷新规则脚本缓存
     *
     * @return {@link Boolean} true:刷新成功 false:刷新失败
     */
    Boolean flushGroovyScriptCache();


    /**
     * 保存规则脚本数据
     *
     * @param saveVO 保存实体
     * @return {@link RuleGroovyScriptSaveVO} 保存实体
     */
    RuleGroovyScriptSaveVO saveGroovyScript(RuleGroovyScriptSaveVO saveVO);


    /**
     * 更新规则脚本数据
     *
     * @param updateVO 更新实体
     * @return {@link RuleGroovyScriptUpdateVO} 更新实体
     */
    RuleGroovyScriptUpdateVO updateGroovyScript(RuleGroovyScriptUpdateVO updateVO);

    /**
     * 删除规则脚本
     *
     * @param id 脚本ID
     * @return {@link Boolean} true:删除成功 false:删除失败
     */
    Boolean deleteGroovyScript(Long id);

    /**
     * 直接执行Groovy脚本
     *
     * @param param Groovy脚本执行参数
     * @return {@link GroovyScriptEngineExecutorResultVO} 执行结果
     */
    GroovyScriptEngineExecutorResultVO executeScript(RuleGroovyScriptExecuteScriptParam param) throws Exception;

    /**
     * 直接编译并执行Groovy脚本
     *
     * @param param Groovy脚本执行参数
     * @return {@link GroovyScriptEngineExecutorResultVO} 执行结果
     */
    GroovyScriptEngineExecutorResultVO runDirectCompile(RuleGroovyScriptDirectCompileParam param) throws Exception;

    /**
     * 列出某「产品 + 产品发布版本 + 渠道」下启用中的设备上行前置转换脚本(scriptType=topicInboundTransform)。
     * <p>供缓存桶刷新:把命中脚本按 {@code 主题模式(topic 模式) → 脚本内容} 聚成 HASH 桶。
     *
     * @param channelCode           渠道编码(mqtt / webSocket)
     * @param productIdentification 产品标识(产品标识维度)
     * @param productVersionNo      产品发布版本号(objectVersion 维度)
     * @return 启用中的前置转换脚本列表
     */
    List<RuleGroovyScript> listEnabledTransformScripts(String channelCode, String productIdentification, String productVersionNo);
}


