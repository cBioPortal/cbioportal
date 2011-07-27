<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.portal.model.CaseSet" %>
<%@ page import="org.mskcc.portal.servlet.CrossCancerSummaryServlet" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.portal.model.ProfileDataSummary" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="org.mskcc.portal.util.MakeOncoPrint" %>
<%@ page import="org.mskcc.portal.model.GeneWithScore" %>

<%
    ArrayList<GeneticProfile> geneticProfileList = (ArrayList<GeneticProfile>)
            request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL);
    HashMap<String, GeneticProfile> defaultGeneticProfileSet = (HashMap<String, GeneticProfile>)
            request.getAttribute(CrossCancerSummaryServlet.DEFAULT_GENETIC_PROFILES);
    ArrayList<CaseSet> caseSetList = (ArrayList<CaseSet>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String defaultCaseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);

    ProfileDataSummary dataSummary = (ProfileDataSummary)
            request.getAttribute(QueryBuilder.PROFILE_DATA_SUMMARY);

    String cancerStudyDetailsUrl = (String) request.getAttribute
            (CrossCancerSummaryServlet.CANCER_STUDY_DETAILS_URL);
    String oncoPrintHtml = (String) request.getAttribute(QueryBuilder.ONCO_PRINT_HTML);
    ArrayList<GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    int fingerPrintPanelHeight = 120 + (MakeOncoPrint.CELL_HEIGHT + 2) * geneWithScoreList.size();
%>

<script type="text/javascript">
$(document).ready(function(){

    // Init Tool Tips
    $(".hide_details").tipTip();

    //  Prevent Default Click Behavior for tool tips
    $(".hide_details").click(function(event) {
      event.preventDefault();
    });
});
</script>
<%
    StringBuffer gp = new StringBuffer("Genomic data shown for the following profiles:");
    gp.append ("<ul>");
    for (GeneticProfile geneticProfile:  geneticProfileList) {
        if (defaultGeneticProfileSet.containsKey(geneticProfile.getId())) {
           gp.append ("<li>" + geneticProfile.getName() );
        }
        gp.append ("</li>");
    }
    gp.append ("</ul>");
    out.println ("<a href='' class='hide_details' title=\"" + gp.toString() + "\">Genomic Profiles</a>");
%>


<%
    StringBuffer cs = new StringBuffer ("Case set:  ");
    for (CaseSet caseSet:  caseSetList) {
        if (caseSet.getId().equals(defaultCaseSetId)) {
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
<jsp:include page="global/xdebug.jsp" flush="true" />