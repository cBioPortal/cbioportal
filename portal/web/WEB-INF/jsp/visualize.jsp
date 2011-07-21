<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.portal.model.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>                 
<%@ page import="org.mskcc.portal.util.GeneSetUtil" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.mskcc.portal.util.ZScoreUtil" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.ParserOutput" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.OncoPrintSpecification" %>
<%@ page import="org.mskcc.portal.util.HeatMapLegend" %>
<%@ page import="org.mskcc.portal.util.OncoPrintSpecificationDriver" %>
<%@ page import="org.mskcc.portal.util.Config" %>
<%@ page import="org.mskcc.portal.oncoPrintSpecLanguage.Utilities" %>

<%
    ArrayList<GeneticProfile> profileList = (ArrayList<GeneticProfile>) request.getAttribute
            (QueryBuilder.PROFILE_LIST_INTERNAL);
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute
            (QueryBuilder.GENETIC_PROFILE_IDS);
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
    ArrayList<CaseSet> caseSets = (ArrayList<CaseSet>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String caseIds = xssUtil.getCleanInput(request, QueryBuilder.CASE_IDS);
    ArrayList<CancerType> cancerTypes = (ArrayList<CancerType>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);

    ProfileData mergedProfile = (ProfileData)
            request.getAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL);
    String geneList = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);

    ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( geneList,
             (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS),
             (ArrayList<GeneticProfile>) request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL),
             zScoreThreshold );

    OncoPrintSpecification theOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile, theOncoPrintSpecification, zScoreThreshold );

    DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
    String geneSetChoice = request.getParameter(QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "custom";
    }
    GeneSetUtil geneSetUtil = new GeneSetUtil();
    ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
    Set<String> warningUnion = (Set<String>) request.getAttribute(QueryBuilder.WARNING_UNION);


    ArrayList <GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    ArrayList<String> mergedCaseList = mergedProfile.getCaseIdList();

    Config globalConfig = Config.getInstance();
    String siteTitle = globalConfig.getProperty("skin.title");
    if (siteTitle == null) {
        siteTitle = "cBio Cancer Genomics Portal";
    }

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");
    String computeLogOddsRatioStr = request.getParameter(QueryBuilder.COMPUTE_LOG_ODDS_RATIO);
    boolean computeLogOddsRatio = false;
    if (computeLogOddsRatioStr != null) {
        computeLogOddsRatio = true;
    }

    ExtendedMutationMap mutationMap = (ExtendedMutationMap)
            request.getAttribute(QueryBuilder.MUTATION_MAP);
    Boolean mutationDetailLimitReached = (Boolean)
            request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);

    ArrayList <ClinicalData> clinicalDataList = (ArrayList<ClinicalData>)
            request.getAttribute(QueryBuilder.CLINICAL_DATA_LIST);
%>

<script type="text/javascript">
    function getTinyURL(longURL, success) {
        var API = 'http://json-tinyurl.appspot.com/?url=',
        URL = API + encodeURIComponent(longURL) + '&callback=?';

	    $.getJSON(URL, function(data){
        	success && success(data.tinyurl);
        });
}
</script>

<script type="text/javascript">
     function shrinkURL(longURL){
     getTinyURL(longURL, function(tinyurl){
          $('#tinyurl').html("<a href=\""+tinyurl+"\">"+tinyurl+"</a>");
     });
};
</script>

<html>

