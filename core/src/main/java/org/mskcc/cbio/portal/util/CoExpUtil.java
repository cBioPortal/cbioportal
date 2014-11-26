package org.mskcc.cbio.portal.util;

import java.util.*;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.dao.*;



public class CoExpUtil {

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

	public static GeneticProfile getPreferedGeneticProfile(String cancerStudyIdentifier) {
        CancerStudy cs = DaoCancerStudy.getCancerStudyByStableId(cancerStudyIdentifier);
        ArrayList<GeneticProfile> gps = DaoGeneticProfile.getAllGeneticProfiles(cs.getInternalId());
        GeneticProfile final_gp = null;
        for (GeneticProfile gp : gps) {
            // TODO: support miRNA later
            if (gp.getGeneticAlterationType() == GeneticAlterationType.MRNA_EXPRESSION) {
                //rna seq profile (no z-scores applied) holds the highest priority)
                if (gp.getStableId().toLowerCase().contains("rna_seq") &&
                   !gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                    break;
                } else if (!gp.getStableId().toLowerCase().contains("zscores")) {
                    final_gp = gp;
                }
            }
        }
        return final_gp;
    }

    public static Map<Long,double[]> getExpressionMap(int profileId, String patientSetId, String patientIdsKey) throws DaoException {
        
        GeneticProfile gp = DaoGeneticProfile.getGeneticProfileById(profileId);
        List<String> sampleIdsFromPatientIds =
            StableIdUtil.getStableSampleIdsFromPatientIds(gp.getCancerStudyId(), getPatientIds(patientSetId, patientIdsKey));
        List<Integer> sampleIds = new ArrayList<Integer>();
        for(String sampleId : sampleIdsFromPatientIds) {
            Sample sample = DaoSample.getSampleByCancerStudyAndSampleId(gp.getCancerStudyId(), sampleId);   
            sampleIds.add(sample.getInternalId()); 
        }   
        sampleIds.retainAll(DaoSampleProfile.getAllSampleIdsInProfile(profileId));

        DaoGeneticAlteration daoGeneticAlteration = DaoGeneticAlteration.getInstance();
        Map<Long, HashMap<Integer, String>> mapStr = daoGeneticAlteration.getGeneticAlterationMap(profileId, null);
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
	
}
