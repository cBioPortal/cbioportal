/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.ClinicalData;

/**
 *
 * @author abeshoua
 */
public class ClinicalDataJSON {
    public String attr_id;
    public String attr_val;
    public String case_id;
    public ClinicalDataJSON(ClinicalData model) {
        this.attr_id = model.getAttrId();
        this.attr_val = model.getAttrVal();
        this.case_id = model.getStableId();
    }
}
