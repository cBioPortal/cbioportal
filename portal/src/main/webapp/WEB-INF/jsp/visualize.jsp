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


<%@ include file="global/global_variables.jsp" %>
<jsp:include page="global/header.jsp" flush="true" />
<%@ page import="java.util.Map" %>
<%@ page import="org.codehaus.jackson.map.ObjectMapper" %>

<%
    // we have session service running AND this was a post, 
    // then modify URL to include session service id so bookmarking will work
    if (useSessionServiceBookmark && "POST".equals(request.getMethod())) {
%>
    <script>
        changeURLToSessionServiceURL(window.location.href, 
            window.location.pageTitle, 
            <%= new ObjectMapper().writeValueAsString(request.getParameterMap()) %>);
   </script>
<% } // end if isPost and we have session service running %>

<div class='main_smry'>
    <div id='main_smry_stat_div' style='float:right;margin-right:15px;margin-bottom:-5px;width:50%;text-align:right;'></div>
    <div id='main_smry_info_div'>
        <table style='margin-left:0px;width:40%;margin-top:-10px;margin-bottom:-5px;' >
            <tr>
                <td><div id='main_smry_modify_query_btn'><div></td>
                <td><div id='main_smry_query_div' style='padding-left: 5px;'></div></td>
            </tr>
        </table>
    </div>
    <div style="margin-left:5px;display:none;margin-top:-5px;" id="query_form_on_results_page">
        <%@ include file="query_form.jsp" %>
    </div>
</div>

