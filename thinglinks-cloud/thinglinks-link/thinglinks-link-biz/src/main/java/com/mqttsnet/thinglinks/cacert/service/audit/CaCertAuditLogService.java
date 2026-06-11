package com.mqttsnet.thinglinks.cacert.service.audit;

import com.mqttsnet.thinglinks.cacert.entity.audit.CaCertAuditLog;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertAuditTypeEnum;

import java.util.List;

/**
 * CA 证书审计日志服务 ── 异步落库 {@link #record} + 按 CA 维度回查 {@link #listByCaId}.
 *
 * @author mqttsnet
 */
public interface CaCertAuditLogService {

    /**
     * 异步记录一条审计日志。
     *
     * @param type            动作类型
     * @param caId            CA ID(可空)
     * @param caSerialNumber  CA 序列号(可空)
     * @param detail          详情自由文本 / JSON
     */
    void record(CaCertAuditTypeEnum type, Long caId, String caSerialNumber, String detail);

    /**
     * 查询指定 CA 的审计日志(按时间倒序,默认上限 200 条,前端时间线展示用).
     *
     * @param caId CA 证书 ID
     */
    List<CaCertAuditLog> listByCaId(Long caId);
}
