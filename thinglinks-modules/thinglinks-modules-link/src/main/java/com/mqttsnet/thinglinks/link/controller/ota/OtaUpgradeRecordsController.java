package com.mqttsnet.thinglinks.link.controller.ota;

import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.save.OtaUpgradeRecordsSaveVO;
import com.mqttsnet.thinglinks.link.api.domain.ota.vo.update.OtaUpgradeRecordsUpdateVO;
import com.mqttsnet.thinglinks.link.api.domain.vo.param.OtaCommandResponseParam;
import com.mqttsnet.thinglinks.link.service.ota.OtaUpgradeRecordsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * <p>
 * 前端控制器
 * OTA升级记录表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-01-12 22:42:04
 * @create [2024-01-12 22:42:04] [mqttsnet]
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/otaUpgradeRecords")
@Api(value = "OtaUpgradeRecords", tags = "OTA升级记录表")
public class OtaUpgradeRecordsController extends BaseController {

    @Resource
    private OtaUpgradeRecordsService otaUpgradeRecordsService;

    @ApiOperation(value = "保存OTA升级记录", httpMethod = "POST", notes = "保存一个新的OTA升级记录")
    @PostMapping("/saveOtaUpgradeRecord")
    public R<OtaUpgradeRecordsSaveVO> saveOtaUpgradeRecord(@Valid @RequestBody OtaUpgradeRecordsSaveVO saveVO) {
        // 从服务中保存OTA升级记录
        OtaUpgradeRecordsSaveVO savedRecord = otaUpgradeRecordsService.saveOtaUpgradeRecord(saveVO);
        // 返回成功响应实体，包含已保存的记录
        return R.ok(savedRecord);
    }

    @ApiOperation(value = "更新OTA升级记录", httpMethod = "PUT", notes = "更新一个现有的OTA升级记录")
    @PutMapping("/updateOtaUpgradeRecord")
    public R<OtaUpgradeRecordsUpdateVO> updateOtaUpgradeRecord(@Valid @RequestBody OtaUpgradeRecordsUpdateVO updateVO) {
        // 从服务中更新OTA升级记录
        OtaUpgradeRecordsUpdateVO updatedRecord = otaUpgradeRecordsService.updateOtaUpgradeRecord(updateVO);
        // 返回成功响应实体，包含已更新的记录
        return R.ok(updatedRecord);
    }

    /**
     * 从MQTT消息接收并保存新的OTA升级记录。此端点
     * 从MQTT消息体捕获命令响应参数并持久化它们。
     *
     * @param otaCommandResponseParam 来自通过MQTT发送的OTA命令的响应参数。
     * @return {@link R< OtaCommandResponseParam >} 包含已保存OTA升级记录的响应实体。
     */
    @ApiOperation(value = "保存OTA升级记录", httpMethod = "POST", notes = "从MQTT消息数据保存一个新的OTA升级记录。")
    @PostMapping("/saveOtaUpgradeRecordByMqtt")
    public R<OtaCommandResponseParam> saveOtaUpgradeRecordByMqtt(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam) {
        try {
            // 调用服务方法保存记录
            OtaCommandResponseParam savedRecord = otaUpgradeRecordsService.saveOtaUpgradeRecordByMqtt(otaCommandResponseParam);
            // 返回包含已保存记录的成功响应实体
            return R.ok(savedRecord);
        } catch (Exception e) {
            // 记录异常并返回错误响应实体
            return R.fail("保存OTA升级记录时出错: " + e.getMessage());
        }
    }

    /**
     * 从HTTP请求接收并保存新的OTA升级记录。此端点
     * 从请求体捕获命令响应参数并持久化它们。
     *
     * @param otaCommandResponseParam 来自通过HTTP发送的OTA命令的响应参数。
     * @return {@link R<OtaCommandResponseParam>} 包含已保存OTA升级记录的响应包装器。
     */
    @ApiOperation(value = "通过HTTP保存OTA升级记录", httpMethod = "POST", notes = "从HTTP请求数据保存一个新的OTA升级记录。")
    @PostMapping("/saveUpgradeRecordByHttp")
    public R<OtaCommandResponseParam> saveUpgradeRecordByHttp(@Valid @RequestBody OtaCommandResponseParam otaCommandResponseParam) {
        try {
            // 调用服务方法保存记录
            OtaCommandResponseParam savedRecord = otaUpgradeRecordsService.saveUpgradeRecordByHttp(otaCommandResponseParam);
            // 返回包含已保存记录的成功响应包装器
            return R.ok(savedRecord);
        } catch (Exception e) {
            // 记录异常并返回失败响应包装器
            return R.fail("通过HTTP保存OTA升级记录时出错: " + e.getMessage());
        }
    }



}


