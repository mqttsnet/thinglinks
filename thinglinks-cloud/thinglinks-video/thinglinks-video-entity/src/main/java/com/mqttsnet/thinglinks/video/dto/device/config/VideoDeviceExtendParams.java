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
 * {@code video_device.extend_params} JSON 字符串字段的类型安全视图。
 * <p>
 * <b>为什么不是 POJO 入库？</b> 系统里 10+ 个 entity 共用 {@code extend_params: String}
 * 且带 {@code condition = LIKE} 支持模糊查询，改类型成本过高。所以本类只做"操作视图"：
 * 业务代码通过 {@link #fromJson(String)} / {@link #toJsonString()} 做 String ↔ POJO 转换。
 * <p>
 * <b>字段范围</b>：GB/T 28181-2016 REGISTER / DeviceInfo 等设备级交互中未映射到实体独立
 * 列的 GB 扩展字段。由协议层 handler 写入，前端一般不直接用，主要留给协议排障、设备
 * 对账、级联透传。
 * <p>
 * <b>按需更新</b>：推荐通过 {@code VideoDeviceService.patchExtendParams(deviceId, patch)}
 * 写入，合并逻辑见 {@link #merge(VideoDeviceExtendParams)}。
 *
 * @author mqttsnet
 * @since 2026-04-19
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "设备扩展参数（extend_params 字段）")
public class VideoDeviceExtendParams implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ---------------- DeviceInfo 响应扩展字段 ----------------

    /** 子设备总数（GB/T 28181-2016 §9.1.2 SummaryNum） */
    @Schema(description = "子设备总数")
    private Integer summaryNum;

    // ---------------- Catalog / REGISTER 扩展字段 ----------------

    /** 设备归属 */
    @Schema(description = "设备归属")
    private String owner;

    /** 是否有子设备：0-无 / 1-有 */
    @Schema(description = "是否有子设备（0无/1有）")
    private String parental;

    /** 信令安全模式：0-不采用 / 1-S/MIME / 2-签名 / 3-加密 */
    @Schema(description = "信令安全模式")
    private String safetyWay;

    /** 注册方式：1-口令 / 2-双向数字证书 / 3-数字证书方式 */
    @Schema(description = "注册方式")
    private String registerWay;

    /** 证书序列号 */
    @Schema(description = "证书序列号")
    private String certNum;

    /** 证书有效标识：0-无效 / 1-有效 */
    @Schema(description = "证书有效标识")
    private String certifiable;

    /** 证书无效原因码 */
    @Schema(description = "证书无效原因码")
    private String errCode;

    /** 证书终止有效期 */
    @Schema(description = "证书终止有效期")
    private String endTime;

    // ---------------- 序列化工具 ----------------

    /** 从 JSON 字符串反序列化；入参为 blank 时返回 null，调用方自行决定是否用默认实例。 */
    public static VideoDeviceExtendParams fromJson(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSON.parseObject(json, VideoDeviceExtendParams.class);
    }

    /** 序列化为 JSON 字符串。全字段为 null 时返回 null，避免落库 {@code "{}"} 噪声。 */
    public String toJsonString() {
        String json = JSON.toJSONString(this);
        return "{}".equals(json) ? null : json;
    }

    /**
     * 将 patch 中有效字段合并进当前对象（就地修改）。
     * <p>判空原则：String 字段用 {@link StrUtil#isNotBlank}（跳过 null / 空串 / 纯空白，防表单空值
     * 覆盖有效数据），非 String 字段用 {@code != null}。
     */
    public void merge(VideoDeviceExtendParams patch) {
        if (patch == null) {
            return;
        }
        if (patch.summaryNum != null) {
            this.summaryNum = patch.summaryNum;
        }
        if (StrUtil.isNotBlank(patch.owner)) {
            this.owner = patch.owner;
        }
        if (StrUtil.isNotBlank(patch.parental)) {
            this.parental = patch.parental;
        }
        if (StrUtil.isNotBlank(patch.safetyWay)) {
            this.safetyWay = patch.safetyWay;
        }
        if (StrUtil.isNotBlank(patch.registerWay)) {
            this.registerWay = patch.registerWay;
        }
        if (StrUtil.isNotBlank(patch.certNum)) {
            this.certNum = patch.certNum;
        }
        if (StrUtil.isNotBlank(patch.certifiable)) {
            this.certifiable = patch.certifiable;
        }
        if (StrUtil.isNotBlank(patch.errCode)) {
            this.errCode = patch.errCode;
        }
        if (StrUtil.isNotBlank(patch.endTime)) {
            this.endTime = patch.endTime;
        }
    }
}
