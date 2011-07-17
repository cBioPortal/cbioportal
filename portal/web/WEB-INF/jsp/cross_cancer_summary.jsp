<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.portal.model.CaseSet" %>
<%@ page import="org.mskcc.portal.servlet.CrossCancerSummaryServlet" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.portal.model.ProfileDataSummary" %>
<%@ page import="java.text.DecimalFormat" %>

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

    ServletXssUtil servletXssUtil = ServletXssUtil.getInstance();
    String geneList = servletXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
    DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
    String oncoPrintHtml = (String) request.getAttribute(QueryBuilder.ONCO_PRINT_HTML);
%>
<b>Altered in <%= percentFormat.format(dataSummary.getPercentCasesAffected()) %> of all cases.</b>
<br/><br/>

<B>Gene List:</B>

<%= geneList %>

<br/><br/>
<b>Selected Genetic Profiles:</b>
<ul>
<%
    for (GeneticProfile geneticProfile:  geneticProfileList) {
        if (defaultGeneticProfileSet.containsKey(geneticProfile.getId())) {
            out.println ("<li>" + geneticProfile.getName() );
        }
        out.println ("</li>");

    }
%>
</ul>


<b>Selected Case Set:</b>
<ul>
<%
    for (CaseSet caseSet:  caseSetList) {
        if (caseSet.getId().equals(defaultCaseSetId)) {
            out.println ("<li>" + caseSet.getName());
        }
        out.println ("</li>");
    }
%>
</ul>

<b>OncoPrint:</b>
<br>
<div class="scroll">
<%= oncoPrintHtml %>
</div>

<jsp:include page="global/xdebug.jsp" flush="true" />