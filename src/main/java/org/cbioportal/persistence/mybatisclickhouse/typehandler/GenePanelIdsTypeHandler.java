package org.cbioportal.persistence.mybatisclickhouse.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class GenePanelIdsTypeHandler extends BaseTypeHandler<Set<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Set<String> parameter, JdbcType jdbcType) throws SQLException {
        // Convert Set to array for storage (if needed)
        throw new UnsupportedOperationException("Storage of GenePanelIds not supported");
    }

    @Override
    public Set<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String[] array = (String[]) rs.getArray(columnName).getArray();
        return new HashSet<>(Arrays.asList(array));
    }

    @Override
    public Set<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String[] array = (String[]) rs.getArray(columnIndex).getArray();
        return new HashSet<>(Arrays.asList(array));
    }

    @Override
    public Set<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String[] array = (String[]) cs.getArray(columnIndex).getArray();
        return new HashSet<>(Arrays.asList(array));
    }
}
