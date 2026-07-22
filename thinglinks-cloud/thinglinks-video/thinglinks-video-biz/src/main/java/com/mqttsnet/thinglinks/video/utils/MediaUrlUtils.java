package com.mqttsnet.thinglinks.video.utils;

/**
 * 流媒体地址构建工具类。
 * 兼容 IP（192.168.1.1）、域名（media.example.com）、
 * 以及已携带协议前缀（http://media.example.com）的地址格式。
 *
 * @author mqttsnet
 * @version 1.0.0
 * @since 2026-03-31
 */
public final class MediaUrlUtils {

    private MediaUrlUtils() {
    }

    /**
     * 构建文件访问 URL。
     * <p>
     * 适用于录像文件下载等场景。如果 url 已是完整的 HTTP 地址则直接返回，
     * 否则使用 host + port 拼接基础地址。
     *
     * @param host IP、域名或带协议的完整地址
     * @param port 端口号
     * @param url  文件路径或完整 URL（如 ZLM 返回的 /record/xxx.mp4 或 http://xxx）
     * @return 完整的文件访问 URL
     */
    public static String buildFileUrl(String host, int port, String url) {
        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
            return url;
        }
        String base = normalizeHost(host, port, "http");
        // url 可能以 / 开头，normalizeHost 结果不带尾部 /
        if (url != null && url.startsWith("/")) {
            return base + url;
        }
        return base + "/" + (url != null ? url : "");
    }

    /**
     * 构建 HTTP API 请求的 base URL。
     * <p>
     * 如果 host 已包含协议前缀（http:// 或 https://），则直接使用；
     * 否则自动添加 http:// 前缀。
     * 如果 host 已包含端口号（如 http://media.example.com:8080），则忽略 port 参数。
     *
     * @param host IP、域名或带协议的完整地址
     * @param port 端口号
     * @param path API 路径（如 index/api/getServerConfig）
     * @return 完整 URL
     */
    public static String buildApiUrl(String host, int port, String path) {
        String base = normalizeHost(host, port, "http");
        return base + "/" + path;
    }

    /**
     * 构建流播放地址。
     * <p>
     * 根据指定协议构建播放 URL，支持 rtsp、rtmp、http、https、ws、wss、rtsps 等。
     *
     * @param scheme 协议（http/https/ws/wss/rtsp/rtmp/rtsps）
     * @param host   IP、域名或带协议的完整地址
     * @param port   端口号
     * @param path   路径（如 live/stream.live.flv）
     * @return 完整播放 URL
     */
    public static String buildStreamUrl(String scheme, String host, Integer port, String path) {
        // 去除 host 上可能存在的协议前缀，因为流地址的协议由 scheme 参数决定
        String cleanHost = stripScheme(host);
        if (port != null && port > 0) {
            return String.format("%s://%s:%s/%s", scheme, cleanHost, port, path);
        }
        return String.format("%s://%s/%s", scheme, cleanHost, path);
    }

    /**
     * 规范化 host 地址：
     * - 已包含协议前缀：直接拼接端口（如果 host 中没有端口）
     * - 不包含协议前缀：添加 defaultScheme + host + port
     */
    private static String normalizeHost(String host, int port, String defaultScheme) {
        if (host == null) {
            return defaultScheme + "://localhost:" + port;
        }
        String trimmed = host.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            // 已有协议前缀，检查是否包含端口
            String afterScheme = trimmed.substring(trimmed.indexOf("://") + 3);
            if (afterScheme.contains(":")) {
                // 已包含端口，直接返回（去掉末尾斜杠）
                return stripTrailingSlash(trimmed);
            }
            // 没有端口，拼接上
            return stripTrailingSlash(trimmed) + ":" + port;
        }
        // 纯 IP 或域名
        return defaultScheme + "://" + trimmed + ":" + port;
    }

    /**
     * 去除协议前缀
     */
    private static String stripScheme(String host) {
        if (host == null) {
            return "localhost";
        }
        String trimmed = host.trim();
        if (trimmed.startsWith("https://")) {
            return trimmed.substring(8);
        }
        if (trimmed.startsWith("http://")) {
            return trimmed.substring(7);
        }
        return trimmed;
    }

    private static String stripTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
