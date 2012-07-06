<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="static org.mskcc.portal.servlet.QueryBuilder.DATA_PRIORITY" %>
<%
    String step2ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP2_ERROR_MSG);
%>

<div class="query_step_section" id="step2">
    <span class="step_header">Select Genomic Profiles:</span>

<%
if (step2ErrorMsg != null) {
    out.println("<div class='ui-state-error ui-corner-all' style='margin-top:4px; padding:5px;'>"
            + "<span class='ui-icon ui-icon-alert' style='float: left; margin-right: .3em;'></span>"
            + "<strong>" + step2ErrorMsg + "</strong>");
}
%>

<div id='genomic_profiles'>
</div>

<%
if (step2ErrorMsg != null) {
    out.println ("</div>");
}
%>

</div>


<%
    Integer priority = (Integer) request.getAttribute(DATA_PRIORITY);
    String checked[] = new String[3];
    if(priority == null)
        priority = 0;

    checked[priority] = " checked";
%>

<div class="query_step_section" id="step2cross">
    <span class="step_header">Select Data Type Priority:</span>
    <input type="radio" name="<%= DATA_PRIORITY %>" id="pri_mutcna" value=0<%=checked[0]%>>
    <label for="pri_mutcna">Mutation and CNA</label>

    <input type="radio" name="<%= DATA_PRIORITY %>" id="pri_mut" value=1<%=checked[1]%>>
    <label for="pri_mut">Only Mutation</label>

    <input type="radio" name="<%= DATA_PRIORITY %>" id="pri_cna" value=2<%=checked[2]%>>
    <label for="pri_cna">Only CNA</label>
</div>


