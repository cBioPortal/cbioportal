/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.dao.DaoPatient;

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
    
}
