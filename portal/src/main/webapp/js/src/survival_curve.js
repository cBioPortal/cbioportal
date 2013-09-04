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

/******************************************************************************************
 * Creating overall survival and disease free curves for the survival tab
 * @author Yichao Sun
 * @date Sep 2013
 *
 * This code performs the following functions:
 * 1. Calculate the survival rates for each time point using kaplan-meier estimator
 * 2. Generate the curves using d3 line charts
 ******************************************************************************************/

var survivalCurves = (function() {

    var util = (function() {

        function sortByAttribute(objs, attrName) {
            function compare(a,b) {
                if (a[attrName] < b[attrName])
                    return -1;
                if (a[attrName] > b[attrName])
                    return 1;
                return 0;
            }
            objs.sort(compare);
            return objs;
        }

        return {
            sortByAttribute: sortByAttribute
        }

    }());

    var data  = (function() {

        var datum = {
                case_id: "",
                time: "",    //num of months
                status: "", //os: DECEASED-->1, LIVING-->0; dfs: Recurred/Progressed --> 1, Disease Free-->0
                num_at_risk: -1
            },
            os_altered_group = [],
            os_unaltered_group = [],
            dfs_altered_group = [],
            dfs_unaltered_group = [];

        var totalAlter = 0,
            totalUnalter = 0;

        //Count the total number of altered and unaltered cases
        function cntAlter(caseLists) {
            for (var key in caseLists) {
                if (caseLists[key] === "altered") totalAlter += 1;
                else if (caseLists[key] === "unaltered") totalUnalter += 1;
            }
        }

        //Settle the overall survival datum group
        function setOSGroups(result, caseLists) {
            for (var caseId in result) {
                if (result.hasOwnProperty(caseId) && (result[caseId] !== "")) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.case_id = result[caseId].case_id;
                    _datum.time = result[caseId].os_months;
                    _datum.status = result[caseId].os_status;
                    if (caseLists[caseId] === "altered") {
                        os_altered_group.push(_datum);
                    } else if (caseLists[caseId] === "unaltered") {
                        totalUnalter += -1;
                    }
                }
            }
            util.sortByAttribute(os_altered_group, "time");
            util.sortByAttribute(os_unaltered_group, "time");

            for (var i in os_altered_group) {
                os_altered_group[i].num_at_risk = totalAlter;
                totalAlter -= 1;
            }
            for (var i in os_unaltered_group) {
                os_unaltered_group[i].num_at_risk = totalUnalter;
                totalUnalter -= 1;
            }

        }



        function setDFSGroups(result, caseLists) {

            for (var caseId in result) {
                if (result.hasOwnProperty(caseId) && (result[caseId] !== "")) {
                    var _datum = jQuery.extend(true, {}, datum);
                    _datum.case_id = result[caseId].case_id;
                    _datum.time = result[caseId].dfs_months;
                    _datum.status = result[caseId].dfs_status;
                    if (caseLists[caseId] === "altered") {
                        dfs_altered_group.push(_datum);
                    } else if (caseLists[caseId] === "unaltered") {
                        dfs_unaltered_group.push(_datum);
                    }
                }
            }
        }

        return {
            init: function(result, caseLists) {
                cntAlter(caseLists);
                setOSGroups(result, caseLists);
                setDFSGroups(result, caseLists);
            }
        }
    }());

    var view = (function() {



    }());

    return {
        init: function(caseLists) {
            var paramsGetSurvivalData = {
                case_set_id: case_set_id,
                case_ids_key: case_ids_key,
                cancer_study_id: cancer_study_id
            };
            $.post("getSurvivalData.json", paramsGetSurvivalData, getResultInit(caseLists), "json");

            function getResultInit(caseLists) {
                return function(result) {
                    data.init(result, caseLists);
                }
            }
        }
    }
}());