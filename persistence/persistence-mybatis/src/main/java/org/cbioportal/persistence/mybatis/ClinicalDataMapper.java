package org.cbioportal.persistence.mybatis;

import org.apache.ibatis.annotations.Param;
import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.meta.BaseMeta;

import java.util.List;

public interface ClinicalDataMapper {

    List<SampleClinicalData> getSampleClinicalData(@Param("studyIds") List<String> studyIds,
                                                   @Param("sampleIds") List<String> sampleIds,
                                                   @Param("attributeId") String attributeId,
                                                   @Param("projection") String projection,
                                                   @Param("limit") Integer limit,
                                                   @Param("offset") Integer offset,
                                                   @Param("sortBy") String sortBy,
                                                   @Param("direction") String direction);

    BaseMeta getMetaSampleClinicalData(@Param("studyIds") List<String> studyIds,
                                       @Param("sampleIds") List<String> sampleIds,
                                       @Param("attributeId") String attributeId);

    List<PatientClinicalData> getPatientClinicalData(@Param("studyIds") List<String> studyIds,
                                                     @Param("patientIds") List<String> patientIds,
                                                     @Param("attributeId") String attributeId,
                                                     @Param("projection") String projection,
                                                     @Param("limit") Integer limit,
                                                     @Param("offset") Integer offset,
                                                     @Param("sortBy") String sortBy,
                                                     @Param("direction") String direction);

    BaseMeta getMetaPatientClinicalData(@Param("studyIds") List<String> studyIds,
                                        @Param("patientIds") List<String> patientIds,
                                        @Param("attributeId") String attributeId);
}
