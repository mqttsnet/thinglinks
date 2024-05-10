package com.mqttsnet.thinglinks.tdengine.api.factory;

import cn.hutool.json.JSONObject;
import com.mqttsnet.thinglinks.common.core.domain.R;
import com.mqttsnet.thinglinks.tdengine.api.RemoteTdEngineService;
import com.mqttsnet.thinglinks.tdengine.api.domain.SelectDto;
import com.mqttsnet.thinglinks.tdengine.api.domain.SuperTableDescribeVO;
import com.mqttsnet.thinglinks.tdengine.api.domain.TagsSelectDao;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.SuperTableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TableDTO;
import com.mqttsnet.thinglinks.tdengine.api.domain.model.TagsSelectDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @ClassDescription: 时序数据库服务降级处理
 * @ClassName: RemoteTdEngineFallbackFactory
 * @Author: thinglinks
 * @Date: 2021-12-31 11:00:59
 * @Version 1.0
 */
@Component
public class RemoteTdEngineFallbackFactory implements FallbackFactory<RemoteTdEngineService> {

    private static final Logger log = LoggerFactory.getLogger(RemoteTdEngineFallbackFactory.class);

    @Override
    public RemoteTdEngineService create(Throwable throwable) {
        log.error("TDengine服务调用失败:{}", throwable.getMessage());
        return new RemoteTdEngineService() {

            @Override
            public R createDatabase(String dataBaseName) {
                return R.fail("创建数据库失败:{}", throwable.getMessage());
            }

            @Override
            public R createSuperTable(String superTableName) {
                return R.fail("创建超级表失败:{}", throwable.getMessage());
            }

            @Override
            public R createSuperTableAndColumn(SuperTableDTO superTableDTO) {
                return R.fail("创建超级表及字段失败:{}", throwable.getMessage());
            }

            @Override
            public R createSuperTableAndColumnTwo(JSONObject object) {
                return R.fail("创建超级表及字段-方式二失败:{}", throwable.getMessage());
            }

            @Override
            public R createSubTable(TableDTO tableDTO) {
                return R.fail("创建子表失败:{}", throwable.getMessage());
            }

            @Override
            public R createSubTableTwo(JSONObject object) {
                return R.fail("创建子表-方式二失败:{}", throwable.getMessage());
            }

            @Override
            public R dropSuperTable(String superTableName) {
                return R.fail("删除超级表失败:{}", throwable.getMessage());
            }

            @Override
            public R alterSuperTableColumn(SuperTableDTO superTableDTO) {
                return R.fail("修改超级表列失败:{}", throwable.getMessage());
            }

            @Override
            public R dropSuperTableColumn(SuperTableDTO superTableDTO) {
                return R.fail("删除超级表列失败:{}", throwable.getMessage());
            }

            @Override
            public R alterSuperTableTag(SuperTableDTO superTableDTO) {
                return R.fail("修改超级表Tag失败:{}", throwable.getMessage());
            }

            @Override
            public R dropSuperTableTag(SuperTableDTO superTableDTO) {
                return R.fail("删除超级表Tag失败:{}", throwable.getMessage());
            }

            @Override
            public R alterSuperTableTagRename(String superTableName, String oldName, String newName) {
                return R.fail("修改超级表Tag名称失败:{}", throwable.getMessage());
            }

            @Override
            public R<List<SuperTableDescribeVO>> describeSuperOrSubTable(String tableName) {
                log.error("查询超级表、子表结构失败:{}", throwable.getMessage());
                return R.fail();
            }

            @Override
            public R insertTableData(TableDTO tableDTO) {
                return R.fail("新增数据失败:{}", throwable.getMessage());
            }

            @Override
            public R<List<Map<String, Object>>> getDataInRangeOrLastRecord(String tableName, Long startTime, Long endTime) {
                log.error("查询超级表、子表结构失败:{}", throwable.getMessage());
                return R.fail();
            }

            @Override
            public R<?> getDataByTimestamp(SelectDto selectDto) {
                return R.fail("查询数据失败:{}", throwable.getMessage());
            }

            @Override
            public R<?> getLastData(SelectDto selectDto) {
                return R.fail("查询最新数据失败:{}", throwable.getMessage());
            }

            @Override
            public R<Map<String, Map<String, Object>>> getLastDataByTags(TagsSelectDTO tagsSelectDao) {
                log.error("查询最新数据失败:{}", throwable.getMessage());
                return R.fail();
            }
        };
    }
}