<div id="tabs">
    <ul>
    <%
        Boolean showMutTab = false;
        Boolean showCancerTypesSummary = false;
        Boolean showEnrichmentsTab = true;
        Boolean showSurvivalTab = true;
        Boolean showPlotsTab = true;
        Boolean showDownloadTab = true;
        Boolean showBookmarkTab = true;
        List<String> disabledTabs = GlobalProperties.getDisabledTabs();

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

                        if (currentValue.contains("mutation") && !disabledTabs.contains("mutations"))
                        {
                            showMutTab = true;
                        }                        
                        if (disabledTabs.contains("co_expression")) 
                        {
                            showCoexpTab = false;
                        }                        
                        if (disabledTabs.contains("IGV")) 
                        {
                            showIGVtab = false;
                        }                        
                        if (disabledTabs.contains("mutual_exclusivity")) 
                        {
                            computeLogOddsRatio = false;
                        }                        
                        if (disabledTabs.contains("enrichments")) 
                        {
                            showEnrichmentsTab = false;
                        }                        
                        if (disabledTabs.contains("survival")) 
                        {
                            has_survival = false;
                        }                        
                        if (disabledTabs.contains("network")) 
                        {
                            includeNetworks = false;
                        }                        
                        if (disabledTabs.contains("plots")) 
                        {
                            showPlotsTab = false;
                        }
                        if (disabledTabs.contains("download")) 
                        {
                            showDownloadTab = false;
                        }
                        if (disabledTabs.contains("bookmark")) {
                            showBookmarkTab = false;
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

            // determine whether to show the cancerTypesSummaryTab
            // retrieve the cancerTypesMap and create an iterator for the values
            Map<String, List<String>>  cancerTypesMap = (Map<String, List<String>>) request.getAttribute(QueryBuilder.CANCER_TYPES_MAP);
            if(cancerTypesMap.keySet().size() > 1) {
            	showCancerTypesSummary = true;
            }
            else if (cancerTypesMap.keySet().size() == 1 && cancerTypesMap.values().iterator().next().size() > 1 )  {
            	showCancerTypesSummary = true;
            }
            if (disabledTabs.contains("cancer_types_summary")) {
                showCancerTypesSummary = false;
            }
            out.println ("<li><a href='#summary' class='result-tab' id='oncoprint-result-tab'>OncoPrint</a></li>");
            // if showCancerTypesSummary is try, add the list item
            if(showCancerTypesSummary){
                out.println ("<li><a href='#pancancer_study_summary' class='result-tab' title='Cancer types summary'>"
                + "Cancer Types Summary</a></li>");
            }

            if (computeLogOddsRatio) {
                out.println ("<li><a href='#mutex' class='result-tab' id='mutex-result-tab'>"
                + "Mutual Exclusivity</a></li>");
            }
            if (showPlotsTab) {
                out.println ("<li><a href='#plots' class='result-tab' id='plots-result-tab'>Plots</a></li>");
            }            
            if (showMutTab){
                out.println ("<li><a href='#mutation_details' class='result-tab' id='mutation-result-tab'>Mutations</a></li>");
            }
            if (showCoexpTab) {
                out.println ("<li><a href='#coexp' class='result-tab' id='coexp-result-tab'>Co-Expression</a></li>");
            }
            if (has_mrna || has_copy_no || showMutTab && showEnrichmentsTab) {
                out.println("<li><a href='#enrichementTabDiv' id='enrichments-result-tab' class='result-tab'>Enrichments</a></li>");
            }
            if (has_survival) {
                out.println ("<li><a href='#survival' class='result-tab' id='survival-result-tab'>Survival</a></li>");
            }
            if (includeNetworks) {
                out.println ("<li><a href='#network' class='result-tab' id='network-result-tab'>Network</a></li>");
            }
            if (showIGVtab){
                out.println ("<li><a href='#igv_tab' class='result-tab' id='igv-result-tab'>CN Segments</a></li>");
            }
            if (showDownloadTab) {
                out.println ("<li><a href='#data_download' class='result-tab' id='data-download-result-tab'>Download</a></li>");
            }       
            if (showBookmarkTab) {
                out.print ("<li><a href='#bookmark_email' class='result-tab' id='bookmark-result-tab'");
                if (useSessionServiceBookmark) {
                    out.print (" data-session='");
	                out.print (new ObjectMapper().writeValueAsString(request.getParameterMap()));
	                out.print ("'");
                } 
	            out.println (">Bookmark</a></li>");
            }            
            out.println ("</ul>");

            out.println ("<div class=\"section\" id=\"bookmark_email\">");

            if (!useSessionServiceBookmark && sampleSetId.equals("-1"))
            {
                out.println("<br>");
                out.println("<h4>The bookmark option is not available for user-defined case lists.</h4>");
            } 
            else 
            {
                out.println ("<h4>Right click on one of the links below to bookmark your results:</h4>");
                out.println("<br>");
                out.println("<div id='session-id'></div>");
                out.println("<br>");
                out.println("If you would like to use a <b>shorter URL that will not break in email postings</b>, you can use the<br><a href='https://bitly.com/'>bitly.com</a> url below:<BR>");
                out.println("<div id='bitly'></div>");
            }
            out.println("</div>");
    %>

        <div class="section" id="summary">
            <% //contents of fingerprint.jsp now come from attribute on request object %>
            <%@ include file="oncoprint/main.jsp" %>
        </div>

        <!-- if showCancerTypes is true, include cancer_types_summary.jsp -->
        <% if(showCancerTypesSummary) { %>
        <%@ include file="pancancer_study_summary.jsp" %>
        <%}%>

        <%@ include file="plots_tab.jsp" %>

        <% if (showIGVtab) { %>
            <%@ include file="igv.jsp" %>
        <% } %>

        <% if (has_survival) { %>
            <%@ include file="survival_tab.jsp" %>
        <% } %>

        <% if (computeLogOddsRatio) { %>
            <%@ include file="mutex_tab.jsp" %>
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

        <% if (includeNetworks) { %>
            <%@ include file="networks.jsp" %>
        <% } %>

        <% if (showCoexpTab) { %>
            <%@ include file="co_expression.jsp" %>
        <% } %>

        <% if (has_mrna || has_copy_no || showMutTab) { %>
            <%@ include file="enrichments_tab.jsp" %>
        <% } %>

        <%@ include file="data_download.jsp" %>

</div> <!-- end tabs div -->


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
    // it is better to check selected tab after document gets ready
    $(document).ready(function() {
        var firstTime = true;

        $("#toggle_query_form").tipTip();
        // check if network tab is initially selected
        if ($("div.section#network").is(":visible"))
        {
            // init the network tab
	        //send2cytoscapeweb(window.networkGraphJSON, "cytoscapeweb", "network");
	        //firstTime = false;

	        // TODO window.networkGraphJSON is null at this point,
	        // this is a workaround to wait for graphJSON to get ready
	        var interval = setInterval(function() {
		        if (window.networkGraphJSON != null)
		        {
			        clearInterval(interval);
			        if (firstTime)
			        {
                $(window).resize();
				        send2cytoscapeweb(window.networkGraphJSON, "cytoscapeweb", "network");
				        firstTime = false;
			        }
		        }
	        }, 50);
        }

        $("a.result-tab").click(function(){

            if($(this).attr("href")=="#network")
            {
              var interval = setInterval(function() {
                if (window.networkGraphJSON != null)
                {
                  clearInterval(interval);
                  if(firstTime)
                  {
                    $(window).resize();
                    send2cytoscapeweb(window.networkGraphJSON, "cytoscapeweb", "network");
                    firstTime = false;
                  }
                else
                  {
                    // TODO this is a workaround to adjust cytoscape canvas
                    // and probably not the best way to do it...
                    $(window).resize();
                  }

                }
              }, 50);
            }
        });

        $("#bookmark-result-tab").parent().click(function() {
            <% if (useSessionServiceBookmark) { %>
                addSessionServiceBookmark(window.location.href, $(this).children("#bookmark-result-tab").data('session'));
            <% } else { %>
                addURLBookmark();
            <% } %>
        });

        //qtips
        $("#oncoprint-result-tab").qtip(
            {
                content: {text: "Compact visualization of genomic alterations"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#oncoprint-result-tab").click(function() {
            $(window).trigger('resize');
        });
        $("#mutex-result-tab").qtip(
            {
                content: {text: "Mutual exclusivity and co-occurrence analysis"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#plots-result-tab").qtip(
            {
                content: {text: "Multiple plots, including CNA v. mRNA expression"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#mutation-result-tab").qtip(
            {
                content: {text: "Mutation details, including mutation type, amino acid change, validation status and predicted functional consequence"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#coexp-result-tab").qtip(
            {
                content: {text: "List of top co-expressed genes"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#enrichments-result-tab").qtip(
            {
                content: {text: "This analysis finds alterations " +
                "(mutations, copy number alterations, mRNA expression changes, and protein expression changes, if available) " +
                "that are enriched in either altered samples (with at least one alteration based on query) or unaltered samples. "},
                //"The analysis is only performed on annotated cancer genes. <a href='cancer_gene_list.jsp' target='_blank'>[List of Portal Cancer Genes]</a>"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#survival-result-tab").qtip(
            {
                content: {text: "Survival analysis and Kaplan-Meier curves"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#network-result-tab").qtip(
            {
                content: {text: "Network visualization and analysis"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#igv-result-tab").qtip(
            {
                content: {text: "Visualize copy number data via the Integrative Genomics Viewer (IGV)"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#data-download-result-tab").qtip(
            {
                content: {text: "Download all alterations or copy and paste into Excel"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
        $("#bookmark-result-tab").qtip(
            {
                content: {text: "Bookmark or generate a URL for email"},
                style: { classes: 'qtip-light qtip-rounded qtip-shadow qtip-lightyellow result-tab-qtip-content' },
                show: {event: "mouseover", delay: 0},
                hide: {fixed:true, delay: 100, event: "mouseout"},
                position: {my:'left top',at:'right bottom', viewport: $(window)}
            }
        );
    });
</script>


<style type="text/css">
    input[type="checkbox"]  {
        margin: 5px;
    }
    input[type="radio"]  {
        margin: 3px;
    }
    button {
        margin: 3px;
    }
    [class*="ui-button-text"] {
        margin: 3px;
    }
    .result-tab-qtip-content{
        font-size: 13px;
        line-height: 110%;
    }
</style>

</body>
</html>
