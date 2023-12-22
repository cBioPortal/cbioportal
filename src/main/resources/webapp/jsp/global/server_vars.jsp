<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="org.mskcc.cbio.portal.util.XssRequestWrapper" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%
    //Security Instance
    ServletXssUtil xssUtil = ServletXssUtil.getInstance();

    //Info about Genetic Profiles
    HashSet<String> geneticProfileIdSet = (HashSet<String>) request.getAttribute(QueryBuilder.GENETIC_PROFILE_IDS);
    String geneticProfiles = StringUtils.join(geneticProfileIdSet.iterator(), " ");
    geneticProfiles = xssUtil.getCleanerInput(geneticProfiles.trim());

    //Info about threshold settings
    String zScoreThreshold = String.valueOf(request.getAttribute(QueryBuilder.Z_SCORE_THRESHOLD));
    String rppaScoreThreshold = String.valueOf(request.getAttribute(QueryBuilder.RPPA_SCORE_THRESHOLD));

    //Onco Query Language Parser Instance
    String oql = request.getParameter(QueryBuilder.GENE_LIST);
    if (request instanceof XssRequestWrapper) {
        oql = ((XssRequestWrapper)request).getRawParameter(QueryBuilder.GENE_LIST);
    }
    oql = xssUtil.getCleanerInput(oql);

    // List of queried gene sets
    String genesetIds = request.getParameter(QueryBuilder.GENESET_LIST);

    String studySampleMapJson = (String)request.getAttribute("STUDY_SAMPLE_MAP");
    String sampleSetId = (String) request.getAttribute(QueryBuilder.CASE_SET_ID);
    String sampleSetName = request.getAttribute("case_set_name") != null ? (String) request.getAttribute("case_set_name") : "User-defined Patient List";
    String sampleSetDescription = request.getAttribute("case_set_description") != null ? (String) request.getAttribute("case_set_description") : "User-defined Patient List.";
    String sampleIdsKey = request.getAttribute(QueryBuilder.CASE_IDS_KEY) != null ? (String) request.getAttribute(QueryBuilder.CASE_IDS_KEY) : "";
    
    String caseIds = (String) request.getAttribute(QueryBuilder.CASE_IDS);
    if (request.getAttribute(QueryBuilder.CASE_IDS) != null) {
        caseIds = caseIds.replace("\n","+");
    }

    sampleSetName = sampleSetName.replaceAll("'", "\\'");
    sampleSetName = sampleSetName.replaceAll("\"", "\\\"");
    
    Integer dataPriority = (Integer) request.getAttribute(QueryBuilder.DATA_PRIORITY);
%>

<script type="text/javascript">
    function jspToJs(val, processFn) {
        processFn = processFn || function(x){ return x; };
        if (val === "null") {
            return undefined;
        } else {
            return processFn(val);
        }
    }
    
    var molecularProfiles = '<%=geneticProfiles%>'.trim();
    if (molecularProfiles.length) {
        molecularProfiles = molecularProfiles.split(/\s+/);
    } else {
        molecularProfiles = undefined;
    }
    
    var oql_html_conversion_vessel = document.createElement("div");
    oql_html_conversion_vessel.innerHTML = '<%=oql%>'.trim();
    var html_decoded_oql = oql_html_conversion_vessel.textContent.trim();
    var uri_and_html_decoded_oql = decodeURIComponent(html_decoded_oql);
    
    window.serverVars = {
    
        molecularProfiles : molecularProfiles,
        caseSetProperties :  {
                case_set_id: jspToJs('<%=sampleSetId%>'),
                case_ids_key: jspToJs('<%=sampleIdsKey%>'),
                case_set_name: jspToJs('<%=sampleSetName%>'),
                case_set_description: jspToJs('<%=sampleSetDescription%>')
            },
            
        zScoreThreshold:jspToJs('<%=zScoreThreshold%>', parseFloat),
        rppaScoreThreshold:jspToJs('<%=rppaScoreThreshold%>', parseFloat),
        dataPriority:jspToJs('<%=dataPriority%>', function(d) { return parseInt(d, 10); }),
        
        theQuery: decodeURIComponent(jspToJs(uri_and_html_decoded_oql) || ""), 
        genesetIds: jspToJs('<%=genesetIds%>'.trim()) || "",
        studySampleObj: jspToJs('<%=studySampleMapJson%>', JSON.parse),
       	caseIds: jspToJs('<%=caseIds%>') || ""
    };
    
    // yes "null" will be string
    if (window.cancerStudyIdList && window.cancerStudyIdList !== "null") { 
        window.serverVars.cohortIdsList = cancerStudyIdList.split(',');
    } else if (window.cancerStudyId) {
         window.serverVars.cohortIdsList = [cancerStudyId];
    } 

    if (window.serverVars.studySampleObj) {
        window.serverVars.studySampleListMap = (function(){
                                          var ret = {};
                                          ret[Object.keys(window.serverVars.studySampleObj)[0]] = window.serverVars.caseSetProperties.case_set_id;
                                          return ret;
                                      })();
        window.serverVars.cancerStudies = Object.keys(window.serverVars.studySampleObj);
    }
</script>
