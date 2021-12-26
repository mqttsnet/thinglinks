package com.mqttsnet.thinglinks.tdengine.util;

import com.mqttsnet.thinglinks.common.core.utils.DateUtils;
import com.mqttsnet.thinglinks.common.core.utils.StringUtils;
import net.sf.cglib.beans.BeanMap;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author thinglinks
 */
public class TdengineUtils {

    private Connection connection;
    private boolean databaseColumnHumpToLine;

    /**
     * @param connection
     * @param databaseColumnHumpToLine
     */
    public TdengineUtils(Connection connection, boolean databaseColumnHumpToLine) {
        this.connection = connection;
        this.databaseColumnHumpToLine = databaseColumnHumpToLine;
    }


    /**
     * 执行sql（无论是否返回结果），将结果注入到指定的类型实例中，且返回
     * 当查询到的数据大于一个时，取第一个
     * <p>
     * 对象遵从以下说明<br/>
     * 1.对象字段为String类型，数据库类型(通过jdbc读取到的)无论什么类型，都将调用Object.toString方法注入值<br/>
     * 2.对象字段为数据库类型(通过jdbc读取到的)一致的情况下，将会直接注入<br/>
     * 3.对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试使用{@link Class#cast(Object)}方法转型，失败此值会是类型默认值(故实体推荐使用封装类型)<br/>
     * 4.对象字段为{@link Date}时，数据库类型为Date才可以注入，如果为long(例如TDengine)将会被当作毫秒的时间戳注入<br/>
     *
     * @param sql   要执行的sql
     * @param clazz 要注入的实体类型
     * @param <T>   要注入的实体类型
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    public <T> T getOne(String sql, Class<T> clazz) throws IllegalAccessException, InstantiationException, SQLException {
        Method[] setterMethods = getSetterMethods(clazz);
        ResultSet resultSet = connection.createStatement().executeQuery(sql);

        //只有一个结果直接下一个就行
        resultSet.next();

        return resultSetToObject(resultSet, setterMethods, clazz);
    }


    /**
     * 执行sql（无论是否返回结果），将结果注入到指定的类型实例中，且返回
     * 当查询到的结果没有时，返回一个大小为0的list;
     * <p>
     * 对象遵从以下说明<br/>
     * 1.对象字段为String类型，数据库类型(通过jdbc读取到的)无论什么类型，都将调用Object.toString方法注入值<br/>
     * 2.对象字段为数据库类型(通过jdbc读取到的)一致的情况下，将会直接注入<br/>
     * 3.对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试使用{@link Class#cast(Object)}方法转型，失败此值会是类型默认值(故实体推荐使用封装类型)<br/>
     * 4.对象字段为{@link Date}时，数据库类型为Date才可以注入，如果为long(例如TDengine)将会被当作毫秒的时间戳注入<br/>
     *
     * @param sql   要执行的sql
     * @param clazz 要注入的实体类型
     * @param <T>   要注入的实体类型
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    public <T> List<T> getList(String sql, Class<T> clazz) throws IllegalAccessException, InstantiationException, SQLException {
        List<T> list = new ArrayList<>();

        Method[] setterMethods = getSetterMethods(clazz);
        ResultSet resultSet = connection.createStatement().executeQuery(sql);

        while (resultSet.next()) {
            list.add(resultSetToObject(resultSet, setterMethods, clazz));
        }
        return list;
    }

    /**
     * 插入对象到指定的表里面
     *
     * @param tableName
     * @param o
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("all")
    public boolean insert(String tableName, Object o) throws SQLException {
        Class clazz = o.getClass();
        Map<String, Object> map = BeanMap.create(o);

        String sql = createInsertSql(tableName, map);
        return connection.createStatement().execute(sql);
    }


    /**
     * 生成插入sql语句
     *
     * @param tableName
     * @param map
     * @return
     */
    public static String createInsertSql(String tableName, Map<String, Object> map) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("INSERT INTO ").append(tableName).append(" (");

        Set<Map.Entry<String, Object>> set = map.entrySet();

        StringBuilder keys = new StringBuilder(" ");
        StringBuilder value = new StringBuilder(" ");

