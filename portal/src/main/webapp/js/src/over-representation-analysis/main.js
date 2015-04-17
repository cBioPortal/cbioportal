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
 * Main page for Initiating over representation analysis
 *
 * Author: yichaoS
 * Date: 3/10/2015
 * 
 */

var or_tab = (function() {
    
    var alteredCaseList = [], unalteredCaseList = [];
    var profile_obj_list = {}, profile_type_list = [];
    
    var init_copy_num_tab = function() {
        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if ((_obj.GENETIC_ALTERATION_TYPE === orAnalysis.profile_type.copy_num && 
                 _obj.STABLE_ID.indexOf("gistic") !== -1)) {
                _profile_list.push(_obj); 
            } 
        });
        var orSubTabMain = new orSubTabView();
        orSubTabMain.init(orAnalysis.ids.sub_tab_copy_num, _profile_list, orAnalysis.profile_type.copy_num);       
    };
    
    var init_mutations_tab = function() {
        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === orAnalysis.profile_type.mutations) {
                _profile_list.push(_obj); 
            } 
        });
        var orSubTabMain = new orSubTabView();
        orSubTabMain.init(orAnalysis.ids.sub_tab_mutations, _profile_list, orAnalysis.profile_type.mutations);       
    };
    
    var init_mrna_exp_tab = function() {
        var _profile_list = [];
        $.each(Object.keys(profile_obj_list), function(_index, _key) {
            var _obj = profile_obj_list[_key];
            if (_obj.GENETIC_ALTERATION_TYPE === orAnalysis.profile_type.mrna) {
                _profile_list.push(_obj);
            }
        });
        var orSubTabMrnaExp = new orSubTabView();
        orSubTabMrnaExp.init(orAnalysis.ids.sub_tab_mrna_exp, _profile_list, orAnalysis.profile_type.mrna);
    };
    
    //var init_advanced_tab = function() {}; 
    
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
                
                $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_mutations + "'></div>");
                $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_copy_num + "'></div>");
                $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_mrna_exp + "'></div>");

                $("#" + orAnalysis.ids.sub_tabs_div).tabs();
                $("#" + orAnalysis.ids.sub_tabs_div).tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
                $("#" + orAnalysis.ids.sub_tabs_div).tabs("option", "active", 0);
                $(window).trigger("resize");
                
                //init sub tab contents
                init_mutations_tab(); 
                $("#" + orAnalysis.ids.sub_tabs_div).on("tabsactivate", function(event, ui) {
                    if (ui.newTab.text() === orAnalysis.texts.sub_tab_copy_num) {
                        if ($("#" + orAnalysis.ids.sub_tab_copy_num).is(':empty')) init_copy_num_tab();
                    } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_mutations) {
                        if ($("#" + orAnalysis.ids.sub_tab_mutations).is(':empty')) init_mutations_tab();
                    } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_mrna_exp) {
                        if ($("#" + orAnalysis.ids.sub_tab_mrna_exp).is(':empty')) init_mrna_exp_tab();
                    }
                });
                
            }).fail(function( jqXHR, textStatus ) {
                alert( "Request failed: " + textStatus );
            }); 
        },
        getAlteredCaseList: function() {
            return alteredCaseList;
        },
        getUnalteredCaseList: function() {
            return unalteredCaseList;
        }
    };
    
}());



