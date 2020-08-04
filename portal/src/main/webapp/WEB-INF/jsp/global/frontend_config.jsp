<%@ page import="java.util.List" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<script type="text/javascript">

window.legacySupportFrontendConfig = {
    enableDarwin : <%=CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()%>,
    appVersion : '<%=GlobalProperties.getAppVersion()%>',
    maxTreeDepth : <%=GlobalProperties.getMaxTreeDepth()%>,
    showOncoKB : <%=GlobalProperties.showOncoKB()%>,
    genomeNexusApiUrl : '<%=GlobalProperties.getGenomeNexusApiUrl()%>',
    genomeNexusGrch38ApiUrl : '<%=GlobalProperties.getGenomeNexusGrch38ApiUrl()%>',
    showCivic : <%=GlobalProperties.showCivic()%>,
    showHotspot : <%=GlobalProperties.showHotspot()%>,
    showMyCancerGenome : <%=GlobalProperties.showMyCancerGenomeUrl()%>,
    showTranscriptDropdown : <%=GlobalProperties.showTranscriptDropdown()%>,
    showGenomeNexus : <%=GlobalProperties.showGenomeNexus()%>,
    showMutationMapperToolGrch38 : <%=GlobalProperties.showMutationMapperToolGrch38()%>,
    querySetsOfGenes : JSON.parse('<%=GlobalProperties.getQuerySetsOfGenes()%>'),
    skinBlurb : '<%=GlobalProperties.getBlurb()%>',
    skinExampleStudyQueries : '<%=GlobalProperties.getExampleStudyQueries().replace("\n","\\n")%>'.split("\n"),
    skinDatasetHeader : '<%=GlobalProperties.getDataSetsHeader()%>',
    skinDatasetFooter : '<%=GlobalProperties.getDataSetsFooter()%>',
    skinRightNavShowDatasets : <%=GlobalProperties.showRightNavDataSets()%>,
    skinRightNavShowExamples : <%=GlobalProperties.showRightNavExamples()%>,
    skinRightNavShowTestimonials : <%=GlobalProperties.showRightNavTestimonials()%>,
    skinRightNavShowWhatsNew : <%=GlobalProperties.showRightNavWhatsNew()%>,
    skinRightNavExamplesHTML : '<%=GlobalProperties.getExamplesRightColumnHtml()%>',
    skinRightNavExamplesHTML : '<%=GlobalProperties.getExamplesRightColumnHtml()%>',
    skinRightNavWhatsNewBlurb : '<%=GlobalProperties.getRightNavWhatsNewBlurb()%>',
    userEmailAddress : '<%=GlobalProperties.getAuthenticatedUserName()%>',
    oncoprintCustomDriverAnnotationBinaryMenuLabel: '<%=GlobalProperties.getBinaryCustomDriverAnnotationMenuLabel()%>',
    oncoprintCustomDriverAnnotationTiersMenuLabel: '<%=GlobalProperties.getTiersCustomDriverAnnotationMenuLabel()%>',
    oncoprintCustomDriverAnnotationDefault:'<%=GlobalProperties.enableDriverAnnotations()%>' !== "false", // true unless "false"
    oncoprintCustomDriverTiersAnnotationDefault:'<%=GlobalProperties.enableTiers()%>' !== "false", // true unless "false"
    oncoprintOncoKbHotspotsDefault:{"true":undefined, "false":"disable", "custom":"custom"}['<%=GlobalProperties.enableOncoKBandHotspots()%>'],
    oncoprintHideVUSDefault:'<%=GlobalProperties.hidePassengerMutations()%>' === "true", // false unless "true"
    priorityStudies : {},
    sessionServiceIsEnabled: '<%=GlobalProperties.getSessionServiceUrl()%>' !== ""
}

// this prevents react router from messing with hash in a way that could is unecessary (static pages)
// or could conflict
// Prioritized studies for study selector
<%
List<String[]> priorityStudies = GlobalProperties.getPriorityStudies();
for (String[] group : priorityStudies) {
    if (group.length > 1) {
        out.println("window.legacySupportFrontendConfig.priorityStudies['"+group[0]+"'] = ");
        out.println("[");
        int i = 1;
        while (i < group.length) {
            if (i >= 2) {
                out.println(",");
            }
            out.println("'"+group[i]+"'");
            i++;
        }
        out.println("];");
    }
}

// Set API root variable for cbioportal-frontend repo
String url = request.getRequestURL().toString();
String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
baseURL = baseURL.replace("https://", "").replace("http://", "");
%>

function assignQuickDirty(obj1, obj2){
    for (var k in obj2) {
        obj1[k] = obj2[k];
    }
    return obj1;
}

// override legacySupportFrontendConfig with new frontendConfig
window.frontendConfig = JSON.parse('<%=GlobalProperties.getFrontendConfig()%>' || "{}");
//window.frontendConfig = Object.assign(window.legacySupportFrontendConfig, window.frontendConfig);
window.frontendConfig = assignQuickDirty(window.legacySupportFrontendConfig, window.frontendConfig);



// get localdev from url and set localStorage accordingly. Setting localStorage
// is necessary when changing page (e.g. from query to study view)
//var url = new URL(window.location.href);
if (/localdev=true/.test(window.location.href)) {
	localStorage.setItem("localdev", "true");
}
window.localdev = localStorage.getItem("localdev") === "true";
// localdist (instead of using npm run start, one serves artifacts from dist
// folder (more production like env)
if (/localdist=true/.test(window.location.href)) {
	localStorage.setItem("localdist", "true");
}
window.localdist = localStorage.getItem("localdist") === "true";

if (window.localdist || window.localdev) {
	window.frontendConfig.frontendUrl = '//localhost:3000/'
} else if (localStorage.netlify) {
	var netlifyInstance = '//' + localStorage.getItem('netlify') + '.netlify.app/';
	window.frontendConfig.frontendUrl = netlifyInstance;
}
// clean userEmailAddress config
if (!window.frontendConfig.userEmailAddress || window.frontendConfig.userEmailAddress === 'anonymousUser') {
    window.frontendConfig.userEmailAddress = '';
}
window.frontendConfig.frontendUrl = window.frontendConfig.frontendUrl? window.frontendConfig.frontendUrl : '<%=GlobalProperties.getFrontendUrl()%>';
window.frontendConfig.apiRoot = window.frontendConfig.apiRoot? window.frontendConfig.apiRoot : '<%=baseURL%>';
// pass baseUrl explicitly for frontend routing, in case somebody wants to
// override apiRoot for some other reason
window.frontendConfig.baseUrl = window.frontendConfig.baseUrl? window.frontendConfig.baseUrl : '<%=baseURL%>';
// default, override on per page bases, set to hash if full react page
window.frontendConfig.historyType = window.frontendConfig.historyType? window.frontendConfig.historyType : 'memory'; 
</script>
