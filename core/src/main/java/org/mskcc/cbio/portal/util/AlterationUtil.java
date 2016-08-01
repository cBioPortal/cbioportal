package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticAlteration;
import org.mskcc.cbio.portal.dao.DaoGeneticProfile;
import org.mskcc.cbio.portal.model.CosmicMutationFrequency;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AlterationUtil {

    public void addEventGenes(Map<Long, Set<Long>> mapTargetToEventGenes, long gene, Set<Long> targets) {
        for (Long target : targets) {
            Set<Long> eventGenes = mapTargetToEventGenes.get(target);
            if (eventGenes==null) {
                eventGenes = new HashSet<>();
                mapTargetToEventGenes.put(target, eventGenes);
            }
            eventGenes.add(gene);
        }
    }

    public void addDrugs(Map<Long, Set<Long>> mapTargetToEventGenes, Map<Long, List<String>> map,
                         Map<Long, Set<String>> ret) {
        for (Map.Entry<Long, List<String>> entry : map.entrySet()) {
            Set<Long> eventGenes = mapTargetToEventGenes.get(entry.getKey());
            if (eventGenes!=null) {
                for (long eventGene : eventGenes) {
                    Set<String> drugs = ret.get(eventGene);
                    if (drugs==null) {
                        drugs = new HashSet<String>();
                        ret.put(eventGene, drugs);
                    }
                    drugs.addAll(entry.getValue());
                }
            }
        }
    }

    public Map<Long, Map<String,Object>> getMrnaContext(Integer internalSampleId, List<Long> entrezGeneIds,
                                                         String mrnaProfileId) throws DaoException {
        Map<Long, Map<String,Object>> mapGenePercentile = new HashMap<>();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        for (Long gene : entrezGeneIds) {
            if (mapGenePercentile.containsKey(gene)) {
                continue;
            }

            Map<Integer,String> mrnaMap = daoGeneticAlteration.getGeneticAlterationMap(
                    DaoGeneticProfile.getGeneticProfileByStableId(mrnaProfileId).getGeneticProfileId(),
                    gene);
            double mrnaCase = parseNumber(mrnaMap.get(internalSampleId));
            if (Double.isNaN(mrnaCase)) {
                continue;
            }

            Map<String,Object> map = new HashMap<>();
            mapGenePercentile.put(gene, map);

            map.put("zscore", mrnaCase);

            int total = 0, below = 0;
            for (String strMrna : mrnaMap.values()) {
                double mrna = parseNumber(strMrna);
                if (Double.isNaN(mrna)) {
                    continue;
                }

                total++;
                if (mrna <= mrnaCase) {
                    below++;
                }
            }

            map.put("perc", 100*below/total);
        }

        return mapGenePercentile;
    }

    private double parseNumber(String mrna) {
        try {
            return Double.parseDouble(mrna);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public List<List> convertCosmicDataToMatrix(Set<CosmicMutationFrequency> cosmic) {
        if (cosmic==null) {
            return null;
        }
        List<List> mat = new ArrayList(cosmic.size());
        for (CosmicMutationFrequency cmf : cosmic) {
            List l = new ArrayList(3);
            l.add(cmf.getId());
            l.add(cmf.getAminoAcidChange());
            l.add(cmf.getFrequency());
            mat.add(l);
        }
        return mat;
    }
}
