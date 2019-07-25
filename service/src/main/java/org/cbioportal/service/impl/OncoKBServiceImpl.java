package org.cbioportal.service.impl;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.OncoKBDataCount;
import org.cbioportal.service.OncoKBService;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Hongxin Zhang on 2019-07-25.
 */
@Service
public class OncoKBServiceImpl implements OncoKBService {
    @Override
    public List<OncoKBDataCount> getDataCounts(List<String> attributes, List<Mutation> mutations) {
        List<OncoKBDataCount> oncoKBDataCounts = new ArrayList<>();
        if (attributes == null || attributes.isEmpty()) {
            return oncoKBDataCounts;
        }
        for (Mutation mutation : mutations) {
            JSONObject jsonMutation = new JSONObject(mutation.getAnnotation());
            if (jsonMutation.has("oncokb")) {
                JSONObject oncokbObject = jsonMutation.getJSONObject("oncokb");
                Iterator<String> attrIterator = oncokbObject.keys();
                while (attrIterator.hasNext()) {
                    String attributeId = attrIterator.next();
                    if (attributes.contains(attributeId)) {
                        String value = oncokbObject.get(attributeId).toString();
                        OncoKBDataCount oncoKBDataCount = getOncoKBDataCount(oncoKBDataCounts, attributeId, value);
                        oncoKBDataCount.setCount(oncoKBDataCount.getCount() + 1);
                    }
                }
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

    private OncoKBDataCount getOncoKBDataCount(List<OncoKBDataCount> list, String attributeId, String value) {
        Optional<OncoKBDataCount> oncoKBDataCountOptional = list.stream().filter(item -> item.getAttributeId().equals(attributeId) && item.getValue().equals(value)).findFirst();
        if (oncoKBDataCountOptional.isPresent()) {
            return oncoKBDataCountOptional.get();
        } else {
            OncoKBDataCount oncoKBDataCount = new OncoKBDataCount();
            oncoKBDataCount.setAttributeId(attributeId);
            oncoKBDataCount.setValue(value);
            list.add(oncoKBDataCount);
            return oncoKBDataCount;
        }
    }
}
