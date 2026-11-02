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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClickhouseJsonMapTypeHandler extends BaseTypeHandler<Map<String, Object>> {

  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseJsonMapTypeHandler.class);
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
  public Map<String, Object> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
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
    String text = String.valueOf(value).trim();
    if (text.isEmpty()) {
      return new LinkedHashMap<>();
    }
    try {
      return OBJECT_MAPPER.readValue(text, TYPE_REFERENCE);
    } catch (Exception e) {
      // One unreadable value must not fail the page it appears on. The column is curator-supplied
      // and the row is still worth showing; the metadata columns render empty for it.
      LOG.warn("Ignoring metadata that is not a JSON object: {}", abbreviate(text));
      return new LinkedHashMap<>();
    }
  }

  private static String abbreviate(String text) {
    return text.length() <= 80 ? text : text.substring(0, 80) + "...";
  }
}
