package org.cbioportal.legacy.persistence.mybatis.typehandler;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.cbioportal.legacy.model.ClinicalEventSample;

public class ClinicalEventSampleTypeHandler extends BaseTypeHandler<List<ClinicalEventSample>> {
  @Override
  public void setNonNullParameter(
      PreparedStatement ps, int i, List<ClinicalEventSample> parameter, JdbcType jdbcType)
      throws SQLException {
    ps.setString(i, Arrays.toString(parameter.toArray()).replace("[", "").replace("]", ""));
  }

  @Override
  public List<ClinicalEventSample> getNullableResult(ResultSet rs, String columnName)
      throws SQLException {
    Array sqlArray = rs.getArray(columnName);
    return convertClickhouseArrayToSamples(sqlArray);
  }

  @Override
  public List<ClinicalEventSample> getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Array sqlArray = rs.getArray(columnIndex);
    return convertClickhouseArrayToSamples(sqlArray);
  }

  @Override
  public List<ClinicalEventSample> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    Array sqlArray = cs.getArray(columnIndex);
    return convertClickhouseArrayToSamples(sqlArray);
  }

  private List<ClinicalEventSample> convertClickhouseArrayToSamples(Array sqlArray)
      throws SQLException {
    return Arrays.stream((Object[]) sqlArray.getArray())
        .map(
            obj -> {
              List<Object> fields = (List<Object>) obj;
              String sampleId = (String) fields.get(0);
              String uniqPatientId = (String) fields.get(1);
              Integer timeTaken = (Integer) fields.get(2);
              String studyId = (String) fields.get(3);
              String patientId = uniqPatientId.replaceFirst(studyId + "_", "");

              ClinicalEventSample sample = new ClinicalEventSample();
              sample.setSampleId(sampleId);
              sample.setPatientId(patientId);
              sample.setTimeTaken(timeTaken);
              sample.setStudyId(studyId);
              return sample;
            })
        .toList();
  }
}
