package org.cbioportal.infrastructure.repository.clickhouse.cancerstudy;

import org.cbioportal.cancerstudy.CancerStudyMetadata;
import org.cbioportal.cancerstudy.repository.CancerStudyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ClickhouseCancerStudyRepository implements CancerStudyRepository {
    
    private final ClickhouseCancerStudyMapper cancerStudyMapper;
    
    @Autowired
    public ClickhouseCancerStudyRepository(ClickhouseCancerStudyMapper cancerStudyMapper) {
        this.cancerStudyMapper = cancerStudyMapper;
    }
    
    /**
     * @return 
     */
    @Override
    public List<CancerStudyMetadata> getCancerStudiesMetadata() {
        return cancerStudyMapper.getCancerStudiesMetadata();
    }
}
