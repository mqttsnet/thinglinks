package com.mqttsnet.thinglinks.file.vo.param;


import com.mqttsnet.thinglinks.file.enumeration.FileStorageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * 附件上传
 *
 * @author mqttsnet
 * @date 2019-05-12 18:49
 */
@Data
@Schema(title = "FileUploadVO", description = "附件上传")
public class FileUploadVO implements Serializable {

    /** 业务类型的安全路径段格式。 */
    public static final String BIZ_TYPE_PATTERN = "[\\p{L}\\p{N}][\\p{L}\\p{N}._-]{0,127}";

    @Schema(description = "业务类型")
    @NotBlank(message = "请填写业务类型")
    @Pattern(regexp = BIZ_TYPE_PATTERN, message = "业务类型格式不正确")
    private String bizType;

    @Schema(description = "桶")
    private String bucket;

    @Schema(description = "存储类型")
    private FileStorageType storageType;
}
