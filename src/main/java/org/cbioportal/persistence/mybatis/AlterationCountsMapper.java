package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.util.Select;

import java.util.List;

public interface AlterationCountsMapper {

    /**
     * Calculate sample-level counts of mutation and discrete CNA alteration events in genes.
     * @param entrezGeneIds  Gene ids to get counts for.
     * @param mutationTypes  Types of mutations to include in alteration counts. 
     * @param cnaTypes  Types of discrete copy number alteration types to include in alteration counts.
     * @param includeDriver Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeVUS  Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeUnknownOncogenicity  Include variants that are not annotated as driver or VUS. Uses annotations loaded as 'custom driver annotations'.
     * @param selectedTiers  Force alterations assigned to a tier to be interpreted as driver events. Uses tier annotations loaded as 'custom driver annotation tiers'.
     * @param includeUnknownTier Include mutations that have unspecified tier, or tiers with '', 'NA' or 'unknown' in alteration counts
     * @param includeGermline  Include germline mutations in alteration counts
     * @param includeSomatic  Include somatic mutations in alteration counts
     * @return  Gene-level counts of (1) the total number of alterations and (2) the number of altered samples.
     */
    List<AlterationCountByGene> getSampleAlterationGeneCounts(List<MolecularProfileCaseIdentifier> mutationMolecularProfileCaseIdentifiers,
                                                          List<MolecularProfileCaseIdentifier> cnaMolecularProfileCaseIdentifiers,
                                                          List<MolecularProfileCaseIdentifier> structuralVariantMolecularProfileCaseIdentifiers,
                                                          Select<Integer> entrezGeneIds,
                                                          Select<String> mutationTypes,
                                                          Select<Short> cnaTypes,
                                                          boolean includeDriver,
                                                          boolean includeVUS,
                                                          boolean includeUnknownOncogenicity,
                                                          Select<String> selectedTiers,
                                                          boolean includeUnknownTier, boolean includeGermline,
                                                          boolean includeSomatic,
                                                          boolean includeUnknownStatus);

    /**
     * Calculate patient-level counts of mutation and discrete CNA alteration events in genes.
     * @param entrezGeneIds  Gene ids to get counts for.
     * @param mutationTypes  Types of mutations to include in alteration counts.
     * @param cnaTypes  Types of discrete copy number alteration types to include in alteration counts.
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
    List<AlterationCountByGene> getPatientAlterationGeneCounts(List<MolecularProfileCaseIdentifier> mutationMolecularProfileCaseIdentifiers,
                                                           List<MolecularProfileCaseIdentifier> cnaMolecularProfileCaseIdentifiers,
                                                           List<MolecularProfileCaseIdentifier> structuralVariantMolecularProfileCaseIdentifiers,
                                                           Select<Integer> entrezGeneIds,
                                                           Select<String> mutationTypes,
                                                           Select<Short> cnaTypes,
                                                           boolean includeDriver,
                                                           boolean includeVUS,
                                                           boolean includeUnknownOncogenicity,
                                                           Select<String> selectedTiers,
                                                           boolean includeUnknownTier,
                                                           boolean includeGermline,
                                                           boolean includeSomatic,
                                                           boolean includeUnknownStatus);

    // legacy method that returns CopyNumberCountByGene
    List<CopyNumberCountByGene> getSampleCnaGeneCounts(List<MolecularProfileCaseIdentifier> cnaMolecularProfileCaseIdentifiers,
                                                   Select<Integer> entrezGeneIds,
                                                   Select<Short> cnaTypes,
                                                   boolean includeDriver,
                                                   boolean includeVUS,
                                                   boolean includeUnknownOncogenicity,
                                                   Select<String> selectedTiers,
                                                   boolean includeUnknownTier);

    // legacy method that returns CopyNumberCountByGene
    List<CopyNumberCountByGene> getPatientCnaGeneCounts(List<MolecularProfileCaseIdentifier> cnaMolecularProfileCaseIdentifiers,
                                                    Select<Integer> entrezGeneIds,
                                                    Select<Short> cnaTypes,
                                                    boolean includeDriver,
                                                    boolean includeVUS,
                                                    boolean includeUnknownOncogenicity,
                                                    Select<String> selectedTiers,
                                                    boolean includeUnknownTier);

    List<MolecularProfileCaseIdentifier> getMolecularProfileCaseInternalIdentifier(List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers, String caseType);

    /**
     * Calculate sample-level counts of structural variant events.
     * @param includeDriver Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeVUS  Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeUnknownOncogenicity  Include variants that are not annotated as driver or VUS. Uses annotations loaded as 'custom driver annotations'.
     * @param selectedTiers  Force alterations assigned to a tier to be interpreted as driver events. Uses tier annotations loaded as 'custom driver annotation tiers'.
     * @param includeUnknownTier Include mutations that have unspecified tier, or tiers with '', 'NA' or 'unknown' in alteration counts
     * @param includeGermline  Include germline mutations in alteration counts
     * @param includeSomatic  Include somatic mutations in alteration counts
     * @return  StructVar-level counts (GeneA::GeneB) of (1) the total number of alterations and (2) the number of altered samples.
     */
    List<AlterationCountByStructuralVariant> getSampleStructuralVariantCounts(List<MolecularProfileCaseIdentifier> structuralVariantMolecularProfileCaseIdentifiers,
                                                                              boolean includeDriver,
                                                                              boolean includeVUS,
                                                                              boolean includeUnknownOncogenicity,
                                                                              Select<String> selectedTiers,
                                                                              boolean includeUnknownTier,
                                                                              boolean includeGermline,
                                                                              boolean includeSomatic,
                                                                              boolean includeUnknownStatus);
    /**
     * Calculate patient-level counts of structural variant events.
     * @param includeDriver Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeVUS  Include Variants of Unknown significance. Uses annotations loaded as 'custom driver annotations'.
     * @param includeUnknownOncogenicity  Include variants that are not annotated as driver or VUS. Uses annotations loaded as 'custom driver annotations'.
     * @param selectedTiers  Force alterations assigned to a tier to be interpreted as driver events. Uses tier annotations loaded as 'custom driver annotation tiers'.
     * @param includeUnknownTier Include mutations that have unspecified tier, or tiers with '', 'NA' or 'unknown' in alteration counts
     * @param includeGermline  Include germline mutations in alteration counts
     * @param includeSomatic  Include somatic mutations in alteration counts
     * @return  StructVar-level counts (GeneA::GeneB) of (1) the total number of alterations and (2) the number of altered patients.
     */
    List<AlterationCountByStructuralVariant> getPatientStructuralVariantCounts(List<MolecularProfileCaseIdentifier> structuralVariantMolecularProfileCaseIdentifiers,
                                                                              boolean includeDriver,
                                                                              boolean includeVUS,
                                                                              boolean includeUnknownOncogenicity,
                                                                              Select<String> selectedTiers,
                                                                              boolean includeUnknownTier,
                                                                              boolean includeGermline,
                                                                              boolean includeSomatic,
                                                                              boolean includeUnknownStatus);
    
}
