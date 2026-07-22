package com.mqttsnet.thinglinks.nacos.api;


import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.constant.Constants;
import com.mqttsnet.thinglinks.nacos.vo.result.NacosInstanceResultVO;
import com.mqttsnet.thinglinks.nacos.vo.result.NacosListViewResultVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Naocs接口
 *
 * @author mqttsnet
 * @date 2024/11/13
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.base-server:thinglinks-base-server}", path = "/inner/nacos")
public interface NacosApi {

    @Operation(summary = "获取服务下的所有实例", description = "根据服务名称和分组名称查询指定分组下所有的服务实例信息。")
    @GetMapping("/getAllInstances")
    R<List<NacosInstanceResultVO>> getAllInstances(@RequestParam String serviceName, @RequestParam String groupName);


    @Operation(summary = "获取所有服务名称", description = "获取所有注册在 Nacos 中的服务名称。")
    @GetMapping("/getServicesOfServer")
    public R<NacosListViewResultVO<String>> getServicesOfServer(@RequestParam Integer pageNo, @RequestParam Integer pageSize);

}
