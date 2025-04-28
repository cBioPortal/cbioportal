package org.cbioportal.infrastructure.repository.clickhouse.generic_assay;

import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenericAssayDataCountItem;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.web.parameter.GenericAssayDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenericAssayDataFilter;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Mapper interface for retrieving generic assay data from ClickHouse.
 * This interface provides methods to fetch molecular profiles and generic assay data counts,
 * as well as filtered molecular profiles by alteration type.
 */
public interface ClickhouseGenericAssayMapper {

    /**
     * Retrieves the list of molecular profiles from the generic assay data.
     *
     * @return a list of molecular profiles
     */
    List<MolecularProfile> getGenericAssayProfiles();

    /**
     * Retrieves the filtered molecular profiles based on the study view filter context and alteration type.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationType         the alteration type (e.g., mutation, CNA)
     * @return a list of filtered molecular profiles
     */
    List<MolecularProfile> getFilteredMolecularProfilesByAlterationType(StudyViewFilterContext studyViewFilterContext, String alterationType);

    /**
     * Retrieves the generic assay data bin counts based on the study view filter context and the provided bin filters.
     *
     * @param studyViewFilterContext     the context of the study view filter
     * @param genericAssayDataBinFilters the list of bin filters
     * @return a list of generic assay data bin counts
     */
    List<ClinicalDataCount> getGenericAssayDataBinCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataBinFilter> genericAssayDataBinFilters);

    /**
     * Retrieves the generic assay data counts based on the study view filter context and the provided data filters.
     *
     * @param studyViewFilterContext  the context of the study view filter
     * @param genericAssayDataFilters the list of data filters
     * @return a list of generic assay data counts
     */
    List<GenericAssayDataCountItem> getGenericAssayDataCounts(StudyViewFilterContext studyViewFilterContext, List<GenericAssayDataFilter> genericAssayDataFilters);
}

