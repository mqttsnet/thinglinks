package com.mqttsnet.thinglinks.file.api;


import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.file.enumeration.FileStorageType;
import com.mqttsnet.thinglinks.file.vo.result.FileResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件接口
 *
 * @author zuihou
 * @date 2019/06/21
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.base-server:thinglinks-base-server}")
public interface FileApi {

    /**
     * 通过feign-form 实现文件 跨服务上传
     *
     * @param file        文件
     * @param bizType     业务类型
     * @param bucket      桶
     * @param storageType 存储类型
     * @return 文件信息
     */
    @PostMapping(value = "/anyone/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<FileResultVO> upload(
            @RequestPart(value = "file") MultipartFile file,
            @RequestParam(value = "bizType") String bizType,
            @RequestParam(value = "bucket", required = false) String bucket,
            @RequestParam(value = "storageType", required = false) FileStorageType storageType);

    /**
     * 根据文件id，获取访问路径
     * 请求中 需要携带TenantId，但 不需要携带Token(不需要登录) 和 不需要验证uri权限
     *
     * @param ids 文件id
     */
    @PostMapping(value = "/inner/file/findUrlById")
    R<Map<Long, String>> findUrlFromDefById(@RequestBody List<Long> ids);

    /**
     * 根据文件id列表，获取文件详细信息
     * 请求中 需要携带TenantId，但 不需要携带Token(不需要登录) 和 不需要验证uri权限
     *
     * @param ids 文件id列表
     */
    @PostMapping(value = "/inner/file/findInfoById")
    R<Map<Long, FileResultVO>> findInfoFromDefById(@RequestBody List<Long> ids);
}
