package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.springframework.cache.annotation.Cacheable;

public interface PatientRepository {
    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<Patient> getAllPatients(
        String keyword,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    BaseMeta getMetaPatients(String keyword);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<Patient> getAllPatientsInStudy(
        String studyId,
        String projection,
        Integer pageSize,
        Integer pageNumber,
        String sortBy,
        String direction
    );

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    BaseMeta getMetaPatientsInStudy(String studyId);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    Patient getPatientInStudy(String studyId, String patientId);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<Patient> fetchPatients(
        List<String> studyIds,
        List<String> patientIds,
        String projection
    );

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    BaseMeta fetchMetaPatients(List<String> studyIds, List<String> patientIds);

    @Cacheable(
        cacheNames = "GeneralRepositoryCache",
        condition = "@cacheEnabledConfig.getEnabled()"
    )
    List<Patient> getPatientsOfSamples(
        List<String> studyIds,
        List<String> sampleIds
    );
}
