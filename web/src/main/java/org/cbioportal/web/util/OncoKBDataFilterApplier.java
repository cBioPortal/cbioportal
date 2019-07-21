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
                                        List<OncoKBDataFilter> attributes,
                                        Boolean negateFilters) {
        List<Mutation> annotationDataList = new ArrayList<>();
        if (!attributes.isEmpty() && !sampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            String annotationMap = "{";
            for(OncoKBDataFilter attributeProfile : attributes) {
              annotationMap += attributeProfile.getAttributeId() + ":";
              annotationMap += "[";
              for(String valueMutation : attributeProfile.getValues()) {
                annotationMap += valueMutation + ","; 
              }
              annotationMap += "]";
            }
            annotationMap += "}";
            annotationDataList = mutationService.getMutationsInMultipleMolecularProfilesByAnnotation(molecularProfileService
                .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, null, SUMMARY, 10000000, 0, null, null, annotationMap);
            
            sampleIdentifiers = annotationDataList.stream().map(mutation -> {
                SampleIdentifier sampleIdentifier = new SampleIdentifier();
                sampleIdentifier.setSampleId(mutation.getSampleId());
                sampleIdentifier.setStudyId(mutation.getStudyId());
                return sampleIdentifier;
            }).distinct().collect(Collectors.toList());
            return sampleIdentifiers;
          }
        else {
          return sampleIdentifiers;
        }
}