<%@ page import="java.util.List" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<script type="text/javascript">

window.legacySupportFrontendConfig = {
    enableDarwin : <%=CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()%>,
    appVersion : '<%=GlobalProperties.getAppVersion()%>',
    maxTreeDepth : '<%=GlobalProperties.getMaxTreeDepth()%>',
    showOncoKB : <%=GlobalProperties.showOncoKB()%>,
    oncoKBApiUrl : '<%=GlobalProperties.getOncoKBPublicApiUrl()%>',
    genomeNexusApiUrl : '<%=GlobalProperties.getGenomeNexusApiUrl()%>',
    showCivic : <%=GlobalProperties.showCivic()%>,
    showHotspot : <%=GlobalProperties.showHotspot()%>,
    showMyCancerGenome : <%=GlobalProperties.showMyCancerGenomeUrl()%>,
    showGenomeNexus : <%=GlobalProperties.showGenomeNexus()%>,
    querySetsOfGenes : JSON.parse('<%=GlobalProperties.getQuerySetsOfGenes()%>'),
    skinBlurb : '<%=GlobalProperties.getBlurb()%>',
    skinExampleStudyQueries : '<%=GlobalProperties.getExampleStudyQueries().replace("\n","\\n")%>'.split("\n"),
    skinDatasetHeader : '<%=GlobalProperties.getDataSetsHeader()%>',
    skinDatasetFooter : '<%=GlobalProperties.getDataSetsFooter()%>',
    skinRightNavShowDatasets : <%=GlobalProperties.showRightNavDataSets()%>,
    skinRightNavShowExamples : <%=GlobalProperties.showRightNavExamples()%>,
    skinRightNavShowTestimonials : <%=GlobalProperties.showRightNavTestimonials()%>,
    skinRightNavExamplesHTML : '<%=GlobalProperties.getExamplesRightColumnHtml()%>',
    skinRightNavExamplesHTML : '<%=GlobalProperties.getExamplesRightColumnHtml()%>',
    skinRightNavWhatsNewBlurb : '<%=GlobalProperties.getRightNavWhatsNewBlurb()%>',
    priorityStudies : {}
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

window.frontendConfig = JSON.parse('<%=GlobalProperties.getFrontendConfig()%>');
if (window.frontendConfig) {
    otherFrontendVariables = 'frontendUrl enableDarwin appVersion maxTreeDepth showOncoKB'
    for (var prop in legacySupportFrontendConfig) {
        // use old property if none is defined in frontendConfig
        if (!window.frontendConfig.hasOwnProperty(prop)) {
            window.frontendConfig[legacySupportFrontendConfig[prop]];
        }
    }
} else {
    window.frontendConfig = window.legacySupportFrontendConfig;
}

// frontend config that can't be changed by deployer
window.frontendUrl = '<%=GlobalProperties.getFrontendUrl()%>',
window.frontendConfig.apiRoot = '<%=baseURL%>';
window.frontendConfig.historyType = 'memory';
</script>
