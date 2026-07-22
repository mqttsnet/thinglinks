package com.mqttsnet.thinglinks.device.service.impl;

import com.mqttsnet.thinglinks.cache.helper.LinkCacheDataHelper;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceAclRuleCacheVO;
import com.mqttsnet.thinglinks.cache.vo.device.DeviceCacheVO;
import com.mqttsnet.thinglinks.device.enumeration.ClientAclActionTypeEnum;
import com.mqttsnet.thinglinks.device.enumeration.DeviceAclRuleActionTypeEnum;
import com.mqttsnet.thinglinks.device.event.publisher.DeviceAclRuleEventPublisher;
import com.mqttsnet.thinglinks.device.vo.query.DeviceAclCheckQuery;
import com.mqttsnet.thinglinks.protocol.vo.result.DeviceAclCheckResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("设备ACL权限校验")
class DeviceAclRuleServiceImplTest {

    @Mock
    private LinkCacheDataHelper linkCacheDataHelper;
    @Mock
    private DeviceAclRuleEventPublisher deviceAclRuleEventPublisher;

    @Test
    @DisplayName("客户端标识找不到设备缓存时拒绝访问并返回明确原因")
    void checkAclPermissionShouldDenyWhenDeviceNotFound() {
        when(linkCacheDataHelper.getDeviceCacheVO("client-a")).thenReturn(Optional.empty());
        DeviceAclRuleServiceImpl service = new DeviceAclRuleServiceImpl(linkCacheDataHelper, deviceAclRuleEventPublisher);

        DeviceAclCheckResultVO result = service.checkAclPermission(query(ClientAclActionTypeEnum.PUBLISH.getValue(),
                "/v1/devices/product-a/device-a/up"));

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Device Not Found");
    }

    @Test
    @DisplayName("设备没有ACL规则时默认拒绝，避免缓存缺失变成放行")
    void checkAclPermissionShouldDenyWhenRuleMissing() {
        DeviceCacheVO device = deviceCache();
        when(linkCacheDataHelper.getDeviceCacheVO("client-a")).thenReturn(Optional.of(device));
        when(linkCacheDataHelper.getDeviceAclRules("product-a", "device-a")).thenReturn(List.of());
        DeviceAclRuleServiceImpl service = new DeviceAclRuleServiceImpl(linkCacheDataHelper, deviceAclRuleEventPublisher);

        DeviceAclCheckResultVO result = service.checkAclPermission(query(ClientAclActionTypeEnum.PUBLISH.getValue(),
                "/v1/devices/product-a/device-a/up"));

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Not ACL Rule");
    }

    @Test
    @DisplayName("发布动作会按设备占位符替换后的主题规则命中允许策略")
    void checkAclPermissionShouldAllowPublishWhenTopicMatches() {
        DeviceCacheVO device = deviceCache();
        DeviceAclRuleCacheVO rule = rule(DeviceAclRuleActionTypeEnum.PUBLISH,
                "/v1/devices/${product_identification}/${device_identification}/up", true);
        when(linkCacheDataHelper.getDeviceCacheVO("client-a")).thenReturn(Optional.of(device));
        when(linkCacheDataHelper.getDeviceAclRules("product-a", "device-a")).thenReturn(List.of(rule));
        DeviceAclRuleServiceImpl service = new DeviceAclRuleServiceImpl(linkCacheDataHelper, deviceAclRuleEventPublisher);

        DeviceAclCheckResultVO result = service.checkAclPermission(query(ClientAclActionTypeEnum.PUBLISH.getValue(),
                "/v1/devices/product-a/device-a/up"));

        assertThat(result.getAllowed()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
        assertThat(rule.getTopicPattern()).isEqualTo("/v1/devices/product-a/device-a/up");
    }

    @Test
    @DisplayName("发布请求不会命中仅订阅动作的规则，动作维度不匹配时拒绝")
    void checkAclPermissionShouldDenyWhenActionDoesNotMatch() {
        DeviceCacheVO device = deviceCache();
        when(linkCacheDataHelper.getDeviceCacheVO("client-a")).thenReturn(Optional.of(device));
        when(linkCacheDataHelper.getDeviceAclRules("product-a", "device-a"))
                .thenReturn(List.of(rule(DeviceAclRuleActionTypeEnum.SUBSCRIBE, "/v1/devices/#", true)));
        DeviceAclRuleServiceImpl service = new DeviceAclRuleServiceImpl(linkCacheDataHelper, deviceAclRuleEventPublisher);

        DeviceAclCheckResultVO result = service.checkAclPermission(query(ClientAclActionTypeEnum.PUBLISH.getValue(),
                "/v1/devices/product-a/device-a/up"));

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("不支持的客户端动作类型拒绝访问并保留动作值方便排查")
    void checkAclPermissionShouldDenyUnsupportedActionType() {
        when(linkCacheDataHelper.getDeviceCacheVO("client-a")).thenReturn(Optional.of(deviceCache()));
        when(linkCacheDataHelper.getDeviceAclRules("product-a", "device-a"))
                .thenReturn(List.of(rule(DeviceAclRuleActionTypeEnum.ALL, "#", true)));
        DeviceAclRuleServiceImpl service = new DeviceAclRuleServiceImpl(linkCacheDataHelper, deviceAclRuleEventPublisher);

        DeviceAclCheckResultVO result = service.checkAclPermission(query(99,
                "/v1/devices/product-a/device-a/up"));

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getErrorMessage()).isEqualTo("Unsupported action type for ACL: 99");
    }

    private DeviceAclCheckQuery query(Integer actionType, String topic) {
        return new DeviceAclCheckQuery()
                .setClientIdentifier("client-a")
                .setActionType(actionType)
                .setTopic(topic);
    }

    private DeviceCacheVO deviceCache() {
        return new DeviceCacheVO()
                .setClientId("client-a")
                .setTenantId(1L)
                .setAppId("app-a")
                .setUserName("user-a")
                .setProductIdentification("product-a")
                .setDeviceIdentification("device-a")
                .setDeviceSdkVersion("1.0.0");
    }

    private DeviceAclRuleCacheVO rule(DeviceAclRuleActionTypeEnum actionType, String topicPattern, boolean decision) {
        return new DeviceAclRuleCacheVO()
                .setId(1L)
                .setRuleName("默认ACL")
                .setActionType(actionType.getValue())
                .setPriority(10)
                .setTopicPattern(topicPattern)
                .setDecision(decision)
                .setEnabled(true)
                .setProductIdentification("product-a")
                .setDeviceIdentification("device-a");
    }
}
