package com.xxl.job.admin.core.util;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 密码摘要工具，固定使用 UTF-8，保证初始化脚本与不同运行环境的计算结果一致。
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String md5(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }
}