        for (Map.Entry<String, Object> entry : set) {
            keys.append(humpToLine(entry.getKey())).append(",");
            try {
                if (entry.getValue().getClass().equals(Date.class)) {
                    Date d = (Date) entry.getValue();
                    value.append(d.getTime()).append(",");
                } else {
                    value.append("'").append(entry.getValue()).append("'").append(",");
                }
            } catch (Exception ignored) {

            }
        }

        keys.deleteCharAt(keys.length() - 1);
        value.deleteCharAt(value.length() - 1);

        buffer.append(keys).append(") VALUES( ").append(value).append(")");

        return buffer.toString();
    }

    /**
     * 插入对象到指定的TD表里面
     *
     * @param tableName
     * @param o
     * @param tags 标签
     * @param superTableName 超级表
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("all")
    public boolean insertTD(String tableName, Object o,Map<String,Object> tags,String superTableName) throws SQLException {
        Class clazz = o.getClass();
        Map<String, Object> map = BeanMap.create(o);

        String sql = createInsertTDSql(tableName, map,tags,superTableName);
        System.out.println(sql);
        return connection.createStatement().execute(sql);
    }

    /**
     * 生成插入sql语句
     *
     * @param tableName
     * @param map
     * @return
     */
    public String createInsertTDSql(String tableName, Map<String, Object> map,Map<String,Object> tags,String superTableName) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("insert into ").append(tableName).append(" (");

        Set<Map.Entry<String, Object>> set = map.entrySet();

        StringBuilder keys = new StringBuilder();
        StringBuilder value = new StringBuilder();

        for (Map.Entry<String, Object> entry : set) {
            if(StringUtils.isNull(entry.getValue())){
                continue;
            }
            keys.append(entry.getKey()).append(",");
            try {
                if (entry.getValue().getClass().equals(Date.class)) {
                    Date d = (Date) entry.getValue();
                    value.append("'").append(DateUtils.dateFormat(d)).append("'").append(",");
                }else if(entry.getValue().getClass().equals(Integer.class) || entry.getValue().getClass().equals(Float.class)|| entry.getValue().getClass().equals(Double.class)|| entry.getValue().getClass().equals(Long.class)){
                    value.append(entry.getValue()).append(",");
                }else if(entry.getKey().equals("timestamp_")) {
                    Date d = new Date((Long) entry.getValue());
                    value.append("'").append(DateUtils.dateFormat(d)).append("'").append(",");
                }else{
                    value.append("'").append(entry.getValue()).append("'").append(",");
                }
            } catch (Exception ignored) {
                System.out.println("数据拼接异常"+ignored);
            }
        }

        StringBuilder tagsKeys = new StringBuilder();
        StringBuilder tagsValue = new StringBuilder();
        for (String key : tags.keySet()) {
            tagsKeys.append(key).append(",");
            Object ve = tags.get(key);
            if (ve instanceof Integer) {
                //数字不用加"'"
                tagsValue.append(ve).append(",");
            }else {
                //其他全加
                tagsValue.append("'").append(ve).append("'").append(",");
            }
        }
        keys.deleteCharAt(keys.length() - 1);
        value.deleteCharAt(value.length() - 1);
        tagsKeys.deleteCharAt(tagsKeys.length()-1);
        tagsValue.deleteCharAt(tagsValue.length() - 1);
        buffer.append(keys).append(")")
                .append(" using ").append(superTableName).append("(").append(tagsKeys).append(") tags(").append(tagsValue).append(") ")
                .append("values(").append(value).append(")");
        return buffer.toString();
    }





    /**
     * 将resultSet注入到指定的类型实例中，且返回
     * 对象遵从以下说明<br/>
     * 1.对象字段为String类型，数据库类型(通过jdbc读取到的)无论什么类型，都将调用Object.toString方法注入值<br/>
     * 2.对象字段为数据库类型(通过jdbc读取到的)一致的情况下，将会直接注入<br/>
     * 3.对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试使用{@link Class#cast(Object)}方法转型，失败此值会是类型默认值(故实体推荐使用封装类型)<br/>
     * 4.对象字段为{@link Date}时，数据库类型为Date才可以注入，如果为long(例如TDengine)将会被当作毫秒的时间戳注入<br/>
     * <p>
     * 注意，此方法只会注入一个结果,不会循环{@link ResultSet#next()}方法，请从外部调用。<br/>
     * 传入setterMethods的目的是为了方便外部循环使用此方法，这样方法内部不会重复调用，提高效率<br/>
     *
     * @param resultSet     查询结果，一定要是{@link ResultSet#next()}操作过的，不然没有数据
     * @param setterMethods clazz对应的所有setter方法，可以使用{@link this#getSetterMethods(Class)}获取
     * @param clazz         注入对象类型
     * @param <T>           注入对象类型
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public <T> T resultSetToObject(ResultSet resultSet, Method[] setterMethods, Class<T> clazz) throws IllegalAccessException, InstantiationException {
        T result;
        try {
            result = clazz.newInstance();
        } catch (InstantiationException e) {
            System.out.println("请检查类" + clazz.getCanonicalName() + "是否有无参构造方法");
            throw e;
        }


        for (Method method : setterMethods) {
            try {
                String fieldName = getFieldNameBySetter(method);

                //因为标准的setter方法只会有一个参数，所以取一个就行了
                Class getParamClass = method.getParameterTypes()[0];


                //获得查询的结果
                Object resultObject;

                //是否启用驼峰转下划线规则获得数据库字段名
                if (databaseColumnHumpToLine) {
                    resultObject = resultSet.getObject(humpToLine(fieldName));
                } else {
                    resultObject = resultSet.getObject(fieldName);
                }

                //如果实体类的类型是String类型，那么无论x数据库类型是什么，都调用其toString方法获取值
                if (getParamClass.equals(String.class)) {
                    method.invoke(result, resultObject.toString());
                } else if (getParamClass.equals(Date.class) && resultObject.getClass().equals(Long.class)) {
                    method.invoke(result, new Date((Long) resultObject));
                } else {
                    try {
                        method.invoke(result, resultObject);
                    } catch (IllegalArgumentException e) {
                        //对象字段与数据库类型(通过jdbc读取到的)不一致的情况下，将尝试强制转型
                        method.invoke(result, getParamClass.cast(resultObject));
                    }
                }
            } catch (Exception ignored) {
                //所有的转型都失败了，则使用默认值
            }
        }

        return result;
    }

    /**
     * 通过setter method,获取到其对应的属性名
     *
     * @param method
     * @return
     */
    public static String getFieldNameBySetter(Method method) {
        return toLowerCaseFirstOne(method.getName().substring(3));
    }


    /**
     * 获取指定类型方法的所有的setter方法
     * 方法属性名为key，对应的方法为value
     *
     * @param clazz
     * @return
     */
    public static Map<String, Method> getSetterMethodsMap(Class clazz) {
        Method[] methods = clazz.getMethods();
        Map<String, Method> setterMethods = new HashMap<>(methods.length / 2);

        for (Method m : methods) {
            if (m.getName().startsWith("set")) {
                setterMethods.put(toLowerCaseFirstOne(m.getName().substring(3)), m);
            }
        }
        return setterMethods;
    }

    /**
     * 获取指定类型方法的所有的setter方法
     *
     * @param clazz
     * @return
     */
    public static Method[] getSetterMethods(Class clazz) {
        Method[] methods = clazz.getMethods();
        Method[] setterMethods = new Method[methods.length / 2];

        int i = 0;
        for (Method m : methods) {
            if (m.getName().startsWith("set")) {
                setterMethods[i] = m;
                i++;
            }
        }
        return setterMethods;
    }

    /**
     * 首字母转小写
     */
    public static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }


    /**
     * 首字母转大写
     */
    public static String toUpperCaseFirstOne(String s) {
        if (Character.isUpperCase(s.charAt(0))) {
            return s;
        } else {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
        }
    }

    private static Pattern linePattern = Pattern.compile("_(\\w)");

    /**
     * 下划线转驼峰
     */
    public static String lineToHump(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 驼峰转下划线,效率比上面高
     */
    public static String humpToLine(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
