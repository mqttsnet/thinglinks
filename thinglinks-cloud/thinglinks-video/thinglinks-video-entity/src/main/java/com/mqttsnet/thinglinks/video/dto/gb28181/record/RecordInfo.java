package com.mqttsnet.thinglinks.video.dto.gb28181.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Description:
 * 设备端录像信息。
 * 从设备查询返回的录像列表信息。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设备端录像信息")
public class RecordInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "设备国标编号")
    private String deviceIdentification;

    @Schema(description = "通道国标编号")
    private String channelIdentification;

    @Schema(description = "命令序列号")
    private String sn;

    @Schema(description = "录像总数")
    private Integer sumNum;

    @Schema(description = "录像列表")
    private List<RecordItem> recordList;

    /**
     * 录像条目
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "录像条目")
    public static class RecordItem implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "设备编号")
        private String deviceIdentification;

        @Schema(description = "录像名称")
        private String name;

        @Schema(description = "文件路径")
        private String filePath;

        @Schema(description = "录像地址（URL）")
        private String address;

        @Schema(description = "开始时间")
        private String startTime;

        @Schema(description = "结束时间")
        private String endTime;

        @Schema(description = "保密属性（0=不涉密，1=涉密）")
        private Integer secrecy;

        @Schema(description = "录像产生类型（time=定时录像，alarm=报警录像，manual=手动录像，all=全部）")
        private String type;

        @Schema(description = "录像触发者")
        private String recorderId;

        @Schema(description = "文件大小（字节）")
        private Long fileSize;
    }
}
