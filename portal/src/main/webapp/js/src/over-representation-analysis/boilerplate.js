/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var orAjaxParam = function(_gene, _profile_id) {
    
    this.cancer_study_id = window.PortalGlobals.getCancerStudyId();
    this.gene = _gene;
    this.case_set_id = window.PortalGlobals.getCaseSetId();
    this.case_ids_key =  window.PortalGlobals.getCaseIdsKey();
    this.profile_id = _profile_id;
    
};