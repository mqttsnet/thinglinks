package com.mqttsnet.thinglinks.anytenant.controller;

import java.util.Collections;
import java.util.Optional;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.TenantUtil;
import com.mqttsnet.thinglinks.device.enumeration.ClientAclActionTypeEnum;
import com.mqttsnet.thinglinks.device.service.DeviceAclRuleService;
import com.mqttsnet.thinglinks.device.service.DeviceService;
import com.mqttsnet.thinglinks.device.vo.query.DeviceAclCheckQuery;
import com.mqttsnet.thinglinks.device.vo.query.DeviceAuthenticationQuery;
import com.mqttsnet.thinglinks.product.enumeration.ProtocolTypeEnum;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceAclCheckResultVO;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceAuthenticationResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 设备相关开放接口（anyTenant）
 * 请求中 不需要携带TenantId 且 不需要携带Token(不需要登录) 和 不需要验证uri权限
 *
 * @author mqttsnet
 * @date 2021-06-30
 * @create [2021-06-30] [mqttsnet]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/anyTenant/deviceOpen")
@Tag(name = "anyTenant-设备相关API")
public class DeviceOpenAnyTenantController {


    private final DeviceService deviceService;

    private final DeviceAclRuleService deviceAclRuleService;


    @Operation(summary = "Device connection authentication", description = "If the authentication succeeds, the status code 200 is returned. If the authentication fails, the advanced status code is returned")
    @PostMapping(value = "/clientConnectionAuthentication")
    public ResponseEntity<DeviceAuthenticationResultVO> clientConnectionAuthentication(@Valid @RequestBody DeviceAuthenticationQuery deviceAuthenticationQuery) {
        log.info("clientConnectionAuthentication clientIdentifier: {} param：{}", deviceAuthenticationQuery.getClientIdentifier(), JSON.toJSONString(deviceAuthenticationQuery));
        ArgumentAssert.notBlank(deviceAuthenticationQuery.getProtocolType(), "The protocol type cannot be null");

        if (ProtocolTypeEnum.fromValue(deviceAuthenticationQuery.getProtocolType()).isEmpty()) {
            log.info("clientConnectionAuthentication skipped for unsupported protocol type: {}", deviceAuthenticationQuery.getProtocolType());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildAuthFailure("Unsupported protocol type: " + deviceAuthenticationQuery.getProtocolType()));
        }

        // Check parameter
        ArgumentAssert.notBlank(deviceAuthenticationQuery.getClientIdentifier(), "clientIdentifier Cannot be null");
        ArgumentAssert.notBlank(deviceAuthenticationQuery.getUsername(), "username Cannot be null");
        ArgumentAssert.notBlank(deviceAuthenticationQuery.getPassword(), "password Cannot be null");

        // Resolve the tenant ID based on the device ID, for example, clientId: 1000000000000000001@tenantId
        String tenantId = TenantUtil.extractTenantIdWithDefault(deviceAuthenticationQuery.getClientIdentifier());
        ContextUtil.setTenantId(tenantId);
        try {
            DeviceAuthenticationResultVO authenticationResult = deviceService.authClient(deviceAuthenticationQuery);
            if (!Boolean.TRUE.equals(authenticationResult.getCertificationResult())) {
                log.warn("clientConnectionAuthentication {} The device connection authentication failed. Procedure：{}", deviceAuthenticationQuery.getClientIdentifier(), authenticationResult.getErrorMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authenticationResult);
            }
            log.info("clientConnectionAuthentication {} Device connection authentication result：true", deviceAuthenticationQuery.getClientIdentifier());
            // 认证成功 → 拉 ACL 规则(deviceInfoResult 理论上 authClient 通过后必填,防御性 null check 兜底)
            // ACL 拉取与连接认证解耦:已认证通过的设备,ACL 规则拉取失败(缓存反序列化/DB 或切库冷启动等)
            // 只降级为空列表、不否决连接。ACL 真正生效在 publish/subscribe 的 clientAclValidation(独立接口),此处空列表不降低安全。
            if (authenticationResult.getDeviceInfoResult() != null) {
                try {
                    authenticationResult.setAclRuleListResult(deviceAclRuleService.getDeviceAclRuleCacheVOList(
                        authenticationResult.getDeviceInfoResult().getProductIdentification(),
                        authenticationResult.getDeviceInfoResult().getDeviceIdentification()));
                } catch (Exception aclEx) {
                    log.warn("clientConnectionAuthentication {} ACL 规则拉取失败,降级为空列表,不影响连接认证", deviceAuthenticationQuery.getClientIdentifier(), aclEx);
                    authenticationResult.setAclRuleListResult(Collections.emptyList());
                }
            }
            return ResponseEntity.ok().body(authenticationResult);
        } catch (Exception e) {
            log.error("An exception occurred during authentication. Procedure: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildAuthFailure("Internal error: " + e.getMessage()));
        } finally {
            // 防 ThreadLocal 串味,请求线程归还前清理租户上下文
            ContextUtil.remove();
        }
    }

