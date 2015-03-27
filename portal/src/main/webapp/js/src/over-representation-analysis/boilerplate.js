/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var orAnalysis = (function() {
    
    return {
        
        ids: {
            main_div: "or_analysis",
            sub_tabs_div: "or-analysis-tabs",
            sub_tabs_list: "or-analysis-tabs-list",
            sub_tabs_content: "or-analysis-tabs-content",
            sub_tab_main: "or-analysis-subtab-main",
            sub_tab_mrna_exp: "or-analysis-subtab-mrna-exp",
            sub_tab_advanced: "or-analysis-subtab-advanced"
        },
        texts: {
            sub_tab_main: "Copy-num Alteration / Mutations",
            sub_tab_mrna_exp: "mRNA Expression",
            sub_tab_advanced: "Advanced"
        },
        postfix: {
            datatable_class: "_datatable_class",
            datatable_div: "_datatable_div",
            datatable_id: "_datatable_table"
        }
    };
    
}());

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

