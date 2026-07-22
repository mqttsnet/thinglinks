package com.mqttsnet.thinglinks.utils.x509;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("MQTT 证书生成口令")
class MqttCertGeneratorTest {

    @Test
    @DisplayName("PKCS12 口令未配置时拒绝生成证书")
    void shouldRejectMissingPkcs12Password() {
        assertThrows(IllegalArgumentException.class, () -> MqttCertGenerator.requirePkcs12Password(null));
        assertThrows(IllegalArgumentException.class, () -> MqttCertGenerator.requirePkcs12Password("   "));
    }

    @Test
    @DisplayName("PKCS12 口令使用外部配置值")
    void shouldUseConfiguredPkcs12Password() {
        assertArrayEquals("local-development-passphrase".toCharArray(),
                MqttCertGenerator.requirePkcs12Password("local-development-passphrase"));
    }
}
