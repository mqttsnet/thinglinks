package com.mqttsnet.thinglinks.file.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.file.service.AppendixService;
import com.mqttsnet.thinglinks.file.service.FileService;
import com.mqttsnet.thinglinks.file.vo.result.FileResultVO;
import com.mqttsnet.thinglinks.model.vo.result.AppendixResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * 增量文件上传日志
 * </p>
 *
 * @author tangyh
 * @date 2021-06-30
 * @create [2021-06-30] [tangyh] [初始创建]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/anyone")
@Tag(name = "业务附件")
public class AppendixController {

    private final AppendixService appendixService;
    private final FileService fileService;
    private final EchoService echoService;

    /**
     * 根据业务id 和 业务类型附件信息
     *
     * @param bizId   业务id
     * @param bizType 业务类型
     */
    @Operation(summary = "根据业务id 和 业务类型查询附件信息", description = "根据业务id 和 业务类型查询文件信息")
    @PostMapping(value = "/appendix/listByBizId")
    @WebLog("根据业务id 和 业务类型查询附件信息")
    public R<List<AppendixResultVO>> listByBizId(@RequestParam Long bizId, @RequestParam(required = false) String bizType) {
        List<AppendixResultVO> result = appendixService.listByBizIdAndBizType(bizId, bizType);
        echoService.action(result);
        return R.success(result);
    }

    @Operation(summary = "根据业务id 和 业务类型查询文件信息", description = "根据业务id 和 业务类型查询文件信息")
    @PostMapping(value = "/appendix/listFileByBizId")
    @WebLog("根据业务id 和 业务类型查询附件信息")
    public R<List<FileResultVO>> listFileByBizId(@RequestParam Long bizId, @RequestParam(required = false) String bizType) {
        List<FileResultVO> result = fileService.listByBizIdAndBizType(bizId, bizType);
        echoService.action(result);
        return R.success(result);
    }

}
