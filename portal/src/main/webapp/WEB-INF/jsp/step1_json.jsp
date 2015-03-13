<%
    String step1ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP1_ERROR_MSG);
%>
<div class="query_step_section" id="select_cancer_type_section">
<span class="step_header">Select Cancer Study:</span>
<input type="text" id="jstree_search_input" style="width:24em"/>
<a href='javascript:$("#jstree_search_input").val("-\"cell line\"");$("#jstree_search_input").trigger("input");'>-"cell line"</a>&nbsp;&nbsp;&nbsp;
<a href='javascript:$("#jstree_search_input").val("tcga");$("#jstree_search_input").trigger("input");'>tcga</a>
<div id="jstree_search_examples" style="display:none">
    Example queries:<br>
    <a href='javascript:$("#jstree_search_input").val("tcga -provisional");$("#jstree_search_input").trigger("input");' >tcga -provisional</a><br>
    <a href='javascript:$("#jstree_search_input").val("tcga -moratorium");$("#jstree_search_input").trigger("input");' >tcga -moratorium</a><br>
    <a href='javascript:$("#jstree_search_input").val("tcga OR icgc");$("#jstree_search_input").trigger("input");'>tcga OR icgc</a><br>
    <a href='javascript:$("#jstree_search_input").val("prostate mskcc");$("#jstree_search_input").trigger("input");'>prostate mskcc</a><br>
    <a href='javascript:$("#jstree_search_input").val("esophageal OR stomach");$("#jstree_search_input").trigger("input");'>esophageal OR stomach</a><br>
    <a href='javascript:$("#jstree_search_input").val("serous");$("#jstree_search_input").trigger("input");'>serous</a><br>
    <a href='javascript:$("#jstree_search_input").val("breast");$("#jstree_search_input").trigger("input");'>breast</a><br>
</div>
&nbsp;&nbsp;
<img id="select_cancer_type_help" src="images/help.png" title="Type in keywords to narrow down the studies. When using two or more search terms, only studies with all terms are returned (AND logic). Add an 'or' between search terms if you want to see studies that contain either term. Placing quotation marks around two or more words will look for the exact string. Placing a dash (-) before a term will exclude results with that term.">
<div id="jstree_selected_study_count">No studies selected.</div>
<a href='javascript:$("#jstree").jstree(true).select_all();'>Select all studies</a>&nbsp;&nbsp;
<a href='javascript:$("#jstree").jstree(true).deselect_all();'>Deselect all studies</a>
<div id="jstree" style="max-height:250px; overflow-y: scroll"></div>
<input id="select_multiple_studies" name="<%= QueryBuilder.CANCER_STUDY_LIST %>" style="display:none">
<input id="select_single_study" name="<%= QueryBuilder.CANCER_STUDY_ID %>" style="display:none">
<script type="text/javascript">
$('#select_cancer_type_help').qtip({
                    content: { text: $('#jstree_search_examples') },
                    style: { classes: 'qtip-light qtip-rounded' },
                    position: { my:'left center',at:'right center',viewport: $(window) },
                    hide: { delay:200, fixed:true }
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
    
