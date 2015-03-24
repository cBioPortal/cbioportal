/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var orAjaxParam = function(alteredCaseList, unalteredCaseList, profileId) {
    
    var _tmp_altered_case_id_list = "", _tmp_unaltered_case_id_list = "";
    $.each(alteredCaseList, function(index, _caseId) {
        _tmp_altered_case_id_list += _caseId + " ";
    });
    $.each(unalteredCaseList, function(index, _caseId) {
        _tmp_unaltered_case_id_list += _caseId + " ";
    });
    
    this.cancer_study_id = window.PortalGlobals.getCancerStudyId();
    this.altered_case_id_list = _tmp_altered_case_id_list;
    this.unaltered_case_id_list = _tmp_unaltered_case_id_list;
    this.profile_id = profileId;
};