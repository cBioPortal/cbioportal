package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface PatientMapper {

    List<Patient> getPatients(@Param("studyIds") List<String> studyIds,
                              @Param("patientIds") List<String> patientIds,
                              @Param("projection") String projection,
                              @Param("limit") Integer limit,
                              @Param("offset") Integer offset,
                              @Param("sortBy") String sortBy,
                              @Param("direction") String direction);

    BaseMeta getMetaPatients(@Param("studyIds") List<String> studyIds,
                             @Param("patientIds") List<String> patientIds);

    Patient getPatient(@Param("studyId") String studyId,
                       @Param("patientId") String patientId,
                       @Param("projection") String projection);
}
