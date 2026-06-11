package com.mqttsnet.thinglinks.tenant.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.util.JdbcUtils;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.creator.DatasourceInitProperties;
import com.baomidou.dynamic.datasource.creator.DefaultDataSourceCreator;
import com.baomidou.dynamic.datasource.creator.druid.DruidConfig;
import com.baomidou.dynamic.datasource.ds.ItemDataSource;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.baomidou.mybatisplus.annotation.DbType;
import com.mqttsnet.basic.context.ContextConstants;
import com.mqttsnet.basic.context.ContextUtil;
import com.mqttsnet.basic.database.properties.DatabaseProperties;
import com.mqttsnet.basic.exception.BizException;
import com.mqttsnet.basic.utils.ArgumentAssert;
import com.mqttsnet.basic.utils.DbPlusUtil;
import com.mqttsnet.thinglinks.model.enumeration.system.DefTenantStatusEnum;
import com.mqttsnet.thinglinks.model.enumeration.system.TenantConnectTypeEnum;
import com.mqttsnet.thinglinks.tenant.dynamic.processor.DsThreadProcessor;
import com.mqttsnet.thinglinks.tenant.mapper.InitDatabaseMapper;
import com.mqttsnet.thinglinks.tenant.model.DefDatasourceConfigBO;
import com.mqttsnet.thinglinks.tenant.model.DefTenantBO;
import com.mqttsnet.thinglinks.tenant.service.DataSourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


