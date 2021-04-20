package org.cbioportal.persistence.mybatis.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StringSplitToIntsHandler implements TypeHandler<Set<Integer>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Set<Integer> parameter, JdbcType jdbcType) throws SQLException {
        if (parameter != null) {
            ps.setString(i, parameter.toString());
        }
    }

    @Override
    public Set<Integer> getResult(ResultSet rs, String columnName) throws SQLException {
        return split(rs.getString(columnName));
    }

    @Override
    public Set<Integer> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return split(rs.getString(columnIndex));
    }

    @Override
    public Set<Integer> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return split(cs.getString(columnIndex));
    }
    
    private Set<Integer> split(String result) {
        if (result != null) {
            return Arrays.stream(result.replaceAll("[ ]", "").split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        }
        return new HashSet<>();
    }
}
