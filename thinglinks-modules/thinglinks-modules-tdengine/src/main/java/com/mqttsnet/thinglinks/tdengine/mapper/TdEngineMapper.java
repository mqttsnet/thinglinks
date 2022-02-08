package com.mqttsnet.thinglinks.tdengine.mapper;

import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.FieldsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassDescription:
 * @ClassName: TdEngineMapper
 * @Author: thinglinks
 * @Date: 2021-12-27 14:52:34
 * @Version 1.0
 */
@Mapper
public interface TdEngineMapper {

    void createDatabase(@Param("databaseName") String databaseName);

    void createSuperTable(@Param("schemaFields") List<FieldsVo> schemaFields,
                          @Param("tagsFields") List<FieldsVo> tagsFields,
                          @Param("databaseName") String databaseName,
                          @Param("superTableName") String superTableName);

    void createTable(TableDto tableDto);

    void insertData(TableDto tableDto);

    List<Map<String, Object>> selectByTimestamp(SelectDto selectDto);

    void addColumnForSuperTable(@Param("superTableName") String superTableName,
                                @Param("fieldsVo") FieldsVo fieldsVo);
}
