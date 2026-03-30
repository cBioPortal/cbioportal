package org.cbioportal.legacy.persistence.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

/**
 * MyBatis type handler that converts the sentinel value "N" (used in place of SQL NULL during ETL)
 * back to null when reading String columns. This handles a data migration artifact where the
 * ClickHouse-to-StarRocks ETL stored literal "N" instead of NULL for nullable string fields.
 */
public class NullSentinelStringTypeHandler extends BaseTypeHandler<String> {

  private static String convert(String value) {
    return ("N".equals(value)) ? null : value;
  }

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, parameter);
  }

  @Override
  public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
    return convert(rs.getString(columnName));
  }

  @Override
  public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    return convert(rs.getString(columnIndex));
  }

  @Override
  public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    return convert(cs.getString(columnIndex));
  }
}
