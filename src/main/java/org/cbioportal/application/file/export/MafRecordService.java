package org.cbioportal.application.file.export;

import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.domain.studyview.StudyViewFilterContext;
import org.cbioportal.legacy.model.Mutation;
import org.cbioportal.legacy.web.parameter.SampleIdentifier;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MafRecordService {

/*
    public Iterator<MafRecord> fetch(Map<String, Set<String>> studyToSampleMap, String molecularProfileStableId) {
        List<String> sampleIds = List.copyOf(studyToSampleMap.values().stream().flatMap(Set::stream).toList());
        StudyViewFilterContext studyViewFilterContext = StudyViewFilterContext.builder()
            .sampleIdentifiers(studyToSampleMap.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(sampleId -> {
                    SampleIdentifier sampleIdentifier = new SampleIdentifier();
                    sampleIdentifier.setStudyId(entry.getKey());
                    sampleIdentifier.setSampleId(sampleId);
                    return sampleIdentifier;
                }))
                .collect(Collectors.toList()))
            .build();
        List<Mutation> mutationList = getMutationsUseCase.execute(studyViewFilterContext);
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
                mutation.getScore() == null ? null : mutation.getScore(),
                mutation.getBamFile(),
                mutation.getSequencer(),
                //TODO how to calculate HgvpShort?
                "",
                mutation.getTumorAltCount(),
                mutation.getTumorRefCount(),
                mutation.getNormalAltCount(),
                mutation.getNormalRefCount()
                //TODO export mutation.annotationJSON ?
            )).iterator();
    }*/
}
