package org.cbioportal.web.util;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.OncoKBDataCount;
import org.cbioportal.service.util.OncoKBConverter;
import org.cbioportal.service.util.oncokb.HasDriver;
import org.cbioportal.service.util.oncokb.MutationAttribute;
import org.cbioportal.service.util.oncokb.SampleAttribute;
import org.cbioportal.web.parameter.OncoKBDataFilter;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hongxin Zhang on 2019-07-25.
 */
@Component
public class OncoKBUtils {
    @Autowired
    public OncoKBConverter oncoKBConverter;

    public List<OncoKBDataCount> getDataCounts(List<String> sampleAttributes, List<Mutation> mutations) {
        List<OncoKBDataCount> oncoKBDataCounts = new ArrayList<>();
        if (sampleAttributes == null || sampleAttributes.isEmpty()) {
            return oncoKBDataCounts;
        }
        Map<String, List<Mutation>> sampleMutations = mutations.stream().collect(Collectors.groupingBy(Mutation::getSampleId));
        for (Map.Entry<String, List<Mutation>> sampleMutation : sampleMutations.entrySet()) {
            Map<String, Set<String>> values = new HashMap<>();
            for (Mutation mutation : sampleMutation.getValue()) {
                JSONObject jsonMutation = new JSONObject(mutation.getAnnotation());
                if (jsonMutation.has("oncokb")) {
                    JSONObject oncokbObject = jsonMutation.getJSONObject("oncokb");
                    Iterator<String> attrIterator = oncokbObject.keys();
                    while (attrIterator.hasNext()) {
                        String mutationAttrId = attrIterator.next();
                        String sampleAttrId = oncoKBConverter.getSampleAttributeByMutationAttribute(mutationAttrId);
                        if (sampleAttributes.contains(sampleAttrId)) {
                            String mutationValue = oncokbObject.get(mutationAttrId).toString();
                            String sampleValue = oncoKBConverter.getSampleValue(sampleAttrId, mutationValue);

                            if (!values.containsKey(sampleAttrId)) {
                                values.put(sampleAttrId, new HashSet<>());
                            }
                            values.get(sampleAttrId).add(sampleValue);
                        }
                    }
                }
            }

            for (Map.Entry<String, Set<String>> map : values.entrySet()) {
                String resolvedValue = resolveSampleValue(map.getKey(), map.getValue());
                OncoKBDataCount oncoKBDataCount = getOncoKBDataCount(oncoKBDataCounts, map.getKey(), resolvedValue);
                oncoKBDataCount.setCount(oncoKBDataCount.getCount() + 1);
            }

        }
        int totalNumOfSamples = mutations.stream().map(mutation -> mutation.getSampleId()).collect(Collectors.toSet()).size();
        Map<String, List<OncoKBDataCount>> map = oncoKBDataCounts.stream().collect(Collectors.groupingBy(OncoKBDataCount::getAttributeId));
        for (Map.Entry<String, List<OncoKBDataCount>> entry : map.entrySet()) {
            int count = entry.getValue().stream().mapToInt(OncoKBDataCount::getCount).sum();
            if (count != totalNumOfSamples) {
                OncoKBDataCount oncoKBDataCount = getOncoKBDataCount(oncoKBDataCounts, entry.getKey(), "NA");
                oncoKBDataCount.setCount(oncoKBDataCount.getCount() + (totalNumOfSamples - count));
            }
        }
        return oncoKBDataCounts;
    }

    public List<OncoKBDataFilter> getMutationFilters(List<OncoKBDataFilter> sampleFilters) {
        List<OncoKBDataFilter> mutationFilters = new ArrayList<>();
        for (OncoKBDataFilter sampleFilter : sampleFilters) {
            if (sampleFilter.getAttributeId().equals(SampleAttribute.HAS_DRIVER.name())) {
                OncoKBDataFilter oncoKBDataFilter = new OncoKBDataFilter();
                oncoKBDataFilter.setAttributeId(MutationAttribute.ONCOGENICITY.name());
                oncoKBDataFilter.setValues(new ArrayList<>());
                for (String value : sampleFilter.getValues()) {
                    oncoKBDataFilter.getValues().addAll(oncoKBConverter.getOncogenicityByHasDriver(value));
                }
                mutationFilters.add(oncoKBDataFilter);
            }
        }
        return mutationFilters;
    }

    public String resolveSampleValue(String attributeId, Set<String> values) {
        if (attributeId.equals(SampleAttribute.HAS_DRIVER.name())) {
            if (values.contains(HasDriver.YES.name())) {
                return HasDriver.YES.name();
            } else {
                return HasDriver.NO.name();
            }
        }
        return values.iterator().next();
    }

    private OncoKBDataCount getOncoKBDataCount(List<OncoKBDataCount> list, String attributeId, String value) {
        Optional<OncoKBDataCount> oncoKBDataCountOptional = list.stream().filter(item -> item.getAttributeId().equals(attributeId) && value != null && item.getValue().equals(value)).findFirst();
        if (oncoKBDataCountOptional.isPresent()) {
            return oncoKBDataCountOptional.get();
        } else {
            OncoKBDataCount oncoKBDataCount = new OncoKBDataCount();
            oncoKBDataCount.setAttributeId(attributeId);
            oncoKBDataCount.setValue(value == null ? "NA" : value);
            list.add(oncoKBDataCount);
            return oncoKBDataCount;
        }
    }
}
