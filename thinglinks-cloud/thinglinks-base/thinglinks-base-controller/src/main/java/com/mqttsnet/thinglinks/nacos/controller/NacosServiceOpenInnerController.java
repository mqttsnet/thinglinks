package com.mqttsnet.thinglinks.nacos.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.thinglinks.nacos.NacosService;
import com.mqttsnet.thinglinks.nacos.vo.result.NacosInstanceResultVO;
import com.mqttsnet.thinglinks.nacos.vo.result.NacosListViewResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * -----------------------------------------------------------------------------
 * File Name: NacosServiceOpenInnerController
 * -----------------------------------------------------------------------------
 * Description:
 * Nacos Naming 服务控制器
 * 提供了与 Nacos 命名服务相关的 RESTful API，支持实例的注册、注销、查询等操作。
 * -----------------------------------------------------------------------------
 *
 * @author xiaonannet
 * @version 1.0
 * -----------------------------------------------------------------------------
 * Revision History:
 * Date         Author          Version     Description
 * --------      --------     -------   --------------------
 * 2024/11/13       xiaonannet        1.0        Initial creation
 * -----------------------------------------------------------------------------
 * @email
 * @date 2024/11/13 17:05
 */

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/inner/nacos")
@Tag(name = "inner-Nacos服务API")
public class NacosServiceOpenInnerController {


    @Autowired
    private NacosService nacosService;

    /**
     * 注册服务实例
     *
     * @param serviceName 服务名称
     * @param ip          实例的 IP 地址
     * @param port        实例的端口
     * @throws NacosException 如果出现 Nacos 相关异常
     */
    @Operation(summary = "注册服务实例", description = "根据服务名称、实例 IP 和端口号注册一个新的服务实例。")
    @PostMapping("/registerInstance")
    public void registerInstance(
            @Parameter(description = "服务名称", required = true, example = "example-service")
            @RequestParam String serviceName,

            @Parameter(description = "实例 IP 地址", required = true, example = "192.168.1.1")
            @RequestParam String ip,

            @Parameter(description = "实例端口", required = true, example = "8080")
            @RequestParam int port
    ) throws NacosException {
        log.info("注册服务实例 - serviceName: {}, ip: {}, port: {}", serviceName, ip, port);
        nacosService.registerInstance(serviceName, ip, port);
    }

    /**
     * 获取指定服务下所有实例
     *
     * @param serviceName 服务名称
     * @param groupName   服务分组名称
     * @return 所有实例的列表
     * @throws NacosException 如果出现 Nacos 相关异常
     */
    @Operation(summary = "获取服务下的所有实例", description = "根据服务名称和分组名称查询指定分组下所有的服务实例信息。")
    @GetMapping("/getAllInstances")
    public R<List<NacosInstanceResultVO>> getAllInstances(
            @Parameter(description = "服务名称", required = true, example = "example-service")
            @RequestParam String serviceName,

            @Parameter(description = "服务分组名称", required = true, example = "DEFAULT_GROUP")
            @RequestParam String groupName) {
        log.info("查询服务实例信息 - serviceName: {}, groupName: {}", serviceName, groupName);
        try {
            return R.success(nacosService.getAllInstances(serviceName, groupName));
        } catch (NacosException e) {
            log.error("查询服务实例信息失败", e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 批量注册多个服务实例
     *
     * @param serviceName 服务名称
     * @param groupName   服务分组名称
     * @param instances   实例列表
     * @throws NacosException 如果出现 Nacos 相关异常
     */
    @Operation(summary = "批量注册多个实例", description = "根据服务名称和分组名称批量注册多个服务实例。")
    @PostMapping("/batchRegisterInstances")
    public void batchRegisterInstances(
            @Parameter(description = "服务名称", required = true, example = "example-service")
            @RequestParam String serviceName,

            @Parameter(description = "服务分组名称", required = true, example = "DEFAULT_GROUP")
            @RequestParam String groupName,

            @Parameter(description = "实例列表", required = true)
            @RequestBody List<Instance> instances
    ) throws NacosException {
        log.info("批量注册服务实例 - serviceName: {}, groupName: {}, instancesCount: {}", serviceName, groupName, instances.size());
        nacosService.batchRegisterInstance(serviceName, groupName, instances);
    }

    /**
     * 注销服务实例
     *
     * @param serviceName 服务名称
     * @param ip          实例 IP 地址
     * @param port        实例端口
     * @throws NacosException 如果出现 Nacos 相关异常
     */
    @Operation(summary = "注销服务实例", description = "根据服务名称、实例 IP 和端口号注销指定服务实例。")
    @DeleteMapping("/deregisterInstance")
    public void deregisterInstance(
            @Parameter(description = "服务名称", required = true, example = "example-service")
            @RequestParam String serviceName,

            @Parameter(description = "实例 IP 地址", required = true, example = "192.168.1.1")
            @RequestParam String ip,

            @Parameter(description = "实例端口", required = true, example = "8080")
            @RequestParam int port
    ) throws NacosException {
        log.info("注销服务实例 - serviceName: {}, ip: {}, port: {}", serviceName, ip, port);
        nacosService.deregisterInstance(serviceName, ip, port);
    }

    /**
     * 获取所有服务名称
     *
     * @param pageNo   页码
     * @param pageSize 每页大小
     * @return 服务名称列表
     * @throws NacosException 如果出现 Nacos 相关异常
     */
    @Operation(summary = "获取所有服务名称", description = "获取所有注册在 Nacos 中的服务名称。")
    @GetMapping("/getServicesOfServer")
    public R<NacosListViewResultVO<String>> getServicesOfServer(
            @Parameter(description = "页码", required = true, example = "1")
            @RequestParam int pageNo,

            @Parameter(description = "每页大小", required = true, example = "20")
            @RequestParam int pageSize) {
        log.info("获取所有服务名称 - pageNo: {}, pageSize: {}", pageNo, pageSize);
        try {
            return R.success(nacosService.getServicesOfServer(pageNo, pageSize));
        } catch (NacosException e) {
            log.error("获取服务名称失败", e);
            return R.fail(e.getMessage());
        }
    }

}
