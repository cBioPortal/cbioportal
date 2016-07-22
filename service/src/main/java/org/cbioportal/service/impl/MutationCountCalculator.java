package org.cbioportal.service.impl;

import org.cbioportal.persistence.MutationRepository;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.dao.DaoSample;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.Sample;
import org.mskcc.cbio.portal.model.converter.MutationModelConverter;
import org.mskcc.cbio.portal.util.InternalIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class MutationCountCalculator {

    @Autowired
    private MutationRepository mutationRepository;
    @Autowired
    private MutationModelConverter mutationModelConverter;

    public Map<String, Integer> calculate(String mutationGeneticProfileStableId, List<String> sampleStableIds) {

        List<Integer> sampleIds = null;

        GeneticProfile mutationProfile;
        Map<String, Integer> count = Collections.emptyMap();

        mutationProfile = DaoGeneticProfile.getGeneticProfileByStableId(mutationGeneticProfileStableId);
        if (sampleStableIds != null) {
            sampleIds = InternalIdUtil.getInternalNonNormalSampleIds(mutationProfile.getCancerStudyId(),
                    sampleStableIds);
        }

        if (mutationProfile != null) {
            count = convertMapSampleKeys(mutationModelConverter.convertMutationCountToMap(
                    mutationRepository.countMutationEvents(mutationProfile.getGeneticProfileId(), sampleIds)));
        }

        return count;
    }

    private Map<String, Integer> convertMapSampleKeys(Map<Integer, Integer> mutationEventCounts) {

        Map<String, Integer> toReturn = new HashMap<>();
        for (Integer sampleId : mutationEventCounts.keySet()) {
            Sample s = DaoSample.getSampleById(sampleId);
            toReturn.put(s.getStableId(), mutationEventCounts.get(sampleId));
        }
        return toReturn;
    }
}
