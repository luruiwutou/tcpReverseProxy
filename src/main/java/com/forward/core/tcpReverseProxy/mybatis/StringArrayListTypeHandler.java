package com.forward.core.tcpReverseProxy.mybatis;

import com.forward.core.constant.Constants;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.forward.core.constant.Constants.COMMA;

public class StringArrayListTypeHandler extends BaseTypeHandler<List<String[]>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String[]> parameter, JdbcType jdbcType) throws SQLException {
        // 这里留空，因为不需要做反向转换

    }



    @Override
    public List<String[]> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从 ResultSet 中获取逗号分隔的字符串，并将其转换为 List<String>
        String value = rs.getString(columnName);
        return Arrays.asList(value.split(COMMA)).stream().map(str->str.split(Constants.MYBATIS_LOCAL_CLIENT_PORT_SPLIT_REGEX)).collect(Collectors.toList());
    }

    @Override
    public List<String[]> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return Arrays.asList(value.split(COMMA)).stream().map(str->str.split(Constants.MYBATIS_LOCAL_CLIENT_PORT_SPLIT_REGEX)).collect(Collectors.toList());
    }

    @Override
    public List<String[]> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return Arrays.asList(value.split(COMMA)).stream().map(str->str.split(Constants.MYBATIS_LOCAL_CLIENT_PORT_SPLIT_REGEX)).collect(Collectors.toList());
    }
}
