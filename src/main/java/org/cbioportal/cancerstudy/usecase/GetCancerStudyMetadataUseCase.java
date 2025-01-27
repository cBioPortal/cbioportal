package org.cbioportal.cancerstudy.usecase;

import org.cbioportal.cancerstudy.CancerStudyMetadata;
import org.cbioportal.cancerstudy.repository.CancerStudyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class GetCancerStudyMetadataUseCase {
    private final CancerStudyRepository studyRepository;
    
    public GetCancerStudyMetadataUseCase(CancerStudyRepository studyRepository) {
        this.studyRepository = studyRepository;
    }
    
    public List<CancerStudyMetadata> execute(){
        return studyRepository.getCancerStudiesMetadata();
    }
}
