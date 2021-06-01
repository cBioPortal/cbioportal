package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.QueryElement;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class AlterationMyBatisRepository implements AlterationRepository {

    @Autowired
    private AlterationCountsMapper alterationCountsMapper;
    @Autowired
    private MolecularProfileRepository molecularProfileRepository;

    @Override
    public List<AlterationCountByGene> getSampleAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                 Select<Integer> entrezGeneIds,
                                                                 QueryElement searchFusions,
                                                                 AlterationFilter alterationFilter) {

        if (!alterationFilter.getSelectedMutationTypes().hasAll() && searchFusions != QueryElement.PASS)
            throw new IllegalArgumentException("Filtering for mutations vs. fusions and specifying mutation types" +
                "simultaneously is not permitted.");

        if ((alterationFilter.getSelectedMutationTypes().hasNone() && alterationFilter.getSelectedCnaTypes().hasNone())
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())
            || (allAlterationsExcludedDriverAnnotation(alterationFilter) && allAlterationsExcludedMutationStatus(alterationFilter))) {
            return Collections.emptyList();
        }

        List<String> molecularProfileIds = molecularProfileCaseIdentifiers.stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId).distinct().collect(Collectors.toList());

        Map<String, MolecularAlterationType> profileTypeByProfileId = molecularProfileRepository
                .getMolecularProfiles(molecularProfileIds, "SUMMARY").stream()
                .collect(Collectors.toMap(MolecularProfile::getStableId, MolecularProfile::getMolecularAlterationType));

        Map<MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType = molecularProfileCaseIdentifiers
                .stream().collect(Collectors.groupingBy(e -> {
                    return profileTypeByProfileId.getOrDefault(e.getMolecularProfileId(), null);
                }));
        return alterationCountsMapper.getSampleAlterationCounts(
            groupedIdentifiersByProfileType.get(MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.STRUCTURAL_VARIANT),
            entrezGeneIds,
            createMutationTypeList(alterationFilter),
            createCnaTypeList(alterationFilter),
            searchFusions,
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier(),
            alterationFilter.getIncludeGermline(),
            alterationFilter.getIncludeSomatic(),
            alterationFilter.getIncludeUnknownStatus());
    }

    @Override
    public List<AlterationCountByGene> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                  Select<Integer> entrezGeneIds,
                                                                  QueryElement searchFusions,
                                                                  AlterationFilter alterationFilter) {

        if (!alterationFilter.getSelectedMutationTypes().hasAll() && searchFusions != QueryElement.PASS)
            throw new IllegalArgumentException("Filtering for mutations vs. fusions and specifying mutation types" +
                "simultaneously is not permitted.");

        if ((alterationFilter.getSelectedMutationTypes().hasNone() && alterationFilter.getSelectedCnaTypes().hasNone())
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())
            || (allAlterationsExcludedDriverAnnotation(alterationFilter) && allAlterationsExcludedMutationStatus(alterationFilter))) {
            return Collections.emptyList();
        }

        List<String> molecularProfileIds = molecularProfileCaseIdentifiers.stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId).distinct().collect(Collectors.toList());

        Map<String, MolecularAlterationType> profileTypeByProfileId = molecularProfileRepository
                .getMolecularProfiles(molecularProfileIds, "SUMMARY").stream()
                .collect(Collectors.toMap(MolecularProfile::getStableId, MolecularProfile::getMolecularAlterationType));

        Map<MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType = molecularProfileCaseIdentifiers
                .stream().collect(Collectors.groupingBy(e -> {
                    return profileTypeByProfileId.getOrDefault(e.getMolecularProfileId(), null);
                }));

        return alterationCountsMapper.getPatientAlterationCounts(
            groupedIdentifiersByProfileType.get(MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.STRUCTURAL_VARIANT),
            entrezGeneIds,
            createMutationTypeList(alterationFilter),
            createCnaTypeList(alterationFilter),
            searchFusions,
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier(),
            alterationFilter.getIncludeGermline(),
            alterationFilter.getIncludeSomatic(),
            alterationFilter.getIncludeUnknownStatus());
    }

    @Override
    public List<CopyNumberCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                          Select<Integer> entrezGeneIds,
                                                          AlterationFilter alterationFilter) {

        if (alterationFilter.getSelectedCnaTypes().hasNone() || molecularProfileCaseIdentifiers == null
            || molecularProfileCaseIdentifiers.isEmpty() || allAlterationsExcludedDriverAnnotation(alterationFilter)) {
            return Collections.emptyList();
        }
        
        return alterationCountsMapper.getSampleCnaCounts(
            molecularProfileCaseIdentifiers,
            entrezGeneIds,
            createCnaTypeList(alterationFilter),
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier());
    }

    @Override
    public List<CopyNumberCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                           Select<Integer> entrezGeneIds,
                                                           AlterationFilter alterationFilter) {

        if (alterationFilter.getSelectedCnaTypes().hasNone() || molecularProfileCaseIdentifiers == null
            || molecularProfileCaseIdentifiers.isEmpty() || allAlterationsExcludedDriverAnnotation(alterationFilter)) {
            return Collections.emptyList();
        }
        
        return alterationCountsMapper.getPatientCnaCounts(
            molecularProfileCaseIdentifiers,
            entrezGeneIds,
            createCnaTypeList(alterationFilter),
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier());
    }
    
    private Select<Short> createCnaTypeList(final AlterationFilter alterationFilter) {
        if (alterationFilter.getSelectedCnaTypes().hasNone())
            return Select.none();
        if (alterationFilter.getSelectedCnaTypes().hasAll())
            return Select.all();
        return alterationFilter.getSelectedCnaTypes().map(CNA::getCode);
    }

    private Select<String> createMutationTypeList(final AlterationFilter alterationFilter) {
        if (alterationFilter.getSelectedMutationTypes().hasNone())
            return Select.none();
        if (alterationFilter.getSelectedMutationTypes().hasAll())
            return Select.all();
        Select<String> mappedMutationTypes = alterationFilter.getSelectedMutationTypes().map(MutationEventType::getMutationType);
        mappedMutationTypes.inverse(alterationFilter.getSelectedMutationTypes().inverse());

        return mappedMutationTypes;
    }

    private boolean allAlterationsExcludedMutationStatus(AlterationFilter alterationFilter) {
        return !alterationFilter.getIncludeGermline() && !alterationFilter.getIncludeSomatic() && !alterationFilter.getIncludeUnknownStatus();
    }
    
    private boolean allAlterationsExcludedDriverAnnotation(AlterationFilter alterationFilter) {
        return !alterationFilter.getIncludeDriver() && !alterationFilter.getIncludeVUS()
            && !alterationFilter.getIncludeUnknownOncogenicity() && alterationFilter.getSelectedTiers().hasNone()
            && !alterationFilter.getIncludeUnknownTier();
    }

}
