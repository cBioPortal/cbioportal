/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


var orAnalysis = (function() {
    
    return {
        
        ids: {
            main_div: "or_analysis",
            sub_tabs_div: "or-analysis-tabs",
            sub_tabs_list: "or-analysis-tabs-list",
            sub_tabs_content: "or-analysis-tabs-content",
            sub_tab_copy_num: "or-analysis-subtab-copy-num",
            sub_tab_mutations: "or-analysis-subtab-mutations",
            sub_tab_mrna_exp: "or-analysis-subtab-mrna-exp",
            sub_tab_advanced: "or-analysis-subtab-advanced"
        },
        texts: {
            sub_tab_copy_num: "Copy-num Alteration",
            sub_tab_mutations : "Mutations",
            sub_tab_mrna_exp: "mRNA Expression",
            sub_tab_advanced: "Advanced",
            null_result: "empt"
        },
        postfix: {
            datatable_class: "_datatable_class",
            datatable_div: "_datatable_div",
            datatable_id: "_datatable_table"
        },
        profile_type: {
            mrna: "MRNA_EXPRESSION",
            copy_num: "COPY_NUMBER_ALTERATION",
            mutations: "MUTATION_EXTENDED"
        },
        col_width : {
            gene: 90,
            altered_pct: 120,
            unaltered_pct: 120,
            log_ratio: 120,
            p_val: 100,
            q_val: 100,
            altered_mean: 100,
            unaltered_mean: 100,
            altered_stdev: 100,
            unaltered_stdev: 100
        },
        col_index: {
            copy_num: {
                gene: 0,
                altered_pct: 1,
                unaltered_pct: 2,
                log_ratio: 3,
                p_val: 4,
                q_val: 5,
                direction: 6
            },
            mrna: {
                gene: 0,
                altered_mean: 1,
                unaltered_mean: 2,
                altered_stdev: 3,
                unaltered_stdev: 4,
                p_val: 5,
                q_val: 6
            },
            mutations: {
                gene: 0,
                altered_pct: 1,
                unaltered_pct: 2,
                log_ratio: 3,
                p_val: 4,
                q_val: 5,
                direction: 6
            }
        }
    };
    
}());

var orAjaxParam = function(alteredCaseList, unalteredCaseList, profileId) {
    
    //convert case id array into a string
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
    this.gene_list = window.PortalGlobals.getGeneListString();
};

