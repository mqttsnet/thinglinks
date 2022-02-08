//package com.mqttsnet.thinglinks.tdengine.common;
//
//import com.mqttsnet.thinglinks.common.core.domain.R;
//import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
//import com.mqttsnet.thinglinks.tdengine.api.domain.Fields;
//import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDto;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @ClassDescription: 初始化表结构
// * @ClassName: InitTableFrame
// * @Author: thinglinks
// * @Date: 2021-12-31 10:52:18
// * @Version 1.0
// */
//@Component
//public class InitTableFrame {
//
//    private static final Logger log = LoggerFactory.getLogger(InitTableFrame.class);
//
//    /**
//     * 时序性数据库服务
//     */
//    @Autowired
//    private RemoteTdEngineService tdEngineService;
//
//    /**
//     * 服务信息mapper
//     */
//    @Autowired
//    private IotThingServiceInfoMapper thingServiceInfoMapper;
//
//    /**
//     * 服务属性信息mapper
//     */
//    @Autowired
//    private IotThingPropertyInfoMapper thingPropertyInfoMapper;
//
//    /**
//     * 设备信息mapper
//     */
//    @Autowired
//    private IotDeviceInfoMapper deviceInfoMapper;
//
//    @Autowired
//    private RedisService redisService;
//
//    /**
//     * 数据库名称
//     */
//    @Value("${tdEngine.databaseName}")
//    private String databaseName;
//
//    /**
//    *@MethodDescription 初始化数据库，超级表和子表
//    *@author thinglinks
//    *@Date 2022/1/10 9:42
//    */
//    public void initTableFrame() throws Exception {
//
//        //创建数据库
//        R<?> cdbResult = this.tdEngineService.createDataBase(databaseName);
//
//        //创建数据库报错，打印报错信息，并结束方法
//        if (cdbResult.getCode() != 200) {
//            log.error("Create Database Exception: " + cdbResult.getMsg());
//            return;
//        }
//
//        //获取服务id和设备模型id都不为空的服务对象列表
//        List<IotThingServiceInfo> serviceInfos
//                = this.thingServiceInfoMapper.getServiceExistProduct();
//
//        //根据服务对象列表循环创建超级表，子表
//        loop:
//        for (IotThingServiceInfo serviceInfo : serviceInfos) {
//            //构建超级表入参对象
//            SuperTableDto superTableDto = new SuperTableDto();
//
//            /*超级表名称命名规则（常量前缀加上设备模型id加上服务名称）：
//                "常量类的超级表名前缀" + “_” + “productId” + “_” + “serviceName"*/
//            String superTableName = TdEngineConstant.SUPER_TABLE_NAME_PREFIX +
//                    "_" + serviceInfo.getProductId() +
//                    "_" + serviceInfo.getServiceName();
//
//            //设置数据库名称和超级表名称
//            superTableDto.setDatabaseName(databaseName);
//            superTableDto.setSuperTableName(superTableName);
//
//            //根据服务id获取该服务下所有属性对象列表
//            List<IotThingPropertyInfo> propertyInfos
//                    = this.thingPropertyInfoMapper.getPropertyListByServiceId(serviceInfo.getServiceId());
//
//            //如果服务下属性值为空，没必要为该服务创建超级表，跳过该循环，进入下个服务
//            if (propertyInfos.isEmpty()) {
//                continue loop;
//            }
//
//            //构建超级表的表结构字段列表
//            List<Fields> schemaFields = new ArrayList<>();
//            //超级表第一个字段数据类型必须为时间戳
//            Fields firstColumn = new Fields();
//            firstColumn.setFieldName("eventTime" + TdEngineConstant.FIELD_NAME_SUFFIX);
//            firstColumn.setDataType(DataTypeEnum.TIMESTAMP);
//            schemaFields.add(firstColumn);
//
//            //根据属性对象列表循环构建超级表表结构
//            for (IotThingPropertyInfo propertyInfo : propertyInfos) {
//                //获取字段名称
//                String filedName = propertyInfo.getName() + TdEngineConstant.FIELD_NAME_SUFFIX;
//                //获取该属性数据类型
//                String datatype = propertyInfo.getDatatype();
//                //获取该属性的数据大小
//                Integer size = propertyInfo.getMaxlength();
//                Fields fields = new Fields(filedName, datatype, size);
//                //添加超级表表结构字段
//                schemaFields.add(fields);
//            }
//
//            //构建超级表标签字段列表
//            //根据业务逻辑，将超级表的标签字段定为设备的客户端Id：clientId
//            List<Fields> tagsFields = new ArrayList<>();
//            Fields tags = new Fields();
//            tags.setFieldName("clientId");
//            tags.setDataType(DataTypeEnum.BINARY);
//            tags.setSize(64);
//            tagsFields.add(tags);
//
//            //设置超级表表结构列表
//            superTableDto.setSchemaFields(schemaFields);
//            //设置超级表标签字段列表
//            superTableDto.setTagsFields(tagsFields);
//            //调用方法创建超级表
//            R<?> cstResult = this.tdEngineService.createSuperTable(superTableDto);
//            //创建超级表报错，打印报错信息，并跳过该循环，继续为下个服务创建表
//            if (cstResult.getCode() != 200) {
//                log.error("Create SuperTable Exception: " + cstResult.getMsg());
//                continue loop;
//            }
//            //将之前存在redis里的同样的名称的超级表的表结构信息删除
//            if (redisService.hasKey(superTableName)) {
//                redisService.deleteObject(superTableName);
//            }
//            //在redis里存入新的超级表对的表结构信息
//            redisService.setCacheList(superTableName, schemaFields);
//
//            //根据设备模型id查询该设备模型下所有设备对象列表
//            List<IotDeviceInfo> devices
//                    = this.deviceInfoMapper.getDeviceListByProductId((long) serviceInfo.getProductId());
//            //根据设备对象列表创建子表
//            for (IotDeviceInfo device : devices) {
//                TableDto tableDto = new TableDto();
//                //为设备的客户端id加上服务名称
//                //子表名称命名规则(客户端id加上服务名称)：“clientId” + “serviceName"
//                tableDto.setTableName(device.getClientId() + serviceInfo.getServiceName());
//                //设置数据库名称
//                tableDto.setDatabaseName(databaseName);
//                //设置超级表名称
//                tableDto.setSuperTableName(superTableName);
//                List<Fields> tagsFieldValues = new ArrayList<>();
//                Fields fieldValue = new Fields();
//                //设置标签字段的值
//                fieldValue.setFieldValue(device.getClientId());
//                tagsFieldValues.add(fieldValue);
//                tableDto.setTagsFieldValues(tagsFieldValues);
//                //创建子表报错，打印报错信息，不做其他操作
//                R<?> ctResult = this.tdEngineService.createTable(tableDto);
//                if (ctResult.getCode() != 200) {
//                    log.error("Create Table Exception: " + ctResult.getMsg());
//                }
//            }
//        }
//    }
//}
