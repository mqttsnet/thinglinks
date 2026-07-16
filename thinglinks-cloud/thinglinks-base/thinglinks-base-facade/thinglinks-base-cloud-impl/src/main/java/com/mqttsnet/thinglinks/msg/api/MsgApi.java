package com.mqttsnet.thinglinks.msg.api;


import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.msg.vo.update.ExtendMsgPublishVO;
import com.mqttsnet.thinglinks.msg.vo.update.ExtendMsgSendVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 文件接口
 *
 * @author zuihou
 * @date 2019/06/21
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.base-server:thinglinks-base-server}")
public interface MsgApi {

    /**
     * 根据模板发送消息
     *
     * @param data 发送内容
     * @return
     */
    @Operation(summary = "根据模板发送消息", description = "根据模板发送消息")
    @PostMapping("/inner/extendMsg/sendByTemplate")
    R<Boolean> sendByTemplate(@RequestBody ExtendMsgSendVO data);

    /**
     * 发布站内信
     *
     * @param data 发送内容
     * @return
     */
    @Operation(summary = "发布站内信", description = "发布站内信")
    @PostMapping("/inner/extendMsg/publish")
    R<Boolean> publish(@RequestBody ExtendMsgPublishVO data);
}
