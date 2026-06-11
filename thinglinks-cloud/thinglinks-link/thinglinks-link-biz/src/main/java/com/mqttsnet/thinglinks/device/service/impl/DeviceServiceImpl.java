package com.mqttsnet.thinglinks.device.service.impl;

import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.base.service.impl.SuperServiceImpl;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.converter.Builder;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.BeanPlusUtil;
import com.mqttsnet.basic.utils.SnowflakeIdUtil;
import com.mqttsnet.basic.utils.TenantUtil;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertAuditTypeEnum;
import com.mqttsnet.thinglinks.cacert.enumeration.CaCertStatusEnum;
import com.mqttsnet.thinglinks.cacert.service.audit.CaCertAuditLogService;
import com.mqttsnet.thinglinks.cacert.service.license.CaCertLicenseService;
import com.mqttsnet.thinglinks.cacert.vo.result.license.CaCertLicenseResultVO;
import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.device.entity.Device;
import com.mqttsnet.thinglinks.device.entity.DeviceAction;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceAuthModeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceConnectStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceEncryptMethodEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceNodeTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceSslTestStepEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceSslTestStepStatusEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceStatusEnum;
import com.mqttsnet.thinglinks.device.event.publisher.DeviceEventPublisher;
import com.mqttsnet.thinglinks.device.event.source.DeviceDeletedEventSource;
import com.mqttsnet.thinglinks.device.event.source.DeviceInfoUpdatedEventSource;
import com.mqttsnet.thinglinks.device.event.source.DeviceRebindEventSource;
import com.mqttsnet.thinglinks.device.manager.DeviceManager;
import com.mqttsnet.thinglinks.device.service.DeviceActionService;
import com.mqttsnet.thinglinks.device.service.DeviceLocationService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceAuthenticationQuery;
import com.mqttsnet.thinglinks.device.vo.query.DeviceDetailsPageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DeviceLocationPageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DevicePageQuery;
import com.mqttsnet.thinglinks.device.vo.query.DeviceSslTestQuery;
import com.mqttsnet.thinglinks.device.vo.result.DeviceDetailsResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceLocationResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceOverviewResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceSslTestResultVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceSslTestStepVO;
import com.mqttsnet.thinglinks.device.vo.result.DeviceVersionResultVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceActionSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceLocationSaveVO;
import com.mqttsnet.thinglinks.device.vo.save.DeviceSaveVO;
import com.mqttsnet.thinglinks.device.vo.update.DeviceUpdateVO;
import com.mqttsnet.thinglinks.product.service.ProductQueryService;
import com.mqttsnet.thinglinks.product.vo.query.ProductPageQuery;
import com.mqttsnet.thinglinks.product.vo.result.ProductResultVO;
import com.mqttsnet.thinglinks.protocol.enumeration.MqttProtocolTopoStatusEnum;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoAddSubDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeleteSubDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoDeviceDataReportParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoQueryDeviceParam;
import com.mqttsnet.thinglinks.protocol.vo.param.TopoUpdateSubDeviceStatusParam;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceAuthenticationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceInfoResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoAddDeviceResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoDeviceOperationResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.TopoQueryDeviceResultVO;
import com.mqttsnet.thinglinks.utils.cacert.CertificateVerifierUtil;
import com.mqttsnet.thinglinks.utils.x509.CertSerialNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 业务实现类
 * 设备档案信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-03-14 19:39:59
 * @create [2023-03-14 19:39:59] [mqttsnet]
 */
