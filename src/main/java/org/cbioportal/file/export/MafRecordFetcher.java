package org.cbioportal.file.export;

import org.cbioportal.file.model.MafRecord;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MafRecordFetcher {

    private final MolecularProfileService molecularProfileService;
    private final MutationService mutationService;

    public MafRecordFetcher(MolecularProfileService molecularProfileService, MutationService mutationService) {
        this.molecularProfileService = molecularProfileService;
        this.mutationService = mutationService;
    }

    public Iterator<MafRecord> fetch(Map<MolecularProfile, Set<String>> molecularProfileToSamplesMap) {
        List<String> molecularProfileStableIds = List.copyOf(molecularProfileToSamplesMap.keySet().stream().map(MolecularProfile::getStableId).toList());
        if (molecularProfileStableIds.size() > 1) {
            throw new IllegalArgumentException("Merging multiple molecular profiles with different stable Id is not supported");
        }
        List<String> sampleIds = List.copyOf(molecularProfileToSamplesMap.values().stream().flatMap(Set::stream).toList());
        List<Integer> entrezGeneIds = List.of();
        List<Mutation> mutationList = mutationService.getMutationsInMultipleMolecularProfiles(
            molecularProfileStableIds, sampleIds, entrezGeneIds, "EXPORT", null, null, null, null);
        Map<String, Set<String>> studyIdToSamplesMap = molecularProfileToSamplesMap.entrySet().stream()
            .collect(Collectors.toMap(molecularProfile -> molecularProfile.getKey().getCancerStudyIdentifier(), Map.Entry::getValue));
        return mutationList.stream()
            .filter(mutation -> studyIdToSamplesMap.get(mutation.getStudyId()).contains(mutation.getSampleId()))
            .map(mutation -> new MafRecord(
                mutation.getGene().getHugoGeneSymbol(),
                mutation.getGene().getEntrezGeneId().toString(),
                mutation.getCenter(),
                mutation.getNcbiBuild(),
                mutation.getChr(),
                mutation.getStartPosition(),
                mutation.getEndPosition(),
                "+",
                mutation.getMutationType(),
                mutation.getVariantType(),
                mutation.getReferenceAllele(),
                mutation.getTumorSeqAllele(),
                //TODO check if this is correct
                mutation.getTumorSeqAllele(),
                mutation.getDbSnpRs(),
                mutation.getDbSnpValStatus(),
                mutation.getSampleId(),
                mutation.getMatchedNormSampleBarcode(),
                mutation.getMatchNormSeqAllele1(),
                mutation.getMatchNormSeqAllele2(),
                mutation.getTumorValidationAllele1(),
                mutation.getTumorValidationAllele2(),
                mutation.getMatchNormValidationAllele1(),
                mutation.getMatchNormValidationAllele2(),
                mutation.getVerificationStatus(),
                mutation.getValidationStatus(),
                mutation.getMutationStatus(),
                mutation.getSequencingPhase(),
                mutation.getSequenceSource(),
                mutation.getValidationMethod(),
                mutation.getScore() == null ? null : mutation.getScore().toString(),
                mutation.getBamFile(),
                mutation.getSequencer(),
                //TODO how to calculate HgvpShort?
                "",
                mutation.getTumorAltCount(),
                mutation.getTumorRefCount(),
                mutation.getNormalAltCount(),
                mutation.getNormalRefCount()
            )).iterator();
    }
}
