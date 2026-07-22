package com.mqttsnet.thinglinks.cacert.service.audit.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mqttsnet.thinglinks.cacert.entity.audit.CaCertAuditLog;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertAuditTypeEnum;
import com.mqttsnet.thinglinks.cacert.mapper.audit.CaCertAuditLogMapper;
import com.mqttsnet.thinglinks.cacert.service.audit.CaCertAuditLogService;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * CA 证书审计日志服务实现 ── 异步写 + 按 CA 倒序读.
 *
 * @author mqttsnet
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class CaCertAuditLogServiceImpl implements CaCertAuditLogService {

    /** 单次回查上限,避免大量历史拖垮前端 */
    private static final int MAX_LIST_SIZE = 200;

    private final CaCertAuditLogMapper mapper;

    @Async
    @Override
    public void record(CaCertAuditTypeEnum type, Long caId, String caSerialNumber, String detail) {
        try {
            CaCertAuditLog entity = new CaCertAuditLog()
                    .setCaId(caId)
                    .setCaSerialNumber(caSerialNumber)
                    .setType(type.getValue())
                    .setDetail(detail);
            mapper.insert(entity);
        } catch (Exception e) {
            log.warn("[CaCertAudit] save failed type={} caId={}: {}", type, caId, e.getMessage());
        }
    }

    @Override
    public List<CaCertAuditLog> listByCaId(Long caId) {
        if (caId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<CaCertAuditLog> q = new LambdaQueryWrapper<CaCertAuditLog>()
                .eq(CaCertAuditLog::getCaId, caId)
                .orderByDesc(CaCertAuditLog::getCreatedTime)
                .last("limit " + MAX_LIST_SIZE);
        return mapper.selectList(q);
    }
}
