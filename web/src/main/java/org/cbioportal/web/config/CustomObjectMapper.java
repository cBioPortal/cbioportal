package org.cbioportal.web.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import java.util.HashMap;
import java.util.Map;
import org.cbioportal.model.*;
import org.cbioportal.web.mixin.*;

public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {
        super.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Map<Class<?>, Class<?>> mixinMap = new HashMap<>();
        mixinMap.put(CancerStudy.class, CancerStudyMixin.class);
        mixinMap.put(Gene.class, GeneMixin.class);
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
