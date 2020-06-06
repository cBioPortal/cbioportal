/*
 * Copyright (c) 2015 - 2016 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import java.util.*;

/**
 * Utility Class for Retrieving Genetic Alteration Data.
 *
 * This class is a wrapper for multiple DAO Classes, and enables you to retrieve different types
 * of genomic data, and based on different types of genes, e.g. canonical (protein-coding) genes
 * and microRNAs.
 *
 * @author Ethan Cerami.
 */
@Component
public class GeneticAlterationUtil {
    private static final String NAN = "NaN";

    /**
     * Gets a Row of data corresponding to:  target gene, within the target genetic profile
     * and only within the target samples.
     *
     * @param targetGene                Target Gene.
     * @param targetSampleList          Target Sample List.
     * @param targetGeneticProfile      Target Genetic Profile.
     * @return Array List of String values.
     * @throws DaoException Database Error.
     */
    public static ArrayList<String> getGeneticAlterationDataRow(Gene targetGene,
                                                                List<Integer> targetSampleList,
                                                                GeneticProfile targetGeneticProfile) throws DaoException {
        ArrayList<String> dataRow = new ArrayList<String>();
        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        //  First branch:  are we dealing with a canonical (protein-coding) gene or a microRNA?
        if (targetGene instanceof CanonicalGene) {
            CanonicalGene canonicalGene = (CanonicalGene) targetGene;
            Map<Integer, String> sampleMap;

            //  Handle Mutations one way
            if (targetGeneticProfile.getGeneticAlterationType() == GeneticAlterationType.MUTATION_EXTENDED) {
                sampleMap = getMutationMap(targetSampleList,
                                            targetGeneticProfile.getGeneticProfileId(),
                                            canonicalGene.getEntrezGeneId());
            }
            else if (targetGeneticProfile.getGeneticAlterationType() == GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) {
                String type = canonicalGene.isPhosphoProtein() ? 
                        GeneticAlterationType.PHOSPHORYLATION.name() : GeneticAlterationType.PROTEIN_LEVEL.name();
                sampleMap = getProteinArrayDataMap (targetGeneticProfile.getCancerStudyId(),
                                                   targetSampleList, canonicalGene, type, null)[0];
            }
            else {

                //  Handle All Other Data Types another way
                sampleMap = daoGeneticAlteration.getGeneticAlterationMap(targetGeneticProfile.getGeneticProfileId(),
                                                                        canonicalGene.getEntrezGeneId());
            }

            //  Iterate through all samples in the profile
            for (Integer sampleId :  targetSampleList) {
                String value = sampleMap.get(sampleId);
                if (value == null) {
                    dataRow.add (NAN);
                } else {
                    dataRow.add (value);
                }
            }
        }
        return dataRow;
    }
    
    public static ArrayList<String> getBestCorrelatedProteinArrayDataRow(int cancerStudyId,
                                                                        CanonicalGene targetGene,
                                                                        List<Integer> targetSampleList,
                                                                        List<String> correlatedToData) throws DaoException {
        ArrayList<String> dataRow = new ArrayList<String>();
        String type = targetGene.isPhosphoProtein() ? 
                GeneticAlterationType.PHOSPHORYLATION.name() : GeneticAlterationType.PROTEIN_LEVEL.name();
        Map<Integer, String>[] sampleMaps = getProteinArrayDataMap(cancerStudyId, targetSampleList,
                                                                    targetGene, type, null);
        Map<Integer, String> sampleMap = getBestCorrelatedSampleMap(sampleMaps, targetSampleList, correlatedToData);
        //  Iterate through all samples in the profile
        for (Integer sampleId :  targetSampleList) {
            String value = sampleMap.get(sampleId);
            if (value == null) {
                dataRow.add (NAN);
            } else {
                dataRow.add (value);
            }
        }
        return dataRow;
    }

    /**
     * Gets a Map of Mutation Data.
     */
    private static HashMap <Integer, String> getMutationMap (List<Integer> targetSampleList,
                                                             int geneticProfileId, long entrezGeneId) throws DaoException {
        HashMap <Integer, String> mutationMap = new HashMap <Integer, String>();
        List <ExtendedMutation> mutationList = DaoMutation.getMutations(geneticProfileId, targetSampleList, entrezGeneId);
        for (ExtendedMutation mutation : mutationList) {
            Integer sampleId = mutation.getSampleId();
            //  Handle the possibility of multiple mutations in the same gene / sample
            //  Handles issue:  165
            if (mutationMap.containsKey(sampleId)) {
                String existingStr = mutationMap.get(sampleId);
                mutationMap.put(sampleId, existingStr + "," +
                        mutation.getProteinChange());
            } else {
                mutationMap.put(sampleId, mutation.getProteinChange());
            }
        }
        return mutationMap;
    }

