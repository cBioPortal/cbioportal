package org.cbioportal.persistence.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.cbioportal.model.Sample;
import org.springframework.stereotype.Component;

@Component
public class SampleTypeHelper extends BaseTypeHandler<Sample.SampleType> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Sample.SampleType parameter,
                                    JdbcType jdbcType) throws SQLException {
        throw new SQLException("Method not implemented");
    }

    @Override
    public Sample.SampleType getNullableResult(ResultSet rs, String columnName)
        throws SQLException {
        return Sample.SampleType.fromString(columnName);
    }

    @Override
    public Sample.SampleType getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        throw new SQLException("Method not implemented");
    }

    @Override
    public Sample.SampleType getNullableResult(CallableStatement cs, int columnIndex)
        throws SQLException {
        throw new SQLException("Method not implemented");
    }
}
