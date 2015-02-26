<%
    String step1ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP1_ERROR_MSG);
%>
<div class="query_step_section" id="select_cancer_type_section">
<span class="step_header">Select Cancer Study:</span>
<input type="text" id="jstree_search_input"/>
<div id="jstree_selected_study_count">No studies selected.</div>
<div id="jstree" style="max-height:250px; overflow-y: scroll"></div>
<select id="select_cancer_type" name="<%= QueryBuilder.CANCER_STUDY_ID %>" ></select>
<select id="select_cancer_type_multiple" name="<%= QueryBuilder.CANCER_STUDY_ID %>" multiple></select>
<img id="select_cancer_type_help" src="images/help.png" title="Type in keywords to narrow down the studies. When using two or more search terms, only studies with all terms are returned (AND logic). Add an 'or' between search terms if you want to see studies that contain either term. Placing quotation marks around two or more words will look for the exact string. Placing a dash (-) before a term will exclude results with that term.">
<span><input id="toggle_select_cancer_type_multiple" type="checkbox"/>Multiple</span>
<script type="text/javascript">
$('#select_cancer_type_help').qtip({
                    content: { attr: 'title' },
                    style: { classes: 'qtip-light qtip-rounded' },
                    position: { my:'left center',at:'right center',viewport: $(window) }
});
</script>

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
    
