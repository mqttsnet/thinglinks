package com.mqttsnet.thinglinks.file.storage;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.file.properties.FileServerProperties;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 本地上传配置
 *
 * @author mqttsnet
 * @date 2019/06/18
 */

@EnableConfigurationProperties(FileServerProperties.class)
@Configuration
@RequiredArgsConstructor
@Slf4j
public class FileAutoConfigure {
    private final FileServerProperties fileServerProperties;

    /**
     * 初始化minio客户端,不用每次都初始化
     *
     * https://www.minio.org.cn/docs/minio/linux/integrations/setup-nginx-proxy-with-minio.html
     *
     * @return MinioClient
     * @author mqttsnet
     */
    @Bean
    public MinioClient minioClient(FileServerProperties properties) {
        FileServerProperties.MinIo minIo = properties.getMinIo();
        MinioClient.Builder builder = new MinioClient.Builder()
                .endpoint(minIo.getEndpoint());
        // 未配置凭证时构建匿名客户端，保证未使用 MIN_IO 存储的服务可正常启动
        if (StrUtil.isAllNotBlank(minIo.getAccessKey(), minIo.getSecretKey())) {
            builder.credentials(minIo.getAccessKey(), minIo.getSecretKey());
        } else {
            log.warn("MinIO accessKey/secretKey 未配置，MIN_IO 存储策略不可用，如需使用请配置 thinglinks.file.minIo");
        }
        return builder.build();
    }

    /**
     * 华东机房,配置自己空间所在的区域
     */
    @Bean
    public com.qiniu.storage.Configuration qiNiuConfig() {
        FileServerProperties.QiNiu qiNiu = fileServerProperties.getQiNiu();
        return switch (qiNiu.getZone()) {
            case z1 -> new com.qiniu.storage.Configuration(Region.region1());
            case z2 -> new com.qiniu.storage.Configuration(Region.region2());
            case na0 -> new com.qiniu.storage.Configuration(Region.regionNa0());
            case as0 -> new com.qiniu.storage.Configuration(Region.regionAs0());
            default -> new com.qiniu.storage.Configuration(Region.region0());
        };
    }

    /**
     * 构建一个七牛上传工具实例
     */
    @Bean
    public UploadManager uploadManager() {
        return new UploadManager(qiNiuConfig());
    }

    /**
     * 认证信息实例
     *
     * @return
     */
    @Bean
    public Auth getQiniuAuth() {
        FileServerProperties.QiNiu qiNiu = fileServerProperties.getQiNiu();
        // Auth.create 对空 key 直接抛异常，未配置凭证时用占位值兜底，保证未使用 QINIU_OSS 存储的服务可正常启动
        if (!StrUtil.isAllNotBlank(qiNiu.getAccessKey(), qiNiu.getSecretKey())) {
            log.warn("七牛云 accessKey/secretKey 未配置，QINIU_OSS 存储策略不可用，如需使用请配置 thinglinks.file.qiNiu");
            return Auth.create("unconfigured", "unconfigured");
        }
        return Auth.create(qiNiu.getAccessKey(), qiNiu.getSecretKey());
    }

    /**
     * 构建七牛空间管理实例
     */
    @Bean
    public BucketManager bucketManager() {
        return new BucketManager(getQiniuAuth(), qiNiuConfig());
    }

}
