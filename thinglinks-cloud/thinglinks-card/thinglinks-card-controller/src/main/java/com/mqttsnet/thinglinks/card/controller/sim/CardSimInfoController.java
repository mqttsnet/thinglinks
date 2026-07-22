package com.mqttsnet.thinglinks.card.controller.sim;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.basic.jackson.JsonUtil;
import com.mqttsnet.thinglinks.card.entity.sim.CardSimInfo;
import com.mqttsnet.thinglinks.card.service.sim.CardSimInfoService;
import com.mqttsnet.thinglinks.card.vo.query.sim.CardSimInfoPageQuery;
import com.mqttsnet.thinglinks.card.vo.result.sim.CardSimInfoResultVO;
import com.mqttsnet.thinglinks.card.vo.result.sim.CardSimOverviewResultVO;
import com.mqttsnet.thinglinks.card.vo.save.sim.CardSimInfoSaveVO;
import com.mqttsnet.thinglinks.card.vo.update.sim.CardSimInfoUpdateVO;
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
 * 物联网卡信息表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-06-26 23:45:39
 * @create [2024-06-26 23:45:39] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/cardSimInfo")
@Tag(name = "物联网卡信息")
public class CardSimInfoController extends SuperController<CardSimInfoService, Long, CardSimInfo, CardSimInfoSaveVO,
        CardSimInfoUpdateVO, CardSimInfoPageQuery, CardSimInfoResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    /**
     * 新增物联网卡信息
     *
     * @param saveVO 保存参数
     * @return {@link CardSimInfoSaveVO} 保存数据
     */
    @Operation(summary = "新增物联网卡信息", description = "新增物联网卡信息")
    @PostMapping("/saveCardSimInfo")
    @WebLog(value = "新增物联网卡信息", request = false)
    public R<CardSimInfoSaveVO> saveCardSimInfo(@RequestBody @Valid CardSimInfoSaveVO saveVO) {
        log.info("新增物联网卡信息，saveVO:{}", JsonUtil.toJson(saveVO));
        return R.success(superService.saveCardSimInfo(saveVO));
    }

    /**
     * 更新物联网卡信息
     *
     * @param updateVO 更新参数
     * @return {@link CardSimInfoUpdateVO} 更新数据
     */
    @Operation(summary = "更新物联网卡信息", description = "更新物联网卡信息")
    @PutMapping("/updateCardSimInfo")
    @WebLog(value = "更新物联网卡信息", request = false)
    public R<CardSimInfoUpdateVO> updateCardSimInfo(@RequestBody @Valid CardSimInfoUpdateVO updateVO) {
        log.info("更新物联网卡信息，updateVO:{}", JsonUtil.toJson(updateVO));
        return R.success(superService.updateCardSimInfo(updateVO));
    }

    /**
     * 根据ID获取物联网卡详情
     *
     * @param id 物联网卡ID
     * @return {@link CardSimInfoResultVO} 物联网卡详情
     */
    @Operation(summary = "根据ID获取物联网卡详情", description = "根据ID获取物联网卡详情")
    @GetMapping("/getCardSimInfoDetails/{id}")
    @Parameters({@Parameter(name = "id", description = "ID", required = true),})
    public R<CardSimInfoResultVO> getCardSimInfoDetails(@PathVariable("id") Long id) {
        log.info("获取物联网卡详情，id:{}", id);
        CardSimInfoResultVO result = superService.getCardSimInfoDetails(id);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 删除物联网卡信息
     *
     * @param id 物联网卡ID
     * @return {@link Boolean} 是否删除成功
     */
    @Operation(summary = "删除物联网卡信息", description = "根据ID删除物联网卡信息")
    @DeleteMapping("/deleteCardSimInfo/{id}")
    @WebLog(value = "删除物联网卡信息", request = false)
    @Parameters({@Parameter(name = "id", description = "物联网卡ID", required = true)})
    public R<Boolean> delete(@PathVariable("id") Long id) {
        log.info("删除物联网卡信息，id:{}", id);
        return R.success(superService.deleteCardSimInfo(id));
    }

    /**
     * 批量删除物联网卡信息
     *
     * @param ids 物联网卡ID集合
     * @return {@link Boolean} 是否删除成功
     */
    @Operation(summary = "批量删除物联网卡信息", description = "根据ID集合批量删除物联网卡信息")
    @DeleteMapping("/deleteBatchCardSimInfo")
    @WebLog(value = "批量删除物联网卡信息", request = false)
    public R<Boolean> deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量删除物联网卡信息，ids:{}", ids);
        return R.success(ids.stream().distinct().allMatch(id -> superService.deleteCardSimInfo(id)));
    }

    /**
     * 获取物联网卡概况统计信息
     *
     * @return 物联网卡概况统计信息
     */
    @Operation(summary = "获取物联网卡概况统计信息", description = "统计物联网卡的概况信息")
    @GetMapping("/cardSimOverview")
    public R<CardSimOverviewResultVO> getCardSimOverview() {
        CardSimOverviewResultVO cardSimOverview = superService.getCardSimOverview();
        return R.success(cardSimOverview);
    }

}


