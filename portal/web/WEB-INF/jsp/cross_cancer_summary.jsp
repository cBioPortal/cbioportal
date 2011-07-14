<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.mskcc.portal.model.GeneticProfile" %>

<%
    ArrayList<GeneticProfile> geneticProfileList = (ArrayList<GeneticProfile>)
            request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL);
%>

<%
    for (GeneticProfile geneticProfile:  geneticProfileList) {
        out.println ("<LI>" + geneticProfile.getName());

    }
%>
