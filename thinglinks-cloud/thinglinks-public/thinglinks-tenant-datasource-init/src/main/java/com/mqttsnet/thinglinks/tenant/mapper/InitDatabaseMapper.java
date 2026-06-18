package com.mqttsnet.thinglinks.tenant.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.mqttsnet.thinglinks.tenant.model.DefDatasourceConfigBO;
import com.mqttsnet.thinglinks.tenant.model.DefTenantBO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 初始化数据库DAO。
 *
 * <p><b>必须 {@code blockAttack="true"} / {@code illegalSql="true"}</b>:本 Mapper 执行多方言建库 DDL
 * (CREATE DATABASE / CREATE USER / GRANT / DROP DATABASE),映射为 {@code <update>/<delete>} 会被 MyBatis 归类为
 * UPDATE/DELETE。开启 {@code isBlockAttack} 后,BlockAttackInnerInterceptor 会用 JSqlParser 解析它们,而这些 DDL
 * (尤其 GRANT、达梦/SQLServer 方言建库)JSqlParser 解析不了 → 新建租户初始化建库失败。illegalSql 同理。</p>
 *
 * @author mqttsnet
 * @date 2019/09/02
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true", blockAttack = "true", illegalSql = "true")
public interface InitDatabaseMapper {
    /**
     * 创建数据库
     *
     * @param database 数据库名
     * @return
     */
    int createDatabase(@Param("database") String database);

    /**
     * 授权
     *
     * @param database database
     * @return int
     * @author mqttsnet
     * @date 2022/8/13 12:28 AM
     * @create [2022/8/13 12:28 AM ] [mqttsnet] [初始创建]
     */
    int grant(@Param("database") String database);


    /**
     * 删除数据库
     *
     * @param database
     * @return
     */
    int dropDatabase(@Param("database") String database);

    /**
     * 根据条件查询租户列表
     *
     * @param status      状态
     * @param connectType 连接类型
     * @return 租户编码
     */
    List<Long> selectTenantCodeList(@Param("status") List<String> status, @Param("connectType") String connectType);


    /**
     * 查询所有租户的数据源
     *
     * @param status      状态
     * @param connectType 连接类型
     * @return
     */
    List<DefDatasourceConfigBO> selectDataSourceConfig(@Param("status") List<String> status, @Param("connectType") String connectType);

    /**
     * 根据租户id，查询租户的 数据源配置
     *
     * @param tenantId tenantId
     * @return java.util.List<com.mqttsnet.thinglinks.tenant.model.DefDatasourceConfigBO>
     * @author mqttsnet
     * @date 2022/4/17 11:11 AM
     * @create [2022/4/17 11:11 AM ] [mqttsnet] [初始创建]
     */
    List<DefDatasourceConfigBO> selectDataSourceConfigByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据租户ID查询租户
     *
     * @param tenantId tenantId
     * @return com.mqttsnet.thinglinks.tenant.model.DefTenantBO
     * @author mqttsnet
     * @date 2022/4/17 11:11 AM
     * @create [2022/4/17 11:11 AM ] [mqttsnet] [初始创建]
     */
    DefTenantBO getTenantById(@Param("tenantId") Long tenantId);
}
