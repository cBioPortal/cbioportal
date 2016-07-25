<%--
 - Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 -
 - This library is distributed in the hope that it will be useful, but WITHOUT
 - ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 - FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 - is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 - obligations to provide maintenance, support, updates, enhancements or
 - modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 - liable to any party for direct, indirect, special, incidental or
 - consequential damages, including lost profits, arising out of the use of this
 - software and its documentation, even if Memorial Sloan-Kettering Cancer
 - Center has been advised of the possibility of such damage.
 --%>

<%--
 - This file is part of cBioPortal.
 -
 - cBioPortal is free software: you can redistribute it and/or modify
 - it under the terms of the GNU Affero General Public License as
 - published by the Free Software Foundation, either version 3 of the
 - License.
 -
 - This program is distributed in the hope that it will be useful,
 - but WITHOUT ANY WARRANTY; without even the implied warranty of
 - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 - GNU Affero General Public License for more details.
 -
 - You should have received a copy of the GNU Affero General Public License
 - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="s" uri="http://www.springframework.org/tags" %>

<%
    String step1ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP1_ERROR_MSG);
%>
<div class="query_step_section" id="select_cancer_type_section">
    <span class="step_header">Select Cancer Study:</span>
    <div class="row step_header_first_line">
        <div class="input-group input-group-sm col-5">
            <input type="text" id="jstree_search_input" class="form-control" placeholder="Search..." title="Search"/>
            <i id="step_header_first_line_empty_search" class="fa fa-times"></i>
            <div class="input-group-btn">
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                  <span class="caret"></span>
                  <span class="sr-only">Toggle Dropdown</span>
                </button>
                <%-- loop over the configured query suggestions --%>
                <ul class="dropdown-menu dropdown-menu-right" role="menu" title="Select from dropdown"><c:forEach var="query" items="${exampleStudyQueries}">
                    <%-- escape \ to \\ and " to \" inside the JS string --%>
                    <c:set var="escapedJsString"><s:escapeBody javaScriptEscape="true"><c:out value="${query}" escapeXml="false" /></s:escapeBody></c:set>
                    <li role="presentation"><a role="menuitem" tabindex="-1" href='javascript:void(0)' onclick='$("#jstree_search_input").val("${escapedJsString}");$("#jstree_search_input").trigger("input");'><c:out value="${query}" /></a></li></c:forEach>
                </ul>
            </div>
        </div>
        <div class="step_header_search_result">
            <span id="jstree_selected_study_count">No studies selected.</span>
            <span><a href='javascript:void(0)' id='jstree_deselect_all_btn' onclick='$("#jstree").jstree(true).deselect_all();'>Deselect all</a></span>
        </div>
        
        
<!--        <div class="btn btn-default btn-sm">
            <input id="jstree_select_cell_line" type="checkbox">    
            <label for="jstree_select_cell_line">Cell Line Studies</label>
        </div>-->
    </div>
    <div id="jstree_search_none_found_msg" style="display:none">
        <h5>No matches found.</h5>
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

<input id="select_multiple_studies" name="<%= QueryBuilder.CANCER_STUDY_LIST %>" style="display:none" title="Select multiple studies">
<input id="select_single_study" name="<%= QueryBuilder.CANCER_STUDY_ID %>" style="display:none" title="Select single study">
<script type="text/javascript">
$('#select_cancer_type_help').qtip({
                    content: { text: $('#jstree_search_examples') },
                    style: { classes: 'qtip-light qtip-rounded' },
                    position: { my:'left center',at:'right center',viewport: $(window) },
                    hide: { delay:200, fixed:true }
});
$('#jstree_search_input').keypress(function(e) { return e.keyCode !== 13; });
$('#jstree').bind('mousewheel DOMMouseScroll', function(e) {
    // thanks to mrtsherman on StackOverflow: http://stackoverflow.com/a/7571867/3158208
    var scrollTo = null;
    
    if (e.type === 'mousewheel') {
        scrollTo = (e.originalEvent.wheelDelta * -1);
    } else if (e.type === 'DOMMouseScroll') {
        scrollTo = 40 * e.originalEvent.detail;
    }
    if (scrollTo) {
        e.preventDefault();
        $(this).scrollTop(scrollTo + $(this).scrollTop());
    }
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

<%
if (step1ErrorMsg != null) {
    out.println ("</div>");
}
%>

</div>    
    
