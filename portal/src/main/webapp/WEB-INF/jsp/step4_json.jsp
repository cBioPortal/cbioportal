<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
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
    <span style="font-size:120%; color:black">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="onco_query_lang_desc.jsp" onclick="return popitup('onco_query_lang_desc.jsp')">Advanced:  Onco Query Language (OQL)</a></span>
    
    <div style='padding-top:10px;'>
        <select id="select_gene_set" name="<%= QueryBuilder.GENE_SET_CHOICE %>"></select>
    </div>
        
    <div>
        <button id="toggle_mutsig_dialog" onclick="promptMutsigTable(); return false;" style="font-size: 1em;">Select From Recurrently Mutated Genes (MutSig)</button>
        <button id="toggle_gistic_dialog_button" onclick="Gistic.UI.open_dialog(); return false;" style="font-size: 1em; display: none;">Select Genes from Recurrent CNAs (Gistic)</button>
    </div>

    <script type="text/javascript">
        $(document).ready(function() {
            GeneSymbolValidator.initialize();
        });
    </script>

<textarea rows='5' cols='80' id='gene_list' placeholder="Enter HUGO Gene Symbols or Gene Aliases" required
name='<%= QueryBuilder.GENE_LIST %>'><%
    if (localGeneList != null && localGeneList.length() > 0) {
	    String geneListWithSemis =
			    org.mskcc.cbio.portal.oncoPrintSpecLanguage.Utilities.appendSemis(localGeneList);
	    // this is for xss security
	    geneListWithSemis = StringEscapeUtils.escapeJavaScript(geneListWithSemis);
	    // ...but we want to keep newlines, and slashes so unescape them
	    geneListWithSemis = geneListWithSemis.replaceAll("\\\\n", "\n").replaceAll("\\\\/", "/");
        out.print(geneListWithSemis);
    }
%></textarea>

<p id="genestatus"></p>

</div>
<script type='text/javascript'>
$('#toggle_gistic_dialog_button').button();
</script>
