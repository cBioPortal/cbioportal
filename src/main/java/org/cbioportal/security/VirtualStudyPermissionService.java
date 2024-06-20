package org.cbioportal.security;

import org.cbioportal.utils.security.AccessLevel;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.web.parameter.VirtualStudySamples;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class VirtualStudyPermissionService {

    private final Optional<CancerStudyPermissionEvaluator> cancerStudyPermissionEvaluator;

    public VirtualStudyPermissionService(Optional<CancerStudyPermissionEvaluator> cancerStudyPermissionEvaluator) {
        this.cancerStudyPermissionEvaluator = cancerStudyPermissionEvaluator;
    }

    public void filterOutForbiddenStudies(List<VirtualStudy> virtualStudies) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || cancerStudyPermissionEvaluator.isEmpty()) {
            return;
        }
        Iterator<VirtualStudy> virtualStudyIterator = virtualStudies.iterator();
        while (virtualStudyIterator.hasNext()) {
            VirtualStudy virtualStudy = virtualStudyIterator.next();
            VirtualStudyData virtualStudyData = virtualStudy.getData();

            Set<VirtualStudySamples> filteredStudies = virtualStudyData.getStudies().stream()
                .filter(study ->
                    cancerStudyPermissionEvaluator.get().hasPermission(authentication, study.getId(), "CancerStudyId", AccessLevel.READ))
                .collect(Collectors.toSet());
            if (filteredStudies.isEmpty()) {
                virtualStudyIterator.remove();
                continue;
            }
            virtualStudyData.setStudies(filteredStudies);

            StudyViewFilter studyViewFilter = virtualStudyData.getStudyViewFilter();
            List<String> filteredStudyIds = studyViewFilter.getStudyIds().stream()
                .filter(studyId ->
                    cancerStudyPermissionEvaluator.get().hasPermission(authentication, studyId, "CancerStudyId", AccessLevel.READ))
                .toList();
            virtualStudyData.getStudyViewFilter().setStudyIds(filteredStudyIds);
        }

    }
}
