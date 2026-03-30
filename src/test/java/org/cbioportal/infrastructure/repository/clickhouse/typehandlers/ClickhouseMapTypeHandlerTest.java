package org.cbioportal.infrastructure.repository.clickhouse.typehandlers;

import static org.junit.Assert.*;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ClickhouseMapTypeHandlerTest {

  private ClickhouseMapTypeHandler handler;

  @Before
  public void setUp() {
    handler = new ClickhouseMapTypeHandler();
  }

  @Test
  public void getNullableResult_withNull_returnsEmptyMap() throws SQLException {
    ResultSet rs = Mockito.mock(ResultSet.class);
    Mockito.when(rs.getObject("col")).thenReturn(null);

    Map<String, String> result = handler.getNullableResult(rs, "col");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getNullableResult_withStringMap_returnsConvertedMap() throws SQLException {
    ResultSet rs = Mockito.mock(ResultSet.class);
    HashMap<String, String> input = new HashMap<>();
    input.put("NAME", "my_gene");
    input.put("DESCRIPTION", "a description");
    Mockito.when(rs.getObject("col")).thenReturn(input);

    Map<String, String> result = handler.getNullableResult(rs, "col");

    assertEquals("my_gene", result.get("NAME"));
    assertEquals("a description", result.get("DESCRIPTION"));
  }

  @Test
  public void getNullableResult_withNonStringValues_convertsViaStringValueOf() throws SQLException {
    // ClickHouse JDBC may return Map<String, Object> with non-String values
    ResultSet rs = Mockito.mock(ResultSet.class);
    LinkedHashMap<String, Object> input = new LinkedHashMap<>();
    input.put("count", 42);
    input.put("flag", true);
    input.put("empty", null);
    Mockito.when(rs.getObject("col")).thenReturn(input);

    Map<String, String> result = handler.getNullableResult(rs, "col");

    assertEquals("42", result.get("count"));
    assertEquals("true", result.get("flag"));
    assertNull(result.get("empty"));
  }

  @Test
  public void getNullableResult_byColumnIndex_withNull_returnsEmptyMap() throws SQLException {
    ResultSet rs = Mockito.mock(ResultSet.class);
    Mockito.when(rs.getObject(1)).thenReturn(null);

    Map<String, String> result = handler.getNullableResult(rs, 1);

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void getNullableResult_callableStatement_withMap_returnsConvertedMap()
      throws SQLException {
    CallableStatement cs = Mockito.mock(CallableStatement.class);
    HashMap<String, String> input = new HashMap<>();
    input.put("key", "value");
    Mockito.when(cs.getObject(1)).thenReturn(input);

    Map<String, String> result = handler.getNullableResult(cs, 1);

    assertEquals("value", result.get("key"));
  }

  @Test
  public void getNullableResult_withUnrecognizedType_returnsEmptyMap() throws SQLException {
    ResultSet rs = Mockito.mock(ResultSet.class);
    Mockito.when(rs.getObject("col")).thenReturn("not a map");

    Map<String, String> result = handler.getNullableResult(rs, "col");

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }
}
