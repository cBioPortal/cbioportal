package org.cbioportal.security;

import org.cbioportal.utils.security.AccessLevel;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.cbioportal.web.parameter.VirtualStudy;
import org.cbioportal.web.parameter.VirtualStudyData;
import org.cbioportal.web.parameter.VirtualStudySamples;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VirtualStudyPermissionService {
    @Autowired(required = false)
    private CancerStudyPermissionEvaluator cancerStudyPermissionEvaluator;

    public void filterOutForbiddenStudies(List<VirtualStudy> virtualStudies) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || cancerStudyPermissionEvaluator == null) {
            return;
        }
        Iterator<VirtualStudy> virtualStudyIterator = virtualStudies.iterator();
        while (virtualStudyIterator.hasNext()) {
            VirtualStudy virtualStudy = virtualStudyIterator.next();
            VirtualStudyData virtualStudyData = virtualStudy.getData();

            Set<VirtualStudySamples> filteredStudies = virtualStudyData.getStudies().stream()
                .filter(study ->
                    cancerStudyPermissionEvaluator.hasPermission(authentication, study.getId(), "CancerStudyId", AccessLevel.READ))
                .collect(Collectors.toSet());
            if (filteredStudies.isEmpty()) {
                virtualStudyIterator.remove();
                continue;
            }
            virtualStudyData.setStudies(filteredStudies);

            StudyViewFilter studyViewFilter = virtualStudyData.getStudyViewFilter();
            List<String> filteredStudyIds = studyViewFilter.getStudyIds().stream()
                .filter(studyId ->
                    cancerStudyPermissionEvaluator.hasPermission(authentication, studyId, "CancerStudyId", AccessLevel.READ))
                .toList();
            virtualStudyData.getStudyViewFilter().setStudyIds(filteredStudyIds);
        }

    }
}
