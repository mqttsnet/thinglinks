package com.mqttsnet.thinglinks.tdengine.service;

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
public interface SuperTableCreateOrUpdateService {

    /**
     * @Author: ShiHuan Sun
     * @E-mail: 13733918655@163.com
     * @Description: 创建超级表
     * @CreateDate: 2021/12/26 22:05
     * @Version: V1.0
     * @Param:
     * msg 产品模型信息
     * @return
     */

    void create(String msg);

    /**
     * @Author: ShiHuan Sun
     * @E-mail: 13733918655@163.com
     * @Description: 修改超级表
     * @CreateDate: 2021/12/26 22:05
     * @Version: V1.0
     * @Param:
     * msg 产品模型信息
     * @return
     */
    void update(String msg);
}
