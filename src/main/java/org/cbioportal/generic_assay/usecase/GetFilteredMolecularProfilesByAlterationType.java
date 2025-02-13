package org.cbioportal.generic_assay.usecase;

import org.cbioportal.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.studyview.StudyViewFilterContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("clickhouse")
public class GetFilteredMolecularProfilesByAlterationType {

    private final GenericAssayRepository genericAssayRepository;

    public GetFilteredMolecularProfilesByAlterationType(GenericAssayRepository genericAssayRepository) {
        this.genericAssayRepository = genericAssayRepository;
    }


    public List<MolecularProfile> excute(StudyViewFilterContext studyViewFilterContext,
                                                                        String alterationType) {
        return genericAssayRepository.getFilteredMolecularProfilesByAlterationType(studyViewFilterContext, alterationType);
    }

}
