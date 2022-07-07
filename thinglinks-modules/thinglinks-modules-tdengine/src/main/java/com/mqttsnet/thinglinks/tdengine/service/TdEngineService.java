package com.mqttsnet.thinglinks.tdengine.service;


import com.mqttsnet.thinglinks.tdengine.api.domain.FieldsVo;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;

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
    void createDateBase(String dataBaseName) throws Exception;

    void createSuperTable(List<FieldsVo> schemaFields, List<FieldsVo> tagsFields, String dataBaseName, String superTableName) throws Exception;

    void createTable(TableDto tableDto) throws Exception;

    void insertData(TableDto tableDto) throws Exception;

    List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto) throws Exception;

    void addColumnForSuperTable(String superTableName, FieldsVo fieldsVo) throws Exception;

    void dropColumnForSuperTable(String superTableName, FieldsVo fieldsVo) throws Exception;

    Long getCountByTimesTamp(SelectDto selectDto) throws Exception;

    void initSTableFrame(String msg) throws Exception;

    List<Map<String, Object>> getLastData(SelectDto selectDto) throws Exception;
}
