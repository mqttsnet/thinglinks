package com.mqttsnet.thinglinks.template.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.base.request.PageParams;
import com.mqttsnet.basic.database.mybatis.conditions.query.QueryWrap;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.datascope.DataScopeHelper;
import com.mqttsnet.thinglinks.template.entity.ProjectTemplate;
import com.mqttsnet.thinglinks.template.service.ProjectTemplateService;
import com.mqttsnet.thinglinks.template.vo.query.ProjectTemplatePageQuery;
import com.mqttsnet.thinglinks.template.vo.result.ProjectTemplateDetailsResultVO;
import com.mqttsnet.thinglinks.template.vo.result.ProjectTemplateResultVO;
import com.mqttsnet.thinglinks.template.vo.save.ProjectTemplateSaveVO;
import com.mqttsnet.thinglinks.template.vo.update.ProjectTemplateUpdateVO;
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

/**
 * <p>
 * 前端控制器
 * 可视化项目模板表
 * </p>
 *
 * @author mqttsnet
 * @date 2023-05-17 13:45:51
 * @create [2023-05-17 13:45:51] [mqttsnet]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/projectTemplate")
@Tag(name = "可视化项目模板API")
public class ProjectTemplateController extends SuperController<ProjectTemplateService, Long, ProjectTemplate, ProjectTemplateSaveVO,
        ProjectTemplateUpdateVO, ProjectTemplatePageQuery, ProjectTemplateResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Override
    public QueryWrap<ProjectTemplate> handlerWrapper(ProjectTemplate model, PageParams<ProjectTemplatePageQuery> params) {
        QueryWrap<ProjectTemplate> queryWrap = super.handlerWrapper(model, params);
        // 开启数据权限
        DataScopeHelper.startDataScope("view_project_template");
        return queryWrap;
    }



    /**
     * 保存项目信息
     *
     * @param saveVO 保存参数
     * @return 实体 r
     */
    @Operation(summary = "保存项目", description = "保存项目信息")
    @PostMapping("/saveTemplate")
    @WebLog(value = "保存项目信息", request = false)
    public R<ProjectTemplateSaveVO> saveTemplate(@RequestBody ProjectTemplateSaveVO saveVO) {
        return R.success(superService.saveVO(saveVO));
    }

    /**
     * 修改 项目档案信息表
     *
     * @param updateVO 更新参数
     * @return 实体
     */
    @Operation(summary = "修改项目档案", description = "修改项目档案")
    @PutMapping("/updateTemplate")
    @WebLog(value = "修改项目档案", request = false)
    public R<ProjectTemplateUpdateVO> updateProject(@RequestBody ProjectTemplateUpdateVO updateVO) {
        return R.success(superService.updateTemplate(updateVO));
    }


    /**
     * 删除项目模板
     *
     * @param id 项目ID
     * @return 删除结果
     */
    @Operation(summary = "删除项目", description = "根据项目ID删除项目")
    @DeleteMapping("/deleteTemplate/{id}")
    @WebLog(value = "删除项目", request = false)
    public R<Boolean> deleteTemplate(@Parameter(description = "项目模板ID", required = true) @PathVariable("id") Long id) {
        log.info("deleteTemplate id:{}", id);
        return R.success(superService.deleteTemplate(id));
    }


    /**
     * 修改项目模板状态
     *
     * @param id     项目ID
     * @param status 1:发布、-1:未发布
     * @return 更新结果
     */
    @Operation(summary = "修改项目发布状态", description = "根据项目ID修改项目发布状态")
    @Parameters({
            @Parameter(description = "项目ID", required = true),
            @Parameter(description = "新连接状态值（1:发布、-1:未发布）", required = true, example = "-1,1")
    })
    @PutMapping("/updateTemplateStatus/{id}")
    @WebLog(value = "修改项目连接状态", request = false)
    public R<Boolean> updateTemplateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        log.info("updateTemplateStatus id:{}, status:{}", id, status);
        return R.success(superService.updateTemplateStatus(id, status));
    }

    /**
     * 根据模版标识获取项目模版详情
     *
     * @param templateIdentification 模版标识
     * @return {@link ProjectTemplateDetailsResultVO} 模版详情
     */
    @Operation(summary = "根据模版标识获取项目模版详情", description = "根据模版标识查询项目模版详情")
    @Parameters({
            @Parameter(description = "模版标识", required = true)
    })
    @GetMapping("/getProjectTemplateDetails/{templateIdentification}")
    @WebLog(value = "根据模版标识获取项目模版详情", request = false)
    public R<ProjectTemplateDetailsResultVO> getProjectTemplateDetails(@PathVariable("templateIdentification") String templateIdentification) {
        log.info("getProjectTemplateDetails templateIdentification: {}", templateIdentification);
        ProjectTemplateDetailsResultVO result = superService.getProjectTemplateDetailsByTemplateIdentification(templateIdentification);
        echoService.action(result);
        return R.success(result);
    }

}


