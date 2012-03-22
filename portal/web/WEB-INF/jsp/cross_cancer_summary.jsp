<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.servlet.CrossCancerSummaryServlet" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.mskcc.portal.model.ProfileDataSummary" %>
<%@ page import="org.mskcc.portal.util.MakeOncoPrint" %>
<%@ page import="org.mskcc.portal.model.GeneWithScore" %>
<%@ page import="org.mskcc.cgds.model.CaseList" %>
<%@ page import="org.mskcc.cgds.model.GeneticProfile" %>
<%@ page import="java.text.DecimalFormat" %>

<%
    ArrayList<GeneticProfile> geneticProfileList =
            (ArrayList<GeneticProfile>)
                    request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL);
    HashMap<String, GeneticProfile> defaultGeneticProfileSet =
            (HashMap<String, GeneticProfile>)
                    request.getAttribute(CrossCancerSummaryServlet.DEFAULT_GENETIC_PROFILES);
    ArrayList<CaseList> caseSetList = (ArrayList<CaseList>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String defaultCaseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);

    ProfileDataSummary dataSummary = (ProfileDataSummary)
            request.getAttribute(QueryBuilder.PROFILE_DATA_SUMMARY);

    String cancerStudyDetailsUrl = (String) request.getAttribute
            (CrossCancerSummaryServlet.CANCER_STUDY_DETAILS_URL);
    String oncoPrintHtml = (String) request.getAttribute(QueryBuilder.ONCO_PRINT_HTML);
    ArrayList<GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    String cancerStudyId = request.getParameter(QueryBuilder.CANCER_STUDY_ID);
    int fingerPrintPanelHeight = 120 + (MakeOncoPrint.CELL_HEIGHT + 2) * geneWithScoreList.size();
    DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
    String percentCasesAffected = percentFormat.format(dataSummary.getPercentCasesAffected());
    Integer numOfCasesAffected = dataSummary.getNumCasesAffected();
    Integer numOfCasesUnaffected = dataSummary.getProfileData().getCaseIdList().size() - numOfCasesAffected;
%>

<script type="text/javascript">
$(document).ready(function(){

    // Init Tool Tips
    $(".hide_details").tipTip();

    //  Prevent Default Click Behavior for tool tips
    $(".hide_details").click(function(event) {
      event.preventDefault();
    });

    var ajaxPercentAltered = "#percent_altered_<%= cancerStudyId %>";
    $(ajaxPercentAltered).html("Altered in <%= percentCasesAffected %> of cases.");
    $("#stats_percent_altered_<%= cancerStudyId %>").hide();
    $("#stats_num_altered_<%= cancerStudyId %>").hide();
    $("#stats_num_all_<%= cancerStudyId %>").hide();

});
</script>
<%
    StringBuffer gp = new StringBuffer("Genomic data shown for the following profiles:");
    gp.append ("<ul>");
    for (GeneticProfile geneticProfile:  geneticProfileList) {
        if (defaultGeneticProfileSet.containsKey(geneticProfile.getStableId())) {
           gp.append ("<li>" + geneticProfile.getProfileName());
        }
        gp.append ("</li>");
    }
    gp.append ("</ul>");
    out.println ("<a href='' class='hide_details' title=\"" + gp.toString() + "\">Genomic Profiles</a>");
%>


<%
    StringBuffer cs = new StringBuffer ("Case set:  ");
    for (CaseList caseSet:  caseSetList) {
        if (caseSet.getStableId().equals(defaultCaseSetId)) {
            cs.append (caseSet.getName());
        }
    }
    out.println ("<a href='' class='hide_details' title=\"" + cs.toString() + "\">Case Sets</a>");
%>
<span style="float:right;font-size:110%;"><b><a href="<%= cancerStudyDetailsUrl %>">View Cancer Study Details</a></b></span>
<br/>
<div class="scroll" style="height:<%= fingerPrintPanelHeight %>px">
<%= oncoPrintHtml %>
</div>
<span id="stats_percent_altered_<%= cancerStudyId %>"><%= percentCasesAffected %></span>
<span id="stats_num_altered_<%= cancerStudyId %>"><%= "" + numOfCasesAffected %></span>
<span id="stats_num_all_<%= cancerStudyId %>"><%= "" + numOfCasesUnaffected %></span>
<jsp:include page="global/xdebug.jsp" flush="true" />