/**
 * 数据源管理
 * <p>
 * thinglinks.database.multiTenantType=DATASOURCE 时，该类才会生效
 *
 * @author mqttsnet
 * @date 2020年03月15日11:35:08
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Primary
public class DynamicDataSourceServiceImpl implements DataSourceService {

    private static final String SCHEMA_PATH = "schema/{}/{}.sql";
    private static final String DATA_PATH = "data/{}/{}.sql";
    private final DataSource dataSource;
    private final DefaultDataSourceCreator defaultDataSourceCreator;
    private final DatabaseProperties databaseProperties;
    private final DynamicDataSourceProperties properties;
    private final InitDatabaseMapper initDbMapper;

    @Override
    public Set<String> findAll() {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        return ds.getDataSources().keySet();
    }

    @Override
    public boolean check(Long tenantId) {
        List<String> initDatabasePrefix = databaseProperties.getInitDatabasePrefix();
        ArgumentAssert.notEmpty(initDatabasePrefix, "请先配置thinglinks.database.initDatabasePrefix");
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        Map<String, DataSource> dataSourceMap = ds.getDataSources();
        return initDatabasePrefix.stream().allMatch(prefix ->
                dataSourceMap.containsKey(DsThreadProcessor.getPoolName(prefix, String.valueOf(tenantId)))
        );
    }

    @Override
    public boolean removeDbAndDs(Long tenantId) {
        DynamicRoutingDataSource ds = (DynamicRoutingDataSource) dataSource;
        databaseProperties.getInitDatabasePrefix().forEach(prefix -> {
            String database = DsThreadProcessor.getPoolName(prefix, String.valueOf(tenantId));
            if (Objects.equals(ContextConstants.TENANT_EXTEND_POOL_NAME_HEADER, prefix)) {
                DynamicDataSourceContextHolder.push(ContextConstants.DEF_TENANT_ID_SEQ);
            }
            if (StrUtil.isNotEmpty(database)) {
                initDbMapper.dropDatabase(database);
            }
            ds.removeDataSource(database);
            DynamicDataSourceContextHolder.clear();
        });
        return true;
    }

    /**
     * 测试链接
     *
     * @param dataSourceProperty
     * @return
     */
    @Override
    public boolean testConnection(DataSourceProperty dataSourceProperty) {
        dataSourceProperty.setSeata(false);
        dataSourceProperty.setDruid(BeanUtil.toBean(properties.getDruid(), DruidConfig.class));
        // 配置获取链接等待超时的时间
        dataSourceProperty.getDruid().setMaxWait(3000);
        // 配置初始化大小、最小、最大
        dataSourceProperty.getDruid().setInitialSize(1);
        dataSourceProperty.getDruid().setMinIdle(1);
        dataSourceProperty.getDruid().setMaxActive(1);
        // 链接错误重试次数
        dataSourceProperty.getDruid().setConnectionErrorRetryAttempts(0);
        // 获取失败后中断
        dataSourceProperty.getDruid().setBreakAfterAcquireFailure(true);

        DataSource testDataSource = null;
        Connection connection = null;
        boolean flag;
        try {
            testDataSource = defaultDataSourceCreator.createDataSource(dataSourceProperty);
            connection = testDataSource.getConnection();

            int timeOut = 5;
            if (connection == null || connection.isClosed() || !connection.isValid(timeOut)) {
                log.info("链接已关闭或无效，请重试获取链接！");
                connection = testDataSource.getConnection();
            }
            flag = connection != null;
        } catch (ErrorCreateDataSourceException e) {
            log.error("数据源初始化期间出现异常", e);
            throw new BizException("数据源初始化期间出现异常", e);
        } catch (Exception e) {
            log.error("创建测试链接错误 {}", dataSourceProperty.getUrl());
            throw new BizException("创建测试链接错误 " + dataSourceProperty.getUrl(), e);
        } finally {
            if (testDataSource instanceof ItemDataSource ids) {
                ids.close();
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.warn("关闭测试数据源链接异常", e);
                }
            }
        }
        return flag;
    }


    /**
     * 初始化数据库
     *
     * @param tenantId 明文租户编码
     */
    @Override
    public void createDatabase(Long tenantId) {
        if (CollUtil.isEmpty(databaseProperties.getInitDatabasePrefix())) {
            throw BizException.wrap("请先配置需要动态创建的数据库 库名前缀");
        }
        for (String database : databaseProperties.getInitDatabasePrefix()) {
            String databaseName = DsThreadProcessor.getPoolName(database, String.valueOf(tenantId));
            if (Objects.equals(ContextConstants.TENANT_EXTEND_POOL_NAME_HEADER, database)) {
                createDynamicDatabaseForExtend(databaseName);
            } else {
                this.initDbMapper.createDatabase(databaseName);
                initDbMapper.grant(databaseName);
            }
        }
    }

    private void createDynamicDatabaseForExtend(String databaseName) {
        DynamicDataSourceContextHolder.push(ContextConstants.DEF_TENANT_ID_SEQ);
        try {
            // TODO 兼容其他扩展库需要根据数据源类型判断
            // taosdata性能参数配置
            String createDatabaseSql = new StringBuilder("create database if not exists ")
                    .append(databaseName)
                    .append(" ")
                    .append(Optional.ofNullable(databaseProperties.getExtendSqlParameters())
                            .map(params -> Optional.ofNullable(params.getTdengineCreateDatabaseOptions())
                                    .orElse(""))
                            .orElse(""))
                    .toString();

            // Execute SQL statement
            log.info("创建数据库 SQL: {}", createDatabaseSql);
            JdbcUtils.execute(dataSource, createDatabaseSql);
            log.info("数据库 {} 创建成功", databaseName);
        } catch (Exception e) {
            log.error("扩展数据库 {} 失败: {}", databaseName, e.getMessage(), e);
            throw BizException.wrap("创建数据库失败: " + databaseName, e);
        } finally {
            DynamicDataSourceContextHolder.clear();
        }
    }

    @Override
    public boolean loadSystemDataSource() {
        List<String> status = List.of(DefTenantStatusEnum.NORMAL.getCode(), DefTenantStatusEnum.WAIT_INIT_DATASOURCE.getCode());
        List<Long> list = initDbMapper.selectTenantCodeList(status, TenantConnectTypeEnum.SYSTEM.name());
        list.forEach(tenantId -> addSystem(tenantId, false, false));
        return true;
    }

    @Override
    public boolean loadCustomDataSource() {
        List<String> status = List.of(DefTenantStatusEnum.NORMAL.getCode(), DefTenantStatusEnum.WAIT_INIT_DATASOURCE.getCode());
        List<DefDatasourceConfigBO> dcList = initDbMapper.selectDataSourceConfig(status, TenantConnectTypeEnum.CUSTOM.name());
        return addCustom(dcList, false, false);
    }

    @Override
    public boolean initDataSource(Long tenantId) {
        DefTenantBO tenant = initDbMapper.getTenantById(tenantId);
        ArgumentAssert.notNull(tenant, "您要初始化的租户不存在");
        if (TenantConnectTypeEnum.CUSTOM.eq(tenant.getConnectType())) {
            List<DefDatasourceConfigBO> dcList = initDbMapper.selectDataSourceConfigByTenantId(tenant.getId());
            return addCustom(dcList, false, true);
        } else {
            return addSystem(tenantId, false, true);
        }
    }

    @Override
    public boolean addSystemDsAndData(Long tenantId) {
        return addSystem(tenantId, true, true);
    }

    @Override
    public boolean addCustomDsAndData(Long tenantId) {
        List<DefDatasourceConfigBO> dcList = initDbMapper.selectDataSourceConfigByTenantId(tenantId);
        return addCustom(dcList, true, true);
    }

    private DbType getDbType(String url) {
        DbType dbType = DbPlusUtil.getDbType(url);
        return dbType;
    }

    private boolean addCustom(List<DefDatasourceConfigBO> dcList, boolean isInitSchema, boolean isNotErrorRetry) {
        if (CollUtil.isEmpty(dcList)) {
            return false;
        }
        DataSourceProperty defDataSourceProperty = properties.getDatasource().get(ContextConstants.DEF_TENANT_ID_STR);
        ArgumentAssert.notNull(defDataSourceProperty, "请先配置默认[{}]数据源", ContextConstants.DEF_TENANT_ID_STR);
        // REMOTE 类型的数据源初始化
        dcList.forEach(dc -> {
            DataSourceProperty dataSourceProperty = new DataSourceProperty();
            BeanUtils.copyProperties(defDataSourceProperty, dataSourceProperty);
            BeanUtils.copyProperties(dc, dataSourceProperty);
            dataSourceProperty.setPoolName(DsThreadProcessor.getPoolName(dc.getDbPrefix(), String.valueOf(dc.getTenantId())));
            if (isInitSchema) {
                DatasourceInitProperties init = dataSourceProperty.getInit();
                if (init == null) {
                    init = new DatasourceInitProperties();
                }
                init.setSchema(StrUtil.format(SCHEMA_PATH, getDbType(dataSourceProperty.getUrl()).getDb(), dc.getDbPrefix()));
                init.setData(StrUtil.format(DATA_PATH, getDbType(dataSourceProperty.getUrl()).getDb(), dc.getDbPrefix()));
                dataSourceProperty.setInit(init);
            }
            dataSourceProperty.setSeata(databaseProperties.getIsSeata());
            dataSourceProperty.setDruid(properties.getDruid());
            if (isNotErrorRetry) {
                // 链接错误重试次数
                dataSourceProperty.getDruid().setConnectionErrorRetryAttempts(0);
                // 获取失败后中断
                dataSourceProperty.getDruid().setBreakAfterAcquireFailure(true);
            }
            putDs(dataSourceProperty);
        });
        return true;
    }

    private boolean addSystem(Long tenantId, boolean isInitSchema, boolean isNotErrorRetry) {
        // 默认基础数据数据库
        DataSourceProperty defFoundationDataSourceProperty = properties.getDatasource().get(ContextConstants.DEF_TENANT_ID_STR);
        // 默认时序型数据库
        DataSourceProperty defSequentialDataSourceProperty = properties.getDatasource().get(ContextConstants.DEF_TENANT_ID_SEQ);
        ArgumentAssert.notNull(defFoundationDataSourceProperty, "请先配置默认基础[{}]数据源", ContextConstants.DEF_TENANT_ID_STR);
        ArgumentAssert.notNull(defSequentialDataSourceProperty, "请先配置默认时序[{}]数据源", ContextConstants.DEF_TENANT_ID_SEQ);

        // 根据前缀处理数据库
        for (String database : databaseProperties.getInitDatabasePrefix()) {
            DataSourceProperty newDataSourceProperty = new DataSourceProperty();
            BeanUtil.copyProperties(defFoundationDataSourceProperty, newDataSourceProperty);
            // 扩展数据源
            if (ContextConstants.TENANT_EXTEND_POOL_NAME_HEADER.equals(database)) {
                BeanUtil.copyProperties(defSequentialDataSourceProperty, newDataSourceProperty);
            }
            newDataSourceProperty.setPoolName(DsThreadProcessor.getPoolName(database, String.valueOf(tenantId)));
            String url = newDataSourceProperty.getUrl();
            String tdOldDatabase = DbPlusUtil.getDataBaseNameByUrl(url);
            String tdNewDatabase = StrUtil.join(StrUtil.UNDERLINE, database, tenantId);
            newDataSourceProperty.setUrl(StrUtil.replace(url, tdOldDatabase, tdNewDatabase));
            if (isInitSchema) {
                DatasourceInitProperties init = newDataSourceProperty.getInit();
                if (init == null) {
                    init = new DatasourceInitProperties();
                }

                init.setSchema(StrUtil.format(SCHEMA_PATH, getDbType(newDataSourceProperty.getUrl()).getDb(), database));
                init.setData(StrUtil.format(DATA_PATH, getDbType(newDataSourceProperty.getUrl()).getDb(), database));
                newDataSourceProperty.setInit(init);
            }
            newDataSourceProperty.setSeata(databaseProperties.getIsSeata());
            newDataSourceProperty.setDruid(properties.getDruid());
            if (isNotErrorRetry) {
                // 链接错误重试次数
                newDataSourceProperty.getDruid().setConnectionErrorRetryAttempts(0);
                // 获取失败后中断
                newDataSourceProperty.getDruid().setBreakAfterAcquireFailure(true);
            }
            putDs(newDataSourceProperty);
        }
        return true;
    }

    private Set<String> putDs(DataSourceProperty dsp) {
        try {
            DynamicRoutingDataSource ds = (DynamicRoutingDataSource) this.dataSource;
            DataSource newDataSource = defaultDataSourceCreator.createDataSource(dsp);
            ds.addDataSource(dsp.getPoolName(), newDataSource);
            setTenantDataBaseName(dsp.getPoolName(), dsp.getUrl());
            return ds.getDataSources().keySet();
        } catch (ErrorCreateDataSourceException e) {
            log.error("数据源初始化期间出现异常", e);
            throw new BizException("数据源初始化期间出现异常", e);
        }
    }

    /**
     * 解析数据源 JDBC URL 中的数据库名称并登记到上下文,供多租户路由与 TDS 操作取用。
     * <p>直接解析配置 URL 取库名,不向连接池借取连接。
     *
     * @param poolName 连接池名称
     * @param jdbcUrl  数据源 JDBC URL
     */
    private void setTenantDataBaseName(String poolName, String jdbcUrl) {
        try {
            ContextUtil.setDataBase(poolName, DbPlusUtil.getDataBaseNameByUrl(jdbcUrl));
        } catch (Exception e) {
            log.error("解析数据库名称异常, poolName: {}, 原因: {}", poolName, e.getMessage());
        }
    }

}
