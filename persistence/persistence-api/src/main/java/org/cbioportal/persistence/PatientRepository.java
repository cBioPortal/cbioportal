package org.cbioportal.persistence;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface PatientRepository {

    @Cacheable("GeneralRepositoryCache")
    List<Patient> getAllPatients(String keyword, String projection, Integer pageSize, Integer pageNumber, String sortBy,
        String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaPatients(String keyword);

    @Cacheable("GeneralRepositoryCache")
    List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, 
                                        String sortBy, String direction);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta getMetaPatientsInStudy(String studyId);

    @Cacheable("GeneralRepositoryCache")
    Patient getPatientInStudy(String studyId, String patientId);

    @Cacheable("GeneralRepositoryCache")
    List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection);

    @Cacheable("GeneralRepositoryCache")
    BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds);

    @Cacheable("GeneralRepositoryCache")
    List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds);
}
