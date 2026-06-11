package com.mqttsnet.thinglinks.cacert.mapper.audit;

import com.mqttsnet.basic.base.mapper.SuperMapper;
import com.mqttsnet.thinglinks.cacert.entity.audit.CaCertAuditLog;
import org.springframework.stereotype.Repository;

/**
 * CA 证书审计日志 Mapper。
 *
 * @author mqttsnet
 */
@Repository
public interface CaCertAuditLogMapper extends SuperMapper<CaCertAuditLog> {
}
