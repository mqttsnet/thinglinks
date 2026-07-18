package com.mqttsnet.thinglinks.tdstest;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.nacos.common.packagescan.util.ResourceUtils;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.tds.enumeration.TdDataTypeEnum;
import com.mqttsnet.basic.tds.model.Fields;
import com.mqttsnet.basic.tds.model.SuperTableDTO;
import com.mqttsnet.basic.tds.model.TableDTO;
import com.mqttsnet.basic.tds.utils.TdsUtils;
import com.mqttsnet.thinglinks.tds.service.TdsService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.util.Map;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Slf4j
public class TDengineTest {

    @Autowired
    private TdsService tdengineService;

//  @Autowired
//  private RocketMQTemplateProducer mqTemplateProducer;

    public static String loadJsonFile(String jsonName) throws Exception {
        File file = ResourceUtils.getFile("classpath:" + jsonName);
        String json = FileUtils.readFileToString(file, "UTF-8");
        return json;
    }

    @BeforeEach
    public void setTenant() {
        ContextUtil.setTenantId(1L);
        ContextUtil.setTenantExtendPoolName(1L);
    }

    /**
     * 超级表的单元测试
     */
    @Test
    public void handleSuperTable() throws Exception {
        String jsonFile = loadJsonFile("superTable.json");
        JSONObject jsonObject = JSONUtil.parseObj(jsonFile);
        Map<String, SuperTableDTO> superTableDTOMap = TdsUtils.handleSuperTable(jsonObject);
        for (Map.Entry<String, SuperTableDTO> entry : superTableDTOMap.entrySet()) {
            SuperTableDTO value = entry.getValue();
            value.setDataBaseName("thinglinks");
            // 进行相应的操作
            tdengineService.createSuperTableAndColumn(value);
        }
        log.info(JSONUtil.toJsonStr(superTableDTOMap));
    }

    /**
     * 子表的单元测试
     */
    @Test
    public void handleSubTable() throws Exception {
        String jsonFile = loadJsonFile("subTable.json");
        JSONObject jsonObject = JSONUtil.parseObj(jsonFile);
        Map<String, TableDTO> subTableMap = TdsUtils.handleSubTable(jsonObject);
        for (Map.Entry<String, TableDTO> entry : subTableMap.entrySet()) {
            TableDTO value = entry.getValue();
            value.setDataBaseName("thinglinks");
            // 进行相应的操作
            tdengineService.createSubTable(value);
        }
        log.info(JSONUtil.toJsonStr(subTableMap));
    }

    @Test
    public void alterSuperTableColumn() {
        Fields fields = new Fields();
        fields.setFieldName("test");
        fields.setSize(10);
        fields.setDataType(TdDataTypeEnum.NCHAR);
        tdengineService.alterSuperTableColumn(ContextUtil.getDataBase(), "weather", fields);
    }

    @Test
    public void setMqTemplateProducer() {
        String str = "{ \"operationType\": \"ADD\", \"productType\": \"COMMON\", \"productIdentification\": \"b6d1914cfe824acf974fb24a242a05bc\", \"serviceCode\": \"xxxxx\" }";
    }

}
