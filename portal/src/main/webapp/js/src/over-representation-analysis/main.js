/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
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
            
            $.ajax({
                method: "POST", 
                url: "getGeneticProfile.json", 
                data: {
                    cancer_study_id: window.PortalGlobals.getCancerStudyId()
                }
            }).done(function(result){
                
                var profile_type_list = [];
                $.each(Object.keys(result), function(index, key) {
                    var _obj = result[key];
                    if($.inArray(_obj.GENETIC_ALTERATION_TYPE, profile_type_list) === -1) {
                        profile_type_list.push(_obj.GENETIC_ALTERATION_TYPE);
                    }
                });
                
                $("#" + orAnalysis.ids.sub_tabs_list).append("<li><a href='#" + orAnalysis.ids.sub_tab_main + "' class='or-analysis-tabs-ref'><span>" + orAnalysis.texts.sub_tab_main + "</span></a></li>");
                if ($.inArray("MRNA_EXPRESSION", profile_type_list) !== -1) { //study has expression data
                    $("#" + orAnalysis.ids.sub_tabs_list).append("<li><a href='#" + orAnalysis.ids.sub_tab_mrna_exp + "' class='or-analysis-tabs-ref'><span>" + orAnalysis.texts.sub_tab_mrna_exp + "</span></a></li>");
                }
                $("#" + orAnalysis.ids.sub_tabs_list).append("<li><a href='#" + orAnalysis.ids.sub_tab_advanced + "' class='or-analysis-tabs-ref'><span>" + orAnalysis.texts.sub_tab_advanced + "</span></a></li>");
                
                $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_main + "'></div>");
                $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_mrna_exp + "'></div>");
                $("#" + orAnalysis.ids.sub_tabs_content).append("<div id='" + orAnalysis.ids.sub_tab_advanced + "'></div>");

                $("#" + orAnalysis.ids.sub_tabs_div).tabs();
                $("#" + orAnalysis.ids.sub_tabs_div).tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
                $("#" + orAnalysis.ids.sub_tabs_div).tabs("option", "active", 0);
                $(window).trigger("resize");
                
                $("#" + orAnalysis.ids.sub_tabs_div).on("tabsactivate", function(event, ui) {
                    if (ui.newTab.text() === orAnalysis.texts.sub_tab_main) {
                        var orSubTabMain = new orSubTabView();
                        orSubTabMain.init(orAnalysis.ids.sub_tab_main);
                    } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_mrna_exp) {
                        var orSubTabMrnaExp = new orSubTabView();
                        orSubTabMrnaExp.init(orAnalysis.ids.sub_tab_mrna_exp);
                    } else if (ui.newTab.text() === orAnalysis.texts.sub_tab_advanced) {
                        var orSubTabAdvanced = new orSubTabView();
                        orSubTabAdvanced.init(orAnalysis.ids.sub_tab_advanced);
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



