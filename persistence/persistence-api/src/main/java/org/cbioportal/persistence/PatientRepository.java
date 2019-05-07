package org.cbioportal.persistence;

import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;

import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public interface PatientRepository {

    @Cacheable("RepositoryCache")
    List<Patient> getAllPatients(String keyword, String projection, Integer pageSize, Integer pageNumber, String sortBy,
        String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaPatients(String keyword);

    @Cacheable("RepositoryCache")
    List<Patient> getAllPatientsInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber, 
                                        String sortBy, String direction);

    @Cacheable("RepositoryCache")
    BaseMeta getMetaPatientsInStudy(String studyId);

    @Cacheable("RepositoryCache")
    Patient getPatientInStudy(String studyId, String patientId);

    @Cacheable("RepositoryCache")
    List<Patient> fetchPatients(List<String> studyIds, List<String> patientIds, String projection);

    @Cacheable("RepositoryCache")
    BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds);

    @Cacheable("RepositoryCache")
    List<Patient> getPatientsOfSamples(List<String> studyIds, List<String> sampleIds);
}