@DS(DsConstant.BASE_TENANT)
@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceServiceImpl extends SuperServiceImpl<DeviceManager, Long, Device> implements DeviceService {

    /**
     * 注入只读 {@link ProductQueryService}(独立 bean,零下游 Service 依赖),切库经过 Service AOP 边界、
     * 类图为 DAG,规避 device↔product 反向依赖循环。
     */
    private final ProductQueryService productQueryService;

    private final DeviceLocationService deviceLocationService;

    private final DeviceActionService deviceActionService;

    private final CaCertLicenseService caCertLicenseService;

    private final CaCertAuditLogService caCertAuditLogService;

    private final DeviceEventPublisher deviceEventPublisher;

    private final LinkCacheDataHelper linkCacheDataHelper;

    @Override
    public IPage<DeviceResultVO> getPage(PageParams<DevicePageQuery> params) {
        IPage<Device> page = superManager.getPage(params);
        return BeanPlusUtil.toBeanPage(page, DeviceResultVO.class);
    }


    @Override
    public Long findDeviceTotal() {
        return superManager.findDeviceTotal();
    }

    @Override
    protected <SaveVO> void saveAfter(SaveVO saveVO, Device entity) {
        // 发布设备信息更新事件
        deviceEventPublisher.publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource.builder()
                .deviceIdentificationList(Collections.singletonList(entity.getDeviceIdentification()))
                .build());
    }

    @Override
    protected <UpdateVO> void updateAfter(UpdateVO updateVO, Device entity) {
        // 发布设备信息更新事件
        deviceEventPublisher.publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource.builder()
                .deviceIdentificationList(Collections.singletonList(entity.getDeviceIdentification()))
                .build());
    }

    /**
     * 客户端认证
     *
     * @param deviceAuthenticationQuery 设备认证查询对象
     * @return {@link DeviceAuthenticationResultVO} 认证结果
     */
    @Override
    public DeviceAuthenticationResultVO authClient(DeviceAuthenticationQuery deviceAuthenticationQuery) {
        log.info("设备认证请求: clientIdentifier={}, authMode={}",
                deviceAuthenticationQuery.getClientIdentifier(), deviceAuthenticationQuery.getAuthMode());

        String clientIdentifier = deviceAuthenticationQuery.getClientIdentifier();
        try {
            // 参数校验
            ArgumentAssert.notBlank(clientIdentifier, "clientIdentifier不能为空");
            ArgumentAssert.notBlank(deviceAuthenticationQuery.getUsername(), "username不能为空");
            ArgumentAssert.notBlank(deviceAuthenticationQuery.getPassword(), "password不能为空");
            ArgumentAssert.notNull(deviceAuthenticationQuery.getAuthMode(), "authMode不能为空");

            // 查询设备缓存
            Optional<DeviceCacheVO> deviceResultVOOptional = linkCacheDataHelper.getDeviceCacheVO(clientIdentifier);
            if (deviceResultVOOptional.isEmpty()) {
                log.warn("设备认证失败: 设备不存在, clientIdentifier={}", clientIdentifier);
                return buildFailureResult("设备不存在");
            }

            DeviceResultVO device = BeanPlusUtil.toBeanIgnoreError(deviceResultVOOptional.get(), DeviceResultVO.class);

            // 校验认证模式
            DeviceAuthModeEnum deviceAuthMode = DeviceAuthModeEnum.fromValue(device.getAuthMode())
                    .orElseThrow(() -> new BizException("无效的设备认证模式: " + device.getAuthMode()));

            if (!deviceAuthMode.getValue().equals(deviceAuthenticationQuery.getAuthMode())) {
                log.warn("设备认证失败: 认证模式不匹配, 期望:{}, 实际:{}",
                        deviceAuthMode.getDesc(), deviceAuthenticationQuery.getAuthMode());
                return buildFailureResult("认证模式不匹配. 期望: " + deviceAuthMode.getDesc());
            }

            // 检查设备状态
            if (DeviceStatusEnum.DISCONNECTION_STATE_COLLECTION.contains(device.getDeviceStatus())) {
                log.warn("设备认证失败: 设备已锁定/未激活, clientIdentifier={}", clientIdentifier);
                return buildFailureResult("设备已锁定或未激活");
            }

            // 用户名密码认证
            if (!CharSequenceUtil.equals(deviceAuthenticationQuery.getUsername(), device.getUserName()) ||
                    !CharSequenceUtil.equals(deviceAuthenticationQuery.getPassword(), device.getPassword())) {
                log.warn("设备认证失败: 用户名或密码无效, clientIdentifier={}", clientIdentifier);
                return buildFailureResult("用户名或密码无效");
            }

            // SSL模式额外验证证书
            if (DeviceAuthModeEnum.SSL_MODE.getValue().equals(deviceAuthenticationQuery.getAuthMode())) {
                return verifySslCertificate(deviceAuthenticationQuery, device);
            }

            // 认证成功
            log.info("设备认证成功: clientIdentifier={}", clientIdentifier);
            return buildSuccessResult(device);
        } catch (BizException e) {
            log.warn("设备认证失败: {}", e.getMessage());
            return buildFailureResult(e.getMessage());
        } catch (Exception e) {
            log.error("设备认证异常: clientIdentifier={}", clientIdentifier, e);
            return buildFailureResult("认证系统异常");
        }
    }

    /**
     * 验证SSL证书
     *
     * @param query  设备认证查询参数
     * @param device 设备信息
     * @return {@link DeviceAuthenticationResultVO} 认证结果
     */
    private DeviceAuthenticationResultVO verifySslCertificate(DeviceAuthenticationQuery query, DeviceResultVO device) {
        if (StrUtil.isBlank(query.getClientCertificate())) {
            log.warn("SSL认证失败: 客户端证书为空, clientIdentifier={}", query.getClientIdentifier());
            return buildFailureResult("SSL模式需要客户端证书");
        }

        Optional<String> caOpt = getCaCertificate(device.getCertSerialNumber());
        if (caOpt.isEmpty()) {
            log.warn("SSL认证失败: 设备未绑定有效 CA 证书, clientIdentifier={} certSerialNumber={}",
                    query.getClientIdentifier(), device.getCertSerialNumber());
            return buildFailureResult("设备未绑定有效的 CA 证书");
        }

        if (!CertificateVerifierUtil.verify(caOpt.get(), query.getClientCertificate())) {
            log.warn("SSL认证失败: 证书校验不通过, clientIdentifier={}", query.getClientIdentifier());
            // 错误信息脱敏 ── 具体原因走日志,不透传给客户端
            return buildFailureResult("无效的SSL证书");
        }
        log.info("SSL证书验证成功: clientIdentifier={}", query.getClientIdentifier());
        return buildSuccessResult(device);
    }

    /**
     * 构建认证成功结果。成功日志由调用方按场景输出,本方法不打 log 避免重复。
     *
     * @param device 已通过认证的设备
     * @return {@link DeviceAuthenticationResultVO} 认证成功结果
     */
    private DeviceAuthenticationResultVO buildSuccessResult(DeviceResultVO device) {
        return DeviceAuthenticationResultVO.builder()
                .certificationResult(true)
                .deviceInfoResult(BeanPlusUtil.toBeanIgnoreError(device, DeviceInfoResultVO.class))
                .tenantId(ContextUtil.getTenantId())
                .build();
    }

    /**
     * 构建认证失败结果
     *
     * @param errorMessage 错误信息
     * @return {@link DeviceAuthenticationResultVO} 认证结果
     */
    private DeviceAuthenticationResultVO buildFailureResult(String errorMessage) {
        return DeviceAuthenticationResultVO.builder()
                .certificationResult(false)
                .errorMessage(errorMessage)
                .tenantId(ContextUtil.getTenantId())
                .build();
    }

    /**
     * 获取设备绑定的 CA 证书 Base64,**仅 {@link CaCertStatusEnum#ISSUED} 状态的 CA 才返回**。
     * 序列号空白 / CA 不存在 / CA 已吊销或未颁发 / Base64 为空 → {@link Optional#empty()}。
     */
    private Optional<String> getCaCertificate(String certSerialNumber) {
        if (StrUtil.isBlank(certSerialNumber)) {
            return Optional.empty();
        }
        return Optional.ofNullable(caCertLicenseService.getByCertSerialNumber(certSerialNumber))
                .filter(vo -> CaCertStatusEnum.ISSUED.getValue().equals(vo.getState()))
                .map(CaCertLicenseResultVO::getLicenseBase64)
                .filter(StrUtil::isNotBlank);
    }

    // ============================== SSL 测试器(端到端 PKI 链路验证)==============================

    @Override
    public DeviceSslTestResultVO sslTest(DeviceSslTestQuery query) {
        long start = System.currentTimeMillis();
        List<DeviceSslTestStepVO> steps = new ArrayList<>(6);
        // 审计上下文:在 FIND_CA 步骤后被填充,供 finish() 写审计日志关联到具体 CA
        SslTestAuditCtx audit = new SslTestAuditCtx();
        audit.clientIdentifier = query.getClientIdentifier();
        audit.caSerialNumber = query.getCaSerialNumber();

        // ① 解析 client 证书
        X509Certificate clientCert;
        try {
            long t0 = System.currentTimeMillis();
            clientCert = CertificateVerifierUtil.decode(query.getClientCertBase64());
            steps.add(buildStep(DeviceSslTestStepEnum.PARSE_CLIENT_CERT,
                    DeviceSslTestStepStatusEnum.PASS, certDetail(clientCert), null,
                    System.currentTimeMillis() - t0));
        } catch (Exception e) {
            steps.add(buildStep(DeviceSslTestStepEnum.PARSE_CLIENT_CERT,
                    DeviceSslTestStepStatusEnum.FAIL, null,
                    "客户端证书解析失败: " + e.getMessage(), 0L));
            return finishWithSkip(steps, start, "客户端证书解析失败,后续步骤跳过", audit);
        }

        // ② 有效期
        long t1 = System.currentTimeMillis();
        try {
            clientCert.checkValidity();
            steps.add(buildStep(DeviceSslTestStepEnum.VALIDITY_CHECK,
                    DeviceSslTestStepStatusEnum.PASS, null, null,
                    System.currentTimeMillis() - t1));
        } catch (Exception e) {
            steps.add(buildStep(DeviceSslTestStepEnum.VALIDITY_CHECK,
                    DeviceSslTestStepStatusEnum.FAIL, null,
                    "证书已过期或尚未生效: " + e.getMessage(),
                    System.currentTimeMillis() - t1));
            return finishWithSkip(steps, start, "证书有效期校验未通过", audit);
        }

        // ③ CA 查找(优先按 caSerialNumber,空则按 clientId 反查设备绑定 CA)
        long t2 = System.currentTimeMillis();
        CaCertLicenseResultVO caVo = resolveCa(query);
        if (caVo == null) {
            steps.add(buildStep(DeviceSslTestStepEnum.FIND_CA,
                    DeviceSslTestStepStatusEnum.FAIL, null,
                    "未找到目标 CA;请检查 CA 序列号或设备绑定关系",
                    System.currentTimeMillis() - t2));
            return finishWithSkip(steps, start, "CA 查找失败", audit);
        }
        // CA 查找成功 → 填审计上下文(后续步骤即使失败也能关联到此 CA 的审计 Tab)
        audit.caId = caVo.getId();
        audit.caSerialNumber = caVo.getSerialNumber();
        audit.caName = caVo.getCertName();
        steps.add(buildStep(DeviceSslTestStepEnum.FIND_CA,
                DeviceSslTestStepStatusEnum.PASS,
                MapUtil.builder(new java.util.LinkedHashMap<String, Object>())
                        .put("caName", caVo.getCertName())
                        .put("caSerialNumber", caVo.getSerialNumber())
                        .put("caState", caVo.getState())
                        .build(),
                null, System.currentTimeMillis() - t2));

        // ④ CA 状态校验
        long t3 = System.currentTimeMillis();
        if (!CaCertStatusEnum.ISSUED.getValue().equals(caVo.getState())) {
            steps.add(buildStep(DeviceSslTestStepEnum.CA_STATE_CHECK,
                    DeviceSslTestStepStatusEnum.FAIL,
                    MapUtil.of("caState", caVo.getState()),
                    "CA 当前状态非「已颁发」,不允许参与认证",
                    System.currentTimeMillis() - t3));
            return finishWithSkip(steps, start, "CA 状态非已颁发", audit);
        }
        steps.add(buildStep(DeviceSslTestStepEnum.CA_STATE_CHECK,
                DeviceSslTestStepStatusEnum.PASS, null, null,
                System.currentTimeMillis() - t3));

        // ⑤ Issuer DN 匹配
        long t4 = System.currentTimeMillis();
        X509Certificate caCert;
        try {
            caCert = CertificateVerifierUtil.decode(caVo.getLicenseBase64());
        } catch (Exception e) {
            steps.add(buildStep(DeviceSslTestStepEnum.ISSUER_MATCH,
                    DeviceSslTestStepStatusEnum.FAIL, null,
                    "CA 证书 Base64 解析失败: " + e.getMessage(),
                    System.currentTimeMillis() - t4));
            return finishWithSkip(steps, start, "CA 证书解析失败", audit);
        }
        if (!clientCert.getIssuerX500Principal().equals(caCert.getSubjectX500Principal())) {
            steps.add(buildStep(DeviceSslTestStepEnum.ISSUER_MATCH,
                    DeviceSslTestStepStatusEnum.FAIL,
                    MapUtil.builder(new java.util.LinkedHashMap<String, Object>())
                            .put("clientIssuer", clientCert.getIssuerX500Principal().toString())
                            .put("caSubject", caCert.getSubjectX500Principal().toString())
                            .build(),
                    "客户端证书的签发者(Issuer)与目标 CA 主题(Subject)不匹配",
                    System.currentTimeMillis() - t4));
            return finishWithSkip(steps, start, "Issuer DN 不匹配 ── 客户端证书非该 CA 签发", audit);
        }
        steps.add(buildStep(DeviceSslTestStepEnum.ISSUER_MATCH,
                DeviceSslTestStepStatusEnum.PASS, null, null,
                System.currentTimeMillis() - t4));

        // ⑥ 密码学签名验证(终极)
        long t5 = System.currentTimeMillis();
        try {
            clientCert.verify(caCert.getPublicKey());
            steps.add(buildStep(DeviceSslTestStepEnum.SIGNATURE_VERIFY,
                    DeviceSslTestStepStatusEnum.PASS, null, null,
                    System.currentTimeMillis() - t5));
        } catch (Exception e) {
            steps.add(buildStep(DeviceSslTestStepEnum.SIGNATURE_VERIFY,
                    DeviceSslTestStepStatusEnum.FAIL, null,
                    "密码学签名校验失败: 客户端证书不是该 CA 签发的",
                    System.currentTimeMillis() - t5));
            return finish(steps, false, start, "签名验证失败 ── 客户端证书不是该 CA 签发的", audit);
        }
        return finish(steps, true, start, "测试通过", audit);
    }

    /** SSL 测试审计上下文 ── 在 FIND_CA 步骤后被填充,供 finish() 写审计日志关联到具体 CA */
    private static class SslTestAuditCtx {
        Long caId;
        String caSerialNumber;
        String caName;
        String clientIdentifier;
    }

    /**
     * 查 CA 证书:优先按 caSerialNumber 直查,为空时回退按 clientId 反查设备绑定的 CA;均未命中返 null。
     *
     * @param query SSL 测试入参(含 caSerialNumber / clientIdentifier 二选一)
     * @return {@link CaCertLicenseResultVO} CA 证书结果;均未命中返回 {@code null}
     */
    private CaCertLicenseResultVO resolveCa(DeviceSslTestQuery query) {
        if (StrUtil.isNotBlank(query.getCaSerialNumber())) {
            return caCertLicenseService.getByCertSerialNumber(query.getCaSerialNumber());
        }
        if (StrUtil.isBlank(query.getClientIdentifier())) {
            return null;
        }
        return linkCacheDataHelper.getDeviceCacheVO(query.getClientIdentifier())
                .map(DeviceCacheVO::getCertSerialNumber)
                .filter(StrUtil::isNotBlank)
                .map(caCertLicenseService::getByCertSerialNumber)
                .orElse(null);
    }

    /**
     * 构造单步 SSL 测试结果。
     *
     * @param step   当前步骤枚举
     * @param status 步骤执行状态
     * @param detail 步骤详情(失败原因 / 证书元信息等)
     * @param reason 步骤摘要文案
     * @param costMs 本步骤耗时毫秒
     * @return {@link DeviceSslTestStepVO} 单步结果 VO
     */
    private DeviceSslTestStepVO buildStep(DeviceSslTestStepEnum step,
                                          DeviceSslTestStepStatusEnum status,
                                          Map<String, Object> detail,
                                          String reason,
                                          long costMs) {
        return DeviceSslTestStepVO.builder()
                .step(step.getValue())
                .name(step.getDesc())
                .status(status.getValue())
                .detail(detail)
                .reason(reason)
                .costMs(costMs)
                .build();
    }

    /**
     * 提取证书核心元信息(subject / issuer / serial / 有效期等)作 step.detail 落地,供审计回放查看。
     *
     * @param cert 当前测试的 X.509 证书
     * @return 证书元信息 map(subject / issuer / serialNumber / notBefore / notAfter / sigAlg / pubKeyAlg)
     */
    private Map<String, Object> certDetail(X509Certificate cert) {
        Map<String, Object> m = MapUtil.newHashMap();
        m.put("subject", cert.getSubjectX500Principal().toString());
        m.put("issuer", cert.getIssuerX500Principal().toString());
        m.put("serialNumber", CertSerialNumberUtil.getOpenSSLSerial(cert));
        m.put("notBefore", cert.getNotBefore());
        m.put("notAfter", cert.getNotAfter());
        m.put("sigAlg", cert.getSigAlgName());
        m.put("pubKeyAlg", cert.getPublicKey().getAlgorithm());
        return m;
    }

    /**
     * 失败提前终止时的兜底收尾:剩余未执行步骤一律补 {@link DeviceSslTestStepStatusEnum#SKIP},
     * 保证返回的 steps 列表与 step 枚举一一对齐。
     *
     * @param steps   已完成的步骤列表
     * @param start   测试起始时间戳(毫秒,用于总耗时统计)
     * @param summary 终止原因摘要
     * @param audit   审计上下文(可空,用于关联 CA + clientId)
     * @return {@link DeviceSslTestResultVO} success=false 的完整结果
     */
    private DeviceSslTestResultVO finishWithSkip(List<DeviceSslTestStepVO> steps, long start,
                                                  String summary, SslTestAuditCtx audit) {
        DeviceSslTestStepEnum[] all = DeviceSslTestStepEnum.values();
        for (int i = steps.size(); i < all.length; i++) {
            steps.add(buildStep(all[i], DeviceSslTestStepStatusEnum.SKIP, null,
                    "前置步骤未通过,跳过", 0L));
        }
        return finish(steps, false, start, summary, audit);
    }

    private DeviceSslTestResultVO finish(List<DeviceSslTestStepVO> steps, boolean success,
                                          long start, String summary, SslTestAuditCtx audit) {
        DeviceSslTestResultVO result = DeviceSslTestResultVO.builder()
                .success(success)
                .steps(steps)
                .summary(summary)
                .totalCostMs(System.currentTimeMillis() - start)
                .build();
        // 审计:SSL 测试动作 ── 关联 CA + clientId,便于详情页 audit Tab 时间线展示
        try {
            String detail = "success=" + success
                    + (audit.clientIdentifier != null ? " clientId=" + audit.clientIdentifier : "")
                    + (audit.caName != null ? " caName=" + audit.caName : "")
                    + " summary=" + summary;
            caCertAuditLogService.record(CaCertAuditTypeEnum.SSL_TEST,
                    audit.caId, audit.caSerialNumber, detail);
        } catch (Exception ignore) {
            // 审计失败静默,不影响测试结果返回
        }
        return result;
    }

    /**
     * 保存设备档案
     *
     * @param saveVO 保存参数
     * @return {@link DeviceSaveVO} 实体
     */
    @Override
    public DeviceSaveVO saveDevice(DeviceSaveVO saveVO) {
        log.info("saveDevice saveVO:{}", saveVO);
        //校验参数(顺带把 ProductResultVO 拿回来用于回填 boundProductVersionNo)
        ProductResultVO productResultVO = checkedDeviceSaveVO(saveVO);

        //构建参数(注册时把 boundProductVersionNo 默认绑定为产品当前版本)
        Device device = builderDeviceSaveVO(saveVO, productResultVO);

        //保存设备位置信息
        if (null != saveVO.getDeviceLocationSaveVO()) {
            saveVO.getDeviceLocationSaveVO().setDeviceIdentification(device.getDeviceIdentification());
            DeviceLocationSaveVO deviceLocationSaveVO = deviceLocationService.saveDeviceLocation(saveVO.getDeviceLocationSaveVO());
            log.info("saveDevice deviceLocationSaveVO:{}", deviceLocationSaveVO);
        }
        //保存设备档案
        superManager.save(device);

        // 发布设备信息更新事件
        deviceEventPublisher.publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource.builder()
                .deviceIdentificationList(Collections.singletonList(device.getDeviceIdentification()))
                .build());

        return BeanPlusUtil.toBeanIgnoreError(device, DeviceSaveVO.class);
    }

    /**
     * 北向API保存设备档案,保存设备并返回完整的设备信息。
     *
     * @param saveVO 保存参数
     * @return {@link DeviceResultVO} 设备结果信息
     */
    @Override
    public DeviceResultVO saveDeviceByNorthbound(DeviceSaveVO saveVO) {
        log.info("saveDeviceByNorthbound saveVO:{}", JSON.toJSONString(saveVO));
        //校验参数(顺带把 ProductResultVO 拿回来用于回填 boundProductVersionNo)
        ProductResultVO productResultVO = checkedDeviceSaveVO(saveVO);
        // 租户一致性校验（必须是当前租户 ContextUtil）
        if (!TenantUtil.validateTenantConsistency(saveVO.getClientId())) {
            throw BizException.wrap("Tenant information does not match. No authority to operate resources.");
        }

        Device device = BeanPlusUtil.copyProperties(saveVO, Device.class);
        device.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        fillBoundProductVersionIfBlank(device, productResultVO);

        //保存设备档案
        superManager.save(device);
        // 发布设备信息更新事件
        deviceEventPublisher.publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource.builder()
                .deviceIdentificationList(Collections.singletonList(device.getDeviceIdentification()))
                .build());

        return findByDeviceIdentification(device.getDeviceIdentification());
    }

    /**
     * 修改设备档案
     *
     * @param updateVO 更新参数
     * @return {@link DeviceUpdateVO} 更新结果
     */
    @Override
    public DeviceUpdateVO updateDevice(DeviceUpdateVO updateVO) {
        log.info("updateDevice updateVO:{}", updateVO);

        //校验参数
        checkedDeviceUpdateVO(updateVO);

        // 从数据库查询设备信息，确保设备存在
        Device existingDevice = superManager.getById(updateVO.getId());
        if (existingDevice == null) {
            throw BizException.wrap("Device not found for ID:{}", updateVO.getId());
        }

        //构建参数
        Device device = buildDeviceWithBuilder(updateVO);

        // 更新或新增设备位置信息
        updateOrInsertDeviceLocation(updateVO, existingDevice);

        //更新
        superManager.updateById(device);

        // 发布设备信息更新事件
        deviceEventPublisher.publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource.builder()
                .deviceIdentificationList(Collections.singletonList(existingDevice.getDeviceIdentification()))
                .build());

        return updateVO;
    }


    /**
     * 使用 Builder 构建设备对象
     *
     * @param updateVO 更新参数
     * @return {@link Device} 设备信息
     */
    private Device buildDeviceWithBuilder(DeviceUpdateVO updateVO) {
        return builderDeviceUpdateVO(updateVO)
                .with(Device::setId, updateVO.getId())
                .build();
    }

    /**
     * 更新或新增设备位置信息
     *
     * @param updateVO       更新参数
     * @param existingDevice 现有设备信息
     */
    private void updateOrInsertDeviceLocation(DeviceUpdateVO updateVO, Device existingDevice) {
        Optional.ofNullable(updateVO.getDeviceLocationUpdateVO()).ifPresent(locationVO -> {
            locationVO.setDeviceIdentification(existingDevice.getDeviceIdentification());
            if (locationVO.getId() == null) {
                deviceLocationService.saveDeviceLocation(
                        BeanPlusUtil.toBeanIgnoreError(locationVO, DeviceLocationSaveVO.class)
                );
            } else {
                deviceLocationService.updateDeviceLocation(locationVO);
            }
        });
    }

    /**
     * 根据设备ID更新设备状态
     *
     * @param id     设备ID
     * @param status 设备状态
     * @return {@link Boolean} 更新结果
     */
    @Override
    public Boolean updateDeviceStatus(Long id, Integer status) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        ArgumentAssert.notNull(status, "status Cannot be null");
        Device device = superManager.findOneById(id);
        if (Objects.isNull(device)) {
            throw BizException.wrap("The device does not exist");
        }
        if (status.equals(device.getDeviceStatus())) {
            throw BizException.wrap("The device status is the same as the current status");
        }
        // 更新设备连接状态
        UpdateWrapper<Device> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(Device::getId, device.getId())
                .set(Device::getDeviceStatus, status);
        return superManager.update(updateWrapper);
    }

    /**
     * 根据设备ID删除设备。删除成功后发布 {@link com.mqttsnet.thinglinks.device.event.DeviceDeletedEvent},
     * 由各下游模块的同步监听器在同一事务内清理残留引用,避免"设备已删但其它表仍持有指针"的孤儿数据。
     *
     * @param id 设备ID
     * @return {@link Boolean} 删除结果
     */
    @Override
    public Boolean deleteDevice(Long id) {
        ArgumentAssert.notNull(id, "id Cannot be null");
        Device device = Optional.ofNullable(superManager.findOneById(id))
                .orElseThrow(() -> BizException.wrap("The device does not exist"));
        Boolean removed = superManager.removeById(id);
        if (removed) {
            // 发布设备删除事件触发下游清理(分组关系等同步监听器同事务执行);设备缓存由监听器 AFTER_COMMIT 失效。
            deviceEventPublisher.publishDeviceDeletedEvent(DeviceDeletedEventSource.builder()
                    .deviceId(device.getId())
                    .deviceIdentification(device.getDeviceIdentification())
                    .clientId(device.getClientId())
                    .contextMap(ContextUtil.getLocalMap())
                    .build());
        }
        return removed;
    }

    /**
     * 批量删除设备(整批事务)。类级 @Transactional 已覆盖,任一条 {@link #deleteDevice(Long)} 抛异常整批回滚。
     * 循环走 this 自调用 ── 不触发新事务(同实例调用不走 Spring 代理),全部 SQL 共享外层事务,正是所需。
     */
    @Override
    public Boolean deleteDevices(List<Long> ids) {
        ArgumentAssert.notEmpty(ids, "ids cannot be empty");
        // 去重 ── 防止上游误传重复 ID 导致 deleteDevice 第二次报"设备不存在"
        ids.stream().distinct().forEach(this::deleteDevice);
        return Boolean.TRUE;
    }

    @Override
    public DeviceResultVO findOneByClientId(String clientId) {
        ArgumentAssert.notBlank(clientId, "clientId Cannot be null");
        Device device = superManager.findOneByClientId(clientId);
        return BeanUtil.copyProperties(device, DeviceResultVO.class);
    }

    /**
     * 根据设备标识查询设备信息(返 {@link DeviceDetailsResultVO})
     *
     * @param deviceIdentification 设备标识
     * @return {@link DeviceDetailsResultVO} 设备信息
     */
    @Override
    public DeviceDetailsResultVO findOneByDeviceIdentification(String deviceIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification Cannot be null");
        Device device = superManager.findOneByDeviceIdentification(deviceIdentification);
        if (device == null) {
            throw BizException.wrap("Device not exist");
        }

        // 将Device转换为DeviceDetailsResultVO
        DeviceDetailsResultVO deviceDetailsResultVO =
                BeanPlusUtil.toBeanIgnoreError(device, DeviceDetailsResultVO.class);

        // 查询产品信息，如果存在则添加到结果中
        Optional.ofNullable(device.getProductIdentification())
                .flatMap(this::queryProductInfo)
                .ifPresent(deviceDetailsResultVO::setProductResultVO);

        // 查询子设备，如果是网关则查询子设备列表，否则设置为空列表
        Optional.ofNullable(device.getNodeType())
                .filter(DeviceNodeTypeEnum.GATEWAY.getValue()::equals)
                .ifPresentOrElse(
                        type -> deviceDetailsResultVO.setSubDeviceResultVOList(
                                querySubDevices(device.getDeviceIdentification())),
                        () -> deviceDetailsResultVO.setSubDeviceResultVOList(Collections.emptyList())
                );

        // 查询设备位置信息
        Optional.ofNullable(device.getDeviceIdentification())
                .flatMap(this::queryDeviceLocation)
                .ifPresent(deviceDetailsResultVO::setDeviceLocationResultVO);

        return deviceDetailsResultVO;
    }

    @Override
    public DeviceResultVO findByDeviceIdentification(String deviceIdentification) {
        ArgumentAssert.notBlank(deviceIdentification, "deviceIdentification Cannot be null");
        Device device = superManager.findOneByDeviceIdentification(deviceIdentification);
        if (Objects.isNull(device)) {
            throw BizException.wrap("Device not exist");
        }
        return BeanPlusUtil.toBean(device, DeviceResultVO.class);
    }


    @Override
    public boolean updateDeviceConnectionStatusById(Long id, Integer connectionStatus) throws IllegalArgumentException {
        // 校验设备ID
        ArgumentAssert.notNull(id, "id cannot be null");

        // 校验连接状态值
        Optional<DeviceConnectStatusEnum> connectStatusEnumOptional = DeviceConnectStatusEnum.fromValue(connectionStatus);
        if (connectStatusEnumOptional.isEmpty()) {
            throw BizException.wrap("Invalid connection status value");
        }

        // 更新设备连接状态
        updateDeviceConnectionStatus(id, connectStatusEnumOptional.get());

        // 查询最新设备信息
        Device device = superManager.findOneById(id);
        if (Objects.isNull(device)) {
            throw BizException.wrap("The device does not exist");
        }

        // 获取设备类型
        Optional<DeviceNodeTypeEnum> deviceNodeTypeEnumOptional = DeviceNodeTypeEnum.fromValue(device.getNodeType());

        // 检查设备是否为网关
        if (deviceNodeTypeEnumOptional.isPresent() && DeviceNodeTypeEnum.GATEWAY.equals(deviceNodeTypeEnumOptional.get())) {
            // 如果设备为网关且设备状态为离线，则更新子设备状态
            if (DeviceConnectStatusEnum.OFFLINE.getValue().equals(connectStatusEnumOptional.get().getValue())) {
                // gatewayId 列是 String,直接传 deviceIdentification;不要 Long.valueOf
                // (含 "_" 的标识会抛 NumberFormatException 导致联动失效)
                updateSubDevicesConnectionStatus(device.getDeviceIdentification(), connectStatusEnumOptional.get().getValue());
            }
        }
        return true;
    }

    @Override
    public boolean updateDeviceConnectionStatusByEvent(String clientId, Integer status, Long eventHlc) {
        ArgumentAssert.notBlank(clientId, "clientId cannot be blank");
        ArgumentAssert.notNull(status, "status cannot be null");
        ArgumentAssert.isTrue(eventHlc != null && eventHlc > 0, "eventHlc must be > 0");

        // CAS 单调写:仅当 DB 内 last_status_event_hlc 严格小于新事件 hlc 时才覆盖
        // (字段 NOT NULL DEFAULT 0,存量行 0 < 任何合法 hlc → 首次事件总能写入)
        UpdateWrapper<Device> wrapper = new UpdateWrapper<>();
        wrapper.lambda()
                .eq(Device::getClientId, clientId)
                .lt(Device::getLastStatusEventHlc, eventHlc)
                .set(Device::getConnectStatus, status)
                .set(Device::getLastStatusEventHlc, eventHlc);
        boolean affected = superManager.update(wrapper);
        if (!affected) {
            // CAS 拒绝 ── 老事件迟到,DB 已有更新 hlc;info 级别便于运维核查抖动 / 乱序场景
            log.info("[Device.updateByEvent] CAS rejected (stale event) clientId={} hlc={} status={}",
                    clientId, eventHlc, status);
            return false;
        }
        // 成功路径也打一条日志 ── 跟 mqs `[bus.lifecycle]` 对应,便于排查"DB 是否真写入"
        log.info("[Device.updateByEvent] applied clientId={} status={} hlc={}",
                clientId, status, eventHlc);

        // 网关设备 OFFLINE → 子设备联动 OFFLINE(与原 updateDeviceConnectionStatusById 行为一致)
        if (DeviceConnectStatusEnum.OFFLINE.getValue().equals(status)) {
            Device device = superManager.lambdaQuery()
                    .eq(Device::getClientId, clientId)
                    .one();
            if (device != null) {
                DeviceNodeTypeEnum.fromValue(device.getNodeType())
                        .filter(DeviceNodeTypeEnum.GATEWAY::equals)
                        // gatewayId 列是 String,直接传 deviceIdentification;不要 Long.valueOf
                        .ifPresent(nt -> updateSubDevicesConnectionStatus(
                                device.getDeviceIdentification(), status));
            }
        }
        return true;
    }

    /**
     * 更新单个设备的连接状态
     *
     * @param deviceId          设备ID
     * @param connectStatusEnum 连接状态
     */
    private void updateDeviceConnectionStatus(Long deviceId, DeviceConnectStatusEnum connectStatusEnum) {
        UpdateWrapper<Device> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(Device::getId, deviceId)
                .set(Device::getConnectStatus, connectStatusEnum.getValue());

        superManager.update(updateWrapper);
    }

    /**
     * 把指定网关下所有"已激活"子设备的连接状态批量更新为目标值。
     *
     * <p>gatewayId 列是 varchar,存的就是父网关 deviceIdentification(可含下划线),故直接接 String,
     * 不要强转 Long:含非数字字符会抛 {@link NumberFormatException} 导致联动失效。</p>
     *
     * @param gatewayDeviceIdentification 父网关的 deviceIdentification(等于子设备 device.gatewayId 列存的值)
     * @param connectStatus               目标连接状态
     */
    private void updateSubDevicesConnectionStatus(String gatewayDeviceIdentification, Integer connectStatus) {
        UpdateWrapper<Device> updateSubDeviceWrapper = new UpdateWrapper<>();
        updateSubDeviceWrapper.lambda()
                .eq(Device::getGatewayId, gatewayDeviceIdentification)
                .eq(Device::getDeviceStatus, DeviceStatusEnum.ACTIVATED.getValue())
                .set(Device::getConnectStatus, connectStatus);

        superManager.update(updateSubDeviceWrapper);
    }


    /**
     * 查询设备信息VO列表
     *
     * @param query 查询参数
     * @return {@link List<DeviceResultVO>} 设备信息VO列表
     */
    @Override
    public List<DeviceResultVO> getDeviceResultVOList(DevicePageQuery query) {
        List<Device> deviceList = superManager.getDevicList(query);
        return BeanPlusUtil.toBeanList(deviceList, DeviceResultVO.class);
    }

    /**
     * 查询设备信息VO详情列表
     *
     * @param query 查询参数
     * @return {@link List<DeviceDetailsResultVO>} 设备信息VO列表
     */
    @Override
    public List<DeviceDetailsResultVO> getDeviceDetailsResultVOList(DevicePageQuery query) {
        // 获取设备列表
        List<Device> deviceList = superManager.getDevicList(query);

        List<DeviceDetailsResultVO> deviceResultVOS = Optional.ofNullable(deviceList)
                .filter(CollUtil::isNotEmpty)
                .map(list -> BeanPlusUtil.toBeanList(list, DeviceDetailsResultVO.class))
                .orElseGet(Collections::emptyList);

        if (deviceResultVOS.isEmpty()) {
            return Collections.emptyList();
        }

        // 提取设备标识符列表（过滤 null 值）
        List<String> deviceIdentificationList = deviceResultVOS.stream()
                .map(DeviceDetailsResultVO::getDeviceIdentification)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 根据设备标识符列表查询设备位置信息
        Map<String, DeviceLocationResultVO> deviceLocationResultVOMap = queryDeviceLocationForDevices(deviceIdentificationList);

        // 将设备位置信息封装到设备信息中
        deviceResultVOS.forEach(deviceResultVO ->
                Optional.ofNullable(deviceResultVO.getDeviceIdentification())
                        .map(deviceLocationResultVOMap::get)
                        .ifPresent(deviceResultVO::setDeviceLocationResultVO)
        );

        return deviceResultVOS;
    }


    @Override
    public DeviceOverviewResultVO getDeviceOverview() {
        PageParams<DevicePageQuery> params = new PageParams<>();
        params.setModel(new DevicePageQuery());
        DeviceOverviewResultVO resultVO = BeanPlusUtil.toBeanIgnoreError(
                superManager.selectDeviceOverview(params), DeviceOverviewResultVO.class);
        // 增长指标:今日 / 近7天 / 近30天 新增设备数(按 created_time,3 次轻量 count)
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        resultVO.setTodayNewCount(countCreatedSince(todayStart));
        resultVO.setWeekNewCount(countCreatedSince(todayStart.minusDays(6)));
        resultVO.setMonthNewCount(countCreatedSince(todayStart.minusDays(29)));
        return resultVO;
    }

    /**
     * 统计 created_time &gt;= since 的设备数量(逻辑删除由 MyBatis-Plus 自动过滤)。
     */
    private Integer countCreatedSince(LocalDateTime since) {
        return Math.toIntExact(superManager.count(
                Wrappers.<Device>lambdaQuery().ge(Device::getCreatedTime, since)));
    }

    @Override
    public DeviceVersionResultVO getDeviceVersionByProduct(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            return DeviceVersionResultVO.builder()
                    .swVersionList(Collections.emptyList())
                    .fwVersionList(Collections.emptyList())
                    .build();
        }
        return superManager.selectDeviceVersionsByProduct(productIdentification)
                .map(deviceVersionDTO -> {
                    List<String> swVersionList = StrUtil.isNotBlank(deviceVersionDTO.getSwVersions())
                            ? Arrays.asList(deviceVersionDTO.getSwVersions().split(StrUtil.COMMA))
                            : Collections.emptyList();
                    List<String> fwVersionList = StrUtil.isNotBlank(deviceVersionDTO.getFwVersions())
                            ? Arrays.asList(deviceVersionDTO.getFwVersions().split(StrUtil.COMMA))
                            : Collections.emptyList();
                    return DeviceVersionResultVO.builder()
                            .swVersionList(swVersionList)
                            .fwVersionList(fwVersionList)
                            .build();
                })
                .orElse(DeviceVersionResultVO.builder()
                        .swVersionList(Collections.emptyList())
                        .fwVersionList(Collections.emptyList())
                        .build());
    }


    /**
     * MQTT协议下添加子设备
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 添加结果
     */
    @Override
    public TopoAddDeviceResultVO saveSubDeviceByMqtt(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return saveSubDevice(topoAddSubDeviceParam);
    }

    /**
     * 北向API添加子设备
     *
     * @param topoAddSubDeviceParam 子设备参数
     * @return {@link TopoAddDeviceResultVO} 添加结果
     */
    @Override
    public TopoAddDeviceResultVO saveSubDeviceByNorthbound(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        return saveSubDevice(topoAddSubDeviceParam);
    }

    /**
     * MQTT协议下更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新参数
     * @return {@link TopoDeviceOperationResultVO} 更新结果
     */
    @Override
    public TopoDeviceOperationResultVO updateSubDeviceConnectStatusByMqtt(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return updateSubDeviceConnectStatus(topoUpdateSubDeviceStatusParam);
    }

    /**
     * 北向API更新子设备连接状态
     *
     * @param topoUpdateSubDeviceStatusParam 更新参数
     * @return {@link TopoDeviceOperationResultVO} 更新结果
     */
    @Override
    public TopoDeviceOperationResultVO updateSubDeviceConnectStatusByNorthbound(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        return updateSubDeviceConnectStatus(topoUpdateSubDeviceStatusParam);
    }

    /** MQTT协议下删除子设备 */
    @Override
    public TopoDeviceOperationResultVO deleteSubDeviceByMqtt(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deleteSubDevice(topoDeleteSubDeviceParam);
    }

    /** 北向API删除子设备 */
    @Override
    public TopoDeviceOperationResultVO deleteSubDeviceByNorthbound(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        return deleteSubDevice(topoDeleteSubDeviceParam);
    }

    /** MQTT协议下上报设备数据 */
    @Override
    public TopoDeviceOperationResultVO deviceDataReportByMqtt(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return deviceDataReport(topoDeviceDataReportParam);
    }

    /** 北向API上报设备数据 */
    @Override
    public TopoDeviceOperationResultVO deviceDataReportByNorthbound(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        return deviceDataReport(topoDeviceDataReportParam);
    }

    /** 根据设备ID查询设备详情 */
    @Override
    public DeviceDetailsResultVO getDeviceDetails(Long id) {
        if (id == null) {
            throw BizException.wrap("Device ID cannot be null");
        }

        Device device = superManager.findOneById(id);
        if (Objects.isNull(device)) {
            throw BizException.wrap("The device does not exist");
        }

        // 将Device转换为DeviceDetailsResultVO
        DeviceDetailsResultVO deviceDetailsResultVO =
                BeanPlusUtil.toBeanIgnoreError(device, DeviceDetailsResultVO.class);

        // 查询产品信息，如果存在则添加到结果中
        Optional.ofNullable(device.getProductIdentification())
                .flatMap(this::queryProductInfo)
                .ifPresent(deviceDetailsResultVO::setProductResultVO);

        // 查询子设备，如果是网关则查询子设备列表，否则设置为空列表
        Optional.ofNullable(device.getNodeType())
                .filter(DeviceNodeTypeEnum.GATEWAY.getValue()::equals)
                .ifPresentOrElse(
                        type -> deviceDetailsResultVO.setSubDeviceResultVOList(
                                querySubDevices(device.getDeviceIdentification())),
                        () -> deviceDetailsResultVO.setSubDeviceResultVOList(Collections.emptyList())
                );

        // 查询设备位置信息
        Optional.ofNullable(device.getDeviceIdentification())
                .flatMap(this::queryDeviceLocation)
                .ifPresent(deviceDetailsResultVO::setDeviceLocationResultVO);

        return deviceDetailsResultVO;
    }

    /** 获取设备详情分页信息 */
    @Override
    public IPage<DeviceDetailsResultVO> getDeviceDetailsPage(PageParams<DeviceDetailsPageQuery> params) {
        // 获取设备分页信息
        IPage<Device> deviceIPage = superManager.getDeviceDetailsPage(params);

        // 将 Device 转换为 DeviceDetailsResultVO 列表（防空处理）
        List<DeviceDetailsResultVO> deviceDetailsResultVOS = Optional.ofNullable(deviceIPage.getRecords())
                .filter(CollUtil::isNotEmpty)
                .map(records -> BeanPlusUtil.toBeanList(records, DeviceDetailsResultVO.class))
                .orElseGet(Collections::emptyList);

        if (deviceDetailsResultVOS.isEmpty()) {
            return new Page<>(deviceIPage.getCurrent(), deviceIPage.getSize(), 0);
        }

        // 提取设备标识符列表（过滤 null 值）
        List<String> deviceIdentificationList = deviceDetailsResultVOS.stream()
                .map(DeviceDetailsResultVO::getDeviceIdentification)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 提取产品标识列表（过滤 null 值）
        List<String> productIdentificationList = deviceDetailsResultVOS.stream()
                .map(DeviceDetailsResultVO::getProductIdentification)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 查询产品信息和设备位置信息
        Map<String, ProductResultVO> productResultVOMap = queryProductInfoForDevices(productIdentificationList);
        Map<String, DeviceLocationResultVO> deviceLocationResultVOMap = queryDeviceLocationForDevices(deviceIdentificationList);

        // 批量查询所有网关设备的子设备（1次查询代替N次，解决N+1问题）
        Map<String, List<DeviceResultVO>> subDeviceMap = querySubDevicesForGateways(deviceDetailsResultVOS);

        // 处理每个设备的子设备、产品信息和位置信息（不使用 parallelStream，避免 @DS 数据源上下文丢失）
        deviceDetailsResultVOS.forEach(device ->
                processDeviceSubDetails(device, productResultVOMap, deviceLocationResultVOMap, subDeviceMap)
        );

        // 复用分页信息，避免重复 bean 转换
        Page<DeviceDetailsResultVO> resultPage = new Page<>(deviceIPage.getCurrent(), deviceIPage.getSize(), deviceIPage.getTotal());
        resultPage.setRecords(deviceDetailsResultVOS);
        return resultPage;
    }

    /**
     * 检查是否有设备正在使用该产品(产品删除 / 修改前的占用校验)。
     *
     * @throws IllegalArgumentException if the productIdentification is null or empty.
     */
    @Override
    public boolean isProductInUseByDevices(String productIdentification) {
        if (StrUtil.isBlank(productIdentification)) {
            throw BizException.wrap("Product identification cannot be null or empty.");
        }

        PageParams<DeviceDetailsPageQuery> params = new PageParams<>();
        params.setModel(new DeviceDetailsPageQuery().setProductIdentification(productIdentification));
        IPage<Device> deviceIPage = superManager.getDeviceDetailsPage(params);
        return deviceIPage != null && !deviceIPage.getRecords().isEmpty();
    }

    /** MQTT协议下查询设备信息 */
    @Override
    public TopoQueryDeviceResultVO queryDeviceByMqtt(TopoQueryDeviceParam topoQueryDeviceParam) {
        return queryDeviceInfo(topoQueryDeviceParam);
    }

    /** 北向API查询设备信息 */
    @Override
    public TopoQueryDeviceResultVO queryDeviceByNorthbound(TopoQueryDeviceParam topoQueryDeviceParam) {
        return queryDeviceInfo(topoQueryDeviceParam);
    }


    /** 根据参数查询设备信息 */
    private TopoQueryDeviceResultVO queryDeviceInfo(TopoQueryDeviceParam topoQueryDeviceParam) {
        TopoQueryDeviceResultVO topoQueryDeviceResultVO = new TopoQueryDeviceResultVO();

        List<String> deviceIds = Optional.ofNullable(topoQueryDeviceParam)
                .map(TopoQueryDeviceParam::getDeviceIds)
                .orElseGet(Collections::emptyList);

        // 批量查询所有设备，避免 N+1
        List<String> distinctDeviceIds = deviceIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Device> deviceMap = Optional.of(distinctDeviceIds)
                .filter(CollUtil::isNotEmpty)
                .map(ids -> {
                    DevicePageQuery query = new DevicePageQuery();
                    query.setDeviceIdentificationList(ids);
                    List<Device> devices = superManager.getDevicList(query);
                    return Optional.ofNullable(devices).orElseGet(Collections::emptyList);
                })
                .map(devices -> devices.stream()
                        .filter(d -> d != null && d.getDeviceIdentification() != null)
                        .collect(Collectors.toMap(Device::getDeviceIdentification, Function.identity(), (a, b) -> a)))
                .orElseGet(Collections::emptyMap);

        List<TopoQueryDeviceResultVO.DataItem> deviceInfoList = distinctDeviceIds.stream()
                .map(deviceIdentification -> {
                    TopoQueryDeviceResultVO.DataItem dataItem = new TopoQueryDeviceResultVO.DataItem();
                    try {
                        dataItem.setDeviceId(deviceIdentification);
                        Optional<Device> optionalDevice = Optional.ofNullable(deviceMap.get(deviceIdentification));
                        TopoQueryDeviceResultVO.DataItem.DeviceInfo deviceInfo = optionalDevice
                                .map(device -> BeanUtil.toBean(device, TopoQueryDeviceResultVO.DataItem.DeviceInfo.class))
                                .orElse(new TopoQueryDeviceResultVO.DataItem.DeviceInfo());

                        dataItem.setDeviceInfo(deviceInfo)
                                .setStatusCode(optionalDevice.isPresent() ? MqttProtocolTopoStatusEnum.SUCCESS.getValue() : MqttProtocolTopoStatusEnum.FAILURE.getValue())
                                .setStatusDesc(optionalDevice.isPresent() ? MqttProtocolTopoStatusEnum.SUCCESS.getDesc() : "Device not found");
                    } catch (Exception e) {
                        dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                                .setStatusDesc("Error querying device: " + e.getMessage());
                    }
                    return dataItem;
                })
                .collect(Collectors.toList());

        topoQueryDeviceResultVO.setData(deviceInfoList)
                .setStatusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                .setStatusDesc("Query completed");
        return topoQueryDeviceResultVO;
    }


    private void processDeviceSubDetails(DeviceDetailsResultVO device,
                                         Map<String, ProductResultVO> productResultVOMap,
                                         Map<String, DeviceLocationResultVO> deviceLocationResultVOMap,
                                         Map<String, List<DeviceResultVO>> subDeviceMap) {
        Optional.ofNullable(device)
                .ifPresent(d -> {
                    Optional.ofNullable(d.getProductIdentification())
                            .map(productResultVOMap::get)
                            .ifPresent(d::setProductResultVO);

                    Optional.ofNullable(d.getDeviceIdentification())
                            .map(deviceLocationResultVOMap::get)
                            .ifPresent(d::setDeviceLocationResultVO);

                    Optional.ofNullable(d.getNodeType())
                            .filter(DeviceNodeTypeEnum.GATEWAY.getValue()::equals)
                            .ifPresent(type ->
                                    d.setSubDeviceResultVOList(
                                            Optional.ofNullable(d.getDeviceIdentification())
                                                    .map(subDeviceMap::get)
                                                    .orElseGet(Collections::emptyList)
                                    )
                            );
                });
    }

    private Map<String, ProductResultVO> queryProductInfoForDevices(List<String> productIdentificationList) {
        return Optional.ofNullable(productIdentificationList)
                .filter(CollUtil::isNotEmpty)
                .map(productIdentification -> {
                    ProductPageQuery query = new ProductPageQuery().setProductIdentificationList(productIdentification);
                    return productQueryService.getProductResultVOList(query);
                })
                .map(list -> list.stream()
                        .filter(p -> p != null && p.getProductIdentification() != null)
                        .collect(Collectors.toMap(
                                ProductResultVO::getProductIdentification,
                                Function.identity(),
                                (a, b) -> {
                                    if (a.getCreatedTime() == null) return b;
                                    if (b.getCreatedTime() == null) return a;
                                    return a.getCreatedTime().isAfter(b.getCreatedTime()) ? a : b;
                                }
                        ))
                )
                .orElseGet(Collections::emptyMap);
    }

    private Map<String, DeviceLocationResultVO> queryDeviceLocationForDevices(List<String> deviceIdentificationList) {
        return Optional.ofNullable(deviceIdentificationList)
                .filter(CollUtil::isNotEmpty)
                .map(deviceIdentification -> deviceLocationService.getDeviceLocationResultVOList(
                        new DeviceLocationPageQuery().setDeviceIdentificationList(deviceIdentification)
                ))
                .map(list -> list.stream()
                        .filter(d -> d != null && d.getDeviceIdentification() != null)
                        .collect(Collectors.toMap(
                                DeviceLocationResultVO::getDeviceIdentification,
                                Function.identity(),
                                (a, b) -> {
                                    if (a.getCreatedTime() == null) return b;
                                    if (b.getCreatedTime() == null) return a;
                                    return a.getCreatedTime().isAfter(b.getCreatedTime()) ? a : b;
                                }
                        ))
                )
                .orElseGet(Collections::emptyMap);
    }


    /**
     * 批量查询所有网关设备的子设备（解决N+1查询问题）
     */
    private Map<String, List<DeviceResultVO>> querySubDevicesForGateways(List<DeviceDetailsResultVO> deviceList) {
        return Optional.ofNullable(deviceList)
                .map(list -> list.stream()
                        .filter(d -> d != null && DeviceNodeTypeEnum.GATEWAY.getValue().equals(d.getNodeType()))
                        .map(DeviceDetailsResultVO::getDeviceIdentification)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList()))
                .filter(CollUtil::isNotEmpty)
                .map(gatewayIds -> {
                    DevicePageQuery query = new DevicePageQuery();
                    query.setGatewayIdList(gatewayIds);
                    query.setNodeType(DeviceNodeTypeEnum.SUBDEVICE.getValue());
                    return getDeviceResultVOList(query);
                })
                .map(subDevices -> subDevices.stream()
                        .filter(d -> d != null && d.getGatewayId() != null)
                        .collect(Collectors.groupingBy(DeviceResultVO::getGatewayId)))
                .orElseGet(Collections::emptyMap);
    }

    private List<DeviceResultVO> querySubDevices(String gatewayId) {
        DevicePageQuery devicePageQuery = new DevicePageQuery();
        devicePageQuery.setGatewayId(gatewayId);
        devicePageQuery.setNodeType(DeviceNodeTypeEnum.SUBDEVICE.getValue());
        return getDeviceResultVOList(devicePageQuery);
    }

    private Optional<DeviceLocationResultVO> queryDeviceLocation(String deviceIdentification) {
        DeviceLocationPageQuery deviceLocationPageQuery = new DeviceLocationPageQuery();
        deviceLocationPageQuery.setDeviceIdentification(deviceIdentification);
        List<DeviceLocationResultVO> deviceLocationResultVOList =
                deviceLocationService.getDeviceLocationResultVOList(deviceLocationPageQuery);
        return deviceLocationResultVOList.isEmpty() ? Optional.empty() : Optional.of(deviceLocationResultVOList.get(0));
    }


    /**
     * 单设备详情页拉产品信息 ── 走 {@link LinkCacheDataHelper#getProductCacheVO} 缓存路径
     * (read-through DB 兜底),避免每次详情请求都直查 DB。
     *
     * <p>写前置校验(saveDevice / updateDevice 里的产品存在性校验)仍直调
     * {@link ProductQueryService#findOneByProductIdentification},确保 DB-fresh。</p>
     */
    private Optional<ProductResultVO> queryProductInfo(String productIdentification) {
        return linkCacheDataHelper.getProductCacheVO(productIdentification)
                .map(p -> BeanPlusUtil.toBeanIgnoreError(p, ProductResultVO.class));
    }


    private TopoDeviceOperationResultVO deviceDataReport(TopoDeviceDataReportParam topoDeviceDataReportParam) {
        // 您的处理逻辑

        return TopoDeviceOperationResultVO.builder()
                .statusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                .statusDesc(MqttProtocolTopoStatusEnum.SUCCESS.getDesc()).build();
    }

    private TopoDeviceOperationResultVO deleteSubDevice(TopoDeleteSubDeviceParam topoDeleteSubDeviceParam) {
        List<String> deviceIds = Optional.ofNullable(topoDeleteSubDeviceParam)
                .map(TopoDeleteSubDeviceParam::getDeviceIds)
                .orElseGet(Collections::emptyList);

        // 批量查询所有设备，避免 N+1
        List<String> distinctDeviceIds = deviceIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Device> deviceMap = Optional.of(distinctDeviceIds)
                .filter(CollUtil::isNotEmpty)
                .map(ids -> {
                    DevicePageQuery query = new DevicePageQuery();
                    query.setDeviceIdentificationList(ids);
                    List<Device> devices = superManager.getDevicList(query);
                    return Optional.ofNullable(devices).orElseGet(Collections::emptyList);
                })
                .map(devices -> devices.stream()
                        .filter(d -> d != null && d.getDeviceIdentification() != null)
                        .collect(Collectors.toMap(Device::getDeviceIdentification, Function.identity(), (a, b) -> a)))
                .orElseGet(Collections::emptyMap);

        // 逐个删除并记录结果
        List<TopoDeviceOperationResultVO.OperationRsp> operationResultList = distinctDeviceIds.stream()
                .map(deviceId -> {
                    TopoDeviceOperationResultVO.OperationRsp operationRsp = new TopoDeviceOperationResultVO.OperationRsp()
                            .setDeviceId(deviceId);

                    Optional.ofNullable(deviceMap.get(deviceId))
                            .ifPresentOrElse(
                                    subDevice -> {
                                        boolean deleteFlag = superManager.removeById(subDevice);
                                        if (deleteFlag) {
                                            // 子设备 MQTT 拓扑删除路径：与 deleteDevice(Long) 共用事件，
                                            // 由各下游同步监听器在同一事务清理残留引用。
                                            deviceEventPublisher.publishDeviceDeletedEvent(DeviceDeletedEventSource.builder()
                                                    .deviceId(subDevice.getId())
                                                    .deviceIdentification(subDevice.getDeviceIdentification())
                                                    .clientId(subDevice.getClientId())
                                                    .contextMap(ContextUtil.getLocalMap())
                                                    .build());
                                        }
                                        MqttProtocolTopoStatusEnum statusEnum = deleteFlag
                                                ? MqttProtocolTopoStatusEnum.SUCCESS
                                                : MqttProtocolTopoStatusEnum.FAILURE;
                                        operationRsp.setStatusCode(statusEnum.getValue())
                                                .setStatusDesc(statusEnum.getDesc());
                                    },
                                    () -> operationRsp.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                                            .setStatusDesc(MqttProtocolTopoStatusEnum.FAILURE.getDesc())
                            );

                    return operationRsp;
                })
                .collect(Collectors.toList());

        return TopoDeviceOperationResultVO.builder()
                .statusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                .statusDesc(MqttProtocolTopoStatusEnum.SUCCESS.getDesc())
                .data(operationResultList)
                .build();
    }

    /** 批量更新子设备连接状态并记录设备动作。 */
    private TopoDeviceOperationResultVO updateSubDeviceConnectStatus(TopoUpdateSubDeviceStatusParam topoUpdateSubDeviceStatusParam) {
        List<TopoDeviceOperationResultVO.OperationRsp> operationRsp = topoUpdateSubDeviceStatusParam.getDeviceStatuses().stream()
                .map(this::processSubDeviceStatus)
                .collect(Collectors.toList());

        return TopoDeviceOperationResultVO.builder()
                .statusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                .statusDesc(MqttProtocolTopoStatusEnum.SUCCESS.getDesc())
                .data(operationRsp)
                .build();
    }

    /** 处理单个子设备的状态:更新并记录动作。 */
    private TopoDeviceOperationResultVO.OperationRsp processSubDeviceStatus(TopoUpdateSubDeviceStatusParam.DeviceStatus subDeviceStatus) {
        Device subDevice = superManager.findOneByDeviceIdentification(subDeviceStatus.getDeviceId());
        TopoDeviceOperationResultVO.OperationRsp dataItem = new TopoDeviceOperationResultVO.OperationRsp()
                .setDeviceId(subDeviceStatus.getDeviceId());

        if (subDevice != null) {
            // 更新设备连接状态
            UpdateWrapper<Device> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda()
                    .eq(Device::getId, subDevice.getId())
                    .set(Device::getConnectStatus, subDeviceStatus.getStatus().getValue())
                    .set(Device::getLastHeartbeatTime, LocalDateTime.now());
            boolean updateFlag = superManager.update(updateWrapper);
            recordDeviceAction(subDevice, subDeviceStatus.getStatus());

            MqttProtocolTopoStatusEnum updateStatusEnum = updateFlag ? MqttProtocolTopoStatusEnum.SUCCESS : MqttProtocolTopoStatusEnum.FAILURE;
            dataItem.setStatusCode(updateStatusEnum.getValue())
                    .setStatusDesc(updateStatusEnum.getDesc());
        } else {
            dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                    .setStatusDesc(MqttProtocolTopoStatusEnum.FAILURE.getDesc());
        }

        return dataItem;
    }

    /** 记录设备动作并落库。 */
    private void recordDeviceAction(Device device, DeviceConnectStatusEnum connectStatus) {
        // 构建设备动作描述和类型
        String describable = getDescriptionForStatus(connectStatus);
        DeviceActionTypeEnum actionType = getActionTypeForStatus(connectStatus);

        // 构建并保存设备动作记录
        DeviceActionSaveVO deviceActionSaveVO = new DeviceActionSaveVO();
        deviceActionSaveVO.setDeviceIdentification(device.getDeviceIdentification());
        deviceActionSaveVO.setActionType(actionType.getValue());
        deviceActionSaveVO.setMessage(actionType.getDesc());
        deviceActionSaveVO.setStatus(DeviceActionStatusEnum.SUCCESSFUL.getValue());
        deviceActionSaveVO.setRemark(describable);

        try {
            DeviceAction deviceAction = deviceActionService.saveDeviceAction(deviceActionSaveVO);
            log.info("Device action saved: {}", deviceAction);
        } catch (Exception e) {
            log.error("Failed to save device action for device ID: {}", device.getDeviceIdentification(), e);
        }
    }

    /** 根据连接状态生成描述文案。 */
    private String getDescriptionForStatus(DeviceConnectStatusEnum status) {
        String desc = Optional.ofNullable(status)
                .map(DeviceConnectStatusEnum::getDesc)
                .orElse("Unknown Status");

        return "The device connection status is updated to " + desc;
    }

    /** 根据连接状态确定动作类型。 */
    private DeviceActionTypeEnum getActionTypeForStatus(DeviceConnectStatusEnum status) {
        if (DeviceConnectStatusEnum.OFFLINE.equals(status)) {
            return DeviceActionTypeEnum.CLOSE;
        } else if (DeviceConnectStatusEnum.ONLINE.equals(status)) {
            return DeviceActionTypeEnum.CONNECT;
        } else {
            // Handle unexpected status here
            log.warn("Unexpected status: {}", status);
            return DeviceActionTypeEnum.UNKNOWN;
        }
    }


    /** 添加网关子设备 */
    private TopoAddDeviceResultVO saveSubDevice(TopoAddSubDeviceParam topoAddSubDeviceParam) {
        // 根据网关ID查找设备
        Device gatewayDevice = superManager.findOneByDeviceIdentification(topoAddSubDeviceParam.getGatewayIdentification());

        // 假设 gatewayDevice.getType() 方法返回设备类型，且 DeviceType.GATEWAY 代表网关设备类型
        MqttProtocolTopoStatusEnum statusEnum = (gatewayDevice != null && DeviceNodeTypeEnum.GATEWAY.getValue().equals(gatewayDevice.getNodeType()))
                ? MqttProtocolTopoStatusEnum.SUCCESS
                : MqttProtocolTopoStatusEnum.FAILURE;

        // 创建返回结果实例并设置状态码和状态描述
        TopoAddDeviceResultVO mqttTopoAddDeviceResultVO = TopoAddDeviceResultVO.builder()
                .statusCode(statusEnum.getValue())
                .statusDesc(statusEnum.getDesc())
                .build();

        // 创建一个设备列表用于存储处理结果
        List<TopoAddDeviceResultVO.DataItem> deviceList = new ArrayList<>();

        // 检查设备信息列表是否为空
        List<TopoAddSubDeviceParam.DeviceInfos> deviceInfos = topoAddSubDeviceParam.getDeviceInfos();
        if (deviceInfos != null) {
            // 遍历添加设备的参数信息列表
            for (TopoAddSubDeviceParam.DeviceInfos item : deviceInfos) {
                try {
                    // 创建数据项实例并验证设备参数
                    TopoAddDeviceResultVO.DataItem dataItem = new TopoAddDeviceResultVO.DataItem();
                    checkedTopoAddDeviceParam(item, dataItem);
                    // 将参数对象转换为设备信息对象并设置到数据项中
                    dataItem.setDeviceInfo(BeanUtil.toBean(item, TopoAddDeviceResultVO.DataItem.DeviceInfo.class, CopyOptions.create().ignoreError()));

                    // 如果设备参数验证不通过，添加到设备列表并继续下一次循环
                    if (!MqttProtocolTopoStatusEnum.SUCCESS.getValue().equals(dataItem.getStatusCode())) {
                        deviceList.add(dataItem);
                        continue;
                    }

                    // 转换并保存子设备信息
                    Device subDeviceDO = conversionDeviceBySaveSubDevice(gatewayDevice, item);
                    boolean saveFlag = superManager.save(subDeviceDO);

                    // 存储子设备经纬度信息
                    DeviceLocationPageQuery deviceLocationPageQuery = new DeviceLocationPageQuery();
                    deviceLocationPageQuery.setDeviceIdentification(gatewayDevice.getDeviceIdentification());

                    List<DeviceLocationResultVO> deviceLocationResultVOList = deviceLocationService.getDeviceLocationResultVOList(deviceLocationPageQuery);

                    Optional.ofNullable(deviceLocationResultVOList)
                            .filter(list -> !list.isEmpty())
                            .map(list -> list.get(0))
                            .map(deviceLocationResultVO -> BeanPlusUtil.toBeanIgnoreError(deviceLocationResultVO, DeviceLocationSaveVO.class))
                            .ifPresent(deviceLocationSaveVO -> {
                                deviceLocationSaveVO.setDeviceIdentification(subDeviceDO.getDeviceIdentification());
                                deviceLocationService.saveDeviceLocation(deviceLocationSaveVO);
                            });

                    // 设置平台生成的设备标识
                    dataItem.getDeviceInfo().setDeviceId(subDeviceDO.getDeviceIdentification());

                    // 根据保存结果设置状态码和状态描述
                    MqttProtocolTopoStatusEnum saveStatusEnum = saveFlag ? MqttProtocolTopoStatusEnum.SUCCESS : MqttProtocolTopoStatusEnum.FAILURE;
                    dataItem.setStatusCode(saveStatusEnum.getValue())
                            .setStatusDesc(saveStatusEnum.getDesc());

                    // 添加数据项到设备列表
                    deviceList.add(dataItem);

                    if (saveFlag) {
                        // 发布设备信息更新事件
                        deviceEventPublisher.publishDeviceInfoUpdatedEvent(DeviceInfoUpdatedEventSource.builder()
                                .deviceIdentificationList(Collections.singletonList(subDeviceDO.getDeviceIdentification()))
                                .build());
                    }
                } catch (Exception e) {
                    // 处理异常情况，将异常信息设置到数据项中
                    TopoAddDeviceResultVO.DataItem dataItem = new TopoAddDeviceResultVO.DataItem();
                    dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                            .setStatusDesc(e.getMessage());
                    deviceList.add(dataItem);
                }
            }
        }

        // 将设备列表设置到返回结果实例中
        mqttTopoAddDeviceResultVO.setData(deviceList);
        return mqttTopoAddDeviceResultVO;
    }


    /** 验证 Topo 添加设备参数,并设置对应的状态码和状态描述。 */
    private void checkedTopoAddDeviceParam(TopoAddSubDeviceParam.DeviceInfos item,
                                           TopoAddDeviceResultVO.DataItem dataItem) {
        // 根据设备标识查找子设备
        Device subDevice = superManager.findOneByDeviceIdentification(item.getNodeId());
        // 用于拼接错误消息的StringBuilder
        StringBuilder errorMessage = new StringBuilder();

        // 检查各参数是否为空，并将错误消息追加到StringBuilder中
        appendErrorMessageIfEmpty(errorMessage, item.getName(), "name is null; ");
        appendErrorMessageIfEmpty(errorMessage, item.getNodeId(), "nodeId is null; ");
        appendErrorMessageIfEmpty(errorMessage, item.getManufacturerId(), "manufacturerId is null; ");
        appendErrorMessageIfEmpty(errorMessage, item.getModel(), "model is null; ");

        // 检查设备节点ID是否已经存在
        if (subDevice != null) {
            errorMessage.append("nodeId is exist; ");
        }

        // 根据错误消息长度判断是否有错误，并设置相应的状态码和状态描述
        if (!errorMessage.isEmpty()) {
            dataItem.setStatusCode(MqttProtocolTopoStatusEnum.FAILURE.getValue())
                    .setStatusDesc(errorMessage.toString());
        } else {
            dataItem.setStatusCode(MqttProtocolTopoStatusEnum.SUCCESS.getValue())
                    .setStatusDesc(MqttProtocolTopoStatusEnum.SUCCESS.getDesc());
        }
    }

    /** 参数值为空时把错误消息追加到 StringBuilder。 */
    private void appendErrorMessageIfEmpty(StringBuilder errorMessage, CharSequence value, String message) {
        if (CharSequenceUtil.isEmpty(value)) {
            errorMessage.append(message);
        }
    }


    /** 网关子设备转换为 Device DO */
    private Device conversionDeviceBySaveSubDevice(Device gatewayDevice, TopoAddSubDeviceParam.DeviceInfos item) {
        Device device = new Device();
        BeanUtil.copyProperties(gatewayDevice, device, CopyOptions.create().setIgnoreProperties("id"));
        device.setDeviceName(item.getName());
        device.setClientId(TenantUtil.buildOptionalItem(SnowflakeIdUtil.nextId(), TenantUtil.extractTenantId(device.getClientId())));
        device.setDeviceIdentification(item.getNodeId());
        device.setNodeType(DeviceNodeTypeEnum.SUBDEVICE.getValue());
        device.setGatewayId(gatewayDevice.getDeviceIdentification());
        device.setConnectStatus(DeviceConnectStatusEnum.UNCONNECTED.getValue());
        device.setDeviceStatus(DeviceStatusEnum.ACTIVATED.getValue());
        device.setPassword(gatewayDevice.getPassword());
        // TODO 产品关联处理,支持多产品关联默认不关联网关设备产品
        return device;
    }


    private Builder<Device> builderDeviceUpdateVO(DeviceUpdateVO updateVO) {

        return Builder.of(Device::new)
                .with(Device::setUserName, updateVO.getUserName())
                .with(Device::setPassword, updateVO.getPassword())
                .with(Device::setCertSerialNumber, updateVO.getCertSerialNumber())
                .with(Device::setAppId, updateVO.getAppId())
                .with(Device::setAuthMode, updateVO.getAuthMode())
                .with(Device::setEncryptKey, updateVO.getEncryptKey())
                .with(Device::setEncryptVector, updateVO.getEncryptVector())
                .with(Device::setEncryptMethod, updateVO.getEncryptMethod())
                .with(Device::setSignKey, updateVO.getSignKey())
                .with(Device::setDeviceName, updateVO.getDeviceName())
                .with(Device::setConnector, updateVO.getConnector())
                .with(Device::setDescription, updateVO.getDescription())
                .with(Device::setDeviceStatus, updateVO.getDeviceStatus())
                .with(Device::setDeviceTags, updateVO.getDeviceTags())
                .with(Device::setSwVersion, updateVO.getSwVersion())
                .with(Device::setFwVersion, updateVO.getFwVersion())
                .with(Device::setDeviceSdkVersion, updateVO.getDeviceSdkVersion())
                .with(Device::setGatewayId, updateVO.getGatewayId())
                .with(Device::setProductIdentification, updateVO.getProductIdentification())
                .with(Device::setNodeType, updateVO.getNodeType())
                .with(Device::setRemark, updateVO.getRemark())
                .with(Device::setCreatedOrgId, ContextUtil.getCurrentDeptId());
    }

    /**
     * 构建保存参数。设备注册时默认把产品 activeVersionNo 作为 boundProductVersionNo 写入,
     * 供后续 TD 超级表寻址 / 物模型解析;saveVO 显式带了 boundProductVersionNo(灰度白名单导入)则不覆盖。
     *
     * @param productResultVO 已校验通过的产品信息(用于回填 boundProductVersionNo)
     */
    private Device builderDeviceSaveVO(DeviceSaveVO saveVO, ProductResultVO productResultVO) {
        Device device = BeanPlusUtil.copyProperties(saveVO, Device.class);
        //设备clientId 生成规则: 唯一标识 + @ + 租户ID
        device.setClientId(TenantUtil.buildOptionalItem(SnowflakeIdUtil.nextId(), ContextUtil.getTenantIdStr()));
        //设备标识生成规则: 雪花算法生成
        device.setDeviceIdentification(String.valueOf(SnowflakeIdUtil.nextId()));
        device.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        fillBoundProductVersionIfBlank(device, productResultVO);
        return device;
    }

    /**
     * 把设备 boundProductVersionNo 兜底回填为产品当前 activeVersionNo。
     * 仅当设备未显式带版本号时生效,避免覆盖外部传入的灰度发布白名单值。
     *
     * @param productResultVO 产品信息(可空,空时跳过填充)
     */
    private void fillBoundProductVersionIfBlank(Device device, ProductResultVO productResultVO) {
        if (device == null || productResultVO == null) {
            return;
        }
        // 设备已显式带版本号(例如新增 / 编辑表单用户主动选,或灰度白名单导入) → 保留入参不覆盖
        if (StrUtil.isNotBlank(device.getBoundProductVersionNo())) {
            return;
        }
        device.setBoundProductVersionNo(productResultVO.getActiveVersionNo());
    }

    /**
     * 校验新增参数。
     *
     * @return 校验通过的产品信息(供调用方回填 boundProductVersionNo 使用)
     */
    private ProductResultVO checkedDeviceSaveVO(DeviceSaveVO saveVO) {
        //设备认证模式
        ArgumentAssert.notNull(saveVO.getAuthMode(), "authMode Cannot be null");
        ArgumentAssert.notBlank(saveVO.getUserName(), "userName Cannot be null");
        ArgumentAssert.notBlank(saveVO.getPassword(), "password Cannot be null");
        if (DeviceAuthModeEnum.SSL_MODE.getValue().equals(saveVO.getAuthMode())) {
            ArgumentAssert.notBlank(saveVO.getCertSerialNumber(), "certSerialNumber Cannot be null");
        }

        //应用ID
        ArgumentAssert.notBlank(saveVO.getAppId(), "appId Cannot be null");

        //设备协议加密方式
        ArgumentAssert.notBlank(saveVO.getSignKey(), "signKey Cannot be null");
        ArgumentAssert.notNull(saveVO.getEncryptMethod(), "encryptMethod Cannot be null");
        if (DeviceEncryptMethodEnum.AES256.getValue().equals(saveVO.getEncryptMethod()) || DeviceEncryptMethodEnum.SM4.getValue().equals(saveVO.getEncryptMethod())) {
            ArgumentAssert.notBlank(saveVO.getEncryptKey(), "encryptKey Cannot be null");
            ArgumentAssert.notBlank(saveVO.getEncryptVector(), "The key vector cannot be empty.");

        }

        //设备状态
        ArgumentAssert.notNull(saveVO.getDeviceStatus(), "deviceStatus Cannot be null");
        if (!DeviceStatusEnum.ALL_STATE_COLLECTION.contains(saveVO.getDeviceStatus())) {
            throw BizException.wrap("DeviceStatusEnum is not exist");
        }

        //设备类型
        DeviceNodeTypeEnum.fromValue(saveVO.getNodeType()).orElseThrow(() -> BizException.wrap("deviceNodeType is not exist"));

        //子设备校验：如果是子设备，网关设备ID不能为空
        if (DeviceNodeTypeEnum.SUBDEVICE.getValue().equals(saveVO.getNodeType())) {
            ArgumentAssert.notBlank(saveVO.getGatewayId(), "The gateway device ID of the sub-device cannot be empty.");
        }

        //产品标识校验：校验产品是否存在
        ArgumentAssert.notBlank(saveVO.getProductIdentification(), "productIdentification Cannot be null");
        ProductResultVO productResultVO = productQueryService.findOneByProductIdentification(saveVO.getProductIdentification());
        ArgumentAssert.notNull(productResultVO, "productIdentification is not exist");
        return productResultVO;
    }

    /** 校验更新参数 */
    private void checkedDeviceUpdateVO(DeviceUpdateVO updateVO) {

        ArgumentAssert.notNull(updateVO.getId(), "id Cannot be null");

        ArgumentAssert.notBlank(updateVO.getUserName(), "userName Cannot be null");
        ArgumentAssert.notBlank(updateVO.getPassword(), "password Cannot be null");
        if (DeviceAuthModeEnum.SSL_MODE.getValue().equals(updateVO.getAuthMode())) {
            ArgumentAssert.notBlank(updateVO.getCertSerialNumber(), "certSerialNumber Cannot be null");
        }

        //应用ID
        ArgumentAssert.notBlank(updateVO.getAppId(), "appId Cannot be null");

        //设备协议加密方式
        ArgumentAssert.notBlank(updateVO.getSignKey(), "signKey Cannot be null");
        ArgumentAssert.notNull(updateVO.getEncryptMethod(), "encryptMethod Cannot be null");
        if (DeviceEncryptMethodEnum.AES256.getValue().equals(updateVO.getEncryptMethod()) || DeviceEncryptMethodEnum.SM4.getValue().equals(updateVO.getEncryptMethod())) {
            ArgumentAssert.notBlank(updateVO.getEncryptKey(), "encryptKey Cannot be null");
            ArgumentAssert.notBlank(updateVO.getEncryptVector(), "encryptVector Cannot be null");
        }

        //设备状态
        ArgumentAssert.notNull(updateVO.getDeviceStatus(), "deviceStatus Cannot be null");
        if (!DeviceStatusEnum.ALL_STATE_COLLECTION.contains(updateVO.getDeviceStatus())) {
            throw BizException.wrap("DeviceStatusEnum is not exist");
        }

        //设备类型
        DeviceNodeTypeEnum.fromValue(updateVO.getNodeType()).orElseThrow(() -> BizException.wrap("deviceNodeType is not exist"));

        //子设备校验：如果是子设备，网关设备ID不能为空
        if (DeviceNodeTypeEnum.SUBDEVICE.getValue().equals(updateVO.getNodeType())) {
            ArgumentAssert.notBlank(updateVO.getGatewayId(), "The gateway device ID of the sub-device cannot be empty.");
        }

        //产品标识校验：校验产品是否存在
        ArgumentAssert.notBlank(updateVO.getProductIdentification(), "productIdentification Cannot be null");
        ProductResultVO productResultVO = productQueryService.findOneByProductIdentification(updateVO.getProductIdentification());
        ArgumentAssert.notNull(productResultVO, "productIdentification is not exist");

    }


    @Override
    public Boolean reportDeviceHeartbeat(String clientIdentifier, Long heartbeatTime, Long eventHlc) {
        //根据客户端标识符查询设备缓存信息
        Device device = superManager.findOneByClientId(clientIdentifier);
        if (Objects.isNull(device)) {
            throw BizException.wrap("客户端标识:{} 设备档案信息不存在", clientIdentifier);
        }
        try {
            // 1. 心跳时间:无条件更新(每条心跳都续 last_heartbeat_time)
            Device updateDO = new Device();
            updateDO.setId(device.getId());
            LocalDateTime heartbeatDateTime = (heartbeatTime != null)
                    ? DateUtil.date(heartbeatTime).toLocalDateTime()
                    : LocalDateTime.now();
            updateDO.setLastHeartbeatTime(heartbeatDateTime);
            superManager.updateById(updateDO);
            // 2. 在线状态:走 eventHlc HLC CAS 单调写置 ONLINE(替代原直写,防迟到/乱序事件把已离线翻回在线);
            //    eventHlc 缺失/非法则不动状态,交由其它带 hlc 的生命周期事件维护(CONNECT/DISCONNECT 等)。
            //    类级 @DS/@Transactional 覆盖,内部 this 调用同数据源、同事务。
            if (eventHlc != null && eventHlc > 0) {
                updateDeviceConnectionStatusByEvent(clientIdentifier, DeviceConnectStatusEnum.ONLINE.getValue(), eventHlc);
            }
            return true;
        } catch (Exception e) {
            log.error("上报设备心跳失败,clientIdentifier:{}", clientIdentifier, e);
            return false;
        }
    }

    @Override
    public Long countByCertSerialNumber(String certSerialNumber) {
        return superManager.count(Wraps.<Device>lbQ()
                .eq(Device::getCertSerialNumber, certSerialNumber));
    }

    @Override
    public Long countOnlineByCertSerialNumber(String certSerialNumber) {
        return superManager.count(Wraps.<Device>lbQ()
                .eq(Device::getCertSerialNumber, certSerialNumber)
                .eq(Device::getConnectStatus, DeviceConnectStatusEnum.ONLINE.getValue()));
    }

    @Override
    public List<Device> listTopBoundDevicesByCertSerialNumber(String certSerialNumber, int limit) {
        return superManager.list(Wraps.<Device>lbQ()
                .eq(Device::getCertSerialNumber, certSerialNumber)
                .orderByDesc(Device::getLastHeartbeatTime)
                .last("LIMIT " + limit));
    }

    // ────────────── 产品版本发布:设备改绑 service 入口 ──────────────
    // 这 3 个方法走 service 而非直接 Manager 的原因:
    //   1. @DS(BASE_TENANT) 切库 AOP 在 Service 层 ── Manager 无 @DS,跨域直调会 fallback 到默认库,
    //      UPDATE 跨租户串味或报 "Table 'thinglinks_ds_c_defaults.device' doesn't exist"
    //   2. 不加 @Transactional ── dynamic-datasource 跟 @Transactional 冲突:事务开启会锁定当前 DS,
    //      后续 @DS SPEL 重新求值不生效。这里是单 UPDATE,InnoDB 单 SQL 本身原子,无需事务包裹

    @Override
    public int bulkRebindByProduct(String productIdentification, String toVersion) {
        int affected = superManager.bulkRebindByProduct(productIdentification, toVersion);
        // 发改绑事件:监听器 AFTER_COMMIT 失效该产品下设备缓存,否则上报仍读旧 boundProductVersionNo
        deviceEventPublisher.publishDeviceRebindEvent(DeviceRebindEventSource.builder()
                .productIdentification(productIdentification)
                .toVersion(toVersion)
                .contextMap(ContextUtil.getLocalMap())
                .build());
        return affected;
    }

    @Override
    public int bulkRebindByDeviceIdentifications(List<String> deviceIdentifications, String toVersion) {
        int affected = superManager.bulkRebindByDeviceIdentifications(deviceIdentifications, toVersion);
        // 灰度白名单改绑:发改绑事件,监听器按设备标识失效缓存
        deviceEventPublisher.publishDeviceRebindEvent(DeviceRebindEventSource.builder()
                .deviceIdentifications(deviceIdentifications)
                .toVersion(toVersion)
                .contextMap(ContextUtil.getLocalMap())
                .build());
        return affected;
    }

    @Override
    public int bulkRebindByProductAndVersion(String productIdentification, String fromVersion, String toVersion) {
        int affected = superManager.bulkRebindByProductAndVersion(productIdentification, fromVersion, toVersion);
        // 回滚 / 灰度晋升改绑:发改绑事件,监听器失效该产品下设备缓存
        deviceEventPublisher.publishDeviceRebindEvent(DeviceRebindEventSource.builder()
                .productIdentification(productIdentification)
                .toVersion(toVersion)
                .contextMap(ContextUtil.getLocalMap())
                .build());
        return affected;
    }

}
