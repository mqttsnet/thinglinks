package com.mqttsnet.thinglinks.video.service.support;

/**
 * 视频业务标识规范化工具。
 */
public final class VideoIdentityNormalizer {

    private VideoIdentityNormalizer() {
    }

    /**
     * 去除首尾普通半角空格，结果为空时返回 {@code null}。
     * 该规则与 MySQL {@code TRIM} 生成列表达式保持一致。
     */
    public static String trimAsciiSpaceToNull(String value) {
        if (value == null) {
            return null;
        }
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == ' ') {
            start++;
        }
        while (end > start && value.charAt(end - 1) == ' ') {
            end--;
        }
        return start == end ? null : value.substring(start, end);
    }

    /**
     * 判断字符串是否为空或只包含普通半角空格。
     */
    public static boolean isAsciiSpaceOnly(String value) {
        return value != null && trimAsciiSpaceToNull(value) == null;
    }
}
