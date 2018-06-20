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

<!-- Collection of all global variables for the result pages of single cancer study query-->

<%@ include file="selected_study_variables.jsp" %>

<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="org.mskcc.cbio.portal.model.*" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.util.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.mskcc.cbio.portal.servlet.ServletXssUtil" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.CallOncoPrintSpecParser" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.ParserOutput" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.OncoPrintSpecification" %>
<%@ page import="org.mskcc.cbio.portal.oncoPrintSpecLanguage.Utilities" %>
<%@ page import="org.mskcc.cbio.portal.model.CancerStudy" %>
<%@ page import="org.mskcc.cbio.portal.model.SampleList" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticProfile" %>
<%@ page import="org.mskcc.cbio.portal.model.GeneticAlterationType" %>
<%@ page import="org.mskcc.cbio.portal.model.Patient" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoGeneticProfile" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@ page import="java.lang.reflect.Array" %>
<%@ page import="org.mskcc.cbio.portal.util.*" %>
<%@ page import="org.mskcc.cbio.portal.dao.DaoMutation" %>

<%@include file="server_vars.jsp"%>
<%
    Boolean showIGVtab = (Boolean) request.getAttribute("showIGVtab");
    Boolean has_mrna = (Boolean) request.getAttribute("hasMrna");
    Boolean has_methylation = (Boolean) request.getAttribute("hasMethylation");
    Boolean has_copy_no = (Boolean) request.getAttribute("hasCopyNo");
    Boolean has_survival = (Boolean) request.getAttribute("hasSurvival");
    boolean includeNetworks = GlobalProperties.includeNetworks();
    boolean computeLogOddsRatio = true;
    Boolean mutationDetailLimitReached = (Boolean)request.getAttribute(QueryBuilder.MUTATION_DETAIL_LIMIT_REACHED);
    boolean showCoexpTab = false;

    //are we using session service for bookmarking?
    boolean useSessionServiceBookmark = !StringUtils.isBlank(GlobalProperties.getSessionServiceUrl());

    //General site info
    String siteTitle = GlobalProperties.getTitle();

    request.setAttribute(QueryBuilder.HTML_TITLE, siteTitle+"::Results");

    //check if show co-expression tab
    if(!isVirtualStudy){
        GeneticProfile final_gp = CoExpUtil.getPreferedGeneticProfile(StudiesMap.keySet().iterator().next());
        if (final_gp != null) {
            showCoexpTab = true;
        }
    }        
%>

<!--Global Data Objects Manager-->
<script type="text/javascript" src="js/lib/jquery.min.js?<%=GlobalProperties.getAppVersion()%>">
    //needed for data manager
</script>
<script type="text/javascript" src="js/lib/oql/oql-parser.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/api/HotspotSet.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/api/cbioportal-datamanager.js?<%=GlobalProperties.getAppVersion()%>"></script>
<script type="text/javascript" src="js/src/oql/oqlfilter.js?<%=GlobalProperties.getAppVersion()%>"></script>

<!-- Global variables : basic information about the main query -->
<script type="text/javascript">
    
    var patientSampleIdMap = {};
    window.PortalGlobals = {
        setPatientSampleIdMap: function(_patientSampleIdMap) {patientSampleIdMap = _patientSampleIdMap;},
    };

    function setUpQuerySession() {
        var studySampleObj = JSON.parse('<%=studySampleMapJson%>');
        var studyIdsList = Object.keys(studySampleObj);
        window.QuerySession = window.initDatamanager('<%=geneticProfiles%>'.trim().split(/\s+/),
                                                            window.serverVars.theQuery,
							    studyIdsList,
							    studySampleObj,
							    parseFloat('<%=zScoreThreshold%>'),
							    parseFloat('<%=rppaScoreThreshold%>'),
							    {
								case_set_id: '<%=sampleSetId%>',
								case_ids_key: '<%=sampleIdsKey%>',
								case_set_name: '<%=sampleSetName%>',
								case_set_description: '<%=sampleSetDescription%>'
							    }, <%=GlobalProperties.enableDriverAnnotations()%>,
                                                            <%=GlobalProperties.showBinaryCustomDriverAnnotation()%>,
                                                            <%=DaoMutation.hasDriverAnnotations(normalizedCancerStudyIdListStr)%>,
                                                            <%=DaoMutation.numTiers(normalizedCancerStudyIdListStr)%>,
                                                            '<%=GlobalProperties.enableOncoKBandHotspots()%>',
                                                            <%=GlobalProperties.showTiersCustomDriverAnnotation()%>,
                                                            <%=GlobalProperties.enableTiers()%>,
                                                            <%=GlobalProperties.hidePassengerMutations()%>);
    };
    
    var QuerySession_initialized = false
    fireQuerySession = function(){
        if (QuerySession_initialized === false) {
                setUpQuerySession();
                QuerySession_initialized = true;
        }
    }
    


</script>

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
%>
