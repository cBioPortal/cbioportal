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


