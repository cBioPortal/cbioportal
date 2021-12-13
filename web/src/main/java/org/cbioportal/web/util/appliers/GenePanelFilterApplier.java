package org.cbioportal.web.util.appliers;

import org.cbioportal.model.StudyViewGenePanel;
import org.cbioportal.service.GenePanelService;
import org.cbioportal.service.StudyViewFilterService;
import org.cbioportal.model.SampleIdentifier;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class GenePanelFilterApplier extends StudyViewSubFilterApplier {
    @Autowired
    private GenePanelService genePanelService;
    
    @Autowired
    private StudyViewFilterService studyViewFilterService;
    
    @Override
    public List<SampleIdentifier> filter(List<SampleIdentifier> toFilter, StudyViewFilter filters) {
        // This logic is less clear than I'd like because of the difference between how the UI represents
        // molecular profiles and how the database represents them.
        
        // Given two studies (`study_a` and `study_b`) that both have a `mutations` molecular profile,
        // the study view UI lists a singular molecular profile, titled `mutations`, but the database is aware of two
        // molecular profiles: `study_a_mutations` and `study_b_mutations`
        
        // I deal with this mapping here in a naive manner. Rather than figure out what molecular profile
        // suffix belongs to what studies, I just permute all suffixes to all studies. These are just getting listed
        // in a big IN clause, so the permutations that don't exist are ultimately harmless.
        // (If this ends up being a performance issue in the future, my bad)
        Set<String> studies = toFilter.stream()
            .map(SampleIdentifier::getStudyId)
            .collect(Collectors.toSet());

        List<StudyViewGenePanel> backendFilters = filters.getGenePanelFilters()
            .stream()
            .flatMap(filter ->
                studies.stream()
                    .map(id -> id + "_" + filter.getMolecularProfileSuffix())
                    .map(fullMolecularProfileId -> {
                        StudyViewGenePanel panel = new StudyViewGenePanel();
                        panel.setGenePanelId(filter.getGenePanelId());
                        panel.setMolecularProfileId(fullMolecularProfileId);
                        return panel;
                    })
            ).collect(Collectors.toList());
        
        Set<SampleIdentifier> allValidIdentifiers = 
            studyViewFilterService.getSampleIdentifiersForPanels(backendFilters);
        
        return toFilter.stream()
            .filter(allValidIdentifiers::contains)
            .collect(Collectors.toList());
    }

    @Override
    public boolean shouldApplyFilter(StudyViewFilter studyViewFilter) {
        return studyViewFilter.getGenePanelFilters() != null && !studyViewFilter.getGenePanelFilters().isEmpty();
    }
}
