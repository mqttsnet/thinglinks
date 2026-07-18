package com.mqttsnet.thinglinks.video.service.device.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mqttsnet.basic.mybatis.typehandler.EncryptTypeHandler;
import com.mqttsnet.basic.secure.config.EncryptKeyManager;
import com.mqttsnet.basic.secure.config.EncryptKeyProperties;
import com.mqttsnet.thinglinks.video.cache.VideoChannelCacheVO;
import com.mqttsnet.thinglinks.video.dto.gb28181.cache.PlatformRegisterCache;
import com.mqttsnet.thinglinks.video.dto.platform.VideoPlatformInfo;
import com.mqttsnet.thinglinks.video.entity.device.VideoChannel;
import com.mqttsnet.thinglinks.video.entity.device.VideoNotifySubscription;
import com.mqttsnet.thinglinks.video.entity.platform.VideoPlatform;
import com.mqttsnet.thinglinks.video.service.platform.impl.VideoPlatformServiceImpl;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoChannelResultVO;
import com.mqttsnet.thinglinks.video.vo.result.device.VideoNotifySubscriptionResultVO;
import com.mqttsnet.thinglinks.video.vo.result.platform.VideoPlatformResultVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoChannelSaveVO;
import com.mqttsnet.thinglinks.video.vo.save.device.VideoNotifySubscriptionSaveVO;
import com.mqttsnet.thinglinks.video.vo.save.platform.VideoPlatformSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoChannelUpdateVO;
import com.mqttsnet.thinglinks.video.vo.update.device.VideoNotifySubscriptionUpdateVO;
import com.mqttsnet.thinglinks.video.vo.update.platform.VideoPlatformUpdateVO;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

class VideoCredentialSecurityTest {

    private static final String SECRET = "credential-value-that-must-not-leak";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeAll
    static void configureEncryptionDefaults() {
        EncryptKeyProperties.AesConfig aesConfig = new EncryptKeyProperties.AesConfig();
        aesConfig.setKey("0123456789abcdef");
        aesConfig.setIv("abcdef9876543210");
        EncryptKeyProperties properties = new EncryptKeyProperties();
        properties.setAes(aesConfig);
        new com.mqttsnet.basic.utils.aes.AesUtils(new EncryptKeyManager(properties));
    }

    @Test
    void blankCredentialsAreOmittedFromPartialUpdates() {
        VideoChannel channel = new TestVideoChannelService().map(
                VideoChannelUpdateVO.builder().id(1L).password("  ").build());
        VideoNotifySubscription subscription = new TestVideoNotifySubscriptionService().map(
                VideoNotifySubscriptionUpdateVO.builder().id(2L).channelConfig("\t").build());
        VideoPlatform platform = new TestVideoPlatformService().map(
                VideoPlatformUpdateVO.builder().id(3L).password(null).build());

        assertThat(channel.getPassword()).isNull();
        assertThat(subscription.getChannelConfig()).isNull();
        assertThat(platform.getPassword()).isNull();
    }

    @Test
    void nonBlankCredentialsRemainAvailableForEncryption() {
        VideoChannel channel = new TestVideoChannelService().map(
                VideoChannelUpdateVO.builder().id(1L).password(SECRET).build());
        VideoNotifySubscription subscription = new TestVideoNotifySubscriptionService().map(
                VideoNotifySubscriptionUpdateVO.builder().id(2L).channelConfig(SECRET).build());
        VideoPlatform platform = new TestVideoPlatformService().map(
                VideoPlatformUpdateVO.builder().id(3L).password(SECRET).build());

        assertThat(channel.getPassword()).isEqualTo(SECRET);
        assertThat(subscription.getChannelConfig()).isEqualTo(SECRET);
        assertThat(platform.getPassword()).isEqualTo(SECRET);
    }

    @Test
    void entitiesAndResultVosNeverSerializeCredentials() throws Exception {
        assertCredentialIsNotSerialized(new VideoChannel().setPassword(SECRET), "password");
        assertCredentialIsNotSerialized(new VideoNotifySubscription().setChannelConfig(SECRET), "channelConfig");
        assertCredentialIsNotSerialized(new VideoPlatform().setPassword(SECRET), "password");

        assertCredentialIsNotSerialized(new VideoChannelResultVO(), "password");
        assertCredentialIsNotSerialized(new VideoNotifySubscriptionResultVO(), "channelConfig");
        assertCredentialIsNotSerialized(new VideoPlatformResultVO(), "password");
    }

