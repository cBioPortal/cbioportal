package org.cbioportal.service.impl;

import org.cbioportal.model.CopyNumberSeg;
import org.cbioportal.model.FractionGenomeAltered;
import org.cbioportal.service.CopyNumberSegmentService;
import org.cbioportal.service.FractionGenomeAlteredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FractionGenomeAlteredServiceImpl implements FractionGenomeAlteredService {

    @Autowired
    private CopyNumberSegmentService copyNumberSegmentService;

    @Override
    public List<FractionGenomeAltered> getFractionGenomeAltered(String studyId, String sampleListId, Double cutoff) {

        List<CopyNumberSeg> copyNumberSegList = copyNumberSegmentService
            .getCopyNumberSegmentsBySampleListId(studyId, sampleListId, "SUMMARY");
        return createFractionGenomeAlteredList(studyId, cutoff, copyNumberSegList);
    }

    @Override
    public List<FractionGenomeAltered> fetchFractionGenomeAltered(String studyId, List<String> sampleIds,
                                                                  Double cutoff) {
        
        List<String> studyIds = new ArrayList<>();
        sampleIds.forEach(s -> studyIds.add(studyId));

        List<CopyNumberSeg> copyNumberSegList = copyNumberSegmentService.fetchCopyNumberSegments(studyIds, sampleIds, 
            "SUMMARY");
        return createFractionGenomeAlteredList(studyId, cutoff, copyNumberSegList);
    }

    private List<FractionGenomeAltered> createFractionGenomeAlteredList(String studyId, Double cutoff, 
                                                                        List<CopyNumberSeg> copyNumberSegList) {

        Map<String, List<CopyNumberSeg>> copyNumberSegMap = copyNumberSegList.stream().collect(
            Collectors.groupingBy(CopyNumberSeg::getSampleStableId));
        
        List<FractionGenomeAltered> fractionGenomeAlteredList = new ArrayList<>();
        for (String sampleId : copyNumberSegMap.keySet()) {

            List<CopyNumberSeg> measuredCopyNumberSegList = copyNumberSegMap.get(sampleId);
            List<CopyNumberSeg> alteredCopyNumberSegList = measuredCopyNumberSegList.stream().filter(c -> Math.abs(
                c.getSegmentMean().doubleValue()) >= cutoff).collect(Collectors.toList());

            long measuredLength = measuredCopyNumberSegList.stream().mapToLong(m -> m.getEnd() - m.getStart()).sum();
            if (measuredLength == 0) {
                continue;
            }
            long alteredLength = alteredCopyNumberSegList.stream().mapToLong(m -> m.getEnd() - m.getStart()).sum();
            FractionGenomeAltered fractionGenomeAltered = new FractionGenomeAltered();
            fractionGenomeAltered.setStudyId(studyId);
            fractionGenomeAltered.setSampleId(sampleId);
            fractionGenomeAltered.setPatientId(measuredCopyNumberSegList.get(0).getPatientId());
            fractionGenomeAltered.setValue(BigDecimal.valueOf((double) alteredLength / (double) measuredLength));
            fractionGenomeAlteredList.add(fractionGenomeAltered);
        }

        return fractionGenomeAlteredList;
    }
}
