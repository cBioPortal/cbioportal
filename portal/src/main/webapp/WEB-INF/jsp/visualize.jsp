<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.cbio.portal.model.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.CaseList" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>
<%@ page import="org.mskcc.cbio.portal.model.Patient" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoGeneticProfile" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.lang.reflect.Array" %>
<%@ page import="static org.mskcc.cbio.portal.servlet.QueryBuilder.INTERNAL_EXTENDED_MUTATION_LIST" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>


<%
    ArrayList<GeneticProfile> profileList =
            (ArrayList<GeneticProfile>) request.getAttribute
                    (QueryBuilder.PROFILE_LIST_INTERNAL);
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute
            (QueryBuilder.GENETIC_PROFILE_IDS);
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();
    double zScoreThreshold = ZScoreUtil.getZScore(geneticProfileIdSet, profileList, request);
    double rppaScoreThreshold = ZScoreUtil.getRPPAScore(request);
    ArrayList<CaseList> caseSets = (ArrayList<CaseList>)
            request.getAttribute(QueryBuilder.CASE_SETS_INTERNAL);
    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String caseIds = xssUtil.getCleanInput(request, QueryBuilder.CASE_IDS);
    ArrayList<CancerStudy> cancerStudies = (ArrayList<CancerStudy>)
            request.getAttribute(QueryBuilder.CANCER_TYPES_INTERNAL);
    String cancerTypeId = (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);


    /**
     * Put together global parameters for injection as javascript variables
     *
     */
    // put geneticProfileIds into the proper form for the JSON request
    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
    geneticProfiles = geneticProfiles.trim();

    String caseIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);

    // get cases
    String cases = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    cases = StringEscapeUtils.escapeJavaScript(cases);

    ProfileData mergedProfile = (ProfileData)
            request.getAttribute(QueryBuilder.MERGED_PROFILE_DATA_INTERNAL);

    String oql = xssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
    ParserOutput theOncoPrintSpecParserOutput = OncoPrintSpecificationDriver.callOncoPrintSpecParserDriver( oql,
            (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS),
            (ArrayList<GeneticProfile>) request.getAttribute(QueryBuilder.PROFILE_LIST_INTERNAL),
            zScoreThreshold, rppaScoreThreshold );

    ArrayList<String> listOfGenes = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();
%>

<script type="text/javascript">
    window.PortalGlobals = {
        getCases: function() { return '<%= cases %>'; },
        getCaseIdsKey: function() { return '<%= caseIdsKey %>'; },
        getOqlString: (function() {
            var oql = '<%=StringEscapeUtils.escapeJavaScript(oql)%>'
                    .replace("&gt;", ">", "gm")
                    .replace("&lt;", "<", "gm")
                    .replace("&eq;", "=", "gm")
                    .replace(/[\r\n]/g, "\\n");

            return function() { return oql; };
        })(),
        getGeneListString: function() { return '<%=StringUtils.join(listOfGenes, " ")%>'},
        getGeneticProfiles: function() { return '<%=geneticProfiles%>'; },
        getZscoreThreshold: function() { return window.zscore_threshold; },
        getRppaScoreThreshold: function() { return window.rppa_score_threshold; }
    };
</script>

<%

    boolean showIGVtab = false;
    String[] cnaTypes = {"_gistic", "_cna", "_consensus", "_rae"};
    for (int lc = 0; lc < cnaTypes.length; lc++) {
        String cnaProfileID = cancerTypeId + cnaTypes[lc];
        if (DaoGeneticProfile.getGeneticProfileByStableId(cnaProfileID) != null){
            showIGVtab = true;
            break;
        }
    }

    OncoPrintSpecification theOncoPrintSpecification = theOncoPrintSpecParserOutput.getTheOncoPrintSpecification();
    ProfileDataSummary dataSummary = new ProfileDataSummary( mergedProfile, theOncoPrintSpecification, zScoreThreshold, rppaScoreThreshold );

    DecimalFormat percentFormat = new DecimalFormat("###,###.#%");
    String geneSetChoice = request.getParameter(QueryBuilder.GENE_SET_CHOICE);
    if (geneSetChoice == null) {
        geneSetChoice = "user-defined-list";
    }
    GeneSetUtil geneSetUtil = GeneSetUtil.getInstance();
    ArrayList<GeneSet> geneSetList = geneSetUtil.getGeneSetList();
    Set<String> warningUnion = (Set<String>) request.getAttribute(QueryBuilder.WARNING_UNION);


    ArrayList <GeneWithScore> geneWithScoreList = dataSummary.getGeneFrequencyList();
    ArrayList<String> mergedCaseList = mergedProfile.getCaseIdList();

    String siteTitle = GlobalProperties.getTitle();
    String bitlyUser = GlobalProperties.getBitlyUser();
    String bitlyKey = GlobalProperties.getBitlyApiKey();

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");

    boolean computeLogOddsRatio = true;

    Boolean mutationDetailLimitReached = (Boolean)
            request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);

    ArrayList <Patient> clinicalDataList = (ArrayList<Patient>)
            request.getAttribute(QueryBuilder.CLINICAL_DATA_LIST);

    boolean rppaExists = countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) > 0;

    boolean has_rppa = countProfiles(profileList, GeneticAlterationType.PROTEIN_ARRAY_PROTEIN_LEVEL) > 0;
    boolean has_mrna = countProfiles(profileList, GeneticAlterationType.MRNA_EXPRESSION) > 0;
    boolean has_methylation = countProfiles(profileList, GeneticAlterationType.METHYLATION) > 0;
    boolean has_copy_no = countProfiles(profileList, GeneticAlterationType.COPY_NUMBER_ALTERATION) > 0;

    boolean includeNetworks = GlobalProperties.includeNetworks();
