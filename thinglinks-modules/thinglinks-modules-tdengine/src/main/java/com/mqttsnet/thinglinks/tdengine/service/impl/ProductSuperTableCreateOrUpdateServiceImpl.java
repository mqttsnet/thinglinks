package com.mqttsnet.thinglinks.tdengine.service.impl;

import com.mqttsnet.thinglinks.tdengine.api.domain.model.ProductSuperTableModel;
import com.mqttsnet.thinglinks.tdengine.mapper.ProductSuperTableCreateOrUpdateMapper;
import com.mqttsnet.thinglinks.tdengine.service.ProductSuperTableCreateOrUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

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
public class ProductSuperTableCreateOrUpdateServiceImpl implements ProductSuperTableCreateOrUpdateService {

    @Autowired
    private ProductSuperTableCreateOrUpdateMapper productSuperTableCreateOrUpdateMapper;


    @Override
    public void createProductSuperTable(String msg) {
        //TODO 创建超级表逻辑处理
        productSuperTableCreateOrUpdateMapper.createDB();
        productSuperTableCreateOrUpdateMapper.createSuperTable();
        ProductSuperTableModel productSuperTableModel = new ProductSuperTableModel();
        //ts时间处理
        long ts = System.currentTimeMillis();
        long thirtySec = 1000 * 30;
        productSuperTableModel.setTs(new Timestamp(ts + (thirtySec)));

    }


    @Override
    public void updateProductSuperTable(String msg) {

    }
}
