package com.mqttsnet.thinglinks.tdengine.service.impl;

import com.mqttsnet.thinglinks.common.core.constant.Constants;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.TableDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.FieldsVo;
import com.mqttsnet.thinglinks.tdengine.mapper.TdEngineMapper;
import com.mqttsnet.thinglinks.tdengine.service.TdEngineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @ClassDescription: TdEngine业务层的实现层
 * @ClassName: TdEngineServiceImpl
 * @Author: thinglinks
 * @Date: 2021-12-27 13:55:49
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class TdEngineServiceImpl implements TdEngineService {

    @Autowired
    private TdEngineMapper tdEngineMapper;
    @Autowired
    private RedisService redisService;

    @Override
    public void createDateBase(String dataBaseName) {
        this.tdEngineMapper.createDatabase(dataBaseName);
    }

    @Override
    public void createSuperTable(List<FieldsVo> schemaFields, List<FieldsVo> tagsFields, String dataBaseName, String superTableName) {
        this.tdEngineMapper.createSuperTable(schemaFields, tagsFields, dataBaseName, superTableName);
    }

    @Override
    public void createTable(TableDto tableDto) {
        this.tdEngineMapper.createTable(tableDto);
    }

    @Override
    public void insertData(TableDto tableDto) {
        this.tdEngineMapper.insertData(tableDto);
    }

    @Override
    public List<Map<String, Object>> selectByTimesTamp(SelectDto selectDto) {
        List<Map<String, Object>> maps = this.tdEngineMapper.selectByTimestamp(selectDto);
        for (Map<String, Object> map : maps) {
            Map<String, Object> filterMap = map.entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return maps;
    }

    @Override
    public void addColumnForSuperTable(String superTableName, FieldsVo fieldsVo) {
        this.tdEngineMapper.addColumnForSuperTable(superTableName, fieldsVo);
    }

    @Override
    public Long getCountByTimesTamp(SelectDto selectDto) {
        Map<String, Long> countMap = this.tdEngineMapper.getCountByTimestamp(selectDto);
        if (countMap == null) {
            return 0L;
        }
        Long count = countMap.get("count");
        return count;
    }

    @Override
    public void initSTableFrame() throws Exception {
        final Object cacheObject = redisService.getCacheObject(Constants.TDENGINE_SUPERTABLEFILELDS);
        if (StringUtils.isNull(cacheObject)) {
            log.info("The production model cache is empty");
        }
        List<Optional> optionalList = StringUtils.cast(cacheObject);
    }


}
