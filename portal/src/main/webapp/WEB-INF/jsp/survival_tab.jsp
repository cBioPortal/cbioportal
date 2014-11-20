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

<script type="text/javascript" src="js/src/survival_tab.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-curve/survivalCurveView.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-curve/survivalCurveProxy.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-curve/component/survivalCurve.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-curve/component/kmEstimator.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-curve/component/logRankTest.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/survival-curve/component/boilerPlate.js?<%=GlobalProperties.getAppVersion()%>"></script>

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
        <%
            JSONObject result = new JSONObject();
            for (String caseId : mergedPatientList) { 
                if (dataSummary.isCaseAltered(caseId)) {
                    result.put(caseId, "altered");
                } else {
                    result.put(caseId, "unaltered");
                }
            }
        %>
        var obj = jQuery.parseJSON('<%=result%>');
        return obj;
    }

    $(document).ready(function() {
        SurvivalTab.init(getSurvivalPlotsCaseList());
    });
</script>