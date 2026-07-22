package com.mqttsnet.thinglinks.system.manager.system.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baidu.fsg.uid.UidGenerator;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.mqttsnet.basic.base.manager.impl.SuperManagerImpl;
import com.mqttsnet.basic.cache.redis2.CacheResult;
import com.mqttsnet.basic.cache.repository.CachePlusOps;
import com.mqttsnet.basic.database.mybatis.conditions.Wraps;
import com.mqttsnet.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.mqttsnet.basic.echo.properties.EchoProperties;
import com.mqttsnet.basic.model.cache.CacheHashKey;
import com.mqttsnet.basic.model.cache.CacheKey;
import com.mqttsnet.basic.utils.CollHelper;
import com.mqttsnet.thinglinks.common.cache.tenant.base.DictCacheKeyBuilder;
import com.mqttsnet.thinglinks.common.constant.DefValConstants;
import com.mqttsnet.thinglinks.common.constant.DsConstant;
import com.mqttsnet.thinglinks.model.enumeration.system.DictClassifyEnum;
import com.mqttsnet.thinglinks.model.enumeration.system.DictDataTypeEnum;
import com.mqttsnet.thinglinks.model.vo.result.Option;
import com.mqttsnet.thinglinks.system.entity.system.DefDict;
import com.mqttsnet.thinglinks.system.manager.system.DefDictManager;
import com.mqttsnet.thinglinks.system.mapper.system.DefDictMapper;
import com.mqttsnet.thinglinks.system.vo.result.system.DefDictItemResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author mqttsnet
 * @date 2021/10/10 23:21
 */
@RequiredArgsConstructor
@Service
public class DefDictManagerImpl extends SuperManagerImpl<DefDictMapper, DefDict> implements DefDictManager {

    private final DefDictMapper defDictMapper;
    private final CachePlusOps cachePlusOps;
    private final EchoProperties ips;
    private final UidGenerator uidGenerator;

    @Override
    // 将枚举数据，同步存入字典表
    public void syncEnumToDict(Map<Option, List<Option>> ennumMap) {
        int sort = 1000;
        for (Map.Entry<Option, List<Option>> entry : ennumMap.entrySet()) {
            Option type = entry.getKey();

            boolean save = true;
            DefDict dict = super.getOne(Wraps.<DefDict>lbQ().eq(DefDict::getKey, type.getValue()).eq(DefDict::getParentId, DefValConstants.PARENT_ID));
            if (dict == null) {
                dict = new DefDict();
                dict.setClassify(DictClassifyEnum.SYSTEM.getCode());
                dict.setParentId(DefValConstants.PARENT_ID);
                dict.setKey(type.getValue());
                dict.setName(type.getLabel());
                dict.setState(true);
                dict.setSortValue(sort++);
                dict.setDataType(DictDataTypeEnum.STRING.getCode());
                dict.setId(uidGenerator.getUid());
            } else {
                save = false;
                dict.setName(type.getLabel());
            }

            List<Option> optionList = entry.getValue();

            List<DefDict> itemList = new ArrayList<>();
            int i = 1;
            for (Option option : optionList) {
                if (save) {
                    DefDict item = new DefDict();
                    item.setParentId(dict.getId())
                            .setParentKey(dict.getKey())
                            .setClassify(DictClassifyEnum.SYSTEM.getCode())
                            .setKey(option.getValue())
                            .setName(option.getLabel())
                            .setState(true)
                            .setPropType(type.getColor())
                            .setDataType(type.getRemark())
                            .setSortValue(dict.getSortValue() + i++)
                            .setCssClass(option.getColor());
                    itemList.add(item);
                } else {
                    DefDict item = super.getOne(Wraps.<DefDict>lbQ().eq(DefDict::getKey, option.getValue()).eq(DefDict::getParentId, dict.getId()));

                    if (item == null) {
                        item = new DefDict();
                        item.setParentId(dict.getId())
                                .setParentKey(dict.getKey())
                                .setClassify(DictClassifyEnum.SYSTEM.getCode())
                                .setKey(option.getValue())
                                .setName(option.getLabel())
                                .setPropType(type.getColor())
                                .setDataType(type.getRemark())
                                .setState(true)
                                .setSortValue(dict.getSortValue() + i++)
                                .setCssClass(option.getColor());
                        itemList.add(item);
                    }
                }
            }

            if (save) {
                save(dict);
            } else {
                updateById(dict);
            }
            saveBatch(itemList);
        }
    }

    @Override
    @DS(DsConstant.DEFAULTS)
    public Map<Serializable, DefDict> findByIds(Set<Serializable> dictKeys) {
        if (dictKeys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Serializable, DefDict> codeValueMap = MapUtil.newHashMap();
        dictKeys.forEach(dictKey -> {
            Function<CacheKey, Map<String, DefDict>> fun = ck -> {
                LbQueryWrap<DefDict> wrap = Wraps.<DefDict>lbQ().eq(DefDict::getParentKey, dictKey);
                List<DefDict> list = defDictMapper.selectList(wrap);

                if (CollUtil.isNotEmpty(list)) {
                    return CollHelper.uniqueIndex(list, DefDict::getKey, item -> item);
                } else {
                    return MapBuilder.<String, DefDict>create().put(DefValConstants.DICT_NULL_VAL_KEY, new DefDict()).build();
                }
            };
            Map<String, CacheResult<DefDict>> map = cachePlusOps.hGetAll(DictCacheKeyBuilder.builder(dictKey), fun);
            map.forEach((itemKey, itemName) -> {
                if (!DefValConstants.DICT_NULL_VAL_KEY.equals(itemKey)) {
                    codeValueMap.put(StrUtil.join(ips.getDictSeparator(), dictKey, itemKey), itemName.getValue());
                }
            });
        });
        return codeValueMap;
    }


    @Override
    @DS(DsConstant.DEFAULTS)
    public Map<String, List<DefDictItemResultVO>> findDictMapItemListByKey(List<String> dictKeys) {
        if (CollUtil.isEmpty(dictKeys)) {
            return Collections.emptyMap();
        }
        // 仅返回启用项 ── 业务下拉框消费方,禁用项不应再可选;字典管理后台走分页 API 不受影响
        LbQueryWrap<DefDict> query = Wraps.<DefDict>lbQ()
                .in(DefDict::getParentKey, dictKeys)
                .eq(DefDict::getState, Boolean.TRUE)
                .orderByAsc(DefDict::getSortValue);
        List<DefDict> list = super.list(query);
        List<DefDictItemResultVO> voList = BeanUtil.copyToList(list, DefDictItemResultVO.class);

        //key 是类型
        return voList.stream().collect(groupingBy(DefDictItemResultVO::getParentKey, LinkedHashMap::new, toList()));
    }

    @Override
    public boolean removeItemByIds(Collection<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return false;
        }
        List<DefDict> list = listByIds(idList);
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        boolean flag = removeByIds(idList);

        CacheHashKey[] hashKeys = list.stream().map(model -> DictCacheKeyBuilder.builder(model.getParentKey(), model.getKey())).toArray(CacheHashKey[]::new);
        cachePlusOps.del(hashKeys);
        return flag;
    }
}
