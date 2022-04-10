package com.mqttsnet.thinglinks.tdengine.controller;


import com.mqttsnet.thinglinks.common.core.web.controller.BaseController;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import com.mqttsnet.thinglinks.common.core.web.page.TableDataInfo;
import com.mqttsnet.thinglinks.tdengine.api.domain.IotSequential;
import com.mqttsnet.thinglinks.tdengine.service.IotSequentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/sequential")
@RestController
public class IotSequentialController extends BaseController {

    @Autowired
    private IotSequentialService iotSequentialService;


    /**
     * 查询时序列表
     * @param iotSequential
     * @return
     */
    @GetMapping("/getList")
    public TableDataInfo getList(@RequestBody IotSequential iotSequential){
        startPage();
        List<IotSequential> list = iotSequentialService.getList(iotSequential);
        return getDataTable(list);
    }

    /**
     * 根据时序时间查询详情
     * @param startTime
     * @return
     */
    @GetMapping("/selectByTime")
    public AjaxResult selectByTime(@PathVariable String startTime){
        IotSequential iotSequential = iotSequentialService.selectByTime(startTime);
        return AjaxResult.success(iotSequential);
    }

    /**
     * 添加
     * @param iotSequential
     * @return
     */
    @PostMapping("/save")
    public int save(@RequestBody IotSequential iotSequential){
        return iotSequentialService.save(iotSequential);
    }
}
