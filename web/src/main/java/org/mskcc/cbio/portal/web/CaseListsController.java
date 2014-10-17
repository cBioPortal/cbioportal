/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPatientList;
import org.mskcc.cbio.portal.model.PatientList;
/**
 *
 * @author abeshoua
 */


public class CaseListsController {

    public static ArrayList<CaseListJSON> getCaseList(String id) throws DaoException {
        DaoPatientList DPL = new DaoPatientList();
        ArrayList<CaseListJSON> ret = new ArrayList<>();
        ret.add(new CaseListJSON(DPL.getPatientListByStableId(id)));
        return ret;
    }
    public static ArrayList<CaseListJSON> getCaseList(int internal_id) throws DaoException {
        DaoPatientList DPL = new DaoPatientList();
        ArrayList<CaseListJSON> ret = new ArrayList<>();
        ret.add(new CaseListJSON(DPL.getPatientListById(internal_id)));
        return ret;
    }
    public static ArrayList<CaseListJSON> getCaseLists(int internal_study_id) throws DaoException {
        DaoPatientList DPL = new DaoPatientList();
        ArrayList<PatientList> lists = DPL.getAllPatientLists(internal_study_id);
        ArrayList<CaseListJSON> ret = new ArrayList<>();
        for (PatientList p: lists) {
            ret.add(new CaseListJSON(p));
        }
        return ret;
    }
}
