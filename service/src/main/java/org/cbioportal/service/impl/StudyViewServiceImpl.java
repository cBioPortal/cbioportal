package org.cbioportal.service.impl;

import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenomicDataCount;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.MolecularProfileCaseIdentifier;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.StudyViewService;
import org.cbioportal.service.util.MolecularProfileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudyViewServiceImpl implements StudyViewService {
    @Autowired
    private MolecularProfileService molecularProfileService;
    @Autowired
    private GenePanelService genePanelService;
    @Autowired
    private MolecularProfileUtil molecularProfileUtil;

    @Override
    public List<GenomicDataCount> getGenomicDataCounts(List<String> studyIds, List<String> sampleIds) {
        List<MolecularProfileCaseIdentifier> molecularProfileSampleIdentifiers = molecularProfileService.getMolecularProfileCaseIdentifiers(studyIds, sampleIds);

        Map<String, Integer> molecularProfileSampleCountSet = genePanelService
            .fetchGenePanelDataInMultipleMolecularProfiles(molecularProfileSampleIdentifiers)
            .stream()
            .filter(GenePanelData::getProfiled)
            .collect(Collectors.groupingBy(GenePanelData::getMolecularProfileId, Collectors.summingInt(s -> 1)));

        List<MolecularProfile> molecularProfiles = molecularProfileService.getMolecularProfilesInStudies(new ArrayList<>(new HashSet<>(studyIds)),
            "SUMMARY");

        return molecularProfileUtil
            .categorizeMolecularProfilesByStableIdSuffixes(molecularProfiles)
            .entrySet()
            .stream()
            .map(entry -> {
                GenomicDataCount dataCount = new GenomicDataCount();
                dataCount.setValue(entry.getKey());

                Integer count = entry
                    .getValue()
                    .stream()
                    .mapToInt(molecularProfile -> molecularProfileSampleCountSet.getOrDefault(molecularProfile.getStableId(), 0))
                    .sum();

                dataCount.setCount(count);
                dataCount.setLabel(entry.getValue().get(0).getName());
                return dataCount;
            })
            .filter(dataCount -> dataCount.getCount() > 0)
            .collect(Collectors.toList());
    }

}
