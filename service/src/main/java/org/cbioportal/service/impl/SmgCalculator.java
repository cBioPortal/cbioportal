package org.cbioportal.service.impl;

import org.cbioportal.persistence.MutationRepository;
import org.cbioportal.service.impl.util.MutSigUtil;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.model.converter.MutationModelConverter;
import org.mskcc.cbio.portal.util.InternalIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class SmgCalculator {

    public static int DEFAULT_THERSHOLD_NUM_SMGS = 500;
    public static int DEFAULT_THERSHOLD_RECURRENCE = 2;

    @Autowired
    private MutationRepository mutationRepository;
    @Autowired
    private MutationModelConverter mutationModelConverter;
    @Autowired
    private MutSigUtil mutSigUtil;

    public List<Map<String, Object>> calculate(String mutationGeneticProfileStableId, List<String> sampleStableIds)
            throws DaoException {

        Map<Long, Double> mutsig = Collections.emptyMap();
        Map<Long, Map<String, String>> smgs = Collections.emptyMap();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();

        GeneticProfile mutationProfile = DaoGeneticProfile.getGeneticProfileByStableId(mutationGeneticProfileStableId);
        if (mutationProfile != null) {
            int profileId = mutationProfile.getGeneticProfileId();
            List<Integer> selectedCaseList = new ArrayList<>();

            if (sampleStableIds != null) {
                selectedCaseList = InternalIdUtil.getInternalSampleIds(mutationProfile.getCancerStudyId(),
                        sampleStableIds);
            }

            smgs = mutationModelConverter.convertSignificantlyMutatedGeneToMap(
                    mutationRepository.getSignificantlyMutatedGenes(profileId, null, selectedCaseList,
                            DEFAULT_THERSHOLD_RECURRENCE, DEFAULT_THERSHOLD_NUM_SMGS));

            Set<Long> cbioCancerGeneIds = daoGeneOptimized.getEntrezGeneIds(
                    daoGeneOptimized.getCbioCancerGenes());
            cbioCancerGeneIds.removeAll(smgs.keySet());
            appendGenes(smgs, profileId, selectedCaseList, cbioCancerGeneIds);

            mutsig = mutSigUtil.getMutSig(mutationProfile.getCancerStudyId());
            if (!mutsig.isEmpty()) {
                Set<Long> mutsigGenes = new HashSet<>(mutsig.keySet());
                mutsigGenes.removeAll(smgs.keySet());
                appendGenes(smgs, profileId, selectedCaseList, mutsigGenes);
            }
        }

        List<Map<String, Object>> data = new ArrayList<>();
        for (Map.Entry<Long, Map<String, String>> entry : smgs.entrySet()) {
            Map<String, Object> map = new HashMap<>();

            Long entrez = entry.getKey();
            CanonicalGene gene = daoGeneOptimized.getGene(entrez);

            String hugo = gene.getHugoGeneSymbolAllCaps();
            map.put("gene_symbol", hugo);

            String cytoband = gene.getCytoband();
            map.put("cytoband", cytoband);

            int length = gene.getLength();
            if (length > 0) {
                map.put("length", length);
            }

            Integer count = Integer.parseInt(entry.getValue().get("count"));
            map.put("num_muts", count);

            Double qvalue = mutsig.get(entrez);
            if (qvalue != null) {
                map.put("qval", qvalue);
            }

            Pattern p = Pattern.compile("[,\\s]+");
            String sampleIds[] = p.split(entry.getValue().get("caseIds"));
            List<Integer> sampleInternalIds = new ArrayList<>();
            for (String s : sampleIds) {
                sampleInternalIds.add(Integer.valueOf(s));
            }

            map.put("caseIds", InternalIdUtil.getStableSampleIds(sampleInternalIds));
            data.add(map);
        }

        return data;
    }

    private void appendGenes(Map<Long, Map<String, String>> smgs, int profileId, List<Integer> selectedCaseList,
                             Set<Long> geneIds) {

        if (!geneIds.isEmpty()) {
            List<Integer> intEntrezGeneIds = new ArrayList<>(geneIds.size());
            for (Long entrezGeneId : geneIds) {
                intEntrezGeneIds.add(entrezGeneId.intValue());
            }
            smgs.putAll(mutationModelConverter.convertSignificantlyMutatedGeneToMap(
                    mutationRepository.getSignificantlyMutatedGenes(profileId, intEntrezGeneIds, selectedCaseList, -1,
                            -1)));
        }
    }
}
