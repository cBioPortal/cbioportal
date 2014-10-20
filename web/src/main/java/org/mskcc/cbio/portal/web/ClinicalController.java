/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoClinicalAttribute;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.model.Patient;
import org.mskcc.cbio.portal.servlet.ClinicalJSON;
import org.mskcc.cbio.portal.util.WebserviceParserUtils;

/**
 *
 * @author abeshoua
 */
class ClinicalController {
   
    public static ArrayList<ClinicalFieldJSON> getClinicalFieldsByCaseList(int internal_case_list_id) throws DaoException {
        ArrayList<ClinicalFieldJSON> ret = new ArrayList<>();
        ArrayList<CaseListJSON> cases = CaseListsController.getCaseList(internal_case_list_id);
        ArrayList<Integer> internal_case_ids = new ArrayList<>();
        for (CaseListJSON c: cases) {
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
        ArrayList<Integer> patientIds = new ArrayList<Integer>();
        for(Patient p: DaoPatient.getPatientsByCancerStudyId(internal_study_id)) {
            patientIds.add(p.getInternalId());
        }       
        List<ClinicalAttribute> clinicalAttrs = DaoClinicalAttribute.getDataByInternalIds(patientIds);
        for (ClinicalAttribute attr: clinicalAttrs) {
            ret.add(new ClinicalFieldJSON(attr));
        }        
        return ret;
    }    
}