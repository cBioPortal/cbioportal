package org.cbioportal.infrastructure.repository.clickhouse.typehandlers;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * MyBatis TypeHandler to convert ClickHouse Map(String, String) columns to Java {@code Map<String,
 * String>}.
 */
public class ClickhouseMapTypeHandler extends BaseTypeHandler<Map<String, String>> {

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, Map<String, String> parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setObject(i, parameter);
  }

  @Override
  public Map<String, String> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    return convertToMap(rs.getObject(columnName));
  }

  @Override
  public Map<String, String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return convertToMap(rs.getObject(columnIndex));
  }

  @Override
  public Map<String, String> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return convertToMap(cs.getObject(columnIndex));
  }

  private Map<String, String> convertToMap(Object value) {
    if (value instanceof Map<?, ?> map) {
      LinkedHashMap<String, String> result = new LinkedHashMap<>();
      map.forEach((k, v) -> result.put(String.valueOf(k), v == null ? null : String.valueOf(v)));
      return result;
    }
    return new LinkedHashMap<>();
  }
}
