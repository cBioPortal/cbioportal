/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.ArrayList;
import java.util.List;
import org.mskcc.cbio.portal.model.PatientList;

/**
 *
 * @author abeshoua
 */
public class CaseListJSON {
    public String id;
    public Integer internal_id;
    public String name;
    public String description;
    public Integer internal_study_id;
    public List<String> case_ids;
    
    public CaseListJSON(PatientList model) {
        this.id = model.getStableId();
        this.internal_id = model.getPatientListId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.internal_study_id = model.getCancerStudyId();
        this.case_ids = model.getPatientList();
    }
    
    public ArrayList<Integer> getInternalCaseIds() {
        List<CaseJSON> cases = CasesController.getCasesByIds(this.internal_study_id, this.case_ids);
        ArrayList<Integer> ret = new ArrayList<>();
        for (CaseJSON c: cases) {
            ret.add(c.internal_id);
        }
        return ret;
    }
}
