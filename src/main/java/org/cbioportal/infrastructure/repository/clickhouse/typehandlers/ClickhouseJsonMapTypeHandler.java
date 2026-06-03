package org.cbioportal.infrastructure.repository.clickhouse.typehandlers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

public class ClickhouseJsonMapTypeHandler extends BaseTypeHandler<Map<String, Object>> {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<LinkedHashMap<String, Object>> TYPE_REFERENCE =
      new TypeReference<>() {};

  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, Map<String, Object> parameter, JdbcType jdbcType)
      throws SQLException {
    try {
      ps.setString(i, OBJECT_MAPPER.writeValueAsString(parameter));
    } catch (Exception e) {
      throw new SQLException("Unable to serialize JSON metadata", e);
    }
  }

  @Override
  public Map<String, Object> getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return convertToMap(rs.getObject(columnName));
  }

  @Override
  public Map<String, Object> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return convertToMap(rs.getObject(columnIndex));
  }

  @Override
  public Map<String, Object> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    return convertToMap(cs.getObject(columnIndex));
  }

  private Map<String, Object> convertToMap(Object value) throws SQLException {
    if (value == null) {
      return new LinkedHashMap<>();
    }
    if (value instanceof Map<?, ?> map) {
      LinkedHashMap<String, Object> result = new LinkedHashMap<>();
      map.forEach((k, v) -> result.put(String.valueOf(k), v));
      return result;
    }
    try {
      return OBJECT_MAPPER.readValue(String.valueOf(value), TYPE_REFERENCE);
    } catch (Exception e) {
      throw new SQLException("Unable to deserialize JSON metadata", e);
    }
  }
}
