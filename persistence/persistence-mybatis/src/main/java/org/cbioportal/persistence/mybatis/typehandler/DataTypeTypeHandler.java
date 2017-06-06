package org.cbioportal.persistence.mybatis.typehandler;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;
import org.cbioportal.model.GeneticProfile;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(GeneticProfile.DataType.class)
public class DataTypeTypeHandler implements TypeHandler<GeneticProfile.DataType> {

    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, GeneticProfile.DataType dataType, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, dataType.getValue());
    }

    @Override
    public GeneticProfile.DataType getResult(ResultSet resultSet, String s) throws SQLException {
        return GeneticProfile.DataType.fromString(resultSet.getString(s));
    }

    @Override
    public GeneticProfile.DataType getResult(ResultSet resultSet, int i) throws SQLException {
        return GeneticProfile.DataType.fromString(resultSet.getString(i));
    }

    @Override
    public GeneticProfile.DataType getResult(CallableStatement callableStatement, int i) throws SQLException {
        return GeneticProfile.DataType.fromString(callableStatement.getString(i));
    }
}
