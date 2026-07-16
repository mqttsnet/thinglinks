package com.mqttsnet.thinglinks.file.controller;

import java.util.List;
import java.util.Map;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.file.service.FileService;
import com.mqttsnet.thinglinks.file.vo.result.FileResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 文件相关内部接口（inner）
 * </p>
 *
 * @author mqttsnet
 * @date 2021-06-30
 * @create [2021-06-30] [mqttsnet] [初始创建]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/inner/file")
@Tag(name = "inner-文件相关")
public class FileInnerController {
    private final FileService fileService;
    private final EchoService echoService;


    /**
     * 根据文件id，获取访问路径
     * 请求中 需要携带TenantId，但 不需要携带Token(不需要登录) 和 不需要验证uri权限
     *
     * @param ids 文件id
     */
    @Operation(summary = "根据文件id查询文件的临时访问路径", description = "根据文件id查询文件的临时访问路径")
    @PostMapping(value = "/findUrlById")
    @WebLog("根据文件id，获取文件临时的访问路径")
    public R<Map<Long, String>> findUrlById(@RequestBody List<Long> ids) {
        return R.success(fileService.findUrlById(ids));
    }

    /**
     * 根据文件id列表，获取文件详细信息
     * 请求中 需要携带TenantId，但 不需要携带Token(不需要登录) 和 不需要验证uri权限
     *
     * @param ids 文件id列表
     */
    @Operation(summary = "根据文件id列表查询文件详细信息", description = "根据文件id列表查询文件详细信息")
    @PostMapping(value = "/findInfoById")
    @WebLog("根据文件id列表，获取文件详细信息")
    public R<Map<Long, FileResultVO>> findInfoById(@RequestBody List<Long> ids) {
        Map<Long, FileResultVO> result = fileService.findByIds(ids);
        echoService.action(result.values());
        return R.success(result);
    }


}
