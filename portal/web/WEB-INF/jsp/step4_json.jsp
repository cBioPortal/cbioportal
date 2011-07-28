<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.Utilities" %>

<%
    String step4ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP4_ERROR_MSG);
%>

<div class="query_step_section">
    <span class="step_header">Enter Gene Set:</span>

    <script language="javascript" type="text/javascript">

    function popitup(url) {
        newwindow=window.open(url,'OncoSpecLangInstructions','height=1000,width=1000,left=400,top=0,scrollbars=yes');
        if (window.focus) {newwindow.focus()}
        return false;
    }
    </script>
    <span style="float:right">
        <a href="onco_query_lang_desc.jsp" onclick="return popitup('onco_query_lang_desc.jsp')">Advanced:  Onco Query Language (OQL)</a>
    </span>

<%
// Output step 4 form validation error
if (step4ErrorMsg != null) {
    out.println("<div class='ui-state-error ui-corner-all' style='margin-top:4px; padding:5px;'>"
            + "<span class='ui-icon ui-icon-alert' style='float: left; margin-right: .3em;'></span>"
            + "<strong>" + step4ErrorMsg + "</strong>");
}
%>

    <P/>
<textarea rows='5' cols='80' id='gene_list' placeholder="Enter HUGO Gene Symbols" required name='<%= QueryBuilder.GENE_LIST %>'><%
    if (localGeneList != null && localGeneList.length() > 0) {
        out.println(Utilities.appendSemis(localGeneList));
    }
%>
</textarea>

<%
// Output step 4 form validation error
if (step4ErrorMsg != null) {
    out.println("</div>");
}
%>
    
    <p><span style="font-size:80%">Or Select from Example Gene Sets:</span></p>
    <p><select id="select_gene_set" name="<%= QueryBuilder.GENE_SET_CHOICE %>"></select>
</div>