%>

<%!
    public int countProfiles (ArrayList<GeneticProfile> profileList, GeneticAlterationType type) {
        int counter = 0;
        for (int i = 0; i < profileList.size(); i++) {
            GeneticProfile profile = profileList.get(i);
            if (profile.getGeneticAlterationType() == type) {
                counter++;
            }
        }
        return counter;
    }

    public String getGeneList(ParserOutput oncoPrintSpecParserOutput)
    {
        // translate Onco Query Language
        ArrayList<String> geneList =
                oncoPrintSpecParserOutput.getTheOncoPrintSpecification().listOfGenes();

        String genes = "";

        for(String gene: geneList)
        {
            genes += gene + " ";
        }

        return genes.trim();
    }
%>


<jsp:include page="global/header.jsp" flush="true" />

<%   String smry = "";

    out.println ("<p><div class='gene_set_summary'>Gene Set / Pathway is altered in "
            + percentFormat.format(dataSummary.getPercentCasesAffected())
            + " of all cases.");
    out.println ("<br></div></p>");
    out.println ("<p><small><strong>");

    for (CancerStudy cancerStudy: cancerStudies){
        if (cancerTypeId.equals(cancerStudy.getCancerStudyStableId())){
            smry = smry + cancerStudy.getName();
        }
    }
    for (CaseList caseSet:  caseSets) {
        if (caseSetId.equals(caseSet.getStableId())) {
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

<script type="text/javascript">
    $(document).ready(function(){

        // Init Tool Tips
        $("#toggle_query_form").tipTip();

    });
</script>

<%
    /**
     * Put together parameters for an AJAX call to GeneAlterations.json
     *
     */

    // put gene string into a form that javascript can swallow
    String genes = (String) request.getAttribute(QueryBuilder.RAW_GENE_STR);
    genes = StringEscapeUtils.escapeJavaScript(genes);
//                genes = genes.replace("\n", " ");

    // get cases
    String samples = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
    samples = StringEscapeUtils.escapeJavaScript(samples);
%>

<script type="text/javascript">
    //  make global variables -- TODO move these global variables into a better jsp file

    // raw gene list (as it is entered by the user, it may contain onco query language)
    var genes = "<%=genes%>";

    // gene list after being processed by the onco query language parser
    var geneList = "<%=getGeneList(theOncoPrintSpecParserOutput)%>";

    // list of samples (case ids)
    var samples = "<%=samples%>";

    // genetic profile ids
    var geneticProfiles = "<%=geneticProfiles%>";
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
            <% Boolean showMutTab = false; %>
            <%
                if (geneWithScoreList.size() > 0) {


                    Enumeration paramEnum = request.getParameterNames();
                    StringBuffer buf = new StringBuffer(request.getAttribute
                            (QueryBuilder.ATTRIBUTE_URL_BEFORE_FORWARDING) + "?");
                    
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
                                    currentValue = URLEncoder.encode(currentValue);
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
                    
                    if (rppaExists) {
                        out.println ("<li><a href='#protein_exp' class='result-tab' title='Protein and Phopshoprotein changes using Reverse Phase Protein Array (RPPA) data'>"
                        + "Protein Changes</a></li>");
                    }

                    if (clinicalDataList != null && clinicalDataList.size() > 0) {
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
                    %>

        <%@ include file="image_tabs.jsp" %>

            <%
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

    					//out.println("If you would like to use a <b>shorter URL that will not break in email postings</b>,");
    					//out.println(" we recommend that you copy and paste the URL above into a URL shortening service, ");
    					//out.println("such as <a href='https://bitly.com/'>Bitly</a> or ");
    					//out.println("<a href='http://goo.gl/'>Google</a>.");
                    }
                 	
                    out.println("</div>");
                }

                %>

        <div class="section" id="summary">
            <% //contents of fingerprint.jsp now come from attribute on request object %>
            <%@ include file="oncoprint/main.jsp" %>
            <%@ include file="gene_info.jsp" %>
        </div>
            <%if ( has_mrna && (has_copy_no || has_methylation || has_copy_no) ) { %>

        <%@ include file="plots_tab.jsp" %>
            <%}%>
            <% if (showIGVtab) { %>
        <%@ include file="igv.jsp" %>
            <% } %>

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
        <%@ include file="mutation_views.jsp" %>
        <%@ include file="mutation_details.jsp" %>
            <%  } %>

            <%
            if (rppaExists) { %>
        <%@ include file="protein_exp.jsp" %>
            <% } %>

            <%
            if (includeNetworks) { %>
        <%@ include file="networks.jsp" %>
            <% } %>

        <%@ include file="data_download.jsp" %>

        <%@ include file="image_tabs_data.jsp" %>

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
    // to initially hide the network tab

    //index of network tab
    var networkTabIndex = $('#tabs a[href="#network"]').parent().index();

    if($.cookie(("results-tab-" + (typeof cancer_study_id_selected === 'undefined'? "" : cancer_study_id_selected))) != networkTabIndex){
        $("div.section#network").attr('style', 'display: none !important; height: 0px; width: 0px; visibility: hidden;');
    }

    // to fix problem of flash repainting
    $("a.result-tab").click(function(){

        if($(this).attr("href")=="#network") {
            $("div.section#network").removeAttr('style');
        } else {
            $("div.section#network").attr('style', 'display: block !important; height: 0px; width: 0px; visibility: hidden;');
        }
    });

    //  Set up Tip-Tip Event Handler for Genomic Profiles help
    $(".result-tab").tipTip({defaultPosition: "bottom", delay:"100", edgeOffset: 10, maxWidth: 200});
</script>

</body>
</html>