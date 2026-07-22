package com.mqttsnet.thinglinks.card.controller.channel;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.card.entity.channel.CardChannelInfo;
import com.mqttsnet.thinglinks.card.service.channel.CardChannelInfoService;
import com.mqttsnet.thinglinks.card.vo.query.channel.CardChannelInfoPageQuery;
import com.mqttsnet.thinglinks.card.vo.result.channel.CardChannelInfoResultVO;
import com.mqttsnet.thinglinks.card.vo.save.channel.CardChannelInfoSaveVO;
import com.mqttsnet.thinglinks.card.vo.update.channel.CardChannelInfoUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * 物联卡渠道表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-06-26 21:55:13
 * @create [2024-06-26 21:55:13] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/cardChannelInfo")
@Tag(name = "物联卡渠道")
public class CardChannelInfoController extends SuperController<CardChannelInfoService, Long, CardChannelInfo, CardChannelInfoSaveVO, CardChannelInfoUpdateVO, CardChannelInfoPageQuery, CardChannelInfoResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    /**
     * 新增渠道信息
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "新增渠道信息", description = "新增渠道信息")
    @PostMapping("/saveChannelInfo")
    @WebLog(value = "新增渠道信息", request = false)
    public R<CardChannelInfoSaveVO> saveChannelInfo(@RequestBody @Valid CardChannelInfoSaveVO saveVO) {
        log.info("新增渠道信息，saveVO:{}", JsonUtil.toJson(saveVO));
        return R.success(superService.saveChannelInfo(saveVO));
    }

    /**
     * 修改渠道信息
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改渠道信息", description = "修改渠道信息")
    @PutMapping("/updateChannelInfo")
    @WebLog(value = "修改渠道信息", request = false)
    public R<CardChannelInfoUpdateVO> updateChannelInfo(@RequestBody @Valid CardChannelInfoUpdateVO updateVO) {
        log.info("修改渠道信息，updateVO:{}", JsonUtil.toJson(updateVO));
        return R.success(superService.updateChannelInfo(updateVO));
    }

    /**
     * 删除渠道信息
     *
     * @param id 渠道信息ID
     * @return 删除结果
     */
    @Operation(summary = "删除渠道信息", description = "根据渠道信息ID删除渠道信息")
    @DeleteMapping("/deleteChannelInfo/{id}")
    @WebLog(value = "删除渠道信息", request = false)
    @Parameters({@Parameter(name = "id", description = "渠道信息ID", required = true)})
    public R<Boolean> deleteChannelInfo(@PathVariable("id") Long id) {
        log.info("deleteChannelInfo id:{}", id);
        return R.success(superService.deleteChannelInfo(id));
    }

    /**
     * 批量删除渠道信息
     *
     * @param ids 渠道信息ID列表
     * @return 删除结果
     */
    @Operation(summary = "批量删除渠道信息", description = "根据渠道信息ID列表删除多个渠道信息")
    @DeleteMapping("/deleteChannelInfos")
    @WebLog(value = "批量删除渠道信息", request = false)
    public R<Boolean> deleteChannelInfos(@RequestBody List<Long> ids) {
        log.info("deleteChannelInfos ids:{}", ids);
        return R.success(superService.deleteChannelInfos(ids));
    }


    /**
     * 根据ID获取渠道详情
     *
     * @param id 渠道ID
     * @return {@link CardChannelInfoResultVO} 渠道详情
     */
    @Operation(summary = "根据ID获取渠道详情", description = "根据ID获取渠道详情")
    @GetMapping("/getChannelInfoDetails/{id}")
    @Parameters({@Parameter(name = "id", description = "渠道ID", required = true),})
    public R<CardChannelInfoResultVO> getChannelInfoDetails(@PathVariable("id") Long id) {
        log.info("获取渠道详情，id:{}", id);
        CardChannelInfoResultVO result = superService.getChannelInfoDetails(id);
        echoService.action(result);
        return R.success(result);
    }


}


