package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;

import java.util.List;

public interface AlterationCountsMapper {

    /**
     * Calculate sample-level counts of mutation and discrete CNA alteration events.
     * @param internalSampleIds List of internal id's of samples to include in alteration counts.
     * @param entrezGeneIds  Gene ids to get counts for.
     * @param mutationTypes  Types of mutations to include in alteration counts. 
     * @param cnaTypes  Types of discrete copy number alteration types to include in alteration counts.
     * @param searchFusions  'ACTIVE': counts are limited to fusion type. 'INACTIVE': counts are limited to non-fusion alterations.'PASS': no filtering on mutation vs fusions (mutation types and cnaTypes are used)
     * @param includeDriver Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeVUS  Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeUnknownOncogenicity  Include variants that are not annotated as driver or VUS. Uses annotations loaded as 'custom driver annotations'.
     * @param selectedTiers  Force alterations assigned to a tier to be interpreted as driver events. Uses tier annotations loaded as 'custom driver annotation tiers'.
     * @param includeUnknownTier Include mutations that have unspecified tier, or tiers with '', 'NA' or 'unknown' in alteration counts
     * @param includeGermline  Include germline mutations in alteration counts
     * @param includeSomatic  Include somatic mutations in alteration counts
     * @return  Gene-level counts of (1) the total number of alterations and (2) the number of altered samples.
     */
    List<AlterationCountByGene> getSampleAlterationCounts(List<Integer> internalSampleIds,
                                                          Select<Integer> entrezGeneIds,
                                                          Select<String> mutationTypes,
                                                          Select<Short> cnaTypes,
                                                          QueryElement searchFusions,
                                                          boolean includeDriver,
                                                          boolean includeVUS,
                                                          boolean includeUnknownOncogenicity,
                                                          Select<String> selectedTiers,
                                                          boolean includeUnknownTier, boolean includeGermline,
                                                          boolean includeSomatic,
                                                          boolean includeUnknownStatus);

    /**
     * Gets internal sample ids for samples that match (molecularProfileId, sampleId) pair
     * @param molecularProfileCaseIdentifiers  List of molecularProfileId, sampleId pairs
     * @return Gene-level counts of (1) the total number of alterations and (2) the number of altered samples.
     */
    List<Integer> getSampleInternalIds(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers);

    /**
     * Calculate patient-level counts of mutation and discrete CNA alteration events.
     * @param internalPatientIds List of internal id's of patients to include in alteration counts.
     * @param entrezGeneIds  Gene ids to get counts for.
     * @param mutationTypes  Types of mutations to include in alteration counts.
     * @param cnaTypes  Types of discrete copy number alteration types to include in alteration counts.
     * @param searchFusions  'ACTIVE': counts are limited to fusion type. 'INACTIVE': counts are limited to non-fusion alterations.'PASS': no filtering on mutation vs fusions (mutation types and cnaTypes are used) 
     * @param includeDriver Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeVUS  Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeUnknownOncogenicity  Include variants that are not annotated as driver or VUS. Uses annotations loaded as 'custom driver annotations'.
     * @param selectedTiers  Force alterations assigned to a tier to be interpreted as driver events. Uses tier annotations loaded as 'custom driver annotation tiers'.
     * @param includeUnknownTier  Include mutations that have unspecified tier, or tiers with '', 'NA' or 'unknown' in alteration counts
     * @param includeGermline  Include germline mutations in alteration counts
     * @param includeSomatic  Include somatic mutations in alteration counts
     * @param includeUnknownStatus  Include mutations that have mutation status 'unknown' in alteration counts
     * @return  Gene-level counts of (1) the total number of alterations and (2) the number of altered patients.
     */
    List<AlterationCountByGene> getPatientAlterationCounts(List<Integer> internalPatientIds,
                                                           Select<Integer> entrezGeneIds,
                                                           Select<String> mutationTypes,
                                                           Select<Short> cnaTypes,
                                                           QueryElement searchFusions,
                                                           boolean includeDriver,
                                                           boolean includeVUS,
                                                           boolean includeUnknownOncogenicity,
                                                           Select<String> selectedTiers,
                                                           boolean includeUnknownTier, boolean includeGermline,
                                                           boolean includeSomatic,
                                                           boolean includeUnknownStatus);

    /**
     * Gets internal patient ids for patients that match (molecularProfileId, patientId) pair
     * @param molecularProfileCaseIdentifiers  List of molecularProfileId, patientId pairs
     * @return  Gene-level counts of (1) the total number of alterations and (2) the number of altered samples.
     */
    List<Integer> getPatientInternalIds(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers);

    // legacy method that returns CopyNumberCountByGene
    List<CopyNumberCountByGene> getSampleCnaCounts(List<Integer> internalSampleIds,
                                                   Select<Integer> entrezGeneIds,
                                                   Select<Short> cnaTypes,
                                                   boolean includeDriver,
                                                   boolean includeVUS,
                                                   boolean includeUnknownOncogenicity,
                                                   Select<String> selectedTiers,
                                                   boolean includeUnknownTier);

    // legacy method that returns CopyNumberCountByGene
    List<CopyNumberCountByGene> getPatientCnaCounts(List<Integer> internalPatientIds,
                                                    Select<Integer> entrezGeneIds,
                                                    Select<Short> cnaTypes,
                                                    boolean includeDriver,
                                                    boolean includeVUS,
                                                    boolean includeUnknownOncogenicity,
                                                    Select<String> selectedTiers,
                                                    boolean includeUnknownTier);
}
