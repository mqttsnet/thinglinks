package com.mqttsnet.thinglinks.rule.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;

import org.apache.ibatis.type.JdbcType;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonTypeHandler extends BaseTypeHandler<Object> {

    private static ObjectMapper objectMapper;
    private Class<?> clazz ;
    static {
        objectMapper = new ObjectMapper();
    }

    public JsonTypeHandler(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("class type is null");
        }
        this.clazz = clazz;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter,JdbcType jdbcType) throws SQLException {
        ps.setString(i, toJsonString(parameter));
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName)throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return  parse(rs.getString(columnIndex));
    }

    @Override
    public  Object getNullableResult(CallableStatement cs, int columnIndex)throws SQLException {
        return parse(cs.getString(columnIndex));
    }
    private  Object parse(String json) {
        try {
            if(json == null || json.length() == 0) {
                return null;
            }
            return objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
