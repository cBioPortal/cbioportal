package org.cbioportal.infrastructure.repository.generic_assay;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class V2GenericAssayRepository implements org.cbioportal.domain.generic_assay.repository.GenericAssayRepository {

    private final V2GenericAssayMapper mapper;

    public V2GenericAssayRepository(V2GenericAssayMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<MolecularProfile> getGenericAssayProfiles() {
        return mapper.getGenericAssayProfiles();
    }

    @Override
    public List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType) {
        return mapper.getFilteredMolecularProfilesByAlterationType(studyViewFilterContext, alterationType);
    }

    @Override
    public List<ClinicalDataCount> getGenericAssayDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataBinFilter> genericAssayDataBinFilters) {
        return mapper.getGenericAssayDataBinCounts(studyViewFilterContext, genericAssayDataBinFilters);
    }

    @Override
    public List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataFilter> genericAssayDataFilters) {
        return mapper.getGenericAssayDataCounts(studyViewFilterContext, genericAssayDataFilters);
    }
}
