package com.ffi.api.pajak.utils;

import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DynamicRowMapper implements RowMapper<Map<String, Object>> {
    @Override
    public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        Map<String, Object> result = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnLabel(i);
            Object columnValue = rs.getObject(i);
            String camelCaseColumnName = convertToCamelCase(columnName);
            result.put(camelCaseColumnName, columnValue);
        }
        return result;
    }

    private String convertToCamelCase(String input) {
        String[] parts = input.split("_");
        StringBuilder camelCaseString = new StringBuilder(parts[0].toLowerCase());
        for (int i = 1; i < parts.length; i++) {
            camelCaseString.append(Character.toUpperCase(parts[i].charAt(0)))
                           .append(parts[i].substring(1).toLowerCase());
        }
        return camelCaseString.toString();
    }
}
