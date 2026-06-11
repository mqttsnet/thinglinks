package com.mqttsnet.thinglinks.video.controller.group;

import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.video.entity.group.VideoDeviceGroup;
import com.mqttsnet.thinglinks.video.service.group.VideoDeviceGroupService;
import com.mqttsnet.thinglinks.video.vo.query.group.VideoDeviceGroupPageQuery;
import com.mqttsnet.thinglinks.video.vo.result.group.VideoDeviceGroupResultVO;
import com.mqttsnet.thinglinks.video.vo.save.group.VideoDeviceGroupSaveVO;
import com.mqttsnet.thinglinks.video.vo.update.group.VideoDeviceGroupUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前端控制器 - 设备分组管理
 * <p>
 * 继承 {@link SuperController} 提供标准 CRUD + 分页，
 * 额外提供树形结构查询和子分组查询。
 *
 * @author mqttsnet
 * @date 2026-04-01
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/deviceGroup")
@Tag(name = "设备分组管理")
public class VideoDeviceGroupController extends SuperController<VideoDeviceGroupService, Long, VideoDeviceGroup,
        VideoDeviceGroupSaveVO, VideoDeviceGroupUpdateVO, VideoDeviceGroupPageQuery, VideoDeviceGroupResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<VideoDeviceGroup> handlerWrapper(VideoDeviceGroup model, PageParams<VideoDeviceGroupPageQuery> params) {
        QueryWrap<VideoDeviceGroup> queryWrap = super.handlerWrapper(model, params);
        DataScopeHelper.startDataScope("video_device_group");
        return queryWrap;
    }

    /**
     * 查询所有启用的分组（用于构建 Tree 组件数据源）
     */
    @GetMapping("/tree")
    @Operation(summary = "查询分组树", description = "获取所有启用的分组，用于构建 Tree 结构")
    public R<List<VideoDeviceGroupResultVO>> tree() {
        List<VideoDeviceGroupResultVO> treeList = superService.buildTree();
        echoService.action(treeList);
        return R.success(treeList);
    }

    /**
     * 根据父分组 ID 查询直属子分组列表
     */
    @GetMapping("/children")
    @Operation(summary = "查询子分组", description = "根据父分组ID获取子分组列表")
    public R<List<VideoDeviceGroup>> children(
            @Parameter(description = "父分组ID", required = true) @RequestParam Long parentId) {
        return R.success(superService.listByParentId(parentId));
    }
}
