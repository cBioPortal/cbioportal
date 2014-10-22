/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoClinicalAttribute;
import org.mskcc.cbio.portal.dao.DaoClinicalData;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.ClinicalData;
import org.mskcc.cbio.portal.model.Patient;

/**
 *
 * @author abeshoua
 */
class ClinicalController {
   
    /* META */
    public static ArrayList<ClinicalFieldJSON> getClinicalFieldsByCaseList(int internal_case_list_id) throws DaoException {
        ArrayList<ClinicalFieldJSON> ret = new ArrayList<>();
        CaseListJSON case_list = CaseListsController.getCaseList(internal_case_list_id).get(0);
        List<String> case_ids = case_list.case_ids;
        ArrayList<Integer> internal_case_ids = new ArrayList<>();
        ArrayList<CaseJSON> cases = CasesController.getCasesByIds(case_list.internal_study_id, case_ids);
        for (CaseJSON c: cases) {
            internal_case_ids.add(c.internal_id);
        }
        List<ClinicalAttribute> clinicalAttrs = DaoClinicalAttribute.getDataByInternalIds(internal_case_ids);
        for (ClinicalAttribute attr: clinicalAttrs) {
            ret.add(new ClinicalFieldJSON(attr));
        }
        return ret;
    }
    public static ArrayList<ClinicalFieldJSON> getClinicalFieldsByCaseList(List<Integer> internal_case_ids) throws DaoException {
        ArrayList<ClinicalFieldJSON> ret = new ArrayList<>();
        List<ClinicalAttribute> clinicalAttrs = DaoClinicalAttribute.getDataByInternalIds(internal_case_ids);
        for (ClinicalAttribute attr: clinicalAttrs) {
            ret.add(new ClinicalFieldJSON(attr));
        }
        return ret;
    }
    public static ArrayList<ClinicalFieldJSON> getClinicalFieldsByStudy(int internal_study_id) throws DaoException {
        ArrayList<ClinicalFieldJSON> ret = new ArrayList<>();
        ArrayList<Integer> patientIds = new ArrayList<>();
        for(Patient p: DaoPatient.getPatientsByCancerStudyId(internal_study_id)) {
            patientIds.add(p.getInternalId());
        }       
        List<ClinicalAttribute> clinicalAttrs = DaoClinicalAttribute.getDataByInternalIds(patientIds);
        for (ClinicalAttribute attr: clinicalAttrs) {
            ret.add(new ClinicalFieldJSON(attr));
        }        
        return ret;
    }    
    
    /* DATA */
    public static ArrayList<ClinicalDataJSON> getClinicalDataByCaseList(int internal_case_list_id) throws DaoException {
        ArrayList<ClinicalDataJSON> ret = new ArrayList<>();
        CaseListJSON case_list = CaseListsController.getCaseList(internal_case_list_id).get(0);
        List<String> case_ids = case_list.case_ids;
        List<ClinicalData> clinicalData = DaoClinicalData.getDataByPatientIds(case_list.internal_study_id, case_ids);
        for (ClinicalData datum: clinicalData) {
            ret.add(new ClinicalDataJSON(datum));
        }
        return ret;
    }
    public static ArrayList<ClinicalDataJSON> getClinicalDataByCaseList(List<Integer> internal_case_ids) throws DaoException {
        ArrayList<ClinicalDataJSON> ret = new ArrayList<>();
        List<String> case_ids = new ArrayList<>();
        List<CaseJSON> cases = CasesController.getCasesByInternalIds(internal_case_ids);
        for(CaseJSON c: cases) {
            case_ids.add(c.id);
        }
        List<ClinicalData> clinicalData = DaoClinicalData.getDataByPatientIds(cases.get(0).internal_study_id, case_ids);
        for (ClinicalData datum: clinicalData) {
            ret.add(new ClinicalDataJSON(datum));
        }
        return ret;
    }
    public static ArrayList<ClinicalDataJSON> getClinicalDataByStudy(int internal_study_id) throws DaoException {
        ArrayList<ClinicalDataJSON> ret = new ArrayList<>();
        List<String> case_ids = new ArrayList<>();
        List<CaseJSON> cases = CasesController.getCasesByInternalStudyId(internal_study_id);
        for(CaseJSON c: cases) {
            case_ids.add(c.id);
        }
        List<ClinicalData> clinicalData = DaoClinicalData.getDataByPatientIds(internal_study_id, case_ids);
        for (ClinicalData datum: clinicalData) {
            ret.add(new ClinicalDataJSON(datum));
        }
        return ret;
    } 
}