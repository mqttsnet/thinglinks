package com.mqttsnet.thinglinks.video.vo.result.device;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * 批量操作结果 VO。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-04-08
 */
@Data
@Schema(description = "批量操作结果")
public class VideoDeviceBatchResultVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "成功数")
    private int successCount;

    @Schema(description = "失败数")
    private int failedCount;

    @Schema(description = "成功列表")
    private List<String> successList = new ArrayList<>();

    @Schema(description = "失败详情")
    private List<FailDetail> failedList = new ArrayList<>();

    public void addSuccess(String deviceId) {
        successList.add(deviceId);
        successCount++;
    }

    public void addFailed(String deviceId, String reason) {
        failedList.add(new FailDetail(deviceId, reason));
        failedCount++;
    }

    @Data
    @Schema(description = "失败详情")
    public static class FailDetail implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "设备编号")
        private String deviceId;

        @Schema(description = "失败原因")
        private String reason;

        public FailDetail(String deviceId, String reason) {
            this.deviceId = deviceId;
            this.reason = reason;
        }
    }
}
