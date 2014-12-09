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

var SurvivalTab = (function() {

    return {
        init: function(_caseList) {

            //Import default settings
            var osOpts = jQuery.extend(true, {}, SurvivalCurveBroilerPlate);
            var dfsOpts = jQuery.extend(true, {}, SurvivalCurveBroilerPlate);  

            //Customize settings
            osOpts.text.xTitle = "Months Survival";
            osOpts.text.yTitle = "Surviving";
            osOpts.text.qTips.estimation = "Survival estimate";
            osOpts.text.qTips.censoredEvent = "Time of last observation";
            osOpts.text.qTips.failureEvent = "Time of death";
            osOpts.settings.include_info_table = true;
            osOpts.divs.curveDivId = "os_survival_curve";
            osOpts.divs.headerDivId = "os_header";
            osOpts.divs.infoTableDivId = "os_stat_table";
            osOpts.text.infoTableTitles.total_cases = "#total cases";
            osOpts.text.infoTableTitles.num_of_events_cases = "#cases deceased";
            osOpts.text.infoTableTitles.median = "median months survival";
            dfsOpts.text.xTitle = "Months Disease Free";
            dfsOpts.text.yTitle = "Disease Free";
            dfsOpts.text.qTips.estimation = "Disease free estimate";
            dfsOpts.text.qTips.censoredEvent = "Time of last observation";
            dfsOpts.text.qTips.failureEvent = "Time of relapse";
            dfsOpts.settings.include_info_table = true;
            dfsOpts.divs.curveDivId = "dfs_survival_curve";
            dfsOpts.divs.headerDivId = "dfs_header";
            dfsOpts.divs.infoTableDivId = "dfs_stat_table";
            dfsOpts.text.infoTableTitles.total_cases = "#total cases";
            dfsOpts.text.infoTableTitles.num_of_events_cases = "#cases relapsed";
            dfsOpts.text.infoTableTitles.median = "median months disease free";

            //Init Instances
            var survivalCurveViewOS = SurvivalCurveView(osOpts);
            var params = {
                case_set_id: PortalGlobals.getCaseSetId(),
                case_ids_key: PortalGlobals.getCaseIdsKey(),
                cancer_study_id: PortalGlobals.getCancerStudyId(),
                data_type: "os"
            };
            $.post("getSurvivalData.json", params, function(data) {
                survivalCurveViewOS.getResultInit(_caseList,data);
            }, "json");
            
            var survivalCurveViewDFS = new SurvivalCurveView(dfsOpts);
            params.data_type = "dfs";
            $.post("getSurvivalData.json", params, function(data) {
                survivalCurveViewDFS.getResultInit(_caseList,data);
            }, "json");
        }
    };

}()); //Close SubvivalTabView (Singular)

function loadSurvivalCurveSVG(svgId) {
    return $("#" + svgId).html();
}