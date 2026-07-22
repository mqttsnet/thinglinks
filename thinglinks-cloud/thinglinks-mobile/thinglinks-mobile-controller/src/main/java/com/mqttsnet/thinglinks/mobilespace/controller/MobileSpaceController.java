package com.mqttsnet.thinglinks.mobilespace.controller;

import com.alibaba.fastjson2.JSON;
import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.mobile.mobilespace.entity.MobileSpace;
import com.mqttsnet.thinglinks.mobile.mobilespace.service.MobileSpaceService;
import com.mqttsnet.thinglinks.mobile.mobilespace.vo.query.MobileSpacePageQuery;
import com.mqttsnet.thinglinks.mobile.mobilespace.vo.result.MobileSpaceDetailsResultVO;
import com.mqttsnet.thinglinks.mobile.mobilespace.vo.result.MobileSpaceResultVO;
import com.mqttsnet.thinglinks.mobile.mobilespace.vo.save.MobileSpaceSaveVO;
import com.mqttsnet.thinglinks.mobile.mobilespace.vo.update.MobileSpaceUpdateVO;
import com.mqttsnet.thinglinks.mobile.mobilespacedevice.vo.save.MobileSpaceDeviceSaveVO;
import com.mqttsnet.thinglinks.mobile.mobilespacemember.vo.save.MobileSpaceMemberSaveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 移动端-空间表
 * </p>
 *
 * @author mqttsnet
 * @date 2024-08-29 10:17:46
 * @create [2024-08-29 10:17:46] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/mobileSpace")
@Tag(name = "移动端-空间")
public class MobileSpaceController extends SuperController<MobileSpaceService, Long, MobileSpace, MobileSpaceSaveVO,
        MobileSpaceUpdateVO, MobileSpacePageQuery, MobileSpaceResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<MobileSpace> handlerWrapper(MobileSpace model, PageParams<MobileSpacePageQuery> params) {
        QueryWrap<MobileSpace> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("mobile_space");
        return queryWrap;
    }

    /**
     * 新增空间
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "新增空间", description = "新增空间")
    @PostMapping("/saveMobileSpace")
    @WebLog(value = "新增空间", request = false)
    public R<MobileSpaceSaveVO> saveMobileSpace(@RequestBody MobileSpaceSaveVO saveVO) {
        log.info("saveMobileSpace param:{}", JSON.toJSONString(saveVO));
        return R.success(superService.saveMobileSpace(saveVO));
    }

    /**
     * 更新空间
     */
    @Operation(summary = "更新空间", description = "更新空间")
    @PutMapping("/updateMobileSpace")
    @WebLog(value = "更新空间", request = false)
    public R<MobileSpaceUpdateVO> updateMobileSpace(@RequestBody MobileSpaceUpdateVO updateVO) {
        log.info("updateMobileSpace param:{}", JSON.toJSONString(updateVO));
        return R.success(superService.updateMobileSpace(updateVO));
    }


    /**
     * 删除空间(解绑)
     */
    @Operation(summary = "删除空间", description = "删除空间(解绑)")
    @DeleteMapping("/deleteMobileSpaceById/{id}")
    @WebLog(value = "删除空间", request = false)
    @Parameters({@Parameter(name = "id", description = "空间ID", required = true)})
    public R<Boolean> deleteMobileSpaceById(@PathVariable("id") Long id) {
        return R.success(superService.deleteMobileSpace(id, ContextUtil.getUserId()));
    }


    @Operation(summary = "获取空间详情信息", description = "获取空间详情信息(空间信息+产品信息+设备信息)")
    @GetMapping("/getMobileSpaceById/{id}")
    @WebLog(value = "获取空间详情信息", request = false)
    @Parameters({@Parameter(name = "id", description = "空间ID", required = true)})
    public R<MobileSpaceDetailsResultVO> getMobileSpaceById(@PathVariable("id") Long id) {
        MobileSpaceDetailsResultVO result = superService.getMobileSpaceById(id);
        echoService.action(result);
        return R.success(result);
    }

    @Operation(summary = "获取我的空间列表", description = "获取我的空间列表")
    @GetMapping("/getMobileSpaceListForMine")
    @WebLog(value = "获取我的空间列表", request = false)
    public R<List<MobileSpaceResultVO>> getMobileSpaceListForMine() {
        MobileSpacePageQuery query = MobileSpacePageQuery.builder().createdUserId(ContextUtil.getUserId()).build();
        List<MobileSpaceResultVO> result = superService.getMobileSpaceResultVOList(query);
        echoService.action(result);
        return R.success(result);
    }

    /**
     * 空间与人员绑定
     *
     * @param saveVO 保存参数
     * @return 实体
     */
    @Operation(summary = "新增空间与人员绑定", description = "新增空间与人员绑定")
    @PostMapping("/saveMobileSpaceMember")
    @WebLog(value = "新增空间与人员绑定", request = false)
    public R<MobileSpaceMemberSaveVO> saveMobileSpaceMember(@RequestBody MobileSpaceMemberSaveVO saveVO) {
        return R.success(superService.saveMobileSpaceMember(saveVO));
    }

    /**
     * 空间绑定设备
     */
    @Operation(summary = "空间绑定设备", description = "空间绑定设备")
    @PutMapping("/updateMobileSpaceDevice")
    @WebLog(value = "空间绑定设备", request = false)
    public R<MobileSpaceDeviceSaveVO> updateMobileSpaceDevice(@RequestBody MobileSpaceDeviceSaveVO saveVO) {
        return R.success(superService.updateMobileSpaceDevice(saveVO));
    }

}
