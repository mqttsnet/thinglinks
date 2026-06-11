package com.mqttsnet.thinglinks.anyuser.controller;

import com.mqttsnet.basic.annotation.log.WebLog;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.base.controller.SuperController;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.interfaces.echo.EchoService;
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
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 项目信息anyUser接口
 * @author mqttsnet
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/anyUser/project")
@Tag(name = "anyUser-可视化项目列表API")
public class ProjectAnyUserController extends SuperController<ProjectService, Long, Project, ProjectSaveVO,
        ProjectUpdateVO, ProjectPageQuery, ProjectResultVO> {
    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    /**
     * 根据项目标识查询租户下项目详情
     *
     * @param tenantId              租户ID
     * @param projectIdentification 项目标识
     * @return 项目详情
     */
    @Operation(summary = "根据项目标识查询项目详情", description = "根据项目标识获取租户下的项目详情")
    @Parameters({
            @Parameter(name = "projectIdentification", description = "项目标识", required = true)
    })
    @GetMapping("/getProjectDetails/{tenantId}/{projectIdentification}")
    @WebLog(value = "根据项目标识查询项目详情", request = false)
    public R<ProjectDetailsResultVO> getProjectDetails(@PathVariable("tenantId") Long tenantId, @PathVariable("projectIdentification") @NotBlank String projectIdentification) {
        log.info("getProjectDetails.... Tenant ID: {}, identification:{}", tenantId, projectIdentification);
        ContextUtil.setTenantId(tenantId);
        ProjectDetailsResultVO result = superService.getProjectDetailsByProjectIdentification(projectIdentification);
        echoService.action(result);
        return R.success(result);
    }
}


