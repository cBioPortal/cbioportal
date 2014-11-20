<%
    String step1ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP1_ERROR_MSG);
%>

<div class="query_step_section">
<span class="step_header">Select Cancer Study:</span>
<select id="select_cancer_type" name="<%= QueryBuilder.CANCER_STUDY_ID %>"></select>

<%
if (step1ErrorMsg != null) {
    out.println("<div class='ui-state-error ui-corner-all' style='margin-top:4px; padding:5px;'>"
            + "<span class='ui-icon ui-icon-alert' style='float: left; margin-right: .3em;'></span>"
            + "<strong>" + step1ErrorMsg + "</strong>");
}
%>

<!-- This div shows the cancer description -->
<div id="cancer_study_desc" style="margin-top: 15px;margin-bottom:-10px;">
</div>

<%
if (step1ErrorMsg != null) {
    out.println ("</div>");
}
%>

</div>    
    
