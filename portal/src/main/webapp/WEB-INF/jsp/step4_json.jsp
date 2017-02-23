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

    <% if (localTabIndex.equals(QueryBuilder.TAB_VISUALIZE)) { %>
        <% out.println("<span style='font-size:120%; color:black'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href='onco_query_lang_desc.jsp' onclick='return popitup(\"onco_query_lang_desc.jsp\")'>Advanced: Onco Query Language (OQL)</a></span>"); %>
    <% } %>
    
    <div style='padding-top:10px;padding-bottom:5px;'>
        <select id="select_gene_set" name="<%= QueryBuilder.GENE_SET_CHOICE %>" title="Select Gene Set"></select>
    </div>
        
    <div style="padding-bottom:5px;margin-left:-3px;">
        <button id="toggle_mutsig_dialog" onclick="promptMutsigTable(); return false;" style="font-size: 1em;">Select From Recurrently Mutated Genes (MutSig)</button>
        <button id="toggle_gistic_dialog_button" onclick="Gistic.UI.open_dialog(); return false;" style="font-size: 1em; display: none;">Select Genes from Recurrent CNAs (Gistic)</button>
    </div>

    <script type="text/javascript">
        $(document).ready(function() {
            GeneSymbolValidator.initialize();
        });
    </script>

<textarea rows='5' cols='80' id='gene_list' placeholder="Enter Gene Symbols or Gene Aliases" required
name='<%= QueryBuilder.GENE_LIST %>' title='Enter Gene Symbols or Gene Aliases'><%
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
