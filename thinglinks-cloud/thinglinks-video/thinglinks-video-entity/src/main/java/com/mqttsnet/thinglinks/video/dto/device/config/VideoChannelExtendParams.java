package com.mqttsnet.thinglinks.video.dto.device.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * {@code video_channel.extend_params} JSON 字符串字段的类型安全视图。
 * <p>
 * <b>为什么不是 POJO 入库而是视图模型？</b> 系统里 10+ 个 entity 共用 {@code extend_params: String}
 * 字段（带 {@code condition = LIKE} 支持 SQL 模糊查询），改类型成本过高且会破坏现有查询。
 * 所以本类只做"操作视图"：业务代码读写 String 时通过 {@link #fromJson(String)} 与
 * {@link #toJsonString()} 在两端转换，IDE 能补全字段、编译期捕获拼写错误。
 * <p>
 * <b>字段范围</b>：GB28181 Catalog Item 里未映射到实体独立列、也不在 {@code <Info>} 子元素
 * 的那些字段。由协议层（{@code CatalogChannelParser}）写入，前端一般不直接用，主要留给
 * 协议排障、设备对账、级联透传。
 * <p>
 * <b>命名约定</b>：Java 字段用 camelCase，序列化通过 {@code @JSONField(name = "PascalCase")}
 * 保留 GB 标准字段名，便于人工排查原始报文。
 *
 * @author mqttsnet
 * @since 2026-04-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "通道扩展参数（extend_params 字段）")
public class VideoChannelExtendParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ---------------- GB/T 28181-2016 Catalog Item 扩展字段 ----------------

    /** 设备归属 */
    @Schema(description = "设备归属")
    private String owner;

    /** 是否有子设备，0-无 / 1-有 */
    @Schema(description = "是否有子设备（0无/1有）")
    private String parental;

    /** 父节点 ID（DeviceID） */
    @Schema(description = "父节点 DeviceID")
    private String parentId;

    /** 信令安全模式（0-不采用/1-S/MIME/2-签名/3-加密） */
    @Schema(description = "信令安全模式")
    private String safetyWay;

    /** 注册方式（1-口令/2-双向数字证书/3-数字证书方式） */
    @Schema(description = "注册方式")
    private String registerWay;

    /** 证书序列号 */
    @Schema(description = "证书序列号")
    private String certNum;

    /** 证书有效标识（0-无效/1-有效） */
    @Schema(description = "证书有效标识")
    private String certifiable;

    /** 证书无效原因码 */
    @Schema(description = "证书无效原因码")
    private String errCode;

    /** 证书终止有效期 */
    @Schema(description = "证书终止有效期")
    private String endTime;

    /** 警区 */
    @Schema(description = "警区")
    private String block;

    /** 补光灯类型 */
    @Schema(description = "补光灯类型")
    private String supplyLightType;

    /** 摄像机方向类型 */
    @Schema(description = "摄像机方向类型")
    private String directionType;

    /** 业务分组 ID */
    @Schema(description = "业务分组 ID")
    private String businessGroupId;

    // ---------------- 1-2 新增：GB28181 补齐字段 ----------------

    /** 摄像机位置类型（1-省际检查站/2-关口/3-公路检查站/4-城市检查站…具体含义见 GB/T 28181-2016 附录 B） */
    @Schema(description = "摄像机位置类型")
    private String positionType;

    /** 摄像机安装场所类型（1-出入口/2-人行道/3-车道/4-停车场/5-自助提款机…） */
    @Schema(description = "摄像机安装场所类型")
    private String roomType;

    /** 摄像机用途类型（1-治安/2-交通/3-重点/4-特种/5-卡口…） */
    @Schema(description = "摄像机用途类型")
    private String useType;

    // ---------------- 序列化工具 ----------------

    /**
     * 从 JSON 字符串反序列化；入参为 blank 时返回 null（由调用方决定是否用默认实例）。
     */
    public static VideoChannelExtendParams fromJson(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, VideoChannelExtendParams.class);
    }

    /**
     * 序列化为 JSON 字符串。本类全字段 null 时返回 null，避免落库 {@code "{}"} 噪声。
     */
    public String toJsonString() {
        String json = JSON.toJSONString(this);
        return "{}".equals(json) ? null : json;
    }
}
