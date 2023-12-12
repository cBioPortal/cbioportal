package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.AlterationCountByStructuralVariant;
import org.cbioportal.model.AlterationFilter;
import org.cbioportal.model.CNA;
import org.cbioportal.model.CopyNumberCountByGene;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.model.MutationEventType;
import org.cbioportal.model.MolecularProfile.MolecularAlterationType;
import org.cbioportal.model.util.Select;
import org.cbioportal.persistence.AlterationRepository;
import org.cbioportal.persistence.MolecularProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class AlterationMyBatisRepository implements AlterationRepository {

    @Autowired
    private AlterationCountsMapper alterationCountsMapper;
    @Autowired
    private MolecularProfileRepository molecularProfileRepository;

    @Override
    public List<AlterationCountByGene> getSampleAlterationGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                     Select<Integer> entrezGeneIds,
                                                                     AlterationFilter alterationFilter) {

        if ((alterationFilter.getMutationTypeSelect().hasNone() && alterationFilter.getCNAEventTypeSelect().hasNone()
                  && !alterationFilter.getStructuralVariants())
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())
            || allAlterationsExcludedDriverAnnotation(alterationFilter)
            || allAlterationsExcludedMutationStatus(alterationFilter)
            || allAlterationsExcludedDriverTierAnnotation(alterationFilter)
        ) {
            // We want a mutable empty list:
            return new ArrayList<>();
        }

        Set<String> molecularProfileIds = molecularProfileCaseIdentifiers.stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
                .collect(Collectors.toSet());
        Map<String, MolecularAlterationType> profileTypeByProfileId = molecularProfileRepository
            .getMolecularProfiles(molecularProfileIds, "SUMMARY")
            .stream()
            .collect(Collectors.toMap(datum -> datum.getMolecularProfileId().toString(), MolecularProfile::getMolecularAlterationType));
        Map<MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType =
            alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(new ArrayList<>(molecularProfileCaseIdentifiers), "SAMPLE_ID")
            .stream()
            .collect(Collectors.groupingBy(e -> profileTypeByProfileId.getOrDefault(e.getMolecularProfileId(), null)));
        return alterationCountsMapper.getSampleAlterationGeneCounts(
            groupedIdentifiersByProfileType.get(MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.STRUCTURAL_VARIANT),
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
            alterationFilter.getIncludeUnknownStatus());
    }

    @Override
    public List<AlterationCountByGene> getPatientAlterationGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                      Select<Integer> entrezGeneIds,
                                                                      AlterationFilter alterationFilter) {

        if ((alterationFilter.getMutationTypeSelect().hasNone() && alterationFilter.getCNAEventTypeSelect().hasNone()
            && !alterationFilter.getStructuralVariants())
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())
            || allAlterationsExcludedDriverAnnotation(alterationFilter)
            || allAlterationsExcludedMutationStatus(alterationFilter)
            || allAlterationsExcludedDriverTierAnnotation(alterationFilter)) {
            return Collections.emptyList();
        }

        Set<String> molecularProfileIds = molecularProfileCaseIdentifiers.stream()
                .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
                .collect(Collectors.toSet());

        Map<String, MolecularAlterationType> profileTypeByProfileId = molecularProfileRepository
            .getMolecularProfiles(molecularProfileIds, "SUMMARY")
            .stream()
            .collect(Collectors.toMap(datum -> datum.getMolecularProfileId().toString(), MolecularProfile::getMolecularAlterationType));

        Map<MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType =
            alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(new ArrayList<>(molecularProfileCaseIdentifiers), "PATIENT_ID")
            .stream()
            .collect(Collectors.groupingBy(e -> profileTypeByProfileId.getOrDefault(e.getMolecularProfileId(), null)));


        return alterationCountsMapper.getPatientAlterationGeneCounts(
            groupedIdentifiersByProfileType.get(MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.STRUCTURAL_VARIANT),
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
    public List<CopyNumberCountByGene> getSampleCnaGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                              Select<Integer> entrezGeneIds,
                                                              AlterationFilter alterationFilter) {

        if (alterationFilter.getCNAEventTypeSelect().hasNone() || molecularProfileCaseIdentifiers == null
            || allAlterationsExcludedDriverAnnotation(alterationFilter)
            || allAlterationsExcludedDriverTierAnnotation(alterationFilter)) {
            return Collections.emptyList();
        }

        List<MolecularProfileCaseIdentifier> molecularProfileCaseInternalIdentifiers =
            alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(new ArrayList<>(molecularProfileCaseIdentifiers), "SAMPLE_ID");

        return alterationCountsMapper.getSampleCnaGeneCounts(
            molecularProfileCaseInternalIdentifiers,
            entrezGeneIds,
            createCnaTypeList(alterationFilter),
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier());
    }

    @Override
    public List<CopyNumberCountByGene> getPatientCnaGeneCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                               Select<Integer> entrezGeneIds,
                                                               AlterationFilter alterationFilter) {

        if (alterationFilter.getCNAEventTypeSelect().hasNone() || molecularProfileCaseIdentifiers == null
            || allAlterationsExcludedDriverAnnotation(alterationFilter)
            || allAlterationsExcludedDriverTierAnnotation(alterationFilter)) {
            return Collections.emptyList();
        }
        List<MolecularProfileCaseIdentifier> molecularProfileCaseInternalIdentifiers =
            alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(new ArrayList<>(molecularProfileCaseIdentifiers), "PATIENT_ID");

        return alterationCountsMapper.getPatientCnaGeneCounts(
            molecularProfileCaseInternalIdentifiers,
            entrezGeneIds,
            createCnaTypeList(alterationFilter),
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier());
    }

    @Override
    public List<AlterationCountByStructuralVariant> getSampleStructuralVariantCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                     AlterationFilter alterationFilter) {

        if (molecularProfileCaseIdentifiers == null
            || molecularProfileCaseIdentifiers.isEmpty()
            || allAlterationsExcludedMutationStatus(alterationFilter)) {
            return Collections.emptyList();
        }

        return alterationCountsMapper.getSampleStructuralVariantCounts(
            new ArrayList<>(molecularProfileCaseIdentifiers),
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
    public List<AlterationCountByStructuralVariant> getPatientStructuralVariantCounts(Set<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                                     AlterationFilter alterationFilter) {

        if (molecularProfileCaseIdentifiers == null
            || molecularProfileCaseIdentifiers.isEmpty()
            || allAlterationsExcludedMutationStatus(alterationFilter)) {
            return Collections.emptyList();
        }

        return alterationCountsMapper.getPatientStructuralVariantCounts(
            new ArrayList<>(molecularProfileCaseIdentifiers),
            alterationFilter.getIncludeDriver(),
            alterationFilter.getIncludeVUS(),
            alterationFilter.getIncludeUnknownOncogenicity(),
            alterationFilter.getSelectedTiers(),
            alterationFilter.getIncludeUnknownTier(),
            alterationFilter.getIncludeGermline(),
            alterationFilter.getIncludeSomatic(),
            alterationFilter.getIncludeUnknownStatus());
    }
    
    private Select<Short> createCnaTypeList(final AlterationFilter alterationFilter) {
        if (alterationFilter.getCNAEventTypeSelect().hasNone())
            return Select.none();
        if (alterationFilter.getCNAEventTypeSelect().hasAll())
            return Select.all();
        return alterationFilter.getCNAEventTypeSelect().map(CNA::getCode);
    }

    private Select<String> createMutationTypeList(final AlterationFilter alterationFilter) {
        if (alterationFilter.getMutationTypeSelect().hasNone())
            return Select.none();
        if (alterationFilter.getMutationTypeSelect().hasAll())
            return Select.all();
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
        return alterationFilter.getSelectedTiers().hasNone()
            && !alterationFilter.getIncludeUnknownTier();
    }

}
