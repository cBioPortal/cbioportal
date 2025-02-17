package org.cbioportal.genomic_data.repository;

import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.GenomicDataCount;
import org.cbioportal.legacy.model.GenomicDataCountItem;
import org.cbioportal.legacy.web.parameter.GenomicDataBinFilter;
import org.cbioportal.legacy.web.parameter.GenomicDataFilter;
import org.cbioportal.studyview.StudyViewFilterContext;

import java.util.List;
import java.util.Map;

/**
 * An interface defining the contract for a repository that provides access to genomic data.
 * This repository is responsible for retrieving various types of genomic data counts and statistics.
 */
public interface GenomicDataRepository {

    /**
     * Retrieves the sample counts for molecular profiles based on the provided study view filter context.
     *
     * @param studyViewFilterContext the context containing study view filter criteria.
     *                               Must not be {@code null}.
     * @return a list of {@link GenomicDataCount} objects representing the sample counts for molecular profiles.
     */
    List<GenomicDataCount> getMolecularProfileSampleCounts(StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves binned genomic data counts based on the provided study view filter context and bin filters.
     *
     * @param studyViewFilterContext the context containing study view filter criteria.
     *                               Must not be {@code null}.
     * @param genomicDataBinFilters  a list of filters to apply to the genomic data for binning.
     *                               Must not be {@code null}.
     * @return a list of {@link ClinicalDataCount} objects representing the binned genomic data counts.
     */
    List<ClinicalDataCount> getGenomicDataBinCounts(StudyViewFilterContext studyViewFilterContext,
                                                    List<GenomicDataBinFilter> genomicDataBinFilters);

    /**
     * Retrieves copy number alteration (CNA) counts based on the provided study view filter context and genomic data filters.
     *
     * @param studyViewFilterContext the context containing study view filter criteria.
     *                               Must not be {@code null}.
     * @param genomicDataFilters     a list of filters to apply to the genomic data.
     *                               Must not be {@code null}.
     * @return a list of {@link GenomicDataCountItem} objects representing the CNA counts.
     */
    List<GenomicDataCountItem> getCNACounts(StudyViewFilterContext studyViewFilterContext, List<GenomicDataFilter> genomicDataFilters);

    /**
     * Retrieves mutation counts based on the provided study view filter context and mutation filters.
     *
     * @param studyViewFilterContext the context containing study view filter criteria.
     *                               Must not be {@code null}.
     * @param mutationFilters        the filter to apply to the mutation data.
     *                               Must not be {@code null}.
     * @return a map where the key is a string representing a mutation type and the value is the count of mutations.
     */
    Map<String, Integer> getMutationCounts(StudyViewFilterContext studyViewFilterContext, GenomicDataFilter mutationFilters);

    /**
     * Retrieves mutation counts grouped by mutation type based on the provided study view filter context and genomic data filters.
     *
     * @param studyViewFilterContext the context containing study view filter criteria.
     *                               Must not be {@code null}.
     * @param genomicDataFilters     a list of filters to apply to the genomic data.
     *                               Must not be {@code null}.
     * @return a list of {@link GenomicDataCountItem} objects representing the mutation counts by type.
     */
    List<GenomicDataCountItem> getMutationCountsByType(StudyViewFilterContext studyViewFilterContext,
                                                       List<GenomicDataFilter> genomicDataFilters);
}
