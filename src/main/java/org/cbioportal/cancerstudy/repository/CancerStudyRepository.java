package org.cbioportal.cancerstudy.repository;

import org.cbioportal.cancerstudy.CancerStudyMetadata;

import java.util.List;

/**
 * Repository interface for accessing cancer study.
 */
public interface CancerStudyRepository {
    
    /**
     * Retrieves a list of metadata for all cancer studies.
     *
     * @return a list of {@link CancerStudyMetadata} objects containing metadata for cancer studies.
     */
    List<CancerStudyMetadata> getCancerStudiesMetadata();
}
