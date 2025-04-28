package org.cbioportal.infrastructure.repository.clickhouse.alteration;

import java.util.List;

import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.CopyNumberCountByGene;
import org.cbioportal.legacy.model.GenePanelToGene;
import org.cbioportal.legacy.model.MolecularProfile;
import org.cbioportal.legacy.persistence.helper.AlterationFilterHelper;

/**
 * Mapper interface for retrieving alteration-related data from ClickHouse.
 * This interface provides methods for fetching mutated gene counts, copy number alteration counts,
 * structural variant gene counts, and more, based on the provided study view filter context and alteration filters.
 */
public interface ClickhouseAlterationMapper {
    /**
     * Retrieves mutated gene counts based on the study view filter context and alteration filter.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationFilterHelper the helper for applying alteration filters
     * @return a list of mutated genes with their respective counts
     */
    List<AlterationCountByGene> getMutatedGenes(
        StudyViewFilterContext studyViewFilterContext,
        AlterationFilterHelper alterationFilterHelper
    );

    /**
     * Retrieves copy number alteration (CNA) gene counts based on the study view filter context and alteration filter.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationFilterHelper the helper for applying alteration filters
     * @return a list of CNA genes with their respective counts
     */
    List<CopyNumberCountByGene> getCnaGenes(
        StudyViewFilterContext studyViewFilterContext,
        AlterationFilterHelper alterationFilterHelper
    );

    /**
     * Retrieves structural variant gene counts based on the study view filter context and alteration filter.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationFilterHelper the helper for applying alteration filters
     * @return a list of structural variant genes with their respective counts
     */
    List<AlterationCountByGene> getStructuralVariantGenes(
        StudyViewFilterContext studyViewFilterContext,
        AlterationFilterHelper alterationFilterHelper
    );

    /**
     * Retrieves the total profiled count for a given alteration type.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationType         the type of alteration (e.g., mutation, CNA, etc.)
     * @return the total profiled count for the given alteration type
     */
    int getTotalProfiledCountByAlterationType(
        StudyViewFilterContext studyViewFilterContext,
        String alterationType
    );

    /**
     * Retrieves the matching gene panel IDs for a given alteration type.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationType         the type of alteration (e.g., mutation, CNA, etc.)
     * @return a list of matching gene panel IDs
     */
    List<GenePanelToGene> getMatchingGenePanelIds(
        StudyViewFilterContext studyViewFilterContext,
        String alterationType
    );

    /**
     * Retrieves the total profiled counts for a given alteration type and molecular profiles.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationType         the type of alteration (e.g., mutation, CNA, etc.)
     * @param molecularProfiles      the list of molecular profiles to be considered
     * @return a list of alteration counts by gene
     */
    List<AlterationCountByGene> getTotalProfiledCounts(
        StudyViewFilterContext studyViewFilterContext,
        String alterationType,
        List<MolecularProfile> molecularProfiles
    );

    /**
     * Retrieves the sample profile count without panel data for a given alteration type.
     *
     * @param studyViewFilterContext the context of the study view filter
     * @param alterationType         the type of alteration (e.g., mutation, CNA, etc.)
     * @return the sample profile count without panel data
     */
    int getSampleProfileCountWithoutPanelData(
        StudyViewFilterContext studyViewFilterContext,
        String alterationType
    );

    List<AlterationCountByGene> getAlterationCountByGeneGivenSamplesAndMolecularProfiles(
        String[] samples,
        String[] molecularProfiles,
        AlterationFilterHelper alterationFilterHelper
    );

    List<AlterationCountByGene> getAlterationCountByGeneGivenPatientsAndMolecularProfiles(
        String[] samples,
        String[] molecularProfiles,
        AlterationFilterHelper alterationFilterHelper
    );

    List<MolecularProfile> getAllMolecularProfiles();
}
