/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.Patient;

/**
 *
 * @author abeshoua
 */
public class CaseJSON {
    public String id;
    public Integer internal_id;
    public String study_id;
    public Integer internal_study_id;
    public CaseJSON(Patient model) {
        this.id = model.getStableId();
        this.internal_id = model.getInternalId();
        this.study_id = model.getCancerStudy().getCancerStudyStableId();
        this.internal_study_id = model.getCancerStudy().getInternalId();
    }
}
