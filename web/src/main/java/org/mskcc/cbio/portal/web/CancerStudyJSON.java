/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import java.util.Set;
import org.mskcc.cbio.portal.model.CancerStudy;

/**
 *
 * @author abeshoua
 */
public class CancerStudyJSON {
    public String id;
    public Integer internal_id;
    public String type_of_cancer;
    public String name;
    public String short_name;
    public String description;
    public String pmid;
    public String citation;
    public Set<String> groups;
    
    public CancerStudyJSON(CancerStudy model) {
        this.id = model.getCancerStudyStableId();
        this.internal_id = model.getInternalId();
        this.type_of_cancer = model.getTypeOfCancerId();
        this.name = model.getName();
        this.short_name = model.getShortName();
        this.description = model.getDescription();
        this.pmid = model.getPmid();
        this.citation = model.getCitation();
        this.groups = model.getGroups();
    }
}
