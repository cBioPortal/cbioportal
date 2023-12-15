package org.cbioportal.persistence.mybatis.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.cbioportal.model.Sample;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Sample.SampleType.class)
public class SampleTypeTypeHandler implements TypeHandler<Sample.SampleType> {

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, Sample.SampleType sampleType, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, sampleType.getValue());
    }

    @Override
    public Sample.SampleType getResult(ResultSet resultSet, String s) throws SQLException {
        return Sample.SampleType.fromString(resultSet.getString(s));
    }

    @Override
    public Sample.SampleType getResult(ResultSet resultSet, int i) throws SQLException {
        return Sample.SampleType.fromString(resultSet.getString(i));
    }

    @Override
    public Sample.SampleType getResult(CallableStatement callableStatement, int i) throws SQLException {
        return Sample.SampleType.fromString(callableStatement.getString(i));
    }
}
