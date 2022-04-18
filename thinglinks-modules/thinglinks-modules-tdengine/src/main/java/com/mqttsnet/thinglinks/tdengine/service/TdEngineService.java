package com.mqttsnet.thinglinks.tdengine.service;


import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.FieldsVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @InterfaceDescription: TdEngine业务层
 * @InterfaceName: ITdEngineService
 * @Author: thinglinks
 * @Date: 2021-12-27 13:54:58
 * @Version 1.0
 */
public interface TdEngineService {
    void createDateBase(String dataBaseName);

    void createSuperTable(List<FieldsVo> schemaFields, List<FieldsVo> tagsFields, String dataBaseName, String superTableName);

    void createTable(TableDto tableDto);

    void insertData(TableDto tableDto);

    List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto);

    void addColumnForSuperTable(String superTableName, FieldsVo fieldsVo);

    Long getCountByTimesTamp(SelectDto selectDto);

    void initSTableFrame(String msg) throws Exception;
}
