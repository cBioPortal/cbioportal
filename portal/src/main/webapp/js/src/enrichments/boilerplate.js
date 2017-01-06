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


var enrichmentsTabSettings = (function() {

    return {

        ids: {
            main_div: "enrichementTabDiv",
            sub_tabs_div: "enrichments-tab-tabs",
            sub_tabs_list: "enrichments-tab-tabs-list",
            sub_tabs_content: "enrichments-tab-tabs-content",
            sub_tab_copy_num: "enrichments-subtab-copy-num",
            sub_tab_mutations: "enrichments-subtab-mutations",
            sub_tab_mrna_exp: "enrichments-subtab-mrna-exp",
            sub_tab_advanced: "enrichments-subtab-advanced",
            sub_tab_protein_exp: "enrichments-subtab-protein-exp",
            gene_set_warning: "enrichments-tab-gene-set-warning"
        },
        texts: {
            sub_tab_copy_num: "Copy-number",
            sub_tab_mutations : "Mutations",
            sub_tab_mrna_exp: "mRNA",
            sub_tab_advanced: "Advanced",
            sub_tab_protein_exp: "Protein",
            null_result: "empt"
        },
        postfix: {
        	plot_div: "_plot_div",
            datatable_class: "_datatable_class",
            datatable_div: "_datatable_div",
            datatable_id: "_datatable_table",
            datatable_update_query_button: "_update_query_btn",
            datatable_gene_checkbox_class: "_gene_checkbox_class",
            mrna_sub_tab_profile_selection_dropdown_menu: "_mrna_sub_tab_profile_selection_dropdown_menu",
            protein_exp_sub_tab_profile_selection_dropdown_menu: "_protein_exp_sub_tab_profile_selection_dropdown_menu",
            update_query_gene_list: "_update_query_gene_list",
            title_log: " (log)"
        },
        profile_type: {
            mrna: "MRNA_EXPRESSION",
            copy_num: "COPY_NUMBER_ALTERATION",
            mutations: "MUTATION_EXTENDED",
            protein_exp: "PROTEIN_LEVEL"
        },
        col_width : {
            gene: 150, //150
            cytoband: 100,
            altered_pct: 80, //90
            unaltered_pct: 80, //90
            log_ratio: 60,
            p_val: 80,
            q_val: 80,
            altered_mean: 95,
            unaltered_mean: 95,
            altered_stdev: 95,
            unaltered_stdev: 95,
            direction: 250,
            plot: 30
        },
        col_index: {
            copy_num: {
                gene: 0,
                cytoband: 1,
                altered_pct: 2,
                unaltered_pct: 3,
                log_ratio: 4,
                p_val: 5,
                q_val: 6,
                direction: 7
            },
            mrna: {
                gene: 0,
                cytoband: 1,
                altered_mean: 2,
                unaltered_mean: 3,
                altered_stdev: 4,
                unaltered_stdev: 5,
                p_val: 6,
                q_val: 7,
                plot: 8
            },
            mutations: {
                gene: 0,
                cytoband: 1,
                altered_pct: 2,
                unaltered_pct: 3,
                log_ratio: 4,
                p_val: 5,
                q_val: 6,
                direction: 7
            },
            protein_exp: {
                gene: 0,
                cytoband: 1,
                altered_mean: 2,
                unaltered_mean: 3,
                altered_stdev: 4,
                unaltered_stdev: 5,
                p_val: 6,
                q_val: 7,
                plot: 8
            }
        },
        _title_ids : {
            gene: "gene-help",
            cytoband: "cytoband-help",
            pct: "pct-alt-help",
            log_ratio: "log_ratio_help",
            p_val: "p_val_help",
            q_val: "q_val_help",
            direction: "direction_help",
            mean_alt: "mean_alt_help",
            stdev_alt: "stdev_alt_help",
            p_val_t_test: "p_val_t_test_help",
            update_query: "update_query_help"
        },
        settings: {
            p_val_threshold: 0.05,
            help_icon_img_src: "images/help.png"
        },
        text: {
            mutex: "Mutual exclusivity",
            cooccurrence: "Co-occurrence"
        }
    };

}());

var orAjaxParam = function(alteredCaseList, unalteredCaseList, profileId, geneSet) {

    //convert case id array into a string
    var _tmp_altered_case_id_list = "", _tmp_unaltered_case_id_list = "";
    $.each(alteredCaseList, function(index, _caseId) {
        _tmp_altered_case_id_list += _caseId + " ";
    });
    $.each(unalteredCaseList, function(index, _caseId) {
        _tmp_unaltered_case_id_list += _caseId + " ";
    });

    this.cancer_study_id = window.QuerySession.getCancerStudyIds()[0];
    this.altered_case_id_list = _tmp_altered_case_id_list;
    this.unaltered_case_id_list = _tmp_unaltered_case_id_list;
    this.profile_id = profileId;
    this.gene_list = window.QuerySession.getQueryGenes().join(" ");
    this.gene_set = geneSet;
};