<jsp:include page="global/header.jsp" flush="true" />

	<table>
        <tr>
            <td>

            <div id="results_container">
            
             <%   String smry = "";
                      
                    out.println ("<p><div class='gene_set_summary'>Gene Set / Pathway is altered in "
                        + percentFormat.format(dataSummary.getPercentCasesAffected())
                        + " of all cases.");
                 out.println ("<br></div></p>");
                 out.println ("<p><small><strong>");

                for (CancerType cancerType: cancerTypes){
                    if (cancerTypeId.equals(cancerType.getCancerTypeId())){
                        smry = smry + cancerType.getCancerName();
                    }
                }
                for (CaseSet caseSet:  caseSets) {
                    if (caseSetId.equals(caseSet.getId())) {
                        smry = smry + "/" + caseSet.getName() + ":  "
                                + " (" + mergedCaseList.size() + ")";
                    }
                }
                for (GeneSet geneSet:  geneSetList) {
                    if (geneSetChoice.equals(geneSet.getId())) {
                        smry = smry + "/" + geneSet.getName();
                    }
                }
                smry = smry + "/" + geneWithScoreList.size();
                if (geneWithScoreList.size() == 1){
                    smry = smry + " gene";
                } else {
                    smry = smry + " genes";
                }

                out.println (smry);
                out.println ("</strong></small></p>");
                 %>

            <% if (warningUnion.size() > 0) {
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
            } else { %>

            <div id="tabs">
                <ul>
                <% Boolean showMutTab = false; %>
                <%
                if (geneWithScoreList.size() > 0) {


                    Enumeration paramEnum = request.getParameterNames();
                    StringBuffer buf = new StringBuffer(request.getAttribute
                            (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?");
                    while (paramEnum.hasMoreElements()) {
                        String paramName = (String) paramEnum.nextElement();
                        String values[] = request.getParameterValues(paramName);
                        if (values != null && values.length >0) {
                            for (int i=0; i<values.length; i++) {
                                String currentValue = values[i].trim();
                                if (currentValue.contains("mutation")){
                                    showMutTab = true;
                                }
                                if (paramName.equals(QueryBuilder.GENE_LIST) || paramName.equals(QueryBuilder.CASE_IDS)
                                    && currentValue != null) {
                                    //  Spaces must be converted to semis
                                    currentValue = Utilities.appendSemis(currentValue);
                                    //  Extra spaces must be removed.  Otherwise OMA Links will not work.
                                    currentValue = currentValue.replaceAll("\\s+", " ");
                                    currentValue = URLEncoder.encode(currentValue);
                                }
                                buf.append (paramName + "=" + currentValue + "&");
                            }
                        }
                    }

                    out.println ("<li><a href='#summary'>Summary</a></li>");

                    if (QueryBuilder.INCLUDE_NETWORKS) {
                        out.println ("<li><a href='#network'>Network</a></li>");
                    }

                    out.println ("<li><a href='#plots'>Plots</a></li>");

                    if (clinicalDataList != null && clinicalDataList.size() > 0) {
                        out.println ("<li><a href='#survival'>Survival</a></li>");
                    }

                    if (computeLogOddsRatio && geneWithScoreList.size() > 1) {
                        out.println ("<li><a href='#gene_correlation'>Mutual Exclusivity</a></li>");
                    }

                    if (showMutTab){
                        out.println ("<li><a href='#mutation_details'>Mutation Details</a></li>");
                    }

                    out.println ("<li><a href='#event_map'>Event Map</a></li>");
                    %>

                    <%@ include file="image_tabs.jsp" %>

                    <%
                    out.println ("<li><a href='#data_download'>Data Download</a></li>");
                    out.println ("<li><a href='#bookmark_email'>Bookmark/Email</a></li>");
                    out.println ("<!--<li><a href='index.do'>Create new query</a> -->");

                    out.println ("</ul>");

                    out.println ("<div class=\"section\" id=\"bookmark_email\">");
                    out.println ("<h4>Right click</b> on the link below to bookmark your results or send by email:</h4><br><a href='"
                            + buf.toString() + "'>" + request.getAttribute
                            (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?...</a>");

                    String longLink = buf.toString();
                    out.println("<br><br>");
                    out.println("If you would like to use a <b>shorter URL that will not break in email postings</b>, you can use the<br><a href='http://tinyurl.com/'>TinyURL.com</a> service below:<BR>");
                    out.println("<BR><form><input type=\"button\" onClick=\"shrinkURL('"+longLink+"')\" value=\"Get TinyURL\"></form>");
                    out.println("<div id='tinyurl'></div>");
                    out.println("</div>");
                }

                %>

            <div class="section" id="summary">
            <%@ include file="fingerprint.jsp" %>
            <%@ include file="frequency_plot.jsp" %>
            </div>

            <%
            if (QueryBuilder.INCLUDE_NETWORKS) { %>
                <%@ include file="networks.jsp" %>
            <% } %>


            <%@ include file="plots_tab.jsp" %>
                    
            <%
                if (clinicalDataList != null && clinicalDataList.size() > 0) { %>
                    <%@ include file="clinical_tab.jsp" %>
            <%    }
            %>

            <% if (computeLogOddsRatio && geneWithScoreList.size() > 1) { %>
                <%@ include file="correlation.jsp" %>
            <% } %>
            <% if (mutationDetailLimitReached != null) {
                    out.println("<div class=\"section\" id=\"mutation_details\">");
                    out.println("<P>To retrieve mutation details, please specify "
                    + QueryBuilder.MUTATION_DETAIL_LIMIT + " or fewer genes.<BR>");
                    out.println("</div>");
                } else if (showMutTab) { %>
                    <%@ include file="mutation_details.jsp" %>
            <%  } %>

            <div class="section" id="event_map">
            <div class="map">
            <% 
            out.println( HeatMapLegend.outputHeatMapLegend( theOncoPrintSpecification.getUnionOfPossibleLevels()) );
            %>
			</div>            
                <br>
            <%@ include file="heatmap.jsp" %>
            </div>   <!-- end heat map div -->
            <%@ include file="image_tabs_data.jsp" %>
            
            </div> <!-- end tabs div -->
            </div>  <!-- end results container -->
            <% } %>
            </td>
        </tr>
    </table>
    </div>
    </td>
   <!-- <td width="172">
    
    </td>   -->
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


</body>
</html>
