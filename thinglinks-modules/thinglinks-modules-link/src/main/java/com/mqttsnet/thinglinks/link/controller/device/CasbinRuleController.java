package com.mqttsnet.thinglinks.link.controller.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.CasbinRule;
import com.mqttsnet.thinglinks.link.service.device.CasbinRuleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * (thinglinks.casbin_rule)表控制层
 *
 * @author xxxxx
 */
@RestController
@RequestMapping("/thinglinks.casbin_rule")
public class CasbinRuleController {
    /**
     * 服务对象
     */
    @Resource
    private CasbinRuleService casbinRuleService;

    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @GetMapping("selectOne")
    public CasbinRule selectOne(Integer id) {
        return casbinRuleService.selectByPrimaryKey(id);
    }

}
