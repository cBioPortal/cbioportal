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
 * Main page for Initiating over representation analysis view
 *
 * Author: yichaoS
 * Date: 3/10/2015
 * 
 */

var or_tab = (function() {

    var alteredCaseList = [], unalteredCaseList = [];
    var profile_obj_list = {}, profile_type_list = [];

    //var init_copy_num_tab = function(gene_set) {
    var init_copy_num_tab = function() {

        $("#" + orAnalysis.ids.sub_tab_copy_num).empty();
        $("#" + orAnalysis.ids.sub_tab_copy_num).append("<div id='" + orAnalysis.ids.sub_tab_copy_num + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif'></div>");

        var _profile_list = [];

        var discretized_cna_profile_keywords = [
            "_cna",
            "_cna_rae",
            "_gistic",
            "_cna_consensus"
        ];

        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === orAnalysis.profile_type.copy_num) {
                var _token = _obj.STABLE_ID.replace(window.PortalGlobals.getCancerStudyId(), "");
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
        //orSubTabCopyNum.init(orAnalysis.ids.sub_tab_copy_num, _split_profile_list, orAnalysis.profile_type.copy_num, gene_set);
        orSubTabCopyNum.init(orAnalysis.ids.sub_tab_copy_num, _split_profile_list, orAnalysis.profile_type.copy_num, "cancer_genes");
    };

    //var init_mutations_tab = function(gene_set) {
    var init_mutations_tab = function() {

        $("#" + orAnalysis.ids.sub_tab_mutations).empty();
        $("#" + orAnalysis.ids.sub_tab_mutations).append("<div id='" + orAnalysis.ids.sub_tab_mutations + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif'></div>");

        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === orAnalysis.profile_type.mutations) {
                _profile_list.push(_obj);
            }
        });

        var orSubTabMutations = new orSubTabView();
        //orSubTabMutations.init(orAnalysis.ids.sub_tab_mutations, _profile_list, orAnalysis.profile_type.mutations, gene_set);
        orSubTabMutations.init(orAnalysis.ids.sub_tab_mutations, _profile_list, orAnalysis.profile_type.mutations, "cancer_genes"); //init with only one profile
    };

    //var init_mrna_exp_tab = function(gene_set) {
    var init_mrna_exp_tab = function() {

        $("#" + orAnalysis.ids.sub_tab_mrna_exp).empty();
        $("#" + orAnalysis.ids.sub_tab_mrna_exp).append("<div id='" + orAnalysis.ids.sub_tab_mrna_exp + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif'></div>");

        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === orAnalysis.profile_type.mrna &&
                _obj.STABLE_ID.toLowerCase().indexOf("z-scores") === -1 &&
                _obj.STABLE_ID.toLowerCase().indexOf("zscores") === -1) {
                _profile_list.push(_obj);
            }
        });

        //filter out profiles without data
        var orSubTabMrnaExp = new orSubTabView();
        //orSubTabMrnaExp.valid(orSubTabMrnaExp.init, _profile_list, "cancer_genes", orAnalysis.ids.sub_tab_mrna_exp,orAnalysis.profile_type.mrna);
        orSubTabMrnaExp.init(orAnalysis.ids.sub_tab_mrna_exp, _profile_list, orAnalysis.profile_type.mrna, "cancer_genes");

    };

    //var init_protein_exp_tab = function(gene_set) {
    var init_protein_exp_tab = function() {

        $("#" + orAnalysis.ids.sub_tab_protein_exp).empty();
        $("#" + orAnalysis.ids.sub_tab_protein_exp).append("<div id='" + orAnalysis.ids.sub_tab_protein_exp + "_loading_img'><img style='padding:20px;' src='images/ajax-loader.gif'></div>");

        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if ((_obj.GENETIC_ALTERATION_TYPE === orAnalysis.profile_type.protein_exp &&
                _obj.STABLE_ID.toLowerCase().indexOf("zscores") === -1)) {
                _profile_list.push(_obj);
            }
        });
        var _phospho_exp_obj = jQuery.extend(true, {}, _profile_list[0]);
        var _protein_exp_obj = jQuery.extend(true, {}, _profile_list[0]);
        _phospho_exp_obj.STABLE_ID += "_phospho";
        _protein_exp_obj.STABLE_ID += "_protein";
        _phospho_exp_obj.NAME = "Phosphoprotein level (RPPA)";
        _protein_exp_obj.NAME = "Protein expression (RPPA)";

        _profile_list.length = 0;
        _profile_list = [];
        _profile_list.push(_phospho_exp_obj);
        _profile_list.push(_protein_exp_obj);

        var orSubTabProteinExp = new orSubTabView();
        //orSubTabProteinExp.init(orAnalysis.ids.sub_tab_protein_exp, _profile_list, orAnalysis.profile_type.protein_exp, gene_set);
        orSubTabProteinExp.init(orAnalysis.ids.sub_tab_protein_exp, _profile_list, orAnalysis.profile_type.protein_exp, "cancer_genes");
    };

    var init_ajax = function() {

        //retrieve data from server
        $.ajax({
            method: "POST",
            url: "getGeneticProfile.json",
            data: {
                cancer_study_id: window.PortalGlobals.getCancerStudyId()
            }
        }).done(function(result){

            profile_obj_list = result;

            //Extract genetic profile info
            $.each(Object.keys(profile_obj_list), function(index, key) {
                var _obj = result[key];
                if($.inArray(_obj.GENETIC_ALTERATION_TYPE, profile_type_list) === -1) {
                    profile_type_list.push(_obj.GENETIC_ALTERATION_TYPE);
                }
            });

            //Generate sub tabs
            if ($.inArray("MUTATION_EXTENDED", profile_type_list) !== -1) { // study has mutation data
                $("#" + orAnalysis.ids.sub_tabs_list).append("<li><a href='#" + orAnalysis.ids.sub_tab_mutations + "' class='or-analysis-tabs-ref'><span>" + orAnalysis.texts.sub_tab_mutations + "</span></a></li>");
            }
            if ($.inArray("COPY_NUMBER_ALTERATION", profile_type_list) !== -1) { //study has copy number data
                $("#" + orAnalysis.ids.sub_tabs_list).append("<li><a href='#" + orAnalysis.ids.sub_tab_copy_num + "' class='or-analysis-tabs-ref'><span>" + orAnalysis.texts.sub_tab_copy_num + "</span></a></li>");
            }
            if ($.inArray("MRNA_EXPRESSION", profile_type_list) !== -1) { //study has expression data
                $("#" + orAnalysis.ids.sub_tabs_list).append("<li><a href='#" + orAnalysis.ids.sub_tab_mrna_exp + "' class='or-analysis-tabs-ref'><span>" + orAnalysis.texts.sub_tab_mrna_exp + "</span></a></li>");
            }
            if ($.inArray("PROTEIN_LEVEL", profile_type_list) !== -1 || $.inArray("PROTEIN_ARRAY_PROTEIN_LEVEL", profile_type_list) !== -1) { //study has RPPA data
                $("#" + orAnalysis.ids.sub_tabs_list).append("<li><a href='#" + orAnalysis.ids.sub_tab_protein_exp + "' class='or-analysis-tabs-ref'><span>" + orAnalysis.texts.sub_tab_protein_exp + "</span></a></li>");
            }

            $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_mutations + "'></div>");
            $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_copy_num + "'></div>");
            $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_mrna_exp + "'></div>");
            $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_protein_exp + "'></div>");

            $("#" + orAnalysis.ids.sub_tabs_div).tabs();
            $("#" + orAnalysis.ids.sub_tabs_div).tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
            $("#" + orAnalysis.ids.sub_tabs_div).tabs("option", "active", 0);
            $(window).trigger("resize");

            //init sub tab contents
            if ($.inArray("MUTATION_EXTENDED", profile_type_list) !== -1) { // study has mutation data
                //init_mutations_tab($("#or_analysis_tab_gene_set_select").val());
                init_mutations_tab();
            } else if ($.inArray("COPY_NUMBER_ALTERATION", profile_type_list) !== -1) {
                //init_copy_num_tab($("#or_analysis_tab_gene_set_select").val());
                init_copy_num_tab();
            } else if ($.inArray("MRNA_EXPRESSION", profile_type_list) !== -1) {
                //init_mrna_exp_tab($("#or_analysis_tab_gene_set_select").val());
                init_mrna_exp_tab();
            } else if ($.inArray("PROTEIN_LEVEL", profile_type_list) !== -1 || $.inArray("PROTEIN_ARRAY_PROTEIN_LEVEL", profile_type_list)) {
                //init_protein_exp_tab($("#or_analysis_tab_gene_set_select").val());
                init_protein_exp_tab();
            }

            //bind event listener
            $("#" + orAnalysis.ids.sub_tabs_div).on("tabsactivate", function(event, ui) {
                if (ui.newTab.text() === orAnalysis.texts.sub_tab_copy_num) {
                    //if ($("#" + orAnalysis.ids.sub_tab_copy_num).is(':empty')) init_copy_num_tab($("#or_analysis_tab_gene_set_select").val());
                    if ($("#" + orAnalysis.ids.sub_tab_copy_num).is(':empty')) init_copy_num_tab();
                } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_mutations) {
                    //if ($("#" + orAnalysis.ids.sub_tab_copy_num).is(':empty')) init_mutations_tab($("#or_analysis_tab_gene_set_select").val());
                    if ($("#" + orAnalysis.ids.sub_tab_copy_num).is(':empty')) init_mutations_tab();
                } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_mrna_exp) {
                    //if ($("#" + orAnalysis.ids.sub_tab_mrna_exp).is(':empty')) init_mrna_exp_tab($("#or_analysis_tab_gene_set_select").val());
                    if ($("#" + orAnalysis.ids.sub_tab_mrna_exp).is(':empty')) init_mrna_exp_tab();
                } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_protein_exp) {
                    //if ($("#" + orAnalysis.ids.sub_tab_protein_exp).is(':empty')) init_protein_exp_tab($("#or_analysis_tab_gene_set_select").val());
                    if ($("#" + orAnalysis.ids.sub_tab_protein_exp).is(':empty')) init_protein_exp_tab();
                }
            });

        }).fail(function( jqXHR, textStatus ) {
            alert( "Request failed: " + textStatus );
        });

    }

    //var update = function() {
    //
    //    //clean the sub tabs
    //    if ($.inArray("MUTATION_EXTENDED", profile_type_list) !== -1) { // study has mutation data
    //        $("#" + orAnalysis.ids.sub_tab_mutations).empty();
    //        //init_mutations_tab($("#or_analysis_tab_gene_set_select").val());
    //        init_mutations_tab();
    //    }
    //    if ($.inArray("COPY_NUMBER_ALTERATION", profile_type_list) !== -1) {
    //        $("#" + orAnalysis.ids.sub_tab_copy_num).empty();
    //        //init_copy_num_tab($("#or_analysis_tab_gene_set_select").val());
    //        init_copy_num_tab();
    //    }
    //    if ($.inArray("MRNA_EXPRESSION", profile_type_list) !== -1) {
    //        $("#" + orAnalysis.ids.sub_tab_mrna_exp).empty();
    //        //init_mrna_exp_tab($("#or_analysis_tab_gene_set_select").val());
    //        init_mrna_exp_tab();
    //    }
    //    if ($.inArray("PROTEIN_LEVEL", profile_type_list) !== -1) {
    //        $("#" + orAnalysis.ids.sub_tab_protein_exp).empty();
    //        //init_protein_exp_tab($("#or_analysis_tab_gene_set_select").val());
    //        init_protein_exp_tab();
    //    }
    //
    //    //bind event listener
    //    $("#" + orAnalysis.ids.sub_tabs_div).on("tabsactivate", function(event, ui) {
    //        if (ui.newTab.text() === orAnalysis.texts.sub_tab_copy_num) {
    //            //if ($("#" + orAnalysis.ids.sub_tab_copy_num).is(':empty')) init_copy_num_tab($("#or_analysis_tab_gene_set_select").val());
    //            if ($("#" + orAnalysis.ids.sub_tab_copy_num).is(':empty')) init_copy_num_tab();
    //        } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_mutations) {
    //            //if ($("#" + orAnalysis.ids.sub_tab_mutations).is(':empty')) init_mutations_tab($("#or_analysis_tab_gene_set_select").val());
    //            if ($("#" + orAnalysis.ids.sub_tab_mutations).is(':empty')) init_mutations_tab();
    //        } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_mrna_exp) {
    //            //if ($("#" + orAnalysis.ids.sub_tab_mrna_exp).is(':empty')) init_mrna_exp_tab($("#or_analysis_tab_gene_set_select").val());
    //            if ($("#" + orAnalysis.ids.sub_tab_mrna_exp).is(':empty')) init_mrna_exp_tab();
    //        } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_protein_exp) {
    //            //if ($("#" + orAnalysis.ids.sub_tab_protein_exp).is(':empty')) init_protein_exp_tab($("#or_analysis_tab_gene_set_select").val());
    //            if ($("#" + orAnalysis.ids.sub_tab_protein_exp).is(':empty')) init_protein_exp_tab();
    //        }
    //    });
    //
    //}

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
                $("#or_analysis").append("No alteration in selected samples, therefore could not perform this calculation.");
            } else if ((unalteredCaseList.length === 1 && unalteredCaseList[0] === "") || unalteredCaseList.length === 0) {
                $("#or_analysis").append("No non-alteration in selected samples, therefore could not perform this calculation.");
            } else {
                init_ajax();
            }

        },
        getAlteredCaseList: function() {
            return alteredCaseList;
        },
        getUnalteredCaseList: function() {
            return unalteredCaseList;
        }
        //},
        //update: update
    };

}());



