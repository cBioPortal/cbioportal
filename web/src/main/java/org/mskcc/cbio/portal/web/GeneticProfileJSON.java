/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.web;

import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
/**
 *
 * @author abeshoua
 */
public class GeneticProfileJSON {
    public String id;
    public Integer internal_id;
    public String name;
    public String description;
    public Integer cancer_study_internal_id;
    public GeneticAlterationType genetic_alteration_type;
    public boolean show_profile_in_analysis_tab;
    
    public GeneticProfileJSON(GeneticProfile model) {
        this.id = model.getStableId();
        this.internal_id = model.getGeneticProfileId();
        this.name = model.getProfileName();
        this.description = model.getProfileDescription();
        this.cancer_study_internal_id = model.getCancerStudyId();
        this.genetic_alteration_type = model.getGeneticAlterationType();
        this.show_profile_in_analysis_tab = model.showProfileInAnalysisTab();
    }
}