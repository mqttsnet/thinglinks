package com.xxl.job.admin.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordUtilTest {

    @Test
    void shouldUseUtf8WhenEncodingPassword() {
        assertEquals("bfb4b86cceb5d88405cb1f63c6c94937", PasswordUtil.md5("初始密码-Aa1!"));
    }
}
