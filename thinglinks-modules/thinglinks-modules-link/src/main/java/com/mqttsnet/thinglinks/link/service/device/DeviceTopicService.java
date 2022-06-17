package com.mqttsnet.thinglinks.link.service.device;

import com.mqttsnet.thinglinks.link.api.domain.device.entity.DeviceTopic;
import java.util.List;
    /**
* @Description: 设备Topic数据Service接口
* @Author: ShiHuan SUN
* @E-mail: 13733918655@163.com
* @Website: http://thinglinks.mqttsnet.com
* @CreateDate: 2022/6/15$ 15:22$
* @UpdateUser: ShiHuan SUN
* @UpdateDate: 2022/6/15$ 15:22$
* @UpdateRemark: 修改内容
* @Version: V1.0
*/
public interface DeviceTopicService{


    int deleteByPrimaryKey(Long id);

    int insert(DeviceTopic record);

    int insertOrUpdate(DeviceTopic record);

    int insertOrUpdateSelective(DeviceTopic record);

    int insertSelective(DeviceTopic record);

    DeviceTopic selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DeviceTopic record);

    int updateByPrimaryKey(DeviceTopic record);

    int updateBatch(List<DeviceTopic> list);

    int updateBatchSelective(List<DeviceTopic> list);

    int batchInsert(List<DeviceTopic> list);

        /**
         * 查询设备Topic数据
         *
         * @param id 设备Topic数据主键
         * @return 设备Topic数据
         */
        public DeviceTopic selectDeviceTopicById(Long id);

        /**
         * 查询设备Topic数据列表
         *
         * @param deviceTopic 设备Topic数据
         * @return 设备Topic数据集合
         */
        public List<DeviceTopic> selectDeviceTopicList(DeviceTopic deviceTopic);

        /**
         * 新增设备Topic数据
         *
         * @param deviceTopic 设备Topic数据
         * @return 结果
         */
        public int insertDeviceTopic(DeviceTopic deviceTopic);

        /**
         * 修改设备Topic数据
         *
         * @param deviceTopic 设备Topic数据
         * @return 结果
         */
        public int updateDeviceTopic(DeviceTopic deviceTopic);

        /**
         * 批量删除设备Topic数据
         *
         * @param ids 需要删除的设备Topic数据主键集合
         * @return 结果
         */
        public int deleteDeviceTopicByIds(Long[] ids);

        /**
         * 删除设备Topic数据信息
         *
         * @param id 设备Topic数据主键
         * @return 结果
         */
        public int deleteDeviceTopicById(Long id);

}
