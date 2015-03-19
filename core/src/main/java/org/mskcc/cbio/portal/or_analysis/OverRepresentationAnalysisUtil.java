package org.mskcc.cbio.or_analysis;

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
