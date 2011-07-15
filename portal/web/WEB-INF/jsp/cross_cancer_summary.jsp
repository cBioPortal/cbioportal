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
%>
<b><%= percentFormat.format(dataSummary.getPercentCasesAffected()) %> of all cases.</b>
<br/><br/>

<B>Gene List:</B>

<%= geneList %>

<br/><br/>
<B>Genetic Profiles:</B>
<ul>
<%
    for (GeneticProfile geneticProfile:  geneticProfileList) {
        out.println ("<li>" + geneticProfile.getName() );
        if (defaultGeneticProfileSet.containsKey(geneticProfile.getId())) {
            out.println (" [Default Profile]*");
        }
        out.println ("</li>");

    }
%>
</ul>


<B>Case Sets:</B>
<ul>
<%
    for (CaseSet caseSet:  caseSetList) {
        out.println ("<li>" + caseSet.getName());
        if (caseSet.getId().equals(defaultCaseSetId)) {
            out.println (" [Defult Case Set]*");
        }
        out.println ("</li>");
    }
%>
</ul>

<jsp:include page="global/xdebug.jsp" flush="true" />