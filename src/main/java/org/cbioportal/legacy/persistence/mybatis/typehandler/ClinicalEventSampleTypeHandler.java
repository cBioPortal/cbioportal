package org.cbioportal.legacy.persistence.mybatis.typehandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
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
    String value = rs.getString(columnName);
    return parseSamplesString(value);
  }

  @Override
  public List<ClinicalEventSample> getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    String value = rs.getString(columnIndex);
    return parseSamplesString(value);
  }

  @Override
  public List<ClinicalEventSample> getNullableResult(CallableStatement cs, int columnIndex)
      throws SQLException {
    String value = cs.getString(columnIndex);
    return parseSamplesString(value);
  }

  /**
   * Parses a semicolon-separated string of pipe-delimited sample records. Format:
   * "sampleId|patientUniqueId|timeTaken|studyId;sampleId|..." This is produced by
   * GROUP_CONCAT(...SEPARATOR ';') in the SQL query.
   */
  private List<ClinicalEventSample> parseSamplesString(String value) {
    if (value == null || value.isEmpty()) {
      return Collections.emptyList();
    }
    return Arrays.stream(value.split(";"))
        .filter(s -> !s.isEmpty())
        .map(
            entry -> {
              String[] fields = entry.split("\\|", -1);
              String sampleId = fields[0];
              String uniqPatientId = fields.length > 1 ? fields[1] : "";
              Integer timeTaken =
                  fields.length > 2 && !fields[2].isEmpty() ? Integer.parseInt(fields[2]) : null;
              String studyId = fields.length > 3 ? fields[3] : "";
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
