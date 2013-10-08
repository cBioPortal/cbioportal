<%@ page import="org.json.simple.JSONObject"%>
<%
    String cancer_study_id = (String)request.getParameter("cancer_study_id");
    String case_set_id = (String)request.getParameter("case_set_id");
%>

<style>
    #survival .survival_stats_table {
        margin-top: -20px;
        margin-bottom: 30px;
        margin-left: 95px;
        width: 620px;
    }
    #survival table{
        background-color: #F8FAB1;
        height: 80px;
        width: 620px;
    }
    #survival td{
        width: 90px;
        font-size: 13px;
        font-family: Arial, Helvetica, sans-serif;
        text-align: center;
        border: 1px solid #D8D8D8;
    }
    #survival h4{
        padding-left: 60px;
        padding-top: 20px;
        margin-bottom: -50px;
    }
</style>

<script>
    var cancer_study_id = "<%out.print(cancer_study_id);%>",
            case_set_id = "<%out.print(case_set_id);%>";
    var case_ids_key = "";
    if (case_set_id === "-1") {
        case_ids_key = "<%out.print(caseIdsKey);%>";
    }
</script>
<script type="text/javascript" src="js/src/survival_curve.js"></script>

<div class="section" id="survival">
    <h4>Overall Survival<div id='os_pdf_svg'></div></h4>
    <div id="os_survival_curve"></div>
    <div class="survival_stats_table" id="os_stat_table"></div>
    <h4>Disease Free Survival<div id='dfs_pdf_svg'></div></h4>
    <div id="dfs_survival_curve"></div>
    <div class="survival_stats_table" id="dfs_stat_table"></div>
</div>

<script>
    function getSurvivalPlotsCaseList() {
        <%
            JSONObject result = new JSONObject();
            for (String caseId : mergedCaseList) {
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

    window.onload = survivalCurves.init(getSurvivalPlotsCaseList());
</script>