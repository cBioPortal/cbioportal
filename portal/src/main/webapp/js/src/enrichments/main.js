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


/**
 *
 * Main page for Initiating enrichment analysis view
 *
 * Author: suny1@mskcc.org
 * Date: 3/10/2015
 *
 */

var enrichmentsTab = (function() {

    var alteredCaseList = [], unalteredCaseList = [];
    var profile_obj_list = {}, profile_type_list = [];
    var gene_set_opt = "all_genes";
    //var gene_set_opt = "cancer_genes";

    //status of gene set option for each sub-tab
    var gene_set_stat = {
        mut: "cancer_genes",
        copy_num: "cancer_genes",
        mrna: "cancer_genes",
        protein_exp: "cancer_genes"
    };

    var init_copy_num_tab = function(gene_set) {
    //var init_copy_num_tab = function() {

        gene_set_stat.copy_num = gene_set;

        $("#" + enrichmentsTabSettings.ids.sub_tab_copy_num).empty();
        $("#" + enrichmentsTabSettings.ids.sub_tab_copy_num).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_copy_num + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif' alt='loading' /></div>");

        var _profile_list = [];

        var discretized_cna_profile_keywords = [
            "_cna",
            "_cna_rae",
            "_gistic",
            "_cna_consensus"
        ];

        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.copy_num) {
                var _token = _obj.STABLE_ID.replace(window.QuerySession.getCancerStudyIds()[0], "");
                if ($.inArray(_token.toLowerCase(), discretized_cna_profile_keywords) !== -1) {
                    _profile_list.push(_obj);
                }
            }
        });

        var _split_profile_list = [];
        _split_profile_list.length = 0;
        $.each(_profile_list, function(_index, _profile_obj) {
            //split copy number profile into two: deep deletion &
            var _del_obj = jQuery.extend(true, {}, _profile_obj);
            var _amp_obj = jQuery.extend(true, {}, _profile_obj);
            _del_obj.STABLE_ID += "_del";
            _amp_obj.STABLE_ID += "_amp";
            _del_obj.NAME += " (Deep Deletion)";
            _amp_obj.NAME += " (Amplification)";

            _split_profile_list.push(_del_obj);
            _split_profile_list.push(_amp_obj);
        });

        var orSubTabCopyNum = new orSubTabView();
        orSubTabCopyNum.init(enrichmentsTabSettings.ids.sub_tab_copy_num, _split_profile_list, enrichmentsTabSettings.profile_type.copy_num, gene_set);
    };

    var init_mutations_tab = function(gene_set) {
    //var init_mutations_tab = function() {

        gene_set_stat.mut = gene_set;

        $("#" + enrichmentsTabSettings.ids.sub_tab_mutations).empty();
        $("#" + enrichmentsTabSettings.ids.sub_tab_mutations).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_mutations + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif' alt='loading' /></div>");

        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.mutations) {
                _profile_list.push(_obj);
            }
        });

        var orSubTabMutations = new orSubTabView();
        orSubTabMutations.init(enrichmentsTabSettings.ids.sub_tab_mutations, _profile_list, enrichmentsTabSettings.profile_type.mutations, gene_set);
    };

    var init_mrna_exp_tab = function(gene_set) {
    //var init_mrna_exp_tab = function() {

        gene_set_stat.mrna = gene_set;

        $("#" + enrichmentsTabSettings.ids.sub_tab_mrna_exp).empty();
        $("#" + enrichmentsTabSettings.ids.sub_tab_mrna_exp).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_mrna_exp + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif' alt='loading' /></div>");

        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.mrna &&
                !isZScoreProfile(_obj)){
                _profile_list.push(_obj);
            }
        });

        //filter out profiles without data
        var orSubTabMrnaExp = new orSubTabView();
        orSubTabMrnaExp.init(enrichmentsTabSettings.ids.sub_tab_mrna_exp, _profile_list, enrichmentsTabSettings.profile_type.mrna, gene_set);

    };

    var init_protein_exp_tab = function(gene_set) {

        gene_set_stat.protein_exp = gene_set;

        $("#" + enrichmentsTabSettings.ids.sub_tab_protein_exp).empty();
        $("#" + enrichmentsTabSettings.ids.sub_tab_protein_exp).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_protein_exp + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif' alt='loading' /></div>");

        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.protein_exp &&
                _obj.STABLE_ID.toLowerCase().indexOf("zscores") === -1) {
                _profile_list.push(_obj);
            }
        });

        var orSubTabProteinExp = new orSubTabView();
        orSubTabProteinExp.init(enrichmentsTabSettings.ids.sub_tab_protein_exp, _profile_list, enrichmentsTabSettings.profile_type.protein_exp, gene_set);
    };

    /**
     * based on the profiles retrieved, checks whether there is valid data available for the plots
     * @param profiles
     * @returns {{expression: boolean, mutations: boolean, cna: boolean, protein: boolean}}
     */
    function getValidProfiles(profiles){
        var validProfiles = {
            expression: false,
            mutations: false,
            cna: false,
            protein: false
        };

        $.each(profiles, function(key, curProfile){
            if(curProfile.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.mrna && !isZScoreProfile(curProfile)){
                validProfiles.expression = true;
            }
            else if(curProfile.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.protein_exp && !isZScoreProfile(curProfile)){
                validProfiles.protein = true;
            }
            else if(curProfile.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.copy_num) {
                validProfiles.cna = true;
            }
            else if(curProfile.GENETIC_ALTERATION_TYPE === enrichmentsTabSettings.profile_type.mutations){
                validProfiles.mutations = true;
            }
        });
        return validProfiles;
    }

    /**
     * check whether we're dealing with a z-score profile
     * apparently the id can also contain z-score. Hopefully in the future we can switch to only DATATYPE
     * @param profile
     * @returns {boolean}
     */
    function isZScoreProfile(profile){
        var id = profile.STABLE_ID.toLowerCase();
        return (
            //profile.DATATYPE.toLowerCase()==='z-score' ||
            id.indexOf("z-scores") !== -1 ||
            id.indexOf("zscores") !== -1);
    }

    var init_ajax = function() {

        //retrieve data from server
        $.ajax({
            method: "POST",
            url: "getGeneticProfile.json",
            data: {
                cancer_study_id: window.QuerySession.getCancerStudyIds()[0]
            }
        }).done(function(result){

            profile_obj_list = result;

            // retrieve which profiles are valid for the plot tabs
            var validProfiles = getValidProfiles(profile_obj_list);

            //Generate sub tabs
            if (validProfiles.mutations) { // study has mutation data which we can show
                $("#" + enrichmentsTabSettings.ids.sub_tabs_list).append("<li><a href='#" + enrichmentsTabSettings.ids.sub_tab_mutations + "' class='enrichments-tabs-ref'><span>" + enrichmentsTabSettings.texts.sub_tab_mutations + "</span></a></li>");
            }
            if (validProfiles.cna) { //study has copy number data
                $("#" + enrichmentsTabSettings.ids.sub_tabs_list).append("<li><a href='#" + enrichmentsTabSettings.ids.sub_tab_copy_num + "' class='enrichments-tabs-ref'><span>" + enrichmentsTabSettings.texts.sub_tab_copy_num + "</span></a></li>");
            }
            if (validProfiles.expression) { //study has expression data
                $("#" + enrichmentsTabSettings.ids.sub_tabs_list).append("<li><a href='#" + enrichmentsTabSettings.ids.sub_tab_mrna_exp + "' class='enrichments-tabs-ref'><span>" + enrichmentsTabSettings.texts.sub_tab_mrna_exp + "</span></a></li>");
            }
            if (validProfiles.protein) { //study has RPPA data
                $("#" + enrichmentsTabSettings.ids.sub_tabs_list).append("<li><a href='#" + enrichmentsTabSettings.ids.sub_tab_protein_exp + "' class='enrichments-tabs-ref'><span>" + enrichmentsTabSettings.texts.sub_tab_protein_exp + "</span></a></li>");
            }

            $("#" + enrichmentsTabSettings.ids.sub_tabs_content).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_mutations + "'></div>");
            $("#" + enrichmentsTabSettings.ids.sub_tabs_content).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_copy_num + "'></div>");
            $("#" + enrichmentsTabSettings.ids.sub_tabs_content).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_mrna_exp + "'></div>");
            $("#" + enrichmentsTabSettings.ids.sub_tabs_content).append("<div id='" + enrichmentsTabSettings.ids.sub_tab_protein_exp + "'></div>");

            $("#" + enrichmentsTabSettings.ids.sub_tabs_div).tabs();
            $("#" + enrichmentsTabSettings.ids.sub_tabs_div).tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
            $("#" + enrichmentsTabSettings.ids.sub_tabs_div).tabs("option", "active", 0);
            $(window).trigger("resize");

            if (validProfiles.mutations) { // study has valid mutation data
                init_mutations_tab(gene_set_opt);
            } else if (validProfiles.cna) {
                init_copy_num_tab(gene_set_opt);
            } else if (validProfiles.expression) {
                init_mrna_exp_tab(gene_set_opt);
            } else if (validProfiles.protein) {
                init_protein_exp_tab(gene_set_opt);
            }

            //bind event listener
            $("#" + enrichmentsTabSettings.ids.sub_tabs_div).on("tabsactivate", function(event, ui) {
                if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_copy_num) {
                    if ($("#" + enrichmentsTabSettings.ids.sub_tab_copy_num).is(':empty')) init_copy_num_tab(gene_set_opt);
                } else if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_mutations) {
                    if ($("#" + enrichmentsTabSettings.ids.sub_tab_copy_num).is(':empty')) init_mutations_tab(gene_set_opt);
                } else if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_mrna_exp) {
                    if ($("#" + enrichmentsTabSettings.ids.sub_tab_mrna_exp).is(':empty')) init_mrna_exp_tab(gene_set_opt);
                } else if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_protein_exp) {
                    if ($("#" + enrichmentsTabSettings.ids.sub_tab_protein_exp).is(':empty')) init_protein_exp_tab(gene_set_opt);
                }
            });

        }).fail(function( jqXHR, textStatus ) {
            alert( "Request failed: " + textStatus );
        });
    }

    var update = function() {

        //get the currently selected tab index
        var $tabs = $('#' + enrichmentsTabSettings.ids.sub_tabs_div).tabs();
        var selected = $tabs.tabs('option', 'active');
        var selectedTabTitle = $($("#" +  + enrichmentsTabSettings.ids.sub_tabs_div + " li")[selected]).text();

        //update current selected sub tab only
        if (selectedTabTitle === enrichmentsTabSettings.texts.sub_tab_mutations) {
            init_mutations_tab(gene_set_opt);
        } else if (selectedTabTitle === enrichmentsTabSettings.texts.sub_tab_copy_num) {
            init_copy_num_tab(gene_set_opt);
        } else if (selectedTabTitle === enrichmentsTabSettings.texts.sub_tab_mrna_exp) {
            init_mrna_exp_tab(gene_set_opt);
        } else if (selectedTabTitle === enrichmentsTabSettings.texts.sub_tab_protein_exp) {
            init_protein_exp_tab(gene_set_opt);
        }

        //bind event listener
        $("#" + enrichmentsTabSettings.ids.sub_tabs_div).on("tabsactivate", function(event, ui) {
            if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_copy_num) {
                if ($("#" + enrichmentsTabSettings.ids.sub_tab_copy_num).is(':empty')) init_copy_num_tab(gene_set_opt);
            } else if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_mutations) {
                if ($("#" + enrichmentsTabSettings.ids.sub_tab_mutations).is(':empty')) init_mutations_tab(gene_set_opt);
            } else if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_mrna_exp) {
                if ($("#" + enrichmentsTabSettings.ids.sub_tab_mrna_exp).is(':empty')) init_mrna_exp_tab(gene_set_opt);
            } else if (ui.newTab.text() === enrichmentsTabSettings.texts.sub_tab_protein_exp) {
                if ($("#" + enrichmentsTabSettings.ids.sub_tab_protein_exp).is(':empty')) init_protein_exp_tab(gene_set_opt);
            }
        });

    }

    return {
        init: function(caseListObj) {

            //re-format the case lists
            alteredCaseList.length = 0;
            unalteredCaseList.length = 0;
            for (var key in caseListObj) {
                if (caseListObj.hasOwnProperty(key)) {
                    if (caseListObj[key] === "altered") {
                        alteredCaseList.push(key);
                    } else if (caseListObj[key] === "unaltered") {
                        unalteredCaseList.push(key);
                    }
                }
            }

            if ((alteredCaseList.length === 1 && alteredCaseList[0] === "") || alteredCaseList.length === 0) {
                $("#" + enrichmentsTabSettings.ids.main_div).append("No alteration in selected samples, therefore could not perform this calculation.");
            } else if ((unalteredCaseList.length === 1 && unalteredCaseList[0] === "") || unalteredCaseList.length === 0) {
                $("#" + enrichmentsTabSettings.ids.main_div).append("No non-alteration in selected samples, therefore could not perform this calculation.");
            } else {
                init_ajax();
            }

        },
        getAlteredCaseList: function() {
            return alteredCaseList;
        },
        getUnalteredCaseList: function() {
            return unalteredCaseList;
        },
        update: update
    };

}());



