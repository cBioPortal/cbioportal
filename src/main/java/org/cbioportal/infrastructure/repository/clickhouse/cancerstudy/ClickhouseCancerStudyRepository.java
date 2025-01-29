package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import org.cbioportal.cancerstudy.CancerStudyMetadata;
import org.cbioportal.cancerstudy.repository.CancerStudyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository implementation for accessing cancer study metadata from ClickHouse.
 * This class delegates database queries to {@link ClickhouseCancerStudyMapper}.
 */
@Repository
public class ClickhouseCancerStudyRepository implements CancerStudyRepository {
    
    private final ClickhouseCancerStudyMapper cancerStudyMapper;

    /**
     * Constructs a new {@code ClickhouseCancerStudyRepository} with the required mapper.
     *
     * @param cancerStudyMapper the mapper responsible for executing ClickHouse queries
     */
    public ClickhouseCancerStudyRepository(ClickhouseCancerStudyMapper cancerStudyMapper) {
        this.cancerStudyMapper = cancerStudyMapper;
    }

    /**
     * Retrieves detailed metadata for all cancer studies.
     *
     * @return a list of {@link CancerStudyMetadata} containing detailed metadata for each study
     */
    @Override
    public List<CancerStudyMetadata> getCancerStudiesMetadata() {
        return cancerStudyMapper.getCancerStudiesMetadata();
    }

    /**
     * Retrieves a summarized version of cancer study metadata.
     *
     * @return a list of {@link CancerStudyMetadata} containing summarized metadata for each study
     */
    @Override
    public List<CancerStudyMetadata> getCancerStudiesMetadataSummary() {
        return cancerStudyMapper.getCancerStudiesMetadataSummary();
    }
}