    /**
     * Gets a Map of Protein Array Data.
     */
    private static Map <Integer, String>[] getProteinArrayDataMap(int cancerStudyId, List<Integer> targetSampleList,
                                                                CanonicalGene canonicalGene, String type,
                                                                List<String> correlatedToData) throws DaoException {
        DaoProteinArrayInfo daoPAI = DaoProteinArrayInfo.getInstance();
        DaoProteinArrayData daoPAD = DaoProteinArrayData.getInstance();
        Map <Integer, String>[] ret;
        List<String> arrayIds = new ArrayList<String>();
        if (canonicalGene.isPhosphoProtein()) {
            //TODO: this is somewhat hacky way--rppa array ids have to be aliases of the phosphoprotein
            for (String arrayId : canonicalGene.getAliases()) {
                ProteinArrayInfo pai = daoPAI.getProteinArrayInfo(arrayId);
                if (pai!=null && pai.getCancerStudies().contains(cancerStudyId)) {
                    if (arrayId.contains("-")) {
                        // normalized
                        arrayIds.add(0, arrayId);
                    } else {
                        arrayIds.add(arrayId);
                    }
                }
            }
        } else {
            for (ProteinArrayInfo pai : daoPAI.getProteinArrayInfoForEntrezId(
                    cancerStudyId, canonicalGene.getEntrezGeneId(), Collections.singleton(type))) {
                arrayIds.add(pai.getId());
            }
        }
        if (arrayIds.isEmpty()) {
            ret = new Map[1];
            Map <Integer, String> map = Collections.emptyMap();
            ret[0] = map;
            return ret;
        }
        int n = correlatedToData==null ? 1 : arrayIds.size();
        ret = new Map[n];
        for (int i=0; i<n; i++) {
            String arrayId = arrayIds.get(i);
            if (arrayId == null) {
                continue;
            }
            ret[i] = new HashMap<Integer,String>();
            List<ProteinArrayData> pads = daoPAD.getProteinArrayData(cancerStudyId, arrayId, targetSampleList);
            for (ProteinArrayData pad : pads) {
                ret[i].put(pad.getSampleId(), Double.toString(pad.getAbundance()));
            }
        }
        return ret;
    }
    
    private static Map<Integer,String> getBestCorrelatedSampleMap(Map<Integer, String>[] sampleMaps,
                                                                List<Integer> targetSampleList,
                                                                List<String> correlatedToData) {
        if (sampleMaps.length==1 || correlatedToData==null || correlatedToData.size()!= targetSampleList.size()) {
            return sampleMaps[0];
        }
        Map<Integer, String> ret = null;
        double maxCorr = Double.NEGATIVE_INFINITY;
        for (Map<Integer, String> map : sampleMaps) {
            double corr = calcCorrCoef(map, targetSampleList, correlatedToData);
            if (corr > maxCorr) {
                maxCorr = corr;
                ret = map;
            }
        }
        return ret;
    }
    
    private static double calcCorrCoef(Map<Integer, String> sampleMap,
                                        List<Integer> targetSampleList,
                                        List<String> correlatedToData) {
        try {
            List<Double> l1 = new ArrayList<Double>();
            List<Double> l2 = new ArrayList<Double>();
            for (int i=0; i<targetSampleList.size(); i++) {
                Integer targetSample = targetSampleList.get(i);
                if (correlatedToData.get(i).equals("NAN")) {
                    continue;
                }
                String abun = sampleMap.get(targetSample);
                if (abun==null) {
                    continue;
                }
                l1.add(Double.valueOf(abun));
                l2.add(Double.valueOf(correlatedToData.get(i)));
            }
            int n = l1.size();
            double[] d1 = new double[n];
            double[] d2 = new double[n];
            for (int i=0; i<n; i++) {
                d1[i] = l1.get(i);
                d2[i] = l2.get(i);
            }
            PearsonsCorrelation pc = new PearsonsCorrelation();
            return pc.correlation(d1, d2);
        } catch (Exception e) {
            return -2.0;
        }
    }
}
