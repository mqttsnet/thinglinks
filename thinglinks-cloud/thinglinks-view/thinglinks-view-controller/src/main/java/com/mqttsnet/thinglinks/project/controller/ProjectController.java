package com.mqttsnet.thinglinks.project.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.project.entity.Project;
import com.mqttsnet.thinglinks.project.service.ProjectService;
import com.mqttsnet.thinglinks.project.vo.query.ProjectPageQuery;
import com.mqttsnet.thinglinks.project.vo.result.ProjectDetailsResultVO;
import com.mqttsnet.thinglinks.project.vo.result.ProjectResultVO;
import com.mqttsnet.thinglinks.project.vo.save.ProjectSaveVO;
import com.mqttsnet.thinglinks.project.vo.update.ProjectUpdateVO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/project")
@Tag(name = "可视化项目列表API")
public class ProjectController extends SuperController<ProjectService, Long, Project, ProjectSaveVO,
        ProjectUpdateVO, ProjectPageQuery, ProjectResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public QueryWrap<Project> handlerWrapper(Project model, PageParams<ProjectPageQuery> params) {
        QueryWrap<Project> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("view_project");
        return queryWrap;
    }

    /**
     * 保存 项目信息
     *
     * @param saveVO 保存参数
     * @return 实体 r
     */
    @Operation(summary = "保存项目", description = "保存项目信息")
    @PostMapping("/saveProject")
    @WebLog(value = "保存项目信息", request = false)
    public R<ProjectSaveVO> saveProject(@RequestBody ProjectSaveVO saveVO) {
        return R.success(superService.saveVO(saveVO));
    }

    /**
     * 修改 项目档案信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改项目档案", description = "修改项目档案")
    @PutMapping("/updateProject")
    @WebLog(value = "修改项目档案", request = false)
    public R<ProjectUpdateVO> updateProject(@RequestBody ProjectUpdateVO updateVO) {
        return R.success(superService.updateProject(updateVO));
    }


    /**
     * 删除项目
     *
     * @param id 项目ID
     * @return 删除结果
     */
    @Operation(summary = "删除项目", description = "根据项目ID删除项目")
    @Parameters({
            @Parameter(name = "id", description = "项目ID", required = true)
    })
    @DeleteMapping("/deleteProject/{id}")
    @WebLog(value = "删除项目", request = false)
    public R<Boolean> deleteProject(@PathVariable("id") Long id) {
        log.info("deleteProject id:{}", id);
        return R.success(superService.deleteProject(id));
    }


    /**
     * 修改项目发布状态
     *
     * @param id     项目ID
     * @param status 1:发布、-1:未发布
     * @return 更新结果
     */
    @Operation(summary = "修改项目发布状态", description = "根据项目ID修改项目发布状态")
    @Parameters({
            @Parameter(name = "id", description = "项目ID", required = true),
            @Parameter(name = "status", description = "新连接状态值（1:发布、-1:未发布）", required = true, example = "-1,1"),
    })
    @PutMapping("/updateProjectStatus/{id}")
    @WebLog(value = "修改项目连接状态", request = false)
    public R<Boolean> updateProjectStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        log.info("updateProjectStatus id:{}, status:{}", id, status);
        return R.success(superService.updateProjectStatus(id, status));
    }

    /**
     * 根据项目标识查询项目详情
     *
     * @param projectIdentification 项目标识
     * @return 项目详情
     */
    @Operation(summary = "根据项目标识查询项目详情", description = "根据项目标识获取项目详情")
    @Parameters({
            @Parameter(name = "projectIdentification", description = "项目标识", required = true)
    })
    @GetMapping("/getProjectDetails/{projectIdentification}")
    @WebLog(value = "根据项目标识查询项目详情", request = false)
    public R<ProjectDetailsResultVO> getProjectDetails(@PathVariable("projectIdentification") String projectIdentification) {
        log.info("getProjectDetails identification: {}", projectIdentification);
        ProjectDetailsResultVO result = superService.getProjectDetailsByProjectIdentification(projectIdentification);
        echoService.action(result);
        return R.success(result);
    }
}


