package org.mskcc.cbio.portal.or_analysis;

import java.util.*;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.PatientSetUtil;


public class OverRepresentationAnalysisUtil {
    
    public static Map<Long,double[]> getExpressionMap(int cancerStudyId, int profileId, String patientSetId, String patientIdsKey) throws DaoException {
        
        List<String> stableSampleIds = getPatientIds(patientSetId, patientIdsKey);
        List<Integer> sampleIds = new ArrayList<Integer>();
        for(String sampleId : stableSampleIds) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);   
            sampleIds.add(sample.getInternalId()); 
        }   
        sampleIds.retainAll(DaoSampleProfile.getAllSampleIdsInProfile(profileId));

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        
        //get cancer genes
        Set<CanonicalGene> cancerGeneSet = daoGeneOptimized.getCbioCancerGenes();
        Set<Long> entrezGeneIds = new HashSet<Long>();
        for (CanonicalGene cancerGene : cancerGeneSet) {
            entrezGeneIds.add(cancerGene.getEntrezGeneId());
        }
        
        //get gene-value map
        Map<Long, HashMap<Integer, String>> mapStr = daoGeneticAlteration.getGeneticAlterationMap(profileId, entrezGeneIds);
        Map<Long, double[]> map = new HashMap<Long, double[]>(mapStr.size());
        for (Map.Entry<Long, HashMap<Integer, String>> entry : mapStr.entrySet()) {
            Long gene = entry.getKey();
            Map<Integer, String> mapCaseValueStr = entry.getValue();
            double[] values = new double[sampleIds.size()];
            for (int i = 0; i < sampleIds.size(); i++) {
                Integer caseId = sampleIds.get(i);
                String value = mapCaseValueStr.get(caseId);
                Double d;
                try {
                    d = Double.valueOf(value);
                } catch (Exception e) {
                    d = Double.NaN;
                }
                values[i]=d;
            }
            map.put(gene, values);
        }
        return map;
    }

    public static Map<Long, String[]> getMutationMap(int cancerStudyId, int profileId, String patientSetId, String patientIdsKey) throws DaoException {
        //sample ids
        List<String> stableSampleIds = getPatientIds(patientSetId, patientIdsKey);
        List<Integer> sampleIds = new ArrayList<Integer>();
        for(String sampleId : stableSampleIds) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudyId, sampleId);   
            sampleIds.add(sample.getInternalId()); 
        }   
        sampleIds.retainAll(DaoSampleProfile.getAllSampleIdsInProfile(profileId));
        
        //get cancer genes
        Set<Long> entrezGeneIds = new HashSet<Long>();
        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        Set<CanonicalGene> cancerGeneSet = daoGeneOptimized.getCbioCancerGenes();
        for (CanonicalGene cancerGene : cancerGeneSet) {
            entrezGeneIds.add(cancerGene.getEntrezGeneId());
        }
        
        //Init over all result map
        Map<Long, String[]> map = new HashMap<Long, String[]>();

        for (Long entrezGeneId : entrezGeneIds) {
            
            //Get the array of mutations for the rotated gene
            ArrayList<ExtendedMutation> mutObjArr = DaoMutation.getMutations(profileId, sampleIds, entrezGeneId);
            
            //Assign every sample (included non mutated ones) values -- mutated -> Mutation Type, non-mutated -> "Non"
            String[] mutTypeArr = new String[sampleIds.size()]; 
            int _index = 0;
            for (Integer sampleId : sampleIds) {
                String mutationType = "Non";
                for (ExtendedMutation mut : mutObjArr) {
                    if (mut.getSampleId() == sampleId) {
                        mutationType = mut.getEvent().getMutationType();
                    }
                }
                mutTypeArr[_index] = mutationType;
                _index += 1;
            }

            //add a new entry into the overall result map
            map.put(entrezGeneId, mutTypeArr);
        }
        return map;
    }
    
    public static ArrayList<String> getPatientIds(String patientSetId, String patientIdsKey) {
        try {
            DaoPatientList daoPatientList = new DaoPatientList();
            PatientList patientList;
            ArrayList<String> patientIdList = new ArrayList<String>();
            if (patientSetId.equals("-1")) {
                String strPatientIds = PatientSetUtil.getPatientIds(patientIdsKey);
                String[] patientArray = strPatientIds.split("\\s+");
                for (String item : patientArray) {
                    patientIdList.add(item);
                }
            } else {
                patientList = daoPatientList.getPatientListByStableId(patientSetId);
                patientIdList = patientList.getPatientList();
            }
            return patientIdList;
        } catch (DaoException e) {
            System.out.println("Caught Dao Exception: " + e.getMessage());
            return null;
        }
    }
	
}