    private DeviceAuthenticationResultVO buildAuthFailure(String message) {
        DeviceAuthenticationResultVO vo = new DeviceAuthenticationResultVO<>();
        vo.setCertificationResult(false);
        vo.setErrorMessage(message);
        return vo;
    }


    /**
     * ACL权限校验接口
     *
     * @param deviceAclCheckQuery ACL权限校验请求参数
     * @return 权限校验结果
     */
    @Operation(summary = "Device ACL validation", description = "Check if the client has permission to perform the operation on the topic")
    @PostMapping(value = "/clientAclValidation")
    public ResponseEntity<DeviceAclCheckResultVO> clientAclValidation(@Valid @RequestBody DeviceAclCheckQuery deviceAclCheckQuery) {
        log.info("clientAclValidation ACL validation clientIdentifier: {} param：{}", deviceAclCheckQuery.getClientIdentifier(), JSON.toJSONString(deviceAclCheckQuery));

        // actionType 早校验,Service 层不再重复(详见 checkAclPermission 内部假设 caller 已校验)
        Optional<ClientAclActionTypeEnum> clientAclActionTypeEnumOptional = ClientAclActionTypeEnum.fromValue(deviceAclCheckQuery.getActionType());
        if (clientAclActionTypeEnumOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(DeviceAclCheckResultVO.builder()
                .allowed(false)
                .errorMessage("Invalid action type: " + deviceAclCheckQuery.getActionType())
                .echoMap(MapUtil.newHashMap())
                .build());
        }

        ContextUtil.setTenantId(Optional.ofNullable(deviceAclCheckQuery.getTenantId()).orElse(ContextConstants.BUILT_IN_TENANT_ID_STR));
        try {
            DeviceAclCheckResultVO result = deviceAclRuleService.checkAclPermission(deviceAclCheckQuery);
            String actionDesc = clientAclActionTypeEnumOptional.get().getDesc();
            if (Boolean.TRUE.equals(result.getAllowed())) {
                log.info("ACL allowed: {}...{} -> {}", deviceAclCheckQuery.getClientIdentifier(), actionDesc, deviceAclCheckQuery.getTopic());
                return ResponseEntity.ok().body(result);
            }
            log.warn("ACL denied: {}...{} -> {} reason={}", deviceAclCheckQuery.getClientIdentifier(), actionDesc, deviceAclCheckQuery.getTopic(), result.getErrorMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(result);
        } catch (Exception e) {
            log.error("ACL validation error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(DeviceAclCheckResultVO.builder()
                .allowed(false)
                .errorMessage("ACL validation error: " + e.getMessage())
                .echoMap(MapUtil.newHashMap())
                .build());
        } finally {
            // 防 ThreadLocal 串味,请求线程归还前清理租户上下文
            ContextUtil.remove();
        }
    }

}
