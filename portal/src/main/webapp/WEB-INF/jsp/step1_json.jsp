<%
    String step1ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP1_ERROR_MSG);
%>
<div class="query_step_section" id="select_cancer_type_section">
    <div class="row step_header_first_line">
        <div class="input-group input-group-sm col-5">
            <input type="text" id="jstree_search_input" class="form-control" placeholder="Select Cancer Study"/>
            <div class="input-group-btn">
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                  <span class="caret"></span>
                  <span class="sr-only">Toggle Dropdown</span>
                </button>
                <ul class="dropdown-menu dropdown-menu-right" role="menu" aria-labelledby="dropdownMenu1">
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("tcga");$("#jstree_search_input").trigger("input");' >tcga</a></li>
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("tcga -provisional");$("#jstree_search_input").trigger("input");' >tcga -provisional</a></li>
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("tcga -moratorium");$("#jstree_search_input").trigger("input");' >tcga -moratorium</a></li>
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("tcga OR icgc");$("#jstree_search_input").trigger("input");'>tcga OR icgc</a></li>
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("prostate mskcc");$("#jstree_search_input").trigger("input");'>prostate mskcc</a></li>
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("esophageal OR stomach");$("#jstree_search_input").trigger("input");'>esophageal OR stomach</a></li>
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("serous");$("#jstree_search_input").trigger("input");'>serous</a></li>
                    <li role="presentation"><a role="menuitem" tabindex="-1"  href='javascript:$("#jstree_search_input").val("breast");$("#jstree_search_input").trigger("input");'>breast</a></li>
                </ul>
            </div>
        </div>
        <div class="step_header_search_result">
            <span id="jstree_selected_study_count">No studies selected.</span>
        </div>
        
<!--        <div class="btn btn-default btn-sm">
            <input id="jstree_select_cell_line" type="checkbox">    
            <label for="jstree_select_cell_line">Cell Line Studies</label>
        </div>-->
    </div>
<!--    <button class="btn btn-default btn-sm" onclick='$("#jstree").jstree(true).deselect_all(); return false;'>Deselect all studies</button>&nbsp;&nbsp;
    <button class="btn btn-default btn-sm" onclick="var jstree = $('#jstree').jstree(true); var celllines = jstree.get_matching_nodes('cell line'); jstree.deselect_node(celllines); return false">Deselect Cell Line Studies</button><br><br>-->

    
<!--<input type="text" id="jstree_search_input" style="width:16em; background:url(images/search.svg) no-repeat scroll 3px 3px; background-size: 1em 1em; padding-left:1.8em"/>&nbsp;&nbsp;&nbsp;&nbsp;
<a href='javascript:$("#jstree_search_input").val("tcga");$("#jstree_search_input").trigger("input");' title="Search for TCGA studies.">tcga</a>
<div id="jstree_search_examples" style="display:none">
    Example queries:<br>
</div>
&nbsp;&nbsp;&nbsp;-->
<!--<a id="select_cancer_type_help" title="Type in keywords to narrow down the studies. When using two or more search terms, only studies with all terms are returned (AND logic). Add an 'or' between search terms if you want to see studies that contain either term. Placing quotation marks around two or more words will look for the exact string. Placing a dash (-) before a term will exclude results with that term."><i>More examples</i></a>-->
<div id="jstree" style="max-height:250px; overflow-y: scroll"></div>
<br>
<!--<div class="row step_header_bottom_line">
    <div class="btn btn-default btn-sm">
        <span id="flatten_jstree_btn">Flatten Tree</span>
    </div>
    <div class="btn btn-default btn-sm">
        <span onclick='javascript:$("#jstree").jstree(true).open_all();'>Expand all</span>
    </div>
    <div class="btn btn-default btn-sm">
        <span onclick='javascript:$("#jstree").jstree(true).close_all();$("#jstree").jstree(true).open_node("tissue");'>Collapse all</span>
    </div>
</div>-->

<input id="select_multiple_studies" name="<%= QueryBuilder.CANCER_STUDY_LIST %>" style="display:none">
<input id="select_single_study" name="<%= QueryBuilder.CANCER_STUDY_ID %>" style="display:none">
<script type="text/javascript">
$('#select_cancer_type_help').qtip({
                    content: { text: $('#jstree_search_examples') },
                    style: { classes: 'qtip-light qtip-rounded' },
                    position: { my:'left center',at:'right center',viewport: $(window) },
                    hide: { delay:200, fixed:true }
});

//$("#jstree_select_cell_line").change(function(){
//    if($(this).attr("checked"))
//    {
//        var jstree = $('#jstree').jstree(true); var celllines = jstree.get_matching_nodes('cell line'); jstree.select_node(celllines);
//    }
//    else
//    {
//        var jstree = $('#jstree').jstree(true); var celllines = jstree.get_matching_nodes('cell line'); jstree.deselect_node(celllines);
//    }
//});
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
    
