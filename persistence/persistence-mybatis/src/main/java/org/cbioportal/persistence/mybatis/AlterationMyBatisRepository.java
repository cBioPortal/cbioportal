package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.AlterationCountByGene;
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
                                                                 final Select<MutationEventType> mutationEventTypes,
                                                                 final Select<CNA> cnaEventTypes,
                                                                 QueryElement searchFusions) {

        // TODO add test
        if (mutationEventTypes != null && !mutationEventTypes.hasAll() && searchFusions != QueryElement.PASS)
            throw new IllegalArgumentException("Filtering for mutations vs. fusions and specifying mutation types" +
                "simultaneously is not permitted.");

        if (((mutationEventTypes == null || mutationEventTypes.hasNone()) && (cnaEventTypes == null || cnaEventTypes.hasNone()))
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())) {
            return Collections.emptyList();
        }

        List<String> molecularProfileIds = molecularProfileCaseIdentifiers
            .stream()
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .distinct()
            .collect(Collectors.toList());

        Map<String, MolecularAlterationType> profileTypeByProfileId = molecularProfileRepository
            .getMolecularProfiles(molecularProfileIds, "SUMMARY")
            .stream()
            .collect(Collectors.toMap(datum -> datum.getMolecularProfileId().toString(), MolecularProfile::getMolecularAlterationType));

        Map<MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType =
            alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(molecularProfileCaseIdentifiers, "SAMPLE_ID")
            .stream()
            .collect(Collectors.groupingBy(e -> profileTypeByProfileId.getOrDefault(e.getMolecularProfileId(), null)));

        return alterationCountsMapper.getSampleAlterationCounts(
            groupedIdentifiersByProfileType.get(MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.STRUCTURAL_VARIANT),
            entrezGeneIds,
            createMutationTypeList(mutationEventTypes),
            createCnaTypeList(cnaEventTypes),
            searchFusions);
    }

    @Override
    public List<AlterationCountByGene> getPatientAlterationCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                                  Select<Integer> entrezGeneIds,
                                                                  Select<MutationEventType> mutationEventTypes,
                                                                  Select<CNA> cnaEventTypes,
                                                                  QueryElement searchFusions) {

        if (mutationEventTypes != null && !mutationEventTypes.hasAll() && searchFusions != QueryElement.PASS)
            throw new IllegalArgumentException("Filtering for mutations vs. fusions and specifying mutation types" +
                "simultaneously is not permitted.");

        if (((mutationEventTypes == null || mutationEventTypes.hasNone()) && (cnaEventTypes == null || cnaEventTypes.hasNone()))
            || (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty())) {
            return Collections.emptyList();
        }

        List<String> molecularProfileIds = molecularProfileCaseIdentifiers
            .stream()
            .map(MolecularProfileCaseIdentifier::getMolecularProfileId)
            .distinct()
            .collect(Collectors.toList());

        Map<String, MolecularAlterationType> profileTypeByProfileId = molecularProfileRepository
            .getMolecularProfiles(molecularProfileIds, "SUMMARY")
            .stream()
            .collect(Collectors.toMap(datum -> datum.getMolecularProfileId().toString(), MolecularProfile::getMolecularAlterationType));

        Map<MolecularAlterationType, List<MolecularProfileCaseIdentifier>> groupedIdentifiersByProfileType =
            alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(molecularProfileCaseIdentifiers, "PATIENT_ID")
            .stream()
            .collect(Collectors.groupingBy(e -> profileTypeByProfileId.getOrDefault(e.getMolecularProfileId(), null)));


        return alterationCountsMapper.getPatientAlterationCounts(
            groupedIdentifiersByProfileType.get(MolecularAlterationType.MUTATION_EXTENDED),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.COPY_NUMBER_ALTERATION),
            groupedIdentifiersByProfileType.get(MolecularAlterationType.STRUCTURAL_VARIANT),
            entrezGeneIds,
            createMutationTypeList(mutationEventTypes),
            createCnaTypeList(cnaEventTypes),
            searchFusions);
    }

    @Override
    public List<CopyNumberCountByGene> getSampleCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                          Select<Integer> entrezGeneIds,
                                                          Select<CNA> cnaEventTypes) {

        if (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty()
            || cnaEventTypes == null || cnaEventTypes.hasNone()) {
            return Collections.emptyList();
        }
        List<MolecularProfileCaseIdentifier> molecularProfileCaseInternalIdentifiers = alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(molecularProfileCaseIdentifiers, "SAMPLE_ID");

        return alterationCountsMapper.getSampleCnaCounts(
            molecularProfileCaseInternalIdentifiers,
            entrezGeneIds,
            createCnaTypeList(cnaEventTypes));
    }

    @Override
    public List<CopyNumberCountByGene> getPatientCnaCounts(List<MolecularProfileCaseIdentifier> molecularProfileCaseIdentifiers,
                                                           Select<Integer> entrezGeneIds,
                                                           Select<CNA> cnaEventTypes) {

        if (molecularProfileCaseIdentifiers == null || molecularProfileCaseIdentifiers.isEmpty()
            || cnaEventTypes == null || cnaEventTypes.hasNone()) {
            return Collections.emptyList();
        }
        List<MolecularProfileCaseIdentifier> molecularProfileCaseInternalIdentifiers = alterationCountsMapper.getMolecularProfileCaseInternalIdentifier(molecularProfileCaseIdentifiers, "PATIENT_ID");

        return alterationCountsMapper.getPatientCnaCounts(
            molecularProfileCaseInternalIdentifiers,
            entrezGeneIds,
            createCnaTypeList(cnaEventTypes));
    }

    private Select<Short> createCnaTypeList(final Select<CNA> cnaEventTypes) {
        return cnaEventTypes != null ? cnaEventTypes.map(CNA::getCode) : Select.none();
    }

    private Select<String> createMutationTypeList(final Select<MutationEventType> mutationEventTypes) {
        if (mutationEventTypes == null) {
            return Select.none();
        }
        Select<String> mappedMutationTypes = mutationEventTypes.map(MutationEventType::getMutationType);
        mappedMutationTypes.inverse(mutationEventTypes.inverse());

        return mappedMutationTypes;
    }

}
