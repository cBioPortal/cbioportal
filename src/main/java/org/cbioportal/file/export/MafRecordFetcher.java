package org.cbioportal.file.export;

import org.cbioportal.file.model.MafRecord;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.exception.MolecularProfileNotFoundException;
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

    public Iterator<MafRecord> fetch(Map<String, Set<String>> studyToSampleMap, String molecularProfileStableId) {
        List<String> sampleIds = List.copyOf(studyToSampleMap.values().stream().flatMap(Set::stream).toList());
        //TODO Check for sample id duplicates?
        List<Integer> entrezGeneIds = List.of();
        try {
            List<Mutation> mutationList = mutationService.fetchMutationsInMolecularProfile(molecularProfileStableId, sampleIds, entrezGeneIds, false, "EXPORT", null, null, null, null);
            return mutationList.stream()
                //TODO let sql do it!
                .filter(mutation -> studyToSampleMap.get(mutation.getStudyId()).contains(mutation.getSampleId()))
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
        } catch (MolecularProfileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
