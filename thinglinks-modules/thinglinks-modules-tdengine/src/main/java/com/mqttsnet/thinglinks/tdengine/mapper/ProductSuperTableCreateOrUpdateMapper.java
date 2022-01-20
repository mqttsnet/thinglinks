package com.mqttsnet.thinglinks.tdengine.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * @Description: 超级表创建及修改持久层接口
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/12/26$ 22:17$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/12/26$ 22:17$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Mapper
public interface ProductSuperTableCreateOrUpdateMapper {

    void dropDB();

    void createDB();

    void createSuperTable();
}
