package org.mskcc.cbio.portal.model.converter;

import org.mskcc.cbio.portal.model.Mutation;
import org.mskcc.cbio.portal.model.MutationCount;
import org.mskcc.cbio.portal.model.MutationEvent;
import org.mskcc.cbio.portal.model.KeywordSampleCount;
import org.mskcc.cbio.portal.model.MutatedGeneSampleCount;
import org.mskcc.cbio.portal.model.SignificantlyMutatedGene;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.ExtendedMutation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MutationModelConverter {

    public List<ExtendedMutation> convert(List<Mutation> mutationList) {

        List<ExtendedMutation> extendedMutationList = new ArrayList<>();
        for (Mutation mutation : mutationList) {
            extendedMutationList.add(convert(mutation));
        }

        return extendedMutationList;
    }

    public ExtendedMutation convert(Mutation mutation) {

        MutationEvent mutationEvent = mutation.getMutationEvent();
        ExtendedMutation.MutationEvent event = new ExtendedMutation.MutationEvent();

        if(mutationEvent.getMutationEventId() != null) {
            event.setMutationEventId(mutationEvent.getMutationEventId());
        }
        if (mutationEvent.getChr() != null) {
            event.setChr(mutationEvent.getChr());
        }
        if (mutationEvent.getStartPosition() != null) {
            event.setStartPosition(mutationEvent.getStartPosition());
        }
        if (mutationEvent.getEndPosition() != null) {
            event.setEndPosition(mutationEvent.getEndPosition());
        }
        if (mutationEvent.getProteinChange() != null) {
            event.setProteinChange(mutationEvent.getProteinChange());
        }
        if (mutationEvent.getMutationType() != null) {
            event.setMutationType(mutationEvent.getMutationType());
        }
        if (mutationEvent.getFunctionalImpactScore() != null) {
            event.setFunctionalImpactScore(mutationEvent.getFunctionalImpactScore());
        }
        if (mutationEvent.getFisValue() != null) {
            event.setFisValue(mutationEvent.getFisValue());
        }
        if (mutationEvent.getLinkXvar() != null) {
            event.setLinkXVar(mutationEvent.getLinkXvar());
        }
        if (mutationEvent.getLinkPdb() != null) {
            event.setLinkPdb(mutationEvent.getLinkPdb());
        }
        if (mutationEvent.getLinkMsa() != null) {
            event.setLinkMsa(mutationEvent.getLinkMsa());
        }
        if (mutationEvent.getNcbiBuild() != null) {
            event.setNcbiBuild(mutationEvent.getNcbiBuild());
        }
        if (mutationEvent.getStrand() != null) {
            event.setStrand(mutationEvent.getStrand());
        }
        if (mutationEvent.getVariantType() != null) {
            event.setVariantType(mutationEvent.getVariantType());
        }
        if (mutationEvent.getDbSnpRs() != null) {
            event.setDbSnpRs(mutationEvent.getDbSnpRs());
        }
        if (mutationEvent.getDbSnpValStatus() != null) {
            event.setDbSnpValStatus(mutationEvent.getDbSnpValStatus());
        }
        if (mutationEvent.getReferenceAllele() != null) {
            event.setReferenceAllele(mutationEvent.getReferenceAllele());
        }
        if (mutationEvent.getOncotatorDbsnpRs() != null) {
            event.setOncotatorDbSnpRs(mutationEvent.getOncotatorDbsnpRs());
        }
        if (mutationEvent.getOncotatorRefseqMrnaId() != null) {
            event.setOncotatorRefseqMrnaId(mutationEvent.getOncotatorRefseqMrnaId());
        }
        if (mutationEvent.getOncotatorCodonChange() != null) {
            event.setOncotatorCodonChange(mutationEvent.getOncotatorCodonChange());
        }
        if (mutationEvent.getOncotatorUniprotEntryName() != null) {
            event.setOncotatorUniprotName(mutationEvent.getOncotatorUniprotEntryName());
        }
        if (mutationEvent.getOncotatorUniprotAccession() != null) {
            event.setOncotatorUniprotAccession(mutationEvent.getOncotatorUniprotAccession());
        }
        if (mutationEvent.getOncotatorProteinPosStart() != null) {
            event.setOncotatorProteinPosStart(mutationEvent.getOncotatorProteinPosStart());
        }
        if (mutationEvent.getOncotatorProteinPosEnd() != null) {
            event.setOncotatorProteinPosEnd(mutationEvent.getOncotatorProteinPosEnd());
        }
        if (mutationEvent.getCanonicalTranscript() != null) {
            event.setCanonicalTranscript(mutationEvent.getCanonicalTranscript());
        }
        if (mutationEvent.getTumorSeqAllele() != null) {
            event.setTumorSeqAllele(mutationEvent.getTumorSeqAllele());
        }
        if (mutationEvent.getKeyword() != null) {
            event.setKeyword(mutationEvent.getKeyword());
        }
        if (mutation.getGene() != null) {
            CanonicalGene canonicalGene = new CanonicalGene(mutation.getGene().getEntrezGeneId(),
                    mutation.getGene().getHugoGeneSymbol());
            event.setGene(canonicalGene);
        }

        ExtendedMutation extendedMutation = new ExtendedMutation(event);
        if (mutation.getGeneticProfileId() != null) {
            extendedMutation.setGeneticProfileId(Integer.parseInt(mutation.getGeneticProfileId().toString()));
        }
        if (mutation.getSampleId() != null) {
            extendedMutation.setSampleId(Integer.parseInt(mutation.getSampleId().toString()));
        }
        if (mutation.getCenter() != null) {
            extendedMutation.setSequencingCenter(mutation.getCenter());
        }
        if (mutation.getSequencer() != null) {
            extendedMutation.setSequencer(mutation.getSequencer());
        }
        if (mutation.getMutationStatus() != null) {
            extendedMutation.setMutationStatus(mutation.getMutationStatus());
        }
        if (mutation.getValidationStatus() != null) {
            extendedMutation.setValidationStatus(mutation.getValidationStatus());
        }
        if (mutation.getTumorSeqAllele1() != null) {
            extendedMutation.setTumorSeqAllele1(mutation.getTumorSeqAllele1());
        }
        if (mutation.getTumorSeqAllele2() != null) {
            extendedMutation.setTumorSeqAllele2(mutation.getTumorSeqAllele2());
        }
        if (mutation.getMatchedNormSampleBarcode() != null) {
            extendedMutation.setMatchedNormSampleBarcode(mutation.getMatchedNormSampleBarcode());
        }
        if (mutation.getMatchNormSeqAllele1() != null) {
            extendedMutation.setMatchNormSeqAllele1(mutation.getMatchNormSeqAllele1());
        }
        if (mutation.getMatchNormSeqAllele2() != null) {
            extendedMutation.setMatchNormSeqAllele2(mutation.getMatchNormSeqAllele2());
        }
        if (mutation.getTumorValidationAllele1() != null) {
            extendedMutation.setTumorValidationAllele1(mutation.getTumorValidationAllele1());
        }
        if (mutation.getTumorValidationAllele2() != null) {
            extendedMutation.setTumorValidationAllele2(mutation.getTumorValidationAllele2());
        }
        if (mutation.getMatchNormValidationAllele1() != null) {
            extendedMutation.setMatchNormValidationAllele1(mutation.getMatchNormValidationAllele1());
        }
        if (mutation.getMatchNormValidationAllele2() != null) {
            extendedMutation.setMatchNormValidationAllele2(mutation.getMatchNormValidationAllele2());
        }
        if (mutation.getVerificationStatus() != null) {
            extendedMutation.setVerificationStatus(mutation.getVerificationStatus());
        }
        if (mutation.getSequencingPhase() != null) {
            extendedMutation.setSequencingPhase(mutation.getSequencingPhase());
        }
        if (mutation.getSequenceSource() != null) {
            extendedMutation.setSequenceSource(mutation.getSequenceSource());
        }
        if (mutation.getValidationMethod() != null) {
            extendedMutation.setValidationMethod(mutation.getValidationMethod());
        }
        if (mutation.getScore() != null) {
            extendedMutation.setScore(mutation.getScore());
        }
        if (mutation.getBamFile() != null) {
            extendedMutation.setBamFile(mutation.getBamFile());
        }
        if (mutation.getTumorAltCount() != null) {
            extendedMutation.setTumorAltCount(mutation.getTumorAltCount());
        }
        if (mutation.getTumorRefCount() != null) {
            extendedMutation.setTumorRefCount(mutation.getTumorRefCount());
        }
        if (mutation.getNormalAltCount() != null) {
            extendedMutation.setNormalAltCount(mutation.getNormalAltCount());
        }
        if (mutation.getNormalRefCount() != null) {
            extendedMutation.setNormalRefCount(mutation.getNormalRefCount());
        }
        if (mutation.getDipLogR() != null) {
            extendedMutation.setDipLogR(mutation.getDipLogR());
        }
        if (mutation.getCellularFraction() != null) {
            extendedMutation.setCellularFraction(mutation.getCellularFraction());
        }
        if (mutation.getTotalCopyNumber() != null) {
            extendedMutation.setTotalCopyNumber(mutation.getTotalCopyNumber());
        }
        if (mutation.getMinorCopyNumber() != null) {
            extendedMutation.setMinorCopyNumber(mutation.getMinorCopyNumber());
        }
        if (mutation.getCellularFractionEm() != null) {
            extendedMutation.setCellularFractionEm(mutation.getCellularFractionEm());
        }
        if (mutation.getTotalCopyNumberEm() != null) {
            extendedMutation.setTotalCopyNumberEm(mutation.getTotalCopyNumberEm());
        }
        if (mutation.getMinorCopyNumber() != null) {
            extendedMutation.setMinorCopyNumberEm(mutation.getMinorCopyNumberEm());
        }
        if (mutation.getPurity() != null) {
            extendedMutation.setPurity(mutation.getPurity());
        }
        if (mutation.getPloidy() != null) {
            extendedMutation.setPloidy(mutation.getPloidy());
        }
        if (mutation.getCcfMCopies() != null) {
            extendedMutation.setCcfMCopies(mutation.getCcfMCopies());
        }
        if (mutation.getCcfMCopiesLower() != null) {
            extendedMutation.setCcfMCopiesLower(mutation.getCcfMCopiesLower());
        }
        if (mutation.getCcfMCopiesUpper() != null) {
            extendedMutation.setCcfMCopiesUpper(mutation.getCcfMCopiesUpper());
        }
        if (mutation.getCcfMCopiesProb95() != null) {
            extendedMutation.setCcfMCopiesProb95(mutation.getCcfMCopiesProb95());
        }
        if (mutation.getCcfMCopiesProb90() != null) {
            extendedMutation.setCcfMCopiesProb90(mutation.getCcfMCopiesProb90());
        }
        if (mutation.getCcfMCopiesEm() != null) {
            extendedMutation.setCcfMCopiesEm(mutation.getCcfMCopiesEm());
        }
        if (mutation.getCcfMCopiesLowerEm() != null) {
            extendedMutation.setCcfMCopiesLowerEm(mutation.getCcfMCopiesLowerEm());
        }
        if (mutation.getCcfMCopiesUpperEm() != null) {
            extendedMutation.setCcfMCopiesUpperEm(mutation.getCcfMCopiesUpperEm());
        }
        if (mutation.getCcfMCopiesProb95Em() != null) {
            extendedMutation.setCcfMCopiesProb95Em(mutation.getCcfMCopiesProb95Em());
        }
        if (mutation.getCcfMCopiesProb90Em() != null) {
            extendedMutation.setCcfMCopiesProb90Em(mutation.getCcfMCopiesProb90Em());
        }

        return extendedMutation;
    }

    public Map<String, String> convertSampleIdAndEntrezGeneIdToMap(List<Mutation> mutationList) {

        Map<String, String> map = new HashMap<>();
        for(Mutation mutation : mutationList) {
            map.put(mutation.getSampleId().toString() + mutation.getEntrezGeneId(), "");
        }

        return map;
    }

    public Map<Long, Map<String, String>> convertSignificantlyMutatedGeneToMap(
            List<SignificantlyMutatedGene> significantlyMutatedGenes) {

        Map<Long, Map<String, String>> map = new HashMap<>();

        for (SignificantlyMutatedGene significantlyMutatedGene : significantlyMutatedGenes) {
            Map<String, String> value = new HashMap<>();
            value.put("caseIds", significantlyMutatedGene.getConcatenatedSampleIds());
            value.put("count", significantlyMutatedGene.getCount().toString());
            map.put(significantlyMutatedGene.getEntrezGeneId().longValue(), value);
        }

        return map;
    }

    public Map<Integer, Integer> convertMutationCountToMap(List<MutationCount> mutationCounts) {

        Map<Integer, Integer> map = new HashMap<>();

        for (MutationCount mutationCount : mutationCounts) {
            map.put(mutationCount.getSampleId(), mutationCount.getMutationCount());
        }

        return map;
    }

    public Map<Long, Integer> convertMutatedGeneSampleCountToMap(List<MutatedGeneSampleCount> mutatedGeneSampleCounts) {

        Map<Long, Integer> map = new HashMap<>();

        for (MutatedGeneSampleCount mutatedGeneSampleCount : mutatedGeneSampleCounts) {
            if (mutatedGeneSampleCount != null && mutatedGeneSampleCount.getEntrezGeneId() != null) {
                map.put(mutatedGeneSampleCount.getEntrezGeneId().longValue(), mutatedGeneSampleCount.getCount());
            }
        }

        return map;
    }

    public Map<String, Integer> convertKeywordSampleCountToMap(List<KeywordSampleCount> keywordSampleCounts) {

        Map<String, Integer> map = new HashMap<>();

        for (KeywordSampleCount keywordSampleCount : keywordSampleCounts) {
            map.put(keywordSampleCount.getKeyword(), keywordSampleCount.getCount());
        }

        return map;
    }
}
