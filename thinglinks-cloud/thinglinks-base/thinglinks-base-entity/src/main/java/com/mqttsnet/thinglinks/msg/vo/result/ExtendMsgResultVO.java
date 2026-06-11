package com.mqttsnet.thinglinks.msg.vo.result;

import com.mqttsnet.basic.annotation.echo.Echo;
import com.mqttsnet.thinglinks.model.constant.EchoApi;
import com.mqttsnet.thinglinks.model.constant.EchoDictType;
import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import com.mqttsnet.thinglinks.msg.enumeration.SourceType;
import com.mqttsnet.thinglinks.msg.enumeration.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 表单查询方法返回值VO
 * 消息
 * </p>
 *
 * @author mqttsnet
 * @date 2022-07-10 11:41:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ExtendMsgResultVO", description = "消息")
public class ExtendMsgResultVO extends AuditableResultVO {

    private static final long serialVersionUID = 1L;

    @Schema(description = "短信记录ID")
    private Long id;

    /**
     * 消息模板;
     * #extend_msg_template
     */
    @Schema(description = "消息模板")
    private String templateCode;
    /**
     * 消息类型;
     * [01-短信 02-邮件 03-站内信];
     *
     * @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_TEMPLATE_TYPE)
     */
    @Schema(description = "消息类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.MSG_TEMPLATE_TYPE)
    private String type;
    /**
     * 执行状态;
     * #TaskStatus{DRAFT:草稿;WAITING:等待执行;SUCCESS:执行成功;FAIL:执行失败}
     */
    @Schema(description = "执行状态")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.TaskStatus)
    private TaskStatus status;
    /**
     * 发送渠道;
     * #SourceType{APP:应用;SERVICE:服务}
     */
    @Schema(description = "发送渠道")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.SourceType)
    private SourceType channel;
    /**
     * 参数;
     * <p>
     * 需要封装为[{{‘key’:'', ’value’:''}, {'key2':'', 'value2':''}]格式
     */
    @Schema(description = "参数")
    private String param;
    /**
     * 标题
     */
    @Schema(description = "标题")
    private String title;
    /**
     * 发送内容;
     * <p>
     * 需要封装正确格式化: 您好，张三，您有一个新的快递。
     */
    @Schema(description = "发送内容")
    private String content;
    /**
     * 发送时间
     */
    @Schema(description = "发送时间")
    private LocalDateTime sendTime;
    /**
     * 业务ID
     */
    @Schema(description = "业务ID")
    private Long bizId;
    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 发布人姓名
     */
    @Schema(description = "发布人姓名")
    private String author;

    /**
     * 提醒方式;
     *
     * @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)
     * [01-待办 02-预警 03-提醒]
     */
    @Schema(description = "提醒方式")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.Base.NOTICE_REMIND_MODE)
    private String remindMode;

    @Schema(description = "接收人")
    private List<String> recipientList;
}
