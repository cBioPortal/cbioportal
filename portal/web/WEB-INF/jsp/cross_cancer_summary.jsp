<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.portal.model.CaseSet" %>
<%@ page import="org.mskcc.portal.servlet.CrossCancerSummaryServlet" %>
<%@ page import="java.util.HashMap" %>

<%
    ArrayList<GeneticProfile> geneticProfileList = (ArrayList<GeneticProfile>)
            request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL);
    HashMap<String, GeneticProfile> defaultGeneticProfileSet = (HashMap<String, GeneticProfile>)
            request.getAttribute(CrossCancerSummaryServlet.DEFAULT_GENETIC_PROFILES);
    ArrayList<CaseSet> caseSetList = (ArrayList<CaseSet>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String defaultCaseSetId = (String) request.getAttribute(CrossCancerSummaryServlet.DEFAULT_CASE_SET_ID);
%>

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