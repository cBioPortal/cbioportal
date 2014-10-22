/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoPatient;
import org.mskcc.cbio.portal.model.Patient;

/**
 *
 * @author abeshoua
 */
public class CasesController {
    public static ArrayList<CaseJSON> getCasesByInternalIds(List<Integer> internalIds) {
        ArrayList<CaseJSON> ret = new ArrayList<>();
        for (int id: internalIds) {
            ret.add(new CaseJSON(DaoPatient.getPatientById(id)));
        }
        return ret;
    }
    
    public static ArrayList<CaseJSON> getCasesByIds(int internalStudyId, List<String> ids) {
        ArrayList<CaseJSON> ret = new ArrayList<>();
        for (String id: ids) {
            ret.add(new CaseJSON(DaoPatient.getPatientByCancerStudyAndPatientId(internalStudyId, id)));
        }
        return ret;
    }
    
    public static ArrayList<CaseJSON> getCasesByInternalStudyId(int internalStudyId) {
        ArrayList<CaseJSON> ret = new ArrayList<>();
        for (Patient p: DaoPatient.getPatientsByCancerStudyId(internalStudyId)) {
            ret.add(new CaseJSON(p));
        }
        return ret;
    }
}
