package org.cbioportal.web.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.*;
import org.cbioportal.model.summary.*;
import org.cbioportal.web.mixin.*;
import org.cbioportal.web.mixin.summary.*;

import java.util.HashMap;
import java.util.Map;

public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {

        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Map<Class<?>, Class<?>> mixinMap = new HashMap<>();
        mixinMap.put(CancerStudySummary.class, CancerStudySummaryMixin.class);
        mixinMap.put(GeneSummary.class, GeneSummaryMixin.class);
        mixinMap.put(GeneticAlterationSummary.class, GeneticAlterationSummaryMixin.class);
        mixinMap.put(GeneticProfileSummary.class, GeneticProfileSummaryMixin.class);
        mixinMap.put(MutationSummary.class, MutationSummaryMixin.class);
        mixinMap.put(PatientSummary.class, PatientSummaryMixin.class);
        mixinMap.put(SampleSummary.class, SampleSummaryMixin.class);
        mixinMap.put(CancerStudy.class, CancerStudyMixin.class);
        mixinMap.put(ClinicalAttribute.class, ClinicalAttributeMixin.class);
        mixinMap.put(ClinicalData.class, ClinicalDataMixin.class);
        mixinMap.put(CopyNumberSegment.class, CopyNumberSegmentMixin.class);
        mixinMap.put(Gene.class, GeneMixin.class);
        mixinMap.put(GeneticAlteration.class, GeneticAlterationMixin.class);
        mixinMap.put(GeneticProfile.class, GeneticProfileMixin.class);
        mixinMap.put(MutationCount.class, MutationCountMixin.class);
        mixinMap.put(MutationEvent.class, MutationEventMixin.class);
        mixinMap.put(Mutation.class, MutationMixin.class);
        mixinMap.put(Patient.class, PatientMixin.class);
        mixinMap.put(Sample.class, SampleMixin.class);
        mixinMap.put(TypeOfCancer.class, TypeOfCancerMixin.class);
        super.setMixInAnnotations(mixinMap);
    }
}
