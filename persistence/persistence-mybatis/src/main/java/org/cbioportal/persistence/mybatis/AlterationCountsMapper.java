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
     * @return  Gene-level counts of (1) the total number of alterations and (2) the number of altered samples.
     */
    List<AlterationCountByGene> getSampleAlterationCounts(List<Integer> internalSampleIds,
                                                          Select<Integer> entrezGeneIds,
                                                          Select<String> mutationTypes,
                                                          Select<Short> cnaTypes,
                                                          QueryElement searchFusions);

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
     * @return  Gene-level counts of (1) the total number of alterations and (2) the number of altered patients.
     */
    List<AlterationCountByGene> getPatientAlterationCounts(List<Integer> internalPatientIds,
                                                           Select<Integer> entrezGeneIds,
                                                           Select<String> mutationTypes,
                                                           Select<Short> cnaTypes,
                                                           QueryElement searchFusions);

    /**
     * Gets internal patient ids for patients that match (molecularProfileId, patientId) pair
     * @param molecularProfileCaseIdentifiers  List of molecularProfileId, patientId pairs
     * @return  Gene-level counts of (1) the total number of alterations and (2) the number of altered samples.
     */
    List<Integer> getPatientInternalIds(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers);

    // legacy method that returns CopyNumberCountByGene
    List<CopyNumberCountByGene> getSampleCnaCounts(List<Integer> internalSampleIds,
                                                   Select<Integer> entrezGeneIds,
                                                   Select<Short> cnaTypes);

    // legacy method that returns CopyNumberCountByGene
    List<CopyNumberCountByGene> getPatientCnaCounts(List<Integer> internalPatientIds,
                                                    Select<Integer> entrezGeneIds,
                                                    Select<Short> cnaTypes);
}
