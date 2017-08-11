<%@ page import="java.util.List" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<script type="text/javascript">

window.frontendUrl = '<%=GlobalProperties.getFrontendUrl()%>';
window.enableDarwin = <%=CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()%>;
window.appVersion = '<%=GlobalProperties.getAppVersion()%>';
window.maxTreeDepth = '<%=GlobalProperties.getMaxTreeDepth()%>';
window.showOncoKB = <%=GlobalProperties.showOncoKB()%>;
window.oncoKBApiUrl = '<%=GlobalProperties.getOncoKBPublicApiUrl()%>';
window.showCivic = <%=GlobalProperties.showCivic()%>;
window.showHotspot = <%=GlobalProperties.showHotspot()%>;
window.showMyCancerGenome = <%=GlobalProperties.showMyCancerGenomeUrl()%>;

// this prevents react router from messing with hash in a way that could is unecessary (static pages)
// or could conflict
window.historyType = 'memory';

window.skinBlurb = '<%=GlobalProperties.getBlurb()%>';
window.skinExampleStudyQueries = '<%=GlobalProperties.getExampleStudyQueries().replace("\n","\\n")%>'.split("\n");
window.skinDatasetHeader = '<%=GlobalProperties.getDataSetsHeader()%>';
window.skinDatasetFooter = '<%=GlobalProperties.getDataSetsFooter()%>';
window.skinRightNavShowDatasets = <%=GlobalProperties.showRightNavDataSets()%>;
window.skinRightNavShowExamples = <%=GlobalProperties.showRightNavExamples()%>;
window.skinRightNavShowTestimonials = <%=GlobalProperties.showRightNavTestimonials()%>;
window.skinRightNavExamplesHTML = '<%=GlobalProperties.getExamplesRightColumnHtml()%>';
// Prioritized studies for study selector
window.priorityStudies = {};
<%
List<String[]> priorityStudies = GlobalProperties.getPriorityStudies();
for (String[] group : priorityStudies) {
    if (group.length > 1) {
        out.println("window.priorityStudies['"+group[0]+"'] = ");
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
__API_ROOT__ = '<%=baseURL%>';
</script>
