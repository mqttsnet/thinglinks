package com.mqttsnet.thinglinks.cacert.service.license.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.DateUtils;
import com.mqttsnet.basic.utils.StrPool;
import com.mqttsnet.thinglinks.cacert.dto.SubjectObjectDN;
import com.mqttsnet.thinglinks.cacert.entity.license.CaCertLicense;
import com.mqttsnet.thinglinks.cacert.event.CaRevokedEvent;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertAlgorithmEnum;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertAuditTypeEnum;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertSignAlgorithmEnum;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertStatusEnum;
import com.mqttsnet.thinglinks.cacert.manager.license.CaCertLicenseManager;
import com.mqttsnet.thinglinks.cacert.service.audit.CaCertAuditLogService;
import com.mqttsnet.thinglinks.cacert.service.license.CaCertLicenseService;
import com.mqttsnet.thinglinks.cacert.vo.result.license.CaCertLicenseImpactResultVO;
import com.mqttsnet.thinglinks.cacert.vo.result.license.CaCertLicenseResultVO;
import com.mqttsnet.thinglinks.cacert.vo.save.license.CaCertLicenseSaveVO;
import com.mqttsnet.thinglinks.cacert.vo.save.license.CaCertPemImportSaveVO;
import com.mqttsnet.thinglinks.cacert.vo.update.license.CaCertLicenseUpdateVO;
import com.mqttsnet.thinglinks.common.constant.AppendixType;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.service.DeviceQueryService;
import com.mqttsnet.thinglinks.common.utils.FileUploadUtils;
import com.mqttsnet.thinglinks.common.utils.FreeMarkerUtil;
import com.mqttsnet.thinglinks.file.facade.FileFacade;
import com.mqttsnet.thinglinks.file.vo.result.FileResultVO;
import com.mqttsnet.thinglinks.utils.x509.CertSerialNumberUtil;
import com.mqttsnet.thinglinks.utils.x509.X509Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 业务实现类
 * CA许可证证书表
 * </p>
 *
 * @author mqttsnet
 * @since 2025-06-27 15:48:10
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
public class CaCertLicenseServiceImpl extends SuperServiceImpl<CaCertLicenseManager, Long, CaCertLicense> implements CaCertLicenseService {

    private final FileFacade fileApi;

    private final DeviceQueryService deviceQueryService;

    private final ApplicationEventPublisher eventPublisher;

    private final CaCertAuditLogService auditLogService;


    @Override
    protected <UpdateVO> CaCertLicense updateBefore(UpdateVO vo) {
        CaCertLicenseUpdateVO updateVO = (CaCertLicenseUpdateVO) vo;

        checkUpdateVO(updateVO);


        return super.updateBefore(updateVO);
    }

    private void checkUpdateVO(CaCertLicenseUpdateVO updateVO) {
        CaCertLicense caCertLicense = superManager.getById(updateVO.getId());
        ArgumentAssert.notNull(caCertLicense, "证书不存在!");
    }

    @Override
    protected <SaveVO> CaCertLicense saveBefore(SaveVO vo) {
        CaCertLicenseSaveVO saveVO = (CaCertLicenseSaveVO) vo;

        checkSaveVO(saveVO);

        saveVO.setState(CaCertStatusEnum.PENDING.getValue());
        return super.saveBefore(saveVO);
    }

    private void checkSaveVO(CaCertLicenseSaveVO saveVO) {
        if (superManager.count(Wraps.<CaCertLicense>lbQ()
                .eq(CaCertLicense::getCommonName, saveVO.getCommonName())
                .eq(CaCertLicense::getOrganization, saveVO.getOrganization())
                .eq(CaCertLicense::getOrganizationalUnit, saveVO.getOrganizationalUnit())
                .eq(CaCertLicense::getCountryName, saveVO.getCountryName())
                .eq(CaCertLicense::getProvinceName, saveVO.getProvinceName())
                .eq(CaCertLicense::getLocalityName, saveVO.getLocalityName())
                .eq(CaCertLicense::getState, CaCertStatusEnum.ISSUED.getValue())
        ) > 0) {
            throw BizException.wrap("该主体已存在已颁发的证书!");
        }
    }

    @Override
    protected <SaveVO> void saveAfter(SaveVO saveVO, CaCertLicense entity) {
//        superManager.refreshCache(Collections.singletonList(entity));
    }


