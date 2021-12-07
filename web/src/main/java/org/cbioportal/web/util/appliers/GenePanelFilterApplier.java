package org.cbioportal.web.util.appliers;

import org.cbioportal.model.GenePanelData;
import org.cbioportal.model.GenePanelFilter;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.web.parameter.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GenePanelFilterApplier extends StudyViewSubFilterApplier {
    @Autowired
    GenePanelService genePanelService;
    
    @Override
    public List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters) {
        // get all unique involved molecular profile ids
        Set<String> molecularProfileIds = filters.getGenePanelFilters().stream()
            .flatMap(f -> f.getMolecularProfileIds().stream())
            .collect(Collectors.toSet());
        
        // get all unique involved gene panel ids
        Set<String> genePanelIds = filters.getGenePanelFilters().stream()
            .map(GenePanelFilter::getGenePanel)
            .collect(Collectors.toSet());

        Map<String, Set<String>> validSamplesByStudy = 
            // get the gene panel objects for those molecular profile ids
            genePanelService.fetchGenePanelDataByMolecularProfileIds(molecularProfileIds)
            .stream()
            // filter out gene panels that belong to those molecular profile ids that aren't included in the filter
            .filter(panel -> genePanelIds.contains(panel.getGenePanelId()))
            // make a study_id -> [sample_id...] map that we can use to filter our list of identifiers
            .collect(Collectors.groupingBy(
                GenePanelData::getStudyId,
                Collectors.mapping(GenePanelData::getSampleId, Collectors.toSet())
            ));
        
        return toFilter.stream()
            .filter(id -> validSamplesByStudy.getOrDefault(id.getStudyId(), new HashSet<>()).contains(id.getSampleId()))
            .collect(Collectors.toList());
    }

    @Override
    public boolean shouldApplyFilter(StudyViewFilter studyViewFilter) {
        return studyViewFilter.getGenePanelFilters() != null && !studyViewFilter.getGenePanelFilters().isEmpty();
    }
}
