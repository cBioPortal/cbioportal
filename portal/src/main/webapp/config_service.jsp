<%@ page language="java" contentType="application/json; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<% 
// Set API root variable for cbioportal-frontend repo
String url = request.getRequestURL().toString();
String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
baseURL = baseURL.replace("https://", "").replace("http://", "");

String studies = "{";

List<String[]> priorityStudies = GlobalProperties.getPriorityStudies();
for (String[] group : priorityStudies) {
    if (group.length > 1) {
        studies += "\""+group[0]+"\" : ";
        studies += "[";
        int i = 1;
        while (i < group.length) {
            if (i >= 2) {
                studies += ",";
            }
            studies += "\""+group[i]+"\"";
            i++;
        }
        studies += "],";
        
    }
}
studies += "}";

%>

<%= request.getParameter("callback") %>({
    enableDarwin : <%=CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()%>,
    appVersion : '<%=GlobalProperties.getAppVersion()%>',
    maxTreeDepth : <%=GlobalProperties.getMaxTreeDepth()%>,
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
    userEmailAddress : '<%=GlobalProperties.getAuthenticatedUserName()%>',
    oncoprintCustomDriverAnnotationBinaryMenuLabel: '<%=GlobalProperties.getBinaryCustomDriverAnnotationMenuLabel()%>',
    oncoprintCustomDriverAnnotationTiersMenuLabel: '<%=GlobalProperties.getTiersCustomDriverAnnotationMenuLabel()%>',
    oncoprintCustomDriverAnnotationDefault:'<%=GlobalProperties.enableDriverAnnotations()%>' !== "false", // true unless "false"
    oncoprintCustomDriverTiersAnnotationDefault:'<%=GlobalProperties.enableTiers()%>' !== "false", // true unless "false"
    oncoprintOncoKbHotspotsDefault:{"true":undefined, "false":"disable", "custom":"custom"}['<%=GlobalProperties.enableOncoKBandHotspots()%>'],
    oncoprintHideVUSDefault:'<%=GlobalProperties.hidePassengerMutations()%>' === "true", // false unless "true"
    priorityStudies : <%=studies%>,
    sessionServiceIsEnabled: '<%=GlobalProperties.getSessionServiceUrl()%>' !== "",
    baseUrl:"<%=baseURL%>",
    fontendCongfigOveride:JSON.parse('<%=GlobalProperties.getFrontendConfig()%>'),
    skinShowDataTabSets: (<%=GlobalProperties.showDataTab()%> === true),
    skinShowNewsTab: (<%=GlobalProperties.showNewsTab()%> === true),
    skinShowFAQSTab: (<%=GlobalProperties.showFaqsTab()%> === true),
    skinShowToolsTab: (<%=GlobalProperties.showToolsTab()%> === true),
    skinShowAboutTab: (<%=GlobalProperties.showAboutTab()%> === true),
    skinShowWebAPITab: (<%=GlobalProperties.showWebApiTab()%> === true),
    skinShowRmatLABTab : (<%=GlobalProperties.showRMatlabTab()%> === true),
    skinShowTutorialsTab: (<%=GlobalProperties.showTutorialsTab()%> === true),
    emailContactAddress: "<%=GlobalProperties.getContactEmailAddress()%>",
    skinDocumentationBaseUrl: "<%=GlobalProperties.getBaseUrl()%>",
    skinIsMarkdownDocumentation: (<%=GlobalProperties.isMarkdownDocumentation()%> === true),
    skinFaqSourceURL: "<%=GlobalProperties.getFaqHtml()%>",
    skinAboutSourceURL: "<%=GlobalProperties.getAboutHtml()%>",
    skinNewsSourceURL: "<%=GlobalProperties.getNewsHtml()%>",
    skinOQLSourceURL: "<%=GlobalProperties.getOqlHtml()%>",
    googleAnalyticsProfile: "<%=GlobalProperties.getGoogleAnalyticsProfileId()%>",
  
    
  
});

