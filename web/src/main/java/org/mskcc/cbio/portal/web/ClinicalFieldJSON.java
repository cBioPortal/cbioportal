/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.ClinicalAttribute;
/**
 *
 * @author abeshoua
 */
public class ClinicalFieldJSON {
    public String attr_id;
    public String display_name;
    public String description;
    public String datatype;
    public String attribute_type;
    public String priority;
    public String category;
    
    public ClinicalFieldJSON(ClinicalAttribute model) {
        this.attr_id = model.getAttrId();
        this.display_name = model.getDisplayName();
        this.description = model.getDescription();
        this.datatype = model.getDatatype();
        this.attribute_type = (model.isPatientAttribute()? "PATIENT":"SAMPLE");
        this.priority = model.getPriority();
        //this.category = model.
    }
}