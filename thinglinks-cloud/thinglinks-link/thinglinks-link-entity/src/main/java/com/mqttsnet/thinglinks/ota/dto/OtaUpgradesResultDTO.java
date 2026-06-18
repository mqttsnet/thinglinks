package com.mqttsnet.thinglinks.ota.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.base.entity.Entity;
import com.mqttsnet.basic.interfaces.echo.EchoVO;
import com.mqttsnet.basic.utils.StrPool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * <p>
 * 表单查询方法返回值DTO
 * OTA升级包
 * </p>
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2025/10/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
public class OtaUpgradesResultDTO extends Entity<Long> implements Serializable, EchoVO {

    @Serial
    private static final long serialVersionUID = 1L;
    @Builder.Default
    private Map<String, Object> echoMap = MapUtil.newHashMap();

    private Long id;

    /**
     * 应用ID
     */
    private String appId;
    /**
     * 包名称
     */
    private String packageName;
    /**
     * 升级包类型
     */
    private Integer packageType;
    /**
     * 产品标识
     */
    private String productIdentification;
    /**
     * 升级包版本号
     */
    private String version;
    /**
     * 目标产品版本号(影子版本)
     */
    private String productVersionNo;
    /**
     * 升级包的位置
     */
    private String fileLocation;

    private Integer signMethod;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 升级包功能描述
     */
    private String description;
    /**
     * 自定义信息
     */
    private String customInfo;
    /**
     * 描述
     */
    private String remark;
    /**
     * 创建人组织
     */
    private Long createdOrgId;


    /**
     * 获取文件ID列表
     * 从fileLocation字段解析逗号分隔的文件ID
     *
     * @return 文件ID列表，如果fileLocation为空则返回空列表
     */
    public List<Long> getFileIds() {
        return Optional.ofNullable(this.fileLocation)
                .map(location -> Arrays.stream(location.split(StrPool.COMMA))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Long::valueOf)
                        .distinct()
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }


}
