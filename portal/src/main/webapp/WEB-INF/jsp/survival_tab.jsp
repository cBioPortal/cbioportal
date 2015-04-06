<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ page import="org.json.simple.JSONObject"%>

<style>
    #survival .survival_stats_table {
        margin-top: 10px;
        margin-bottom: 30px;
        margin-left: 95px;
        width: 620px;
        background-color: #FEFFC5;
        height: 80px;
        width: 720px;
    }
    #survival td{
        width: 140px;
        font-size: 13px;
        font-family: Arial, Helvetica, sans-serif;
        text-align: center;
        border: 1px solid #D8D8D8;
    }
    #survival h4{
        margin-left: 60px;
        margin-top: 20px;
        font-size: 150%;
        height: 30px;
    }
    #survival .img_buttons{
        font-size: 13px;
        display: inline;
        padding-left: 5px;
    }
</style>

<script type="text/javascript" src="js/src/survival-tab/survival_tab.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-tab/survivalCurveView.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-tab/survivalCurveProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-tab/component/survivalCurve.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-tab/component/kmEstimator.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-tab/component/logRankTest.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-tab/component/boilerPlate.js?<%=GlobalProperties.getAppVersion()%>"></script>

<div class="section" id="survival">
    <h4 id='os_header'>Overall Survival Kaplan-Meier Estimate</h4>
    <div id="os_survival_curve"></div>
    <div class="survival_stats_table" id="os_stat_table"></div>
    <h4 id='dfs_header'>Disease Free Survival Kaplan-Meier Estimate</h4>
    <div id="dfs_survival_curve"></div>
    <div class="survival_stats_table" id="dfs_stat_table"></div>
</div>

<script>
    
    function getSurvivalPlotsCaseList() {
        var obj = {};
        $.each(window.PortalGlobals.getAlteredSampleIdList().split(" "), function(_index, _sampleId) {
            obj[_sampleId] = "altered";
        });
        $.each(window.PortalGlobals.getUnalteredSampleIdList().split(" "), function(_index, _sampleId) {
            obj[_sampleId] = "unaltered";
        });
        return obj;
    }

    $(document).ready(function() {
        SurvivalTab.init(getSurvivalPlotsCaseList());
    });
    
</script>