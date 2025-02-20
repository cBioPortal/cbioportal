package org.cbioportal.domain.generic_assay.repository;

import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Repository interface for retrieving molecular profiles and assay-related data.
 */
public interface GenericAssayRepository {

    /**
     * Retrieves all generic assay molecular profiles.
     *
     * @return A list of {@link MolecularProfile} representing generic assay profiles.
     */
    List<MolecularProfile> getGenericAssayProfiles();

    /**
     * Retrieves molecular profiles filtered by alteration type based on the study view filter context.
     *
     * @param studyViewFilterContext The filter criteria for the study view.
     * @param alterationType The type of alteration to filter the molecular profiles.
     * @return A list of {@link MolecularProfile} matching the criteria.
     */
    List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(StudyViewFilterContext studyViewFilterContext,
                                                                        String alterationType);

    /**
     * Retrieves bin counts for generic assay data based on the study view filter context and specified bin filters.
     *
     * @param studyViewFilterContext The filter criteria for the study view.
     * @param genericAssayDataBinFilters A list of bin filters to apply to the assay data.
     * @return A list of {@link ClinicalDataCount} representing bin counts for the generic assay data.
     */
    List<ClinicalDataCount> getGenericAssayDataBinCounts(StudyViewFilterContext studyViewFilterContext,
                                                         List<GenericAssayDataBinFilter> genericAssayDataBinFilters);

    /**
     * Retrieves counts for generic assay data based on the study view filter context and specified data filters.
     *
     * @param studyViewFilterContext The filter criteria for the study view.
     * @param genericAssayDataFilters A list of data filters to apply to the assay data.
     * @return A list of {@link GenericAssayDataCountItem} representing assay data counts.
     */
    List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilterContext studyViewFilterContext,
                                                              List<GenericAssayDataFilter> genericAssayDataFilters);
}
