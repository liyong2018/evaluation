package com.evaluate.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * 将 PostgreSQL 的 TIMESTAMP/TIMESTAMPTZ 安全映射为 Java 的 LocalDateTime。
 * 通过使用 java.sql.Timestamp 作为中转，避免驱动直接拒绝 TIMESTAMPTZ→LocalDateTime 的转换。
 */
@MappedTypes(LocalDateTime.class)
@MappedJdbcTypes({JdbcType.TIMESTAMP, JdbcType.TIMESTAMP_WITH_TIMEZONE})
public class LocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnIndex);
        return ts != null ? ts.toLocalDateTime() : null;
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp ts = cs.getTimestamp(columnIndex);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}

