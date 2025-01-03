package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.*;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.util.MolecularProfileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class AlterationMyBatisRepository implements AlterationRepository {

    @Autowired
    private AlterationCountsMapper alterationCountsMapper;

    @Autowired
    private MolecularProfileUtil molecularProfileUtil; // Dependency injection of the newly created MolecularProfileUtil for handling molecular profile-specific logic.

    @Override
    public List<AlterationCountByGene> getSampleAlterationGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                     Select<Integer> entrezGeneIds,
                                                                     AlterationFilter alterationFilter) {

        if (filtrosInvalidos(molecularProfileCaseIdentifiers, alterationFilter)) {
            return Collections.emptyList();
        }

        // Grouping molecular profile case identifiers by alteration type has been moved to MolecularProfileUtil.
        Map<MolecularProfile.MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType =
            molecularProfileUtil.groupIdentifiersByProfileType(molecularProfileCaseIdentifiers, "SAMPLE_ID");

        return alterationCountsMapper.getSampleAlterationGeneCounts(
            // The grouped identifiers are now fetched directly from the utility class, which centralizes this logic.
            groupedIdentifiersByProfileType.get(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT),
            entrezGeneIds,
            createMutationTypeList(alterationFilter),
            createCnaTypeList(alterationFilter),
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier(),
            alterationFilter.getIncludeGermline(),
            alterationFilter.getIncludeSomatic(),
            alterationFilter.getIncludeUnknownStatus()
        );
    }

    @Override
    public List<AlterationCountByGene> getPatientAlterationGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                      Select<Integer> entrezGeneIds,
                                                                      AlterationFilter alterationFilter) {

        if (filtrosInvalidos(molecularProfileCaseIdentifiers, alterationFilter)) {
            return Collections.emptyList();
        }

        // Similar to the sample counts method, the grouping logic is now abstracted in MolecularProfileUtil
        Map<MolecularProfile.MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType =
            molecularProfileUtil.groupIdentifiersByProfileType(molecularProfileCaseIdentifiers, "PATIENT_ID");

        return alterationCountsMapper.getPatientAlterationGeneCounts(
            groupedIdentifiersByProfileType.get(MolecularProfile.MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularProfile.MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularProfile.MolecularAlterationType.STRUCTURAL_VARIANT),
            entrezGeneIds,
            createMutationTypeList(alterationFilter),
            createCnaTypeList(alterationFilter),
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier(),
            alterationFilter.getIncludeGermline(),
            alterationFilter.getIncludeSomatic(),
            alterationFilter.getIncludeUnknownStatus()
        );
    }

    private boolean filtrosInvalidos(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers, AlterationFilter alterationFilter) {
        return (alterationFilter.getMutationTypeSelect().hasNone() && alterationFilter.getCNAEventTypeSelect().hasNone()
            && !alterationFilter.getStructuralVariants())
            || molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty()
            || allAlterationsExcludedDriverAnnotation(alterationFilter)
            || allAlterationsExcludedMutationStatus(alterationFilter)
            || allAlterationsExcludedDriverTierAnnotation(alterationFilter);
    }

    private Select<Short> createCnaTypeList(final AlterationFilter alterationFilter) {
        if (alterationFilter.getCNAEventTypeSelect().hasNone()) {
            return Select.none();
        }
        if (alterationFilter.getCNAEventTypeSelect().hasAll()) {
            return Select.all();
        }
        return alterationFilter.getCNAEventTypeSelect().map(CNA::getCode);
    }

    private Select<String> createMutationTypeList(final AlterationFilter alterationFilter) {
        if (alterationFilter.getMutationTypeSelect().hasNone()) {
            return Select.none();
        }
        if (alterationFilter.getMutationTypeSelect().hasAll()) {
            return Select.all();
        }
        Select<String> mappedMutationTypes = alterationFilter.getMutationTypeSelect().map(MutationEventType::getMutationType);
        mappedMutationTypes.inverse(alterationFilter.getMutationTypeSelect().inverse());
        return mappedMutationTypes;
    }

    private boolean allAlterationsExcludedMutationStatus(AlterationFilter alterationFilter) {
        return !alterationFilter.getIncludeGermline() && !alterationFilter.getIncludeSomatic() && !alterationFilter.getIncludeUnknownStatus();
    }

    private boolean allAlterationsExcludedDriverAnnotation(AlterationFilter alterationFilter) {
        return !alterationFilter.getIncludeDriver() && !alterationFilter.getIncludeVUS()
            && !alterationFilter.getIncludeUnknownOncogenicity();
    }

    private boolean allAlterationsExcludedDriverTierAnnotation(AlterationFilter alterationFilter) {
        return alterationFilter.getSelectedTiers().hasNone() && !alterationFilter.getIncludeUnknownTier();
    }
}