    @Test
    void generatedToStringNeverContainsCredentials() {
        assertThat(VideoChannelSaveVO.builder().password(SECRET).build().toString()).doesNotContain(SECRET);
        assertThat(VideoChannelUpdateVO.builder().password(SECRET).build().toString()).doesNotContain(SECRET);
        assertThat(VideoNotifySubscriptionSaveVO.builder().channelConfig(SECRET).build().toString()).doesNotContain(SECRET);
        assertThat(VideoNotifySubscriptionUpdateVO.builder().channelConfig(SECRET).build().toString()).doesNotContain(SECRET);
        assertThat(VideoPlatformSaveVO.builder().password(SECRET).build().toString()).doesNotContain(SECRET);
        assertThat(VideoPlatformUpdateVO.builder().password(SECRET).build().toString()).doesNotContain(SECRET);
        assertThat(new VideoChannel().setPassword(SECRET).toString()).doesNotContain(SECRET);
        assertThat(new VideoNotifySubscription().setChannelConfig(SECRET).toString()).doesNotContain(SECRET);
        assertThat(new VideoPlatform().setPassword(SECRET).toString()).doesNotContain(SECRET);
        assertThat(findField(VideoChannelCacheVO.class, "password")).isNull();
        assertThat(findField(VideoPlatformInfo.class, "password")).isNull();
        assertThat(PlatformRegisterCache.builder().password(SECRET).build().toString()).doesNotContain(SECRET);
    }

    @Test
    void credentialFieldsUseTheEncryptionHandlerAndRejectEmptyWrites() throws Exception {
        assertEncryptedField(VideoChannel.class, "password");
        assertEncryptedField(VideoNotifySubscription.class, "channelConfig");
        assertEncryptedField(VideoPlatform.class, "password");
    }

    @Test
    void explicitMapperResultMapsAlsoDecryptCredentials() throws Exception {
        assertMapperUsesEncryptionHandler("mapper_video/base/VideoChannelMapper.xml", "property=\"password\"");
        assertMapperUsesEncryptionHandler(
                "mapper_video/base/VideoNotifySubscriptionMapper.xml", "property=\"channelConfig\"");
        assertMapperUsesEncryptionHandler("mapper_video/base/VideoPlatformMapper.xml", "property=\"password\"");
    }

    @Test
    void legacyPlaintextIsReadableAndMarkedCiphertextIsNotEncryptedAgain() throws Exception {
        EncryptTypeHandler handler = new EncryptTypeHandler();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getString("credential")).thenReturn(SECRET);

        assertThat(handler.getResult(resultSet, "credential")).isEqualTo(SECRET);

        PreparedStatement encryptionStatement = mock(PreparedStatement.class);
        handler.setParameter(encryptionStatement, 1, SECRET, JdbcType.VARCHAR);
        ArgumentCaptor<String> ciphertextCaptor = ArgumentCaptor.forClass(String.class);
        verify(encryptionStatement).setString(org.mockito.ArgumentMatchers.eq(1), ciphertextCaptor.capture());
        String ciphertext = ciphertextCaptor.getValue();

        PreparedStatement statement = mock(PreparedStatement.class);
        handler.setParameter(statement, 1, ciphertext, JdbcType.VARCHAR);
        verify(statement).setString(1, ciphertext);
    }

    private void assertCredentialIsNotSerialized(Object value, String fieldName) throws Exception {
        Field field = findField(value.getClass(), fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(value, SECRET);
        }
        String json = objectMapper.writeValueAsString(value);
        assertThat(json).doesNotContain(SECRET).doesNotContain("\"" + fieldName + "\"");
    }

    private Field findField(Class<?> type, String fieldName) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                // 结果 VO 可以直接移除敏感字段。
            }
        }
        return null;
    }

    private void assertEncryptedField(Class<?> type, String fieldName) throws Exception {
        TableField tableField = type.getDeclaredField(fieldName).getAnnotation(TableField.class);
        assertThat(tableField).isNotNull();
        assertThat(tableField.typeHandler()).isEqualTo(EncryptTypeHandler.class);
        assertThat(tableField.insertStrategy()).isEqualTo(FieldStrategy.NOT_EMPTY);
        assertThat(tableField.updateStrategy()).isEqualTo(FieldStrategy.NOT_EMPTY);
    }

    private void assertMapperUsesEncryptionHandler(String resourceName, String mappedProperty) throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertThat(input).as(resourceName).isNotNull();
            String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(xml).contains(mappedProperty)
                    .contains("typeHandler=\"com.mqttsnet.basic.mybatis.typehandler.EncryptTypeHandler\"");
        }
    }

    private static final class TestVideoChannelService extends VideoChannelServiceImpl {
        private TestVideoChannelService() {
            super(mock(ApplicationEventPublisher.class));
        }

        private VideoChannel map(VideoChannelUpdateVO updateVO) {
            return updateBefore(updateVO);
        }
    }

    private static final class TestVideoNotifySubscriptionService extends VideoNotifySubscriptionServiceImpl {
        private VideoNotifySubscription map(VideoNotifySubscriptionUpdateVO updateVO) {
            return updateBefore(updateVO);
        }
    }

    private static final class TestVideoPlatformService extends VideoPlatformServiceImpl {
        private VideoPlatform map(VideoPlatformUpdateVO updateVO) {
            return updateBefore(updateVO);
        }
    }
}
