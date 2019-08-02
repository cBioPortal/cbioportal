package org.cbioportal.service.util;

import org.cbioportal.service.util.oncokb.HasDriver;
import org.cbioportal.service.util.oncokb.MutationAttribute;
import org.cbioportal.service.util.oncokb.Oncogenicity;
import org.cbioportal.service.util.oncokb.SampleAttribute;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Hongxin Zhang on 2019-07-25.
 */
@Component
public class OncoKBConverter {
    private static final Map<String, String> ONCOGENICITY_TO_HAS_DRIVER = getOncogenicity2hasDriverMap();
    private static final Map<String, List<String>> HAS_DRIVER_TO_ONCOGENICITY = getHasDriver2OncogenicityMap();
    private static final Map<String, String> SAMPLE_ATTR_TO_MUTATION_ATTR = getSampleAttrToMutationAttrMap();
    private static final Map<String, String> MUTATION_ATTR_TO_SAMPLE_ATTR = getMutationAttrToSampleAttrMap();


    private static Map<String, String> getOncogenicity2hasDriverMap() {
        Map<String, String> map = new HashMap<>();
        map.put(Oncogenicity.YES.getOncogenic(), HasDriver.YES.name());
        map.put(Oncogenicity.LIKELY.getOncogenic(), HasDriver.YES.name());
        map.put(Oncogenicity.PREDICTED.getOncogenic(), HasDriver.YES.name());
        map.put(Oncogenicity.LIKELY_NEUTRAL.getOncogenic(), HasDriver.NO.name());
        map.put(Oncogenicity.INCONCLUSIVE.getOncogenic(), HasDriver.NO.name());
        map.put(Oncogenicity.UNKNOWN.getOncogenic(), HasDriver.NO.name());
        return map;
    }

    private static Map<String, List<String>> getHasDriver2OncogenicityMap() {
        Map<String, List<String>> map = new HashMap<>();
        List<String> driverOncogenicity = new ArrayList<>();
        driverOncogenicity.add(Oncogenicity.YES.getOncogenic());
        driverOncogenicity.add(Oncogenicity.LIKELY.getOncogenic());
        driverOncogenicity.add(Oncogenicity.PREDICTED.getOncogenic());

        List<String> notDriverOncogenicity = new ArrayList<>();
        notDriverOncogenicity.add(Oncogenicity.LIKELY_NEUTRAL.getOncogenic());
        notDriverOncogenicity.add(Oncogenicity.INCONCLUSIVE.getOncogenic());
        notDriverOncogenicity.add(Oncogenicity.UNKNOWN.getOncogenic());

        map.put(HasDriver.YES.name(), driverOncogenicity);
        map.put(HasDriver.NO.name(), notDriverOncogenicity);
        return map;
    }

    private static Map<String, String> getSampleAttrToMutationAttrMap() {
        Map<String, String> map = new HashMap<>();
        map.put(SampleAttribute.HAS_DRIVER.name(), MutationAttribute.ONCOGENICITY.name());
        map.put(SampleAttribute.HIGHEST_SENSITIVE_LEVEL.name(), MutationAttribute.HIGHEST_SENSITIVE_LEVEL.name());
        return map;
    }

    private static Map<String, String> getMutationAttrToSampleAttrMap() {
        Map<String, String> map = new HashMap<>();
        map.put(MutationAttribute.ONCOGENICITY.name(), SampleAttribute.HAS_DRIVER.name());
        map.put(MutationAttribute.HIGHEST_SENSITIVE_LEVEL.name(), SampleAttribute.HIGHEST_SENSITIVE_LEVEL.name());
        return map;
    }

    public String getHasDriverByOncogenicity(String oncogenicity) {
        return ONCOGENICITY_TO_HAS_DRIVER.get(oncogenicity);
    }

    public List<String> getOncogenicityByHasDriver(String hasDriver) {
        return HAS_DRIVER_TO_ONCOGENICITY.get(hasDriver);
    }

    public String getMutationAttributeBySampleAttribute(String sampleAttribute) {
        return SAMPLE_ATTR_TO_MUTATION_ATTR.get(sampleAttribute);
    }

    public String getSampleAttributeByMutationAttribute(String mutationAttribute) {
        return MUTATION_ATTR_TO_SAMPLE_ATTR.get(mutationAttribute);
    }
    
    public String getSampleValue(String sampleAttribute, String mutationValue) {
        if(sampleAttribute.equals(SampleAttribute.HAS_DRIVER.name())) {
            return ONCOGENICITY_TO_HAS_DRIVER.get(mutationValue);
        }else if(sampleAttribute.equals(SampleAttribute.HIGHEST_SENSITIVE_LEVEL.name())) {
            return null;
        }
        return null;
    }
}
