<%@ include file="global/global_variables.jsp" %>
<jsp:include page="global/header.jsp" flush="true" />

<%
    String smry = "<a href=\"study.do?cancer_study_id="+cancerTypeId+"\">"+cancerStudyName +
            "</a>/" + caseSetName + ": (" +
            mergedCaseListSize + ")" + "/" +
            geneSetName + "/" + geneWithScoreList.size() +
            (geneWithScoreList.size() == 1?"gene":"genes");
%>

<p>
    <div class='gene_set_summary'>
        Gene Set / Pathway is altered in <%=percentCasesAffected%> of all cases. <br>
    </div>
</p>
<p>
    <small><strong><%=smry%></strong></small>
</p>

<%
    if (warningUnion.size() > 0) {
        out.println ("<div class='warning'>");
        out.println ("<h4>Errors:</h4>");
        out.println ("<ul>");
        Iterator<String> warningIterator = warningUnion.iterator();
        int counter = 0;
        while (warningIterator.hasNext()) {
            String warning = warningIterator.next();
            if (counter++ < 10) {
                out.println ("<li>" +  warning + "</li>");
            }
        }
        if (warningUnion.size() > 10) {
            out.println ("<li>...</li>");
        }
        out.println ("</ul>");
        out.println ("</div>");
    }

    if (geneWithScoreList.size() == 0) {
        out.println ("<b>Please go back and try again.</b>");
        out.println ("</div>");
    } else {
%>

<script type="text/javascript">
    $(document).ready(function(){
        // Init Tool Tips
        $("#toggle_query_form").tipTip();
    });
</script>

<p><a href="" title="Modify your original query.  Recommended over hitting your browser's back button." id="toggle_query_form">
    <span class='query-toggle ui-icon ui-icon-triangle-1-e' style='float:left;'></span>
    <span class='query-toggle ui-icon ui-icon-triangle-1-s' style='float:left; display:none;'></span><b>Modify Query</b></a>
<p/>

<div style="margin-left:5px;display:none;" id="query_form_on_results_page">
    <%@ include file="query_form.jsp" %>
</div>

<div id="tabs">
    <ul>
    <%
        Boolean showMutTab = false;
        if (geneWithScoreList.size() > 0) {

            Enumeration paramEnum = request.getParameterNames();
            StringBuffer buf = new StringBuffer(request.getAttribute(QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?");

            while (paramEnum.hasMoreElements())
            {
                String paramName = (String) paramEnum.nextElement();
                String values[] = request.getParameterValues(paramName);

                if (values != null && values.length >0)
                {
                    for (int i=0; i<values.length; i++)
                    {
                        String currentValue = values[i].trim();

                        if (currentValue.contains("mutation"))
                        {
                            showMutTab = true;
                        }

                        if (paramName.equals(QueryBuilder.GENE_LIST)
                            && currentValue != null)
                        {
                            //  Spaces must be converted to semis
                            currentValue = Utilities.appendSemis(currentValue);
                            //  Extra spaces must be removed.  Otherwise OMA Links will not work.
                            currentValue = currentValue.replaceAll("\\s+", " ");
                            //currentValue = URLEncoder.encode(currentValue);
                        }
                        else if (paramName.equals(QueryBuilder.CASE_IDS) ||
                                paramName.equals(QueryBuilder.CLINICAL_PARAM_SELECTION))
                        {
                            // do not include case IDs anymore (just skip the parameter)
                            // if we need to support user-defined case lists in the future,
                            // we need to replace this "parameter" with the "attribute" caseIdsKey

                            // also do not include clinical param selection parameter, since
                            // it is only related to user-defined case sets, we need to take care
                            // of unsafe characters such as '<' and '>' if we decide to add this
                            // parameter in the future
                            continue;
                        }

                        // this is required to prevent XSS attacks
                        currentValue = xssUtil.getCleanInput(currentValue);
                        //currentValue = StringEscapeUtils.escapeJavaScript(currentValue);
                        //currentValue = StringEscapeUtils.escapeHtml(currentValue);
                        currentValue = URLEncoder.encode(currentValue);

                        buf.append (paramName + "=" + currentValue + "&");
                    }
                }
            }

            out.println ("<li><a href='#summary' class='result-tab' title='Compact visualization of genomic alterations'>OncoPrint</a></li>");

            if (computeLogOddsRatio && geneWithScoreList.size() > 1) {
                out.println ("<li><a href='#gene_correlation' class='result-tab' title='Mutual exclusivity and co-occurrence analysis'>"
                + "Mutual Exclusivity</a></li>");
            }

            if ( has_mrna && (has_rppa || has_methylation || has_copy_no) ) {
                out.println ("<li><a href='#plots' class='result-tab' title='Multiple plots, including CNA v. mRNA expression'>" + "Plots</a></li>");
            }

            if (showMutTab){
                out.println ("<li><a href='#mutation_details' class='result-tab' title='Mutation details, including mutation type, "
                 + "amino acid change, validation status and predicted functional consequence'>"
                 + "Mutations</a></li>");
            }

            if (showCoexpTab) {
                out.println ("<li><a href='#coexp' class='result-tab' title='List of top co-expressed genes'>Co-Expression</a></li>");
            }

            if (has_rppa) {
                out.println ("<li><a href='#protein_exp' class='result-tab' title='Protein and Phopshoprotein changes using Reverse Phase Protein Array (RPPA) data'>"
                + "Protein Changes</a></li>");
            }

            if (has_survival) {
                out.println ("<li><a href='#survival' class='result-tab' title='Survival analysis and Kaplan-Meier curves'>"
                + "Survival</a></li>");
            }

            if (includeNetworks) {
                out.println ("<li><a href='#network' class='result-tab' title='Network visualization and analysis'>"
                + "Network</a></li>");
            }

            if (showIGVtab){
                out.println ("<li><a href='#igv_tab' class='result-tab' title='Visualize copy number data via the Integrative Genomics Viewer (IGV).'>IGV</a></li>");
            }
            out.println ("<li><a href='#data_download' class='result-tab' title='Download all alterations or copy and paste into Excel'>Download</a></li>");
            out.println ("<li><a href='#bookmark_email' class='result-tab' title='Bookmark or generate a URL for email'>Bookmark</a></li>");
            out.println ("<!--<li><a href='index.do' class='result-tab'>Create new query</a> -->");
            out.println ("</ul>");

            out.println ("<div class=\"section\" id=\"bookmark_email\">");

            // diable bookmark link if case set is user-defined
            if (caseSetId.equals("-1"))
            {
                out.println("<br>");
                out.println("<h4>The bookmark option is not available for user-defined case lists.</h4>");
            }
            else
            {
                out.println ("<h4>Right click</b> on the link below to bookmark your results or send by email:</h4><br><a href='"
                        + buf.toString() + "'>" + request.getAttribute
                        (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?...</a>");
                String longLink = buf.toString();
                out.println("<br><br>");
                out.println("If you would like to use a <b>shorter URL that will not break in email postings</b>, you can use the<br><a href='https://bitly.com/'>bitly.com</a> service below:<BR>");
                out.println("<BR><form><input type=\"button\" onClick=\"bitlyURL('"+longLink+"', '"+bitlyUser+"', '"+bitlyKey+"')\" value=\"Shorten URL\"></form>");
                out.println("<div id='bitly'></div>");
            }

            out.println("</div>");
        }
    %>

        <div class="section" id="summary">
            <% //contents of fingerprint.jsp now come from attribute on request object %>
            <%@ include file="oncoprint/main.jsp" %>
            <%@ include file="gene_info.jsp" %>
        </div>

            <% if ( has_mrna && (has_copy_no || has_methylation || has_copy_no) ) { %>
        <%@ include file="plots_tab.jsp" %>
            <% } %>

            <% if (showIGVtab) { %>
        <%@ include file="igv.jsp" %>
            <% } %>

            <% if (has_survival) { %>
        <%@ include file="survival_tab.jsp" %>
            <% } %>

            <% if (computeLogOddsRatio && geneWithScoreList.size() > 1) { %>
        <%@ include file="correlation.jsp" %>
            <% } %>

            <% if (mutationDetailLimitReached != null) {
        out.println("<div class=\"section\" id=\"mutation_details\">");
        out.println("<P>To retrieve mutation details, please specify "
        + QueryBuilder.MUTATION_DETAIL_LIMIT + " or fewer genes.<BR>");
        out.println("</div>");
    } else if (showMutTab) { %>
        <%@ include file="mutation_views.jsp" %>
        <%@ include file="mutation_details.jsp" %>
            <%  } %>

        <% if (has_rppa) { %>
            <%@ include file="protein_exp.jsp" %>
        <% } %>

        <% if (includeNetworks) { %>
            <%@ include file="networks.jsp" %>
        <% } %>

        <%@ include file="data_download.jsp" %>
        <%@ include file="image_tabs_data.jsp" %>
        
        <% if (showCoexpTab) { %>
            <%@ include file="co_expression.jsp" %>
        <% } %>

</div> <!-- end tabs div -->
<% } %>

</div>
</td>
</tr>
<tr>
    <td colspan="3">
        <jsp:include page="global/footer.jsp" flush="true" />
    </td>
</tr>
</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />
</form>

<script type="text/javascript">
	// initially hide network tab
	$("div.section#network").attr('style', 'height: 0px; width: 0px; visibility: hidden;');

	// it is better to check selected tab after document gets ready
	$(document).ready(function() {
		// check if network tab is initially selected
		// TODO this depends on aria-hidden attribute which may not be safe...
		if ($("div.section#network").attr('aria-hidden') == "false")
		{
			// make the network tab visible...
			$("div.section#network").removeAttr('style');
		}
	});

    // to fix problem of flash repainting
    $("a.result-tab").click(function(){

        if($(this).attr("href")=="#network") {
            $("div.section#network").removeAttr('style');
        } else {
	        // since we never allow display:none we should adjust visibility, height, and width properties
            $("div.section#network").attr('style', 'height: 0px; width: 0px; visibility: hidden;');
        }
    });

    //  Set up Tip-Tip Event Handler for Genomic Profiles help
    $(".result-tab").tipTip({defaultPosition: "bottom", delay:"100", edgeOffset: 10, maxWidth: 200});
</script>

</body>
</html>