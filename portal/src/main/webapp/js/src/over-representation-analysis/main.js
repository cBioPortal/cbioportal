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
                $.each(profile_type_list, function(index, profile_type) {
                    $("#or-analysis-tabs-list").append("<li><a href='#" + profile_type + 
                      "_subtab' class='or-analysis-tabs-ref'><span>" + profile_type + "</span></a></li>");
                });
                $("#or-analysis-tabs").tabs();
                $("#or-analysis-tabs").tabs('paging', {tabsPerPage: 10, follow: true, cycle: false});
                $("#or-analysis-tabs").tabs("option", "active", 0);
                $(window).trigger("resize");
                
                
                
//        var param = new orAjaxParam(alteredCaseList, unalteredCaseList, window.PortalGlobals.getCancerStudyId() + "_mutations");
//        var or_data = new orData();
//        or_data.init(param);
//        var or_table = new orTable();
//        or_data.get(or_table.init, "or_analysis");       
            }).fail(function( jqXHR, textStatus ) {
                alert( "Request failed: " + textStatus );
            }); 
            
            
        }
    };
    
}());



