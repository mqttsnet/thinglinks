package com.mqttsnet.thinglinks.alarm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.entity.alarm.RuleAlarmRecord;
import com.mqttsnet.thinglinks.protocol.vo.param.DeviceAlarmNotificationRequestParam;
import com.mqttsnet.thinglinks.service.alarm.RuleAlarmRecordService;
import com.mqttsnet.thinglinks.vo.param.linkage.RuleAlarmRecordHandleParamVO;
import com.mqttsnet.thinglinks.vo.query.alarm.RuleAlarmRecordPageQuery;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmRecordDetailsResultVO;
import com.mqttsnet.thinglinks.vo.result.alarm.RuleAlarmRecordResultVO;
import com.mqttsnet.thinglinks.vo.save.alarm.RuleAlarmRecordSaveVO;
import com.mqttsnet.thinglinks.vo.update.alarm.RuleAlarmRecordUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 告警记录表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-09-09 21:15:22
 * @create [2023-09-09 21:15:22] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/ruleAlarmRecord")
@Tag(name = "告警记录")
public class RuleAlarmRecordController extends SuperController<RuleAlarmRecordService, Long, RuleAlarmRecord, RuleAlarmRecordSaveVO, RuleAlarmRecordUpdateVO,
        RuleAlarmRecordPageQuery, RuleAlarmRecordResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Override
    public QueryWrap<RuleAlarmRecord> handlerWrapper(RuleAlarmRecord model, PageParams<RuleAlarmRecordPageQuery> params) {
        QueryWrap<RuleAlarmRecord> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("rule_alarm_record");
        return queryWrap;
    }

    @Override
    public void handlerResult(IPage<RuleAlarmRecordResultVO> page) {
        superService.fillAlarmRuleDetails(page.getRecords());
        if (echoService != null) {
            echoService.action(page);
        }
    }


    /**
     * 保存告警记录
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "保存告警记录", description = "保存告警记录")
    @PostMapping("/saveAlarmRecord")
    @WebLog(value = "保存告警记录", request = false)
    public R<RuleAlarmRecordSaveVO> saveAlarmRecord(@RequestBody RuleAlarmRecordSaveVO saveVO) {
        return R.success(superService.saveAlarmRecord(saveVO));
    }

    /**
     * 修改告警记录
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改告警记录", description = "修改告警记录")
    @PutMapping("/updateAlarmRecord")
    @WebLog(value = "修改告警记录", request = false)
    public R<RuleAlarmRecordUpdateVO> updateAlarmRecord(@RequestBody RuleAlarmRecordUpdateVO updateVO) {
        return R.success(superService.updateAlarmRecord(updateVO));
    }

    /**
     * 删除告警记录
     *
     * @param id 告警记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除告警记录", description = "根据告警记录ID删除告警记录")
    @Parameters({
            @Parameter(name = "id", description = "告警记录ID", required = true)
    })
    @DeleteMapping("/deleteAlarmRecord/{id}")
    @WebLog(value = "删除告警记录", request = false)
    public R<Boolean> deleteAlarmRecord(@PathVariable("id") Long id) {
        return R.success(superService.deleteAlarmRecord(id));
    }

    /**
     * 获取告警记录详情
     *
     * @param id 告警记录ID
     * @return 告警记录详情
     */
    @Operation(summary = "获取告警记录详情", description = "根据告警记录ID获取告警记录详情")
    @Parameters({
            @Parameter(name = "id", description = "告警记录ID", required = true)
    })
    @GetMapping("/getAlarmRecordDetails/{id}")
    public R<RuleAlarmRecordDetailsResultVO> getAlarmRecordDetails(@PathVariable("id") Long id) {
        RuleAlarmRecordDetailsResultVO result = superService.getAlarmRecordDetails(id);
        echoService.action(result);
        return R.success(result);
    }

    @Operation(summary = "处理或解决告警记录", description = "处理或解决告警记录")
    @PutMapping("/handleOrSolveAlarmRecord")
    @WebLog(value = "处理或解决告警记录", request = false)
    public R<RuleAlarmRecordUpdateVO> handleOrSolveAlarmRecord(@RequestBody RuleAlarmRecordHandleParamVO recordHandleParamVO) {
        return R.success(superService.handleOrSolveAlarmRecord(recordHandleParamVO));
    }


    /**
     * Triggers an alarm notification for a specific device.
     * <p>
     * This method initiates an alarm notification for a device identified by the provided parameters.
     * It requires the device's unique identification, product identification, alarm identification, and content data.
     * Optionally, a list of phone numbers can be provided to receive the alarm notification.
     * </p>
     *
     * @param notificationParam The parameters for the device alarm notification.
     * @return {@link R} containing the result of the alarm notification trigger.
     */
    @Operation(summary = "触发设备告警通知", description = "Triggers an alarm notification for a specific device.")
    @PostMapping("/triggerDeviceAlarmNotification")
    @WebLog(value = "Trigger Device Alarm Notification", request = false)
    public R<String> triggerDeviceAlarmNotification(@RequestBody DeviceAlarmNotificationRequestParam notificationParam) {

        log.info("Trigger Device Alarm Notification - param:{}", JsonUtil.toJson(notificationParam));

        boolean success = superService.triggerDeviceAlarm(notificationParam);

        if (success) {
            return R.success("Alarm notification triggered successfully");
        } else {
            return R.fail("Failed to trigger alarm notification");
        }
    }

}
