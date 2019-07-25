package org.cbioportal.web.util;

import org.cbioportal.model.AnnotationFilter;
import org.cbioportal.model.Mutation;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.MutationService;
import org.cbioportal.service.SampleService;
import org.cbioportal.web.parameter.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.cbioportal.web.parameter.Projection.SUMMARY;

@Component
public class OncoKBDataFilterApplier {
    private MutationService mutationService;
    private SampleService sampleService;
    protected StudyViewFilterUtil studyViewFilterUtil;
    private MolecularProfileService molecularProfileService;

    @Autowired
    public OncoKBDataFilterApplier(MutationService mutationService,
                                   SampleService sampleService,
                                   StudyViewFilterUtil studyViewFilterUtil,
                                   MolecularProfileService molecularProfileService) {
        this.mutationService = mutationService;
        this.sampleService = sampleService;
        this.studyViewFilterUtil = studyViewFilterUtil;
        this.molecularProfileService = molecularProfileService;
    }

    public List<SampleIdentifier> apply(List<SampleIdentifier> sampleIdentifiers,
                                        List<OncoKBDataFilter> attributes,
                                        Boolean negateFilters) {
        List<Mutation> annotationDataList = new ArrayList<>();
        if (!attributes.isEmpty() && !sampleIdentifiers.isEmpty()) {
            List<String> studyIds = new ArrayList<>();
            List<String> sampleIds = new ArrayList<>();
            studyViewFilterUtil.extractStudyAndSampleIds(sampleIdentifiers, studyIds, sampleIds);
            annotationDataList = mutationService.getMutationsInMultipleMolecularProfilesByAnnotation(molecularProfileService
                    .getFirstMutationProfileIds(studyIds, sampleIds), sampleIds, null, SUMMARY.name(), 10000000, 0, null, null,
                getAnnotationFilter(attributes));

            sampleIdentifiers = annotationDataList.stream().map(mutation -> {
                SampleIdentifier sampleIdentifier = new SampleIdentifier();
                sampleIdentifier.setSampleId(mutation.getSampleId());
                sampleIdentifier.setStudyId(mutation.getStudyId());
                return sampleIdentifier;
            }).distinct().collect(Collectors.toList());
            return sampleIdentifiers;
        } else {
            return sampleIdentifiers;
        }
    }

    public List<AnnotationFilter> getAnnotationFilter(List<OncoKBDataFilter> oncoKBDataFilters) {
        List<AnnotationFilter> annotationFilters = new ArrayList<>();
        if (oncoKBDataFilters == null) {
            return annotationFilters;
        }

        for (OncoKBDataFilter oncoKBDataFilter : oncoKBDataFilters) {
            AnnotationFilter annotationFilter = new AnnotationFilter();
            annotationFilter.setPath("$.oncokb." + oncoKBDataFilter.getAttributeId());
            annotationFilter.setValues(oncoKBDataFilter.getValues());
            annotationFilters.add(annotationFilter);
        }
        return annotationFilters;
    }
}