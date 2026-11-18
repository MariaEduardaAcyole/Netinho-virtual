package br.com.fiap.netinho.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Centraliza conversoes de tipos JDBC, incluindo valores numericos retornados pelo Oracle.
 */
public final class DaoUtils {
    private DaoUtils() {}

    public static LocalDateTime localDateTime(Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    public static Long nullableLong(ResultSet resultSet, String column) throws SQLException {
        Number value = (Number) resultSet.getObject(column);
        return value == null ? null : value.longValue();
    }

    public static Integer nullableInteger(ResultSet resultSet, String column) throws SQLException {
        Number value = (Number) resultSet.getObject(column);
        return value == null ? null : value.intValue();
    }
}
