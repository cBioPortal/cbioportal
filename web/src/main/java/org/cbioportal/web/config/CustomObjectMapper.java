package org.cbioportal.web.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.CopyNumberSegment;
import org.cbioportal.model.Gene;
import org.cbioportal.model.GeneticAlteration;
import org.cbioportal.model.GeneticProfile;
import org.cbioportal.model.Mutation;
import org.cbioportal.model.MutationCount;
import org.cbioportal.model.MutationEvent;
import org.cbioportal.model.Patient;
import org.cbioportal.model.PatientClinicalData;
import org.cbioportal.model.Sample;
import org.cbioportal.model.SampleClinicalData;
import org.cbioportal.model.TypeOfCancer;
import org.cbioportal.model.summary.CancerStudySummary;
import org.cbioportal.model.summary.ClinicalDataSummary;
import org.cbioportal.model.summary.GeneSummary;
import org.cbioportal.model.summary.GeneticAlterationSummary;
import org.cbioportal.model.summary.GeneticProfileSummary;
import org.cbioportal.model.summary.MutationSummary;
import org.cbioportal.model.summary.PatientSummary;
import org.cbioportal.model.summary.SampleSummary;
import org.cbioportal.web.mixin.CancerStudyMixin;
import org.cbioportal.web.mixin.ClinicalAttributeMixin;
import org.cbioportal.web.mixin.CopyNumberSegmentMixin;
import org.cbioportal.web.mixin.GeneMixin;
import org.cbioportal.web.mixin.GeneticAlterationMixin;
import org.cbioportal.web.mixin.GeneticProfileMixin;
import org.cbioportal.web.mixin.MutationCountMixin;
import org.cbioportal.web.mixin.MutationEventMixin;
import org.cbioportal.web.mixin.MutationMixin;
import org.cbioportal.web.mixin.PatientClinicalDataMixin;
import org.cbioportal.web.mixin.PatientMixin;
import org.cbioportal.web.mixin.SampleClinicalDataMixin;
import org.cbioportal.web.mixin.SampleMixin;
import org.cbioportal.web.mixin.TypeOfCancerMixin;
import org.cbioportal.web.mixin.summary.CancerStudySummaryMixin;
import org.cbioportal.web.mixin.summary.ClinicalDataSummaryMixin;
import org.cbioportal.web.mixin.summary.GeneSummaryMixin;
import org.cbioportal.web.mixin.summary.GeneticAlterationSummaryMixin;
import org.cbioportal.web.mixin.summary.GeneticProfileSummaryMixin;
import org.cbioportal.web.mixin.summary.MutationSummaryMixin;
import org.cbioportal.web.mixin.summary.PatientSummaryMixin;
import org.cbioportal.web.mixin.summary.SampleSummaryMixin;

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
        mixinMap.put(ClinicalDataSummary.class, ClinicalDataSummaryMixin.class);
        mixinMap.put(SampleClinicalData.class, SampleClinicalDataMixin.class);
        mixinMap.put(PatientClinicalData.class, PatientClinicalDataMixin.class);
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
