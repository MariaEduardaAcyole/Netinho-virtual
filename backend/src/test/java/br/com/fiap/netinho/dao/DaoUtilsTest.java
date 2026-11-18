package br.com.fiap.netinho.dao;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifica conversoes JDBC de numeros Oracle e o tratamento de valores nulos.
 */
class DaoUtilsTest {
    @Test
    void convertsOracleNumberValuesToNullableJavaNumbers() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("long_value")).thenReturn(new BigDecimal("566337"));
        when(resultSet.getObject("integer_value")).thenReturn(new BigDecimal("7"));
        when(resultSet.getObject("null_value")).thenReturn(null);

        assertEquals(566337L, DaoUtils.nullableLong(resultSet, "long_value"));
        assertEquals(7, DaoUtils.nullableInteger(resultSet, "integer_value"));
        assertNull(DaoUtils.nullableLong(resultSet, "null_value"));
        assertNull(DaoUtils.nullableInteger(resultSet, "null_value"));
    }
}
