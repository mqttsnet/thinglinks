package com.mqttsnet.thinglinks.tdengine.service.impl;

import com.mqttsnet.thinglinks.tdengine.mapper.SuperTableCreateOrUpdateMapper;
import com.mqttsnet.thinglinks.tdengine.service.SuperTableCreateOrUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: java类作用描述
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 21:51$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 21:51$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Service
@Slf4j
public class SuperTableCreateOrUpdateServiceImpl implements SuperTableCreateOrUpdateService {

    @Autowired
    private SuperTableCreateOrUpdateMapper superTableCreateOrUpdateMapper;


    @Override
    public void create(String msg) {
        //TODO 创建超级表逻辑处理
        superTableCreateOrUpdateMapper.createDB();
        superTableCreateOrUpdateMapper.createSuperTable();
    }


    @Override
    public void update(String msg) {

    }
}
