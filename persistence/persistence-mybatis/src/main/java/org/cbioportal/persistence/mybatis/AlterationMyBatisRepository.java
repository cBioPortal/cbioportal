package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class AlterationMyBatisRepository implements AlterationRepository {

    @Autowired
    private AlterationCountsMapper alterationCountsMapper;

    @Override
    public List<AlterationCountByGene> getSampleAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                 Select<Integer> entrezGeneIds,
                                                                 final Select<MutationEventType> mutationEventTypes,
                                                                 final Select<CNA> cnaEventTypes,
                                                                 QueryElement searchFusions,
                                                                 boolean includeDriver,
                                                                 boolean includeVUS,
                                                                 boolean includeUnknownOncogenicity,
                                                                 Select<String> selectedTiers,
                                                                 boolean includeUnknownTier,
                                                                 boolean includeGermline,
                                                                 boolean includeSomatic,
                                                                 boolean includeUnknownStatus) {

        if (mutationEventTypes != null && !mutationEventTypes.hasAll() && searchFusions != QueryElement.PASS)
            throw new IllegalArgumentException("Filtering for mutations vs. fusions and specifying mutation types" +
                "simultaneously is not permitted.");

        if (((mutationEventTypes == null || mutationEventTypes.hasNone()) && (cnaEventTypes == null || cnaEventTypes.hasNone()))
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())
            || (!includeGermline && !includeSomatic && !includeUnknownStatus
            && !includeDriver && !includeVUS && !includeUnknownOncogenicity && selectedTiers.hasNone() && !includeUnknownTier)) {
            return Collections.emptyList();
        }

        List<Integer> internalSampleIds = alterationCountsMapper.getSampleInternalIds(molecularProfileCaseIdentifiers);

        return alterationCountsMapper.getSampleAlterationCounts(
            internalSampleIds,
            entrezGeneIds,
            createMutationTypeList(mutationEventTypes),
            createCnaTypeList(cnaEventTypes),
            searchFusions,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            selectedTiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);
    }

    @Override
    public List<AlterationCountByGene> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                  Select<Integer> entrezGeneIds,
                                                                  Select<MutationEventType> mutationEventTypes,
                                                                  Select<CNA> cnaEventTypes,
                                                                  QueryElement searchFusions,
                                                                  boolean includeDriver,
                                                                  boolean includeVUS,
                                                                  boolean includeUnknownOncogenicity,
                                                                  Select<String> selectedTiers,
                                                                  boolean includeUnknownTier, boolean includeGermline,
                                                                  boolean includeSomatic,
                                                                  boolean includeUnknownStatus) {

        if (mutationEventTypes != null && !mutationEventTypes.hasAll() && searchFusions != QueryElement.PASS)
            throw new IllegalArgumentException("Filtering for mutations vs. fusions and specifying mutation types" +
                "simultaneously is not permitted.");

        if (((mutationEventTypes == null || mutationEventTypes.hasNone()) && (cnaEventTypes == null || cnaEventTypes.hasNone()))
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())
            || (!includeGermline && !includeSomatic && !includeUnknownStatus
            && !includeDriver && !includeVUS && !includeUnknownOncogenicity && selectedTiers.hasNone() && !includeUnknownTier)) {
            return Collections.emptyList();
        }

        List<Integer> internalPatientIds = alterationCountsMapper.getPatientInternalIds(molecularProfileCaseIdentifiers);

        return alterationCountsMapper.getPatientAlterationCounts(
            internalPatientIds,
            entrezGeneIds,
            createMutationTypeList(mutationEventTypes),
            createCnaTypeList(cnaEventTypes),
            searchFusions,
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            selectedTiers,
            includeUnknownTier,
            includeGermline,
            includeSomatic,
            includeUnknownStatus);
    }

    @Override
    public List<CopyNumberCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                          Select<Integer> entrezGeneIds,
                                                          Select<CNA> cnaEventTypes,
                                                          boolean includeDriver,
                                                          boolean includeVUS,
                                                          boolean includeUnknownOncogenicity,
                                                          Select<String> selectedTiers, boolean includeUnknownTier) {

        if (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty()
            || cnaEventTypes == null || cnaEventTypes.hasNone()
            || (!includeDriver && !includeVUS && !includeUnknownOncogenicity && selectedTiers.hasNone() && !includeUnknownTier)) {
            return Collections.emptyList();
        }

        List<Integer> internalSampleIds = alterationCountsMapper.getSampleInternalIds(molecularProfileCaseIdentifiers);
        
        return alterationCountsMapper.getSampleCnaCounts(
            internalSampleIds,
            entrezGeneIds,
            createCnaTypeList(cnaEventTypes),
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            selectedTiers,
            includeUnknownTier);
    }

    @Override
    public List<CopyNumberCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                           Select<Integer> entrezGeneIds,
                                                           Select<CNA> cnaEventTypes,
                                                           boolean includeDriver,
                                                           boolean includeVUS,
                                                           boolean includeUnknownOncogenicity,
                                                           Select<String> selectedTiers,
                                                           boolean includeUnknownTier) {

        if (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty()
            || cnaEventTypes == null || cnaEventTypes.hasNone()
            || (!includeDriver && !includeVUS && !includeUnknownOncogenicity && selectedTiers.hasNone() && !includeUnknownTier)) {
            return Collections.emptyList();
        }

        List<Integer> internalPatientIds = alterationCountsMapper.getPatientInternalIds(molecularProfileCaseIdentifiers);
        
        return alterationCountsMapper.getPatientCnaCounts(
            internalPatientIds,
            entrezGeneIds,
            createCnaTypeList(cnaEventTypes),
            includeDriver,
            includeVUS,
            includeUnknownOncogenicity,
            selectedTiers,
            includeUnknownTier);
    }
    
    private Select<Short> createCnaTypeList(final Select<CNA> cnaEventTypes) {
        return cnaEventTypes != null ? cnaEventTypes.map(CNA::getCode) : Select.none();
    }

    private Select<String> createMutationTypeList(final Select<MutationEventType> mutationEventTypes) {
        return mutationEventTypes != null ? mutationEventTypes.map(MutationEventType::getMutationType) : Select.none();
    }

}
