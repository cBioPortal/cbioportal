package org.cbioportal.generic_assay.repository;

import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;

public interface GenericAssayRepository {
    List<MolecularProfile> getGenericAssayProfiles();
    List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(StudyViewFilterContext studyViewFilterContext,
                                                   String alterationType);
    List<ClinicalDataCount> getGenericAssayDataBinCounts(StudyViewFilterContext studyViewFilterContext,
                                                         List<GenericAssayDataBinFilter> genericAssayDataBinFilters);
    List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilterContext studyViewFilterContext,
                                                              List<GenericAssayDataFilter> genericAssayDataFilters);
}
