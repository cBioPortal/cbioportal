/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

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
    public Integer cancer_study_internal_id;
    public List<String> case_ids;
    
    public CaseListJSON(PatientList model) {
        this.id = model.getStableId();
        this.internal_id = model.getPatientListId();
        this.name = model.getName();
        this.description = model.getDescription();
        this.cancer_study_internal_id = model.getCancerStudyId();
        this.case_ids = model.getPatientList();
    }
}
