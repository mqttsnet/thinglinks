package com.mqttsnet.thinglinks.oauth.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.interfaces.echo.EchoService;
import com.mqttsnet.thinglinks.model.vo.result.Option;
import com.mqttsnet.thinglinks.oauth.service.DictService;
import com.mqttsnet.thinglinks.oauth.service.ParamService;
import com.mqttsnet.thinglinks.oauth.vo.param.CodeQueryVO;
import com.mqttsnet.thinglinks.system.vo.result.system.DefDictItemResultVO;
import com.mqttsnet.thinglinks.system.vo.result.system.DefDictResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 通用 控制器
 *
 * @author mqttsnet
 * @date 2019/07/25
 */
@Slf4j
@RestController
@Tag(name = "字典-枚举-参数-通用查询")
@RequiredArgsConstructor
public class GeneralController {


    private final DictService dictService;
    private final ParamService paramService;
    private final EchoService echoService;

    @Operation(summary = "同步枚举到字典", description = "同步枚举到字典")
    @PostMapping("/anyTenant/enums/syncEnumToDict")
    public R<Boolean> syncEnumToDict() {
        dictService.syncEnumToDict();
        return R.success();
    }

    @Operation(summary = "返回服务中所有枚举类", description = "只能扫描实现了BaseEnum类的枚举")
    @PostMapping("/anyUser/dict/enums/findAll")
    public R<List<DefDictResultVO>> findAll() {
        List<DefDictResultVO> result = dictService.findAll();
        echoService.action(result);
        return R.success(result);
    }

    @Operation(summary = "根据字典类型编码批量查询字典项", description = "根据字典类型编码批量查询字典项")
    @PostMapping("/anyUser/dict/findDictItemByType")
    public R<Map<String, List<DefDictItemResultVO>>> findDictItemByType(@RequestBody List<String> query) {
        Map<String, List<DefDictItemResultVO>> result = dictService.findDictItemByType(query);
        if (MapUtil.isNotEmpty(result)) {
            List<DefDictItemResultVO> flat = result.values().stream()
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(List::stream)
                    .toList();
            echoService.action(flat);
        }
        return R.success(result);
    }


    @Operation(summary = "根据枚举类名批量查询枚举值列表", description = "获取当前系统指定枚举")
    @PostMapping("/anyTenant/enums/findEnumMapByType")
    @Deprecated
    public R<Map<String, List<Option>>> findEnumMapByType(@RequestBody List<CodeQueryVO> types) {
        return R.success(dictService.findEnumMapByType(types));
    }


    @Operation(summary = "根据枚举类名查询枚举值列表", description = "获取当前系统指定枚举")
    @PostMapping("/anyTenant/enums/findEnumByType")
    @Deprecated
    public R<List<Option>> findEnumByType(@RequestBody CodeQueryVO type) {
        return R.success(dictService.findEnumByType(type));
    }

    @Operation(summary = "根据字典类型编码批量查询字典项,并排除指定项", description = "根据字典类型编码批量查询字典项")
    @PostMapping("/anyUser/dict/findDictMapByType")
    @Deprecated
    public R<Map<String, List<Option>>> findDictMapByType(@RequestBody List<CodeQueryVO> codeQueryVO) {
        Map<String, List<DefDictItemResultVO>> map = dictService.findDictMapByType(codeQueryVO.stream().map(CodeQueryVO::getType).toList());
        return R.success(dictService.mapOptionByDict(map, codeQueryVO));
    }

    @Operation(summary = "根据字典类型编码批量查询字典项,并排除指定项2", description = "根据字典类型编码批量查询字典项2")
    @PostMapping("/anyUser/dict/findDictMapByType2")
    @Deprecated
    public R<Map<String, List<DefDictItemResultVO>>> findDictMapByType2(@RequestBody List<CodeQueryVO> codeQueryVO) {
        Map<String, List<DefDictItemResultVO>> map = dictService.findDictMapByType(codeQueryVO.stream().map(CodeQueryVO::getType).toList());
        Map<String, List<DefDictItemResultVO>> result = mapByDict(map, codeQueryVO);
        if (MapUtil.isNotEmpty(result)) {
            List<DefDictItemResultVO> flat = result.values().stream()
                    .filter(CollUtil::isNotEmpty)
                    .flatMap(List::stream)
                    .toList();
            echoService.action(flat);
        }
        return R.success(result);
    }

    public Map<String, List<DefDictItemResultVO>> mapByDict(Map<String, List<DefDictItemResultVO>> map, List<CodeQueryVO> codeQueryVO) {
        if (MapUtil.isEmpty(map)) {
            return Collections.emptyMap();
        }
        Map<String, CodeQueryVO> codeMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(codeQueryVO)) {
            codeQueryVO.forEach(item -> codeMap.put(item.getType(), item));
        }

        Map<String, List<DefDictItemResultVO>> newMap = MapUtil.newHashMap();
        map.forEach((type, values) -> {
            CodeQueryVO codeQuery = codeMap.get(type);
            boolean extendFirst = codeQuery == null || codeQuery.getExtendFirst() == null || codeQuery.getExtendFirst();

            List<DefDictItemResultVO> options = new ArrayList<>();
            if (codeQuery != null && extendFirst && codeQuery.getExtend() != null) {
                Option extend = codeQuery.getExtend();
                DefDictItemResultVO resultVO = new DefDictItemResultVO();
                resultVO.setKey(extend.getValue());
                resultVO.setName(extend.getLabel());
                resultVO.setCssClass(extend.getColor());
                resultVO.setRemark(extend.getRemark());
                options.add(resultVO);
            }
            List<DefDictItemResultVO> optionList = values.stream().filter(item -> {
                if (codeQuery != null) {
                    List<String> excludes = codeQuery.getExcludes() == null ? Collections.emptyList() : codeQuery.getExcludes();
                    return !excludes.contains(item.getKey());
                }
                return false;
            }).toList();
            options.addAll(optionList);
            if (codeQuery != null && !extendFirst && codeQuery.getExtend() != null) {
                Option extend = codeQuery.getExtend();
                DefDictItemResultVO resultVO = new DefDictItemResultVO();
                resultVO.setKey(extend.getValue());
                resultVO.setName(extend.getLabel());
                resultVO.setCssClass(extend.getColor());
                resultVO.setRemark(extend.getRemark());
                options.add(resultVO);
            }
            newMap.put(type, options);
        });
        return newMap;
    }

    @Operation(summary = "根据字典类型编码查询字典项,并排除指定项", description = "根据类型编码查询字典项")
    @PostMapping("/anyUser/dict/findDictByType")
    public R<List<Option>> findDictByType(@RequestBody CodeQueryVO codeQueryVO) {
        Map<String, List<DefDictItemResultVO>> map = dictService.findDictMapByType(Collections.singletonList(codeQueryVO.getType()));
        return R.success(dictService.mapOptionByDict(map, codeQueryVO));
    }

//    @GetMapping("/anyUser/parameter/value")
//    @Operation(value = "根据key获取系统参数", notes = "根据key获取系统参数")
//    public R<String> getValue(@RequestParam(value = "key") String key, @RequestParam(value = "defVal", required = false) String defVal) {
//        return R.success(parameterService.getValue(key, defVal));
//    }

    @PostMapping("/anyUser/parameter/findParamMapByKey")
    @Operation(summary = "根据key批量获取系统参数", description = "根据key批量获取系统参数")
    public R<Map<String, String>> findParams(@RequestBody List<String> keys) {
        return R.success(paramService.findParamMapByKey(keys));
    }
}

