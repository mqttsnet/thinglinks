package com.mqttsnet.thinglinks.device.vo.result;

import java.io.Serial;
import java.util.List;

import com.mqttsnet.thinglinks.model.vo.AuditableResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "DeviceVersionResultVO", description = "设备软固件版本集合结果VO")
public class DeviceVersionResultVO extends AuditableResultVO {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "软件版本号集合")
    private List<String> swVersionList;

    @Schema(description = "固件版本号集合")
    private List<String> fwVersionList;
}