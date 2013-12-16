<%@ page import="org.mskcc.cbio.portal.servlet.*" %>
<%@ page import="org.mskcc.cbio.portal.util.XssRequestWrapper" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.apache.commons.lang.*" %>

<%
    org.mskcc.cbio.portal.servlet.ServletXssUtil localXssUtil = ServletXssUtil.getInstance();
    String localCancerTypeId =
		    (String) request.getAttribute(QueryBuilder.CANCER_STUDY_ID);
    String localCaseSetId =
		    (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    HashSet<String> localGeneticProfileIdSet = (HashSet<String>) request.getAttribute
            (QueryBuilder.GENETIC_PROFILE_IDS);
    String localCaseIds = request.getParameter(QueryBuilder.CASE_IDS);
	//String localGeneList = localXssUtil.getCleanInput(request, QueryBuilder.GENE_LIST);
	String localGeneList = request.getParameter(QueryBuilder.GENE_LIST);

	if (request instanceof XssRequestWrapper)
	{
		localGeneList = localXssUtil.getCleanInput(
			((XssRequestWrapper)request).getRawParameter(QueryBuilder.GENE_LIST));
	}
    
    String localTabIndex = request.getParameter(QueryBuilder.TAB_INDEX);
    String localzScoreThreshold = request.getParameter(QueryBuilder.Z_SCORE_THRESHOLD);
    if (localzScoreThreshold == null) {
        localzScoreThreshold = "2.0";
    }
    String localRppaScoreThreshold = request.getParameter(QueryBuilder.RPPA_SCORE_THRESHOLD);
    if (localRppaScoreThreshold == null) {
        localRppaScoreThreshold = "2.0";
    }
    if (localTabIndex == null) {
        localTabIndex = QueryBuilder.TAB_VISUALIZE;
    } else {
        localTabIndex = URLEncoder.encode(localTabIndex);
    }

    String localGeneSetChoice = request.getParameter(QueryBuilder.GENE_SET_CHOICE);
    //String clientTranspose = localXssUtil.getCleanInput(request, QueryBuilder.CLIENT_TRANSPOSE_MATRIX);
	String clientTranspose = request.getParameter(QueryBuilder.CLIENT_TRANSPOSE_MATRIX);
    if (localGeneSetChoice == null) {
        localGeneSetChoice = "user-defined-list";
    }
%>

<%
    /**
     * Put together global parameters
     *
     */
//    HashSet<String> geneticProfileIdSet =
//            (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS);

    // put geneticProfileIds into the proper form for the JSON request
//    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute
//            (QueryBuilder.GENETIC_PROFILE_IDS);
//    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
//    geneticProfiles = geneticProfiles.trim();
//
//    // put gene string into a form that javascript can swallow
//    String genes = (String) request.getAttribute(QueryBuilder.RAW_GENE_STR);
//    genes = StringEscapeUtils.escapeJavaScript(genes);
//
//    // get cases
//    String cases = (String) request.getAttribute(QueryBuilder.SET_OF_CASE_IDS);
//    cases = StringEscapeUtils.escapeJavaScript(cases);
//
//    String caseSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
//    String caseIdsKey = (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY);
%>


<script type="text/javascript">
    // Store the currently selected options as global variables;
    window.cancer_study_id_selected = '<%= localCancerTypeId%>';
    window.case_set_id_selected = '<%= localCaseSetId %>';
    window.gene_set_id_selected = '<%= localGeneSetChoice %>';
    window.tab_index = '<%= localTabIndex %>';
    window.zscore_threshold = '<%= localzScoreThreshold %>';
    window.rppa_score_threshold = '<%= localRppaScoreThreshold %>';

    //  Store the currently selected genomic profiles within an associative array
    window.genomic_profile_id_selected = new Array();
    <%
        if (localGeneticProfileIdSet != null) {
            for (String geneticProfileId:  localGeneticProfileIdSet) {
                geneticProfileId = localXssUtil.getCleanerInput(geneticProfileId);
                out.println ("window.genomic_profile_id_selected['" + geneticProfileId + "']=1;");
            }
        }
    %>
</script>
<div class="main_query_panel">
    <div id="main_query_form">
        <form id="main_form" action="index.do" method="post">
        <%@ include file="step1_json.jsp" %>
        <%@ include file="step2_json.jsp" %>
        <%@ include file="step3_json.jsp" %>
        <%@ include file="step4_json.jsp" %>
        <%@ include file="step5_json.jsp" %>
        <input type="hidden" id="clinical_param_selection" name="clinical_param_selection"
        	value='<%= request.getParameter("clinical_param_selection") %>'>
        <input type="hidden" id="<%= QueryBuilder.TAB_INDEX %>" name="<%= QueryBuilder.TAB_INDEX %>"
           value="<%= localTabIndex %>">
        <p/>
        <% conditionallyOutputTransposeMatrixOption (localTabIndex, clientTranspose, out); %>
        &nbsp;<br/>
        <input id="main_submit" class="ui-button ui-widget ui-state-default ui-corner-all" style="height: 34px;"
                   type=submit name="<%= QueryBuilder.ACTION_NAME%>" value="<%= QueryBuilder.ACTION_SUBMIT %>"/>
        </form>
    </div>
</div>

<%!
    private void conditionallyOutputTransposeMatrixOption(String localTabIndex,
            String clientTranspose, JspWriter out)
            throws IOException {
        if (localTabIndex.equals(QueryBuilder.TAB_DOWNLOAD)) {
            outputTransposeMatrixOption(clientTranspose, out);
        }
    }

    private void outputTransposeMatrixOption(String clientTranspose, JspWriter out) throws IOException {
        String checked = hasUserSelectedTheTransposeOption(clientTranspose);
        out.println ("&nbsp;Clicking submit will generate a tab-delimited file "
            + " containing your requested data.");
        out.println ("<p/>");
        out.println ("<input id='client_transpose_matrix' type=\"checkbox\" "+ checked + " name=\""
                + QueryBuilder.CLIENT_TRANSPOSE_MATRIX
                + "\"/> Transpose data matrix.");
        out.println ("<p/>");
    }

    private String hasUserSelectedTheTransposeOption(String clientTranspose) {
        if (clientTranspose != null) {
            return "checked";
        } else {
            return "";
        }
    }
%>