    @Override
    protected <UpdateVO> void updateAfter(UpdateVO updateVO, CaCertLicense entity) {
//        superManager.refreshCache(Collections.singletonList(entity));
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public CaCertLicenseResultVO importPemCertificate(CaCertPemImportSaveVO caCertPemImportSaveVO) {
        try {
            // 解析CA证书
            X509Certificate rootCert = X509Util.parseRootCertificate(caCertPemImportSaveVO.getCaCertPem());

            // 构建存储实体
            CaCertLicense entity = buildCertificateEntity(caCertPemImportSaveVO.getCertName(), rootCert, caCertPemImportSaveVO.getRemark());

            // 保存到数据库
            superManager.save(entity);

            // 审计
            auditLogService.record(CaCertAuditTypeEnum.IMPORT, entity.getId(), entity.getSerialNumber(),
                    "name=" + entity.getCertName());

            //返回标准化VO
            return BeanPlusUtil.toBean(entity, CaCertLicenseResultVO.class);
        } catch (Exception e) {
            throw BizException.wrap(e.getMessage());
        }

    }

    /**
     * 构建证书存储实体
     *
     * @param certName 证书名称
     * @param rootCert 根证书
     * @param remark   备注信息
     * @return 构建好的证书实体
     */
    private CaCertLicense buildCertificateEntity(String certName, X509Certificate rootCert, String remark) {
        CaCertLicense entity = new CaCertLicense();
        log.info("证书名称: {}, 品牌: {},version: {}", certName, rootCert.getIssuerX500Principal().getName(), rootCert.getVersion());
        String serialHex = CertSerialNumberUtil.getOpenSSLSerial(rootCert);
        CaCertLicense caCertLicense = superManager.getByCertSerialNumber(serialHex);
        ArgumentAssert.isNull(caCertLicense, "证书已存在，证书序列号: {}", serialHex);
        try {
            PublicKey publicKey = rootCert.getPublicKey();
            // 基础信息
            entity.setCertName(certName);
            entity.setIssuerCommonName(rootCert.getIssuerX500Principal().getName());
            entity.setSerialNumber(serialHex);
            entity.setCaCertPem(X509Util.certificateToPem(rootCert));
            entity.setThumbprint(X509Util.getFingerPrint(rootCert));
            entity.setLicenseBase64(X509Util.toBase64(rootCert));
            entity.setRemark(remark);
            entity.setNotBefore(DateUtils.date2LocalDateTime(rootCert.getNotBefore()));
            entity.setNotAfter(DateUtils.date2LocalDateTime(rootCert.getNotAfter()));
            SubjectObjectDN subjectObjectDN = SubjectObjectDN.parseSubjectDN(rootCert);
            entity.setCommonName(subjectObjectDN.getCommonName());
            entity.setOrganization(subjectObjectDN.getOrganization());
            entity.setOrganizationalUnit(subjectObjectDN.getOrganizationalUnit());
            entity.setCountryName(subjectObjectDN.getCountryName());
            entity.setProvinceName(subjectObjectDN.getProvinceName());
            entity.setLocalityName(subjectObjectDN.getLocalityName());
            entity.setEmail(subjectObjectDN.getEmail());
            entity.setState(CaCertStatusEnum.ISSUED.getValue());

            // 根据算法类型提取密钥参数
            String algorithm = publicKey.getAlgorithm();
            CaCertAlgorithmEnum algorithmEnum = CaCertAlgorithmEnum.fromDesc(algorithm)
                    .orElseThrow(() -> new BizException("不支持的密钥算法: " + algorithm));
            entity.setAlgorithm(algorithmEnum.getValue());
            CaCertSignAlgorithmEnum signAlgorithmEnum = CaCertSignAlgorithmEnum.fromDesc(rootCert.getSigAlgName())
                    .orElseThrow(() -> new BizException("不支持的签名算法: " + rootCert.getSigAlgName()));
            entity.setSignAlgorithm(signAlgorithmEnum.getValue());

        } catch (Exception e) {
            log.error("buildCertificateEntity 证书实体构建失败:{}", e.getMessage(), e);
            throw new BizException("证书实体构建失败: " + e.getMessage());
        }
        return entity;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public CaCertLicenseResultVO issueCertificate(Long id, LocalDateTime notAfter) {
        CaCertLicense caCertLicense = getById(id);
        ArgumentAssert.notNull(caCertLicense, "证书不存在!");
        ArgumentAssert.isTrue(CaCertStatusEnum.PENDING.getValue().equals(caCertLicense.getState()), "证书状态不合法!");

        // 获取算法枚举
        CaCertAlgorithmEnum algorithm = CaCertAlgorithmEnum.fromValue(caCertLicense.getAlgorithm())
                .orElseThrow(() -> new BizException("不支持的证书算法类型"));

        LocalDateTime notBefore = LocalDateTime.now();
        File tempCertFile = null;
        try {
            // 构建CA证书主题信息
            SubjectObjectDN subjectDN = buildSubjectDN(caCertLicense);

            PublicKey publicKey;
            // 自定义RSA公钥
            if (CaCertAlgorithmEnum.RSA.equals(algorithm)) {
                publicKey = X509Util.customRSAPublicKey(caCertLicense.getParam1(), caCertLicense.getParam2());
            } else {
                publicKey = X509Util.customECPublicKey(caCertLicense.getParam1(), caCertLicense.getParam2());
            }

            // 生成证书（使用JcaX509v3CertificateBuilder）
            X509Certificate rootCert = X509Util.generateRootCert(3, subjectDN, publicKey,
                    DateUtils.localDateTime2Date(notBefore),
                    DateUtils.localDateTime2Date(notAfter));

            tempCertFile = FileUploadUtils.createTempFile("root_", ".cer");
            FileUtil.writeBytes(rootCert.getEncoded(), tempCertFile);
            MultipartFile multipartFile = FileUploadUtils.toMultipartFile(tempCertFile);
            FileResultVO uploadResult = fileApi.upload(
                    multipartFile,
                    AppendixType.Link.BASE__CA__CERT__CONTENT,
                    StrPool.EMPTY,
                    null
            );

            ArgumentAssert.notNull(uploadResult, "证书文件上传失败,请重试!");

            // 更新证书信息
            CaCertLicenseUpdateVO caCertLicenseUpdateVO = new CaCertLicenseUpdateVO();
            caCertLicenseUpdateVO.setId(caCertLicense.getId());
            caCertLicenseUpdateVO.setSerialNumber(rootCert.getSerialNumber().toString());
            caCertLicenseUpdateVO.setNotBefore(notBefore);
            caCertLicenseUpdateVO.setNotAfter(notAfter);
            caCertLicenseUpdateVO.setState(CaCertStatusEnum.ISSUED.getValue());
            caCertLicenseUpdateVO.setLicenseBase64(X509Util.toBase64(rootCert));
            caCertLicenseUpdateVO.setCertFileid(uploadResult.getId().toString());
            caCertLicenseUpdateVO.setThumbprint(X509Util.getFingerPrint(rootCert));
            updateById(caCertLicenseUpdateVO);
            return BeanPlusUtil.toBeanIgnoreError(getById(caCertLicense.getId()), CaCertLicenseResultVO.class);
        } catch (Exception e) {
            log.error("证书生成失败", e);
            throw new BizException("证书生成失败: " + e.getMessage());
        } finally {
            // 删除临时文件
            if (tempCertFile != null) {
                FileUtil.del(tempCertFile);
            }
        }
    }


    /**
     * 构建证书主体信息
     * Country (C) → State/Province (ST) → Locality (L) → Organization (O) → Organizational Unit (OU) → Common Name (CN)
     *
     * @param license 证书信息
     * @return {@link SubjectObjectDN} 证书主体信息
     */
    private SubjectObjectDN buildSubjectDN(CaCertLicense license) {
        return SubjectObjectDN.builder()
                .countryName(license.getCountryName())
                .provinceName(license.getProvinceName())
                .localityName(license.getLocalityName())
                .organization(license.getOrganization())
                .organizationalUnit(license.getOrganizationalUnit())
                .commonName(license.getCommonName())
                .build();
    }


    /**
     * 获取许可证URL
     *
     * @param caCertLicense 证书信息
     * @return {@link String} 许可证URL
     */
    private String getLicenseUrl(CaCertLicense caCertLicense) {
        // 获取有效的文件ID列表
        List<Long> fileIdList = Stream.of(
                        Optional.ofNullable(caCertLicense.getAuthorizationCertFileid()).orElse(""),
                        Optional.ofNullable(caCertLicense.getBusinessLicenseFileid()).orElse(""))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Long.valueOf(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (fileIdList.isEmpty()) {
            return "";
        }

        //  获取文件URL
        R<Map<Long, String>> fileUrlMap = fileApi.findUrlFromDefById(fileIdList);
        if (!fileUrlMap.getIsSuccess() || fileUrlMap.getData() == null) {
            log.error("Failed to retrieve file URLs, result is null or empty");
            return "";
        }
        return fileIdList.stream()
                .map(fileUrlMap.getData()::get)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.joining(StrPool.COMMA));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean revokeCertificate(Long id, String revocationReason) {
        CaCertLicense ca = getById(id);
        ArgumentAssert.notNull(ca, "CA 证书不存在: id=" + id);
        if (CaCertStatusEnum.REVOKED.getValue().equals(ca.getState())) {
            log.warn("[CaCert] revoke skipped: ca already revoked id={}", id);
            return true;
        }
        ca.setState(CaCertStatusEnum.REVOKED.getValue());
        ca.setRevokeTime(LocalDateTime.now());
        ca.setRevokeReason(revocationReason);
        boolean ok = superManager.updateById(ca);
        if (!ok) {
            throw new BizException("CA 证书状态更新失败");
        }
        log.info("[CaCert] revoked id={} serialNumber={} reason={}",
                id, ca.getSerialNumber(), revocationReason);
        // 发布事件 → 触发关联设备 cache 失效
        eventPublisher.publishEvent(new CaRevokedEvent(this, id, ca.getSerialNumber(), revocationReason));
        // 审计
        auditLogService.record(CaCertAuditTypeEnum.REVOKE, id, ca.getSerialNumber(),
                "reason=" + revocationReason);
        return true;
    }

    @Override
    public CaCertLicenseResultVO getByCertSerialNumber(String certSerialNumber) {
        CaCertLicense caCertLicense = superManager.getByCertSerialNumber(certSerialNumber);
        return BeanPlusUtil.toBeanIgnoreError(caCertLicense, CaCertLicenseResultVO.class);
    }

    @Override
    public CaCertLicenseImpactResultVO getImpact(Long id) {
        CaCertLicense ca = getById(id);
        if (ca == null) {
            return null;
        }
        String serialNumber = ca.getSerialNumber();

        long bound = deviceQueryService.countByCertSerialNumber(serialNumber);
        long online = deviceQueryService.countOnlineByCertSerialNumber(serialNumber);

        List<Device> top = deviceQueryService.listTopBoundDevicesByCertSerialNumber(serialNumber, 50);

        List<Map<String, Object>> topBrief = top.stream()
                .map(d -> {
                    Map<String, Object> m = new HashMap<>(6);
                    m.put("id", d.getId());
                    m.put("deviceIdentification", d.getDeviceIdentification());
                    m.put("deviceName", d.getDeviceName());
                    m.put("productIdentification", d.getProductIdentification());
                    m.put("connectStatus", d.getConnectStatus());
                    m.put("lastHeartbeatTime", d.getLastHeartbeatTime());
                    return m;
                })
                .collect(Collectors.toList());

        return CaCertLicenseImpactResultVO.builder()
                .caId(id)
                .caSerialNumber(serialNumber)
                .caName(ca.getCertName())
                .boundDeviceCount(bound)
                .onlineDeviceCount(online)
                .topDevices(topBrief)
                .build();
    }

    @Override
    public File generateClientCertPackage(Long id, LocalDateTime notAfter) throws Exception {
        // 验证根CA证书
        CaCertLicense caCertLicense = getById(id);
        ArgumentAssert.notNull(caCertLicense, "根CA证书不存在!");
        ArgumentAssert.isTrue(CaCertStatusEnum.ISSUED.getValue().equals(caCertLicense.getState()),
                "根CA证书状态无效，无法用于签发");

        CaCertAlgorithmEnum algorithm = CaCertAlgorithmEnum.fromValue(caCertLicense.getAlgorithm())
                .orElseThrow(() -> new BizException("不支持的证书算法类型"));

        // 准备临时目录
        Path tempDir = Files.createTempDirectory(caCertLicense.getSerialNumber() + "client_cert_");

        try {
            // 加载根CA证书
            X509Certificate caCert = loadCACertificate(caCertLicense);

            // 生成客户端密钥对（与根CA同算法）
            KeyPair clientKeyPair = X509Util.generateKeyPair(algorithm.getDesc());

            // 密钥对应的公钥
            PublicKey clientPublicKey = clientKeyPair.getPublic();

            // CA证书的DN
            SubjectObjectDN issuerDN = buildSubjectDN(caCertLicense);

            // 构建客户端DN（基于根CA信息生成）
            SubjectObjectDN clientDN = buildSubjectDN(caCertLicense);

            // 签发客户端证书
            X509Certificate clientCert = X509Util.generateUserCert(3,
                    issuerDN.getX500Principal(),
                    clientDN,
                    clientPublicKey,
                    caCert,
                    DateUtils.localDateTime2Date(LocalDateTime.now()),
                    DateUtils.localDateTime2Date(notAfter));

            // 生成文件包
            File zipFile = createCertPackage(tempDir, clientCert, clientKeyPair, caCert);
            // 审计:下载客户端证书包
            auditLogService.record(CaCertAuditTypeEnum.DOWNLOAD_PACK, id, caCertLicense.getSerialNumber(),
                    "notAfter=" + notAfter);
            return zipFile;
        } finally {
            if (tempDir != null) {
                FileUtils.deleteQuietly(tempDir.toFile());
            }
        }
    }

    /**
     * 加载根CA证书
     */
    private X509Certificate loadCACertificate(CaCertLicense caCert) throws Exception {
        byte[] certData = Base64.getDecoder().decode(caCert.getLicenseBase64());
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certData));
    }


    private File createCertPackage(Path tempDir,
                                   X509Certificate clientCert,
                                   KeyPair clientKeyPair,
                                   X509Certificate caCert) throws IOException {
        try {
            // 1. 客户端证书（PEM）
            Files.writeString(tempDir.resolve("client.crt"),
                    X509Util.X509CertificateToPem(clientCert));

            // 2. 客户端私钥（PKCS#8标准PEM）
            Files.writeString(tempDir.resolve("client.key"), "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes())
                            .encodeToString(clientKeyPair.getPrivate().getEncoded()) +
                    "\n-----END PRIVATE KEY-----\n");

            // 3. CA证书链（包含根CA）
            Files.writeString(tempDir.resolve("ca.crt"),
                    X509Util.X509CertificateToPem(caCert));

            //  README文件
            File readmeFile = FileUtil.file(tempDir.toFile(), "README.txt");
            FileUtil.writeUtf8String(buildReadme(clientCert), readmeFile);

            // ZIP打包（改用 Hutool 的 ZipUtil）
            return ZipUtil.zip(tempDir.toString());
        } catch (CertificateException e) {
            throw new IOException("证书格式转换失败", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 使用字符串模板构建README文件内容
     */
    private String buildReadme(X509Certificate clientCert) throws Exception {
        // 提取证书关键信息
        Map<String, Object> params = new HashMap<>();
        params.put("clientDn", clientCert.getSubjectX500Principal().getName());
        params.put("caDn", clientCert.getIssuerX500Principal().getName());
        params.put("notAfter", clientCert.getNotAfter().toString());
        params.put("serialNumber", clientCert.getSerialNumber().toString());
        params.put("fingerprint", X509Util.getFingerPrint(clientCert));
        params.put("algorithm", clientCert.getPublicKey().getAlgorithm());
        params.put("keyLength", X509Util.getKeyLength(clientCert.getPublicKey()));

        // 使用字符串模板直接渲染
        String template = "客户端证书信息：\n" +
                "================================\n" +
                "• 使用者DN: ${clientDn}\n" +
                "• 颁发者DN: ${caDn}\n" +
                "• 有效期至: ${notAfter}\n" +
                "• 序列号: ${serialNumber}\n" +
                "• 指纹(SHA-256): ${fingerprint}\n" +
                "• 密钥算法: ${algorithm}\n" +
                "• 密钥长度: ${keyLength} bits\n" +
                "================================\n" +
                "注意事项：\n" +
                "1. 请妥善保管私钥文件\n" +
                "2. 证书过期前请及时更新\n" +
                "3. 私钥泄露请立即吊销证书";

        return FreeMarkerUtil.generateString(template, params);
    }

}


