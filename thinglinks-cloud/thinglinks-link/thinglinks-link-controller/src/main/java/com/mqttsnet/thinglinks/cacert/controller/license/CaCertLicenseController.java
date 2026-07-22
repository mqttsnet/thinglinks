package com.mqttsnet.thinglinks.cacert.controller.license;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;

import cn.hutool.core.io.FileUtil;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.cacert.entity.audit.CaCertAuditLog;
import com.mqttsnet.thinglinks.cacert.entity.license.CaCertLicense;
import com.mqttsnet.thinglinks.cacert.service.audit.CaCertAuditLogService;
import com.mqttsnet.thinglinks.cacert.service.license.CaCertLicenseService;
import com.mqttsnet.thinglinks.cacert.vo.query.license.CaCertLicensePageQuery;
import com.mqttsnet.thinglinks.cacert.vo.result.license.CaCertLicenseImpactResultVO;
import com.mqttsnet.thinglinks.cacert.vo.result.license.CaCertLicenseResultVO;
import com.mqttsnet.thinglinks.cacert.vo.save.license.CaCertLicenseSaveVO;
import com.mqttsnet.thinglinks.cacert.vo.save.license.CaCertPemImportSaveVO;
import com.mqttsnet.thinglinks.cacert.vo.update.license.CaCertLicenseUpdateVO;

import java.util.List;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/**
 * CA 许可证证书 控制器 ── 导入 / 影响面 / 吊销 / 客户端证书签发 + 基础 CRUD.
 *
 * @author mqttsnet
 * @since 2025-06-27
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/caCertLicense")
@Tag(name = "CA许可证证书")
public class CaCertLicenseController extends SuperController<CaCertLicenseService, Long, CaCertLicense
        , CaCertLicenseSaveVO, CaCertLicenseUpdateVO, CaCertLicensePageQuery, CaCertLicenseResultVO> {
    private final EchoService echoService;
    private final CaCertAuditLogService auditLogService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<CaCertLicense> handlerWrapper(CaCertLicense model, PageParams<CaCertLicensePageQuery> params) {
        QueryWrap<CaCertLicense> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("ca_cert_license");
        return queryWrap;
    }

    /**
     * 导入PEM格式CA根证书
     *
     * @param caCertPemImportSaveVO 证书导入请求体
     * @return 证书导入结果
     * @apiNote 证书需为标准的PEM格式
     */
    @Operation(summary = "导入CA根证书(PEM)", description = "CA根证书(不支持证书链)")
    @PostMapping("/importPemCertificate")
    public R<CaCertLicenseResultVO> importPemCertificate(
            @Parameter(description = "证书导入参数", required = true)
            @Valid @RequestBody CaCertPemImportSaveVO caCertPemImportSaveVO) {

        CaCertLicenseResultVO result = superService.importPemCertificate(caCertPemImportSaveVO);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * CA 证书影响面 ── 返回绑定此 CA 的设备总数 / 在线数 / 前 50 条设备简要。
     * 用于运维吊销前评估。
     */
    @Operation(summary = "CA 证书影响面", description = "返回绑定此 CA 的设备总数、在线数与前 50 条设备")
    @GetMapping("/impact/{id}")
    public R<CaCertLicenseImpactResultVO> getImpact(@PathVariable("id") @NotNull Long id) {
        CaCertLicenseImpactResultVO result = superService.getImpact(id);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 吊销 CA 证书。
     * 将证书 state 改为 REVOKED + 发布 {@link com.mqttsnet.thinglinks.cacert.event.CaRevokedEvent} 触发绑定设备 cache 失效。
     *
     * @param id               证书 ID
     * @param revocationReason 吊销原因(可选, 用于审计追溯)
     */
    @Operation(summary = "吊销 CA 证书", description = "吊销证书 + 失效绑定设备 cache")
    @PutMapping("/revoke/{id}")
    @WebLog(value = "吊销 CA 证书", request = false)
    public R<Boolean> revokeCertificate(
            @PathVariable("id") @NotNull Long id,
            @RequestParam(required = false) String revocationReason) {
        return R.success(superService.revokeCertificate(id, revocationReason));
    }

    /**
     * 签发并下载客户端证书 ZIP 包。
     * 由 {@link CaCertLicenseService#generateClientCertPackage} 生成 ZIP 后流式回写, 完成后清理临时文件.
     *
     * @param id       CA 证书 ID
     * @param notAfter 客户端证书过期时间
     */
    @Operation(summary = "签发客户端证书包", description = "根 CA 签发客户端证书并打包 ZIP 下载")
    @PostMapping("/issueClientCert")
    public ResponseEntity<StreamingResponseBody> issueClientCert(
            @RequestParam @NotNull Long id,
            @RequestParam @Future LocalDateTime notAfter) throws Exception {
        File zipFile = superService.generateClientCertPackage(id, notAfter);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=client_cert_" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(outputStream -> {
                    try (InputStream in = Files.newInputStream(zipFile.toPath())) {
                        IOUtils.copy(in, outputStream);
                    } finally {
                        FileUtil.del(zipFile);
                    }
                });
    }

    /**
     * 查询指定 CA 的审计日志(按时间倒序, 上限 200 条, 详情页时间线展示用).
     *
     * @param id CA 证书 ID
     */
    @Operation(summary = "CA 证书审计日志", description = "按 CA 维度返回审计时间线(上限 200 条)")
    @GetMapping("/audit/{id}")
    public R<List<CaCertAuditLog>> listAudit(@PathVariable("id") @NotNull Long id) {
        return R.success(auditLogService.listByCaId(id));
    }
}


