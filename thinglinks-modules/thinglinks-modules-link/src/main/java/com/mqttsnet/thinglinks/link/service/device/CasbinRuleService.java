package com.mqttsnet.thinglinks.link.service.device;

import java.util.List;
import com.mqttsnet.thinglinks.link.api.domain.device.entity.CasbinRule;
    /**
* @Description: java类作用描述
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:22$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:22$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
public interface CasbinRuleService{


    int deleteByPrimaryKey(Integer id);

    int insert(CasbinRule record);

    int insertOrUpdate(CasbinRule record);

    int insertOrUpdateSelective(CasbinRule record);

    int insertSelective(CasbinRule record);

    CasbinRule selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(CasbinRule record);

    int updateByPrimaryKey(CasbinRule record);

    int updateBatch(List<CasbinRule> list);

    int updateBatchSelective(List<CasbinRule> list);

    int batchInsert(List<CasbinRule> list);

}
