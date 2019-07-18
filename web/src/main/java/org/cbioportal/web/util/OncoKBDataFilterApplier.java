package org.cbioportal.web.util;

import org.cbioportal.model.Sample;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class OncoKBDataFilterApplier
{
    private MutationService mutationService;
    private SampleService sampleService;
    protected StudyViewFilterUtil studyViewFilterUtil;

    public OncoKBDataFilterApplier(MutationService mutationaService, 
                                     SampleService sampleService,
                                     StudyViewFilterUtil studyViewFilterUtil) 
    {
        this.mutationService = mutationService;
        this.sampleService = sampleService;
        this.studyViewFilterUtil = studyViewFilterUtil;
    }

    public List<SampleIdentifier> apply(List<SampleIdentifier> sampleIdentifiers,
                                        List<T> attributes,
                                        Boolean negateFilters) {
        List<Mutation> annotationDataList = new ArrayList<>();
        if (!attributes.isEmpty() && !sampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            annotationDataList = mutationService.getMutationsInMultipleMolecularProfilesByAnnotation(molecularProfileService
                .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, null, SUMMARY, 10000000, 0, null, null, null);
          }
        else {
          return sampleIdentifiers;
        }
}