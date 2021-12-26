package com.mqttsnet.thinglinks.tdengine.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.taosdata.jdbc.TSDBDriver;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.*;
import java.util.*;

/**
 * @Description: TDengine数据库工具类
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://thinglinks.mqttsnet.com
 * @CreateDate: 2021/11/22$ 16:11$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/22$ 16:11$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Configuration
@Data
@Slf4j
public class TdengineDatabaseUtils {

    /** TDengine数据库的Url */
    @Value("${spring.datasource.url}")
    public String url = "";

    /** TDengine数据库的username */
    @Value("${spring.datasource.username}")
    private String userName = "";

    /** TDengine数据库的password */
    @Value("${spring.datasource.password}")
    private String password = "";

    /** TDengine数据库的driver-class-name */
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName = "";

    private static DruidDataSource druidPool;

    /**
     * 获取 Connetion
     * @return
     * @throws SQLException
     */

    public Connection getConnection(){
        Connection conn = null;
        try {
            Properties properties = new Properties();
            properties.setProperty(TSDBDriver.PROPERTY_KEY_CHARSET, "UTF-8");
            properties.setProperty(TSDBDriver.PROPERTY_KEY_TIME_ZONE, "UTC-8");
            properties.setProperty("username", userName);
            properties.setProperty("password", password);
            properties.setProperty("url", url);
            properties.setProperty("driverClassName", driverClassName);
            druidPool = (DruidDataSource) DruidDataSourceFactory.createDataSource(properties);
            druidPool.setValidationQuery("SELECT 1");
            druidPool.setTestWhileIdle(true);
            druidPool.setTestOnBorrow(false);
            druidPool.setTestOnReturn(false);
            druidPool.setInitialSize(20);
            druidPool.setMaxActive(2000);
            druidPool.setRemoveAbandoned(true);
            druidPool.setRemoveAbandonedTimeout(5);
            druidPool.setMaxWait(6000);
            druidPool.setTimeBetweenEvictionRunsMillis(6000);
            druidPool.setMinEvictableIdleTimeMillis(300000);
            conn = druidPool.getConnection();

        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return conn;
    }

    /**
     * 关闭ResultSet资源
     * @param rs
     */

    public void close(ResultSet rs){
        if(null != rs){
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭Statement资源
     * @param stmt
     */

    public void close(Statement stmt){
        if(null != stmt){
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭Statement资源
     * @param pps
     */

    public void close(PreparedStatement pps){
        if(null != pps){
            try {
                pps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭close资源
     * @param conn
     */
    public void close(Connection conn){
        if(null != conn){
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 关闭资源
     * @param rs
     * @param pps
     * @param conn
     */

    public void close(ResultSet rs,PreparedStatement pps,Connection conn){
        close(rs);
        close(pps);
        close(conn);
    }

    public ResultSet query(Connection conn, String sql){
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            conn.setAutoCommit(false);
            preparedStatement = conn.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            conn.commit();
            return resultSet;
        } catch (Exception var5) {
            log.error(var5.getMessage());
            return null;
        }
    }


    /**
     * jdbc 单条数据查询转成map
     * @param sql result 查询的结果
     * @return
     */
    public Map<String, Object> queryConvertMap(String sql) {
        Connection conn = this.getConnection();
        ResultSet rs = null;
        PreparedStatement pps = null;
        Map<String, Object> map = new TreeMap<>();
        try {
            conn.setAutoCommit(false);
            pps = conn.prepareStatement(sql);
            rs = pps.executeQuery();
            conn.commit();

            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if("-2".equals(String.valueOf(md.getColumnType(i)))){
                        map.put(md.getColumnName(i),(Object)rs.getString(i));
                    }else {
                        map.put(md.getColumnName(i), rs.getObject(i));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        } finally {
            close(rs,pps,conn);
            return map;
        }
    }


    /**
     * jdbc 单条数据查询转成map
     * @param sql result 查询的结果
     * @return
     */
    public Map<String, Object> queryConvertMap(Connection conn,String sql) {
        ResultSet rs = null;
        PreparedStatement pps = null;
        Map<String, Object> map = new TreeMap<>();
        try {
            if(conn.isClosed()) {
                conn = this.getConnection();
                conn.setAutoCommit(false);
            }
            pps = conn.prepareStatement(sql);
            rs = pps.executeQuery();
            conn.commit();

            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    if("-2".equals(String.valueOf(md.getColumnType(i)))){
                        map.put(md.getColumnName(i),(Object)rs.getString(i));
                    }else {
                        map.put(md.getColumnName(i), rs.getObject(i));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        } finally {
            close(rs);
            close(pps);
            return map;
        }
    }




    /**
     * jdbc 多条数据查询转成list
     * @param sql
     * @return
     */
    public List<Map<String, Object>> queryConvertList(String sql) {
        Connection conn = this.getConnection();
        ResultSet rs = null;
        PreparedStatement pps = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            conn.setAutoCommit(false);
            pps = conn.prepareStatement(sql);
            rs = pps.executeQuery();
            conn.commit();

            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    if("-2".equals(String.valueOf(md.getColumnType(i)))){
                        rowData.put(md.getColumnName(i),(Object)rs.getString(i));
                    }else {
                        rowData.put(md.getColumnName(i), rs.getObject(i));
                    }
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return list;
        } finally {
            close(rs,pps,conn);
        }
        return list;
    }

    /**
     * jdbc 多条数据查询转成list
     * @param sql
     * @return
     */
    public List<Map<String, Object>> queryConvertList(Connection conn,String sql) {
        ResultSet rs = null;
        PreparedStatement pps = null;
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            if(conn.isClosed()) {
                conn = this.getConnection();
                conn.setAutoCommit(false);
            }
            pps = conn.prepareStatement(sql);
            rs = pps.executeQuery();
            conn.commit();

            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next()) {
                Map<String, Object> rowData = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    if("-2".equals(String.valueOf(md.getColumnType(i)))){
                        rowData.put(md.getColumnName(i),(Object)rs.getString(i));
                    }else {
                        rowData.put(md.getColumnName(i), rs.getObject(i));
                    }
                }
                list.add(rowData);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return list;
        } finally {
            close(rs);
            close(pps);
        }
        return list;
    }

}
