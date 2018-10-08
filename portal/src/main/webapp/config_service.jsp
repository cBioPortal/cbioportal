<%@ page language="java" contentType="text/javascript; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ page import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>

<%--
legacyConfig = 
{
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
    bitlyToken: '<%=GlobalProperties.getBitlyAccessToken()%>',
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
    skinRightLogo : '<%=GlobalProperties.getRightLogo()%>',
    skinTitle : '<%=GlobalProperties.getTitle()%>',
    
    userEmailAddress : '<%=GlobalProperties.getAuthenticatedUserName()%>',
    
    oncoprintCustomDriverAnnotationBinaryMenuLabel: '<%=GlobalProperties.getBinaryCustomDriverAnnotationMenuLabel()%>',
    oncoprintCustomDriverAnnotationTiersMenuLabel: '<%=GlobalProperties.getTiersCustomDriverAnnotationMenuLabel()%>',
    oncoprintCustomDriverAnnotationDefault:'<%=GlobalProperties.enableDriverAnnotations()%>' !== "false", // true unless "false"
    oncoprintCustomDriverTiersAnnotationDefault:'<%=GlobalProperties.enableTiers()%>' !== "false", // true unless "false"
    oncoprintOncoKbHotspotsDefault:{"true":undefined, "false":"disable", "custom":"custom"}['<%=GlobalProperties.enableOncoKBandHotspots()%>'],
    oncoprintHideVUSDefault:'<%=GlobalProperties.hidePassengerMutations()%>' === "true", // false unless "true"
    priorityStudies : <%=studies%>,  //done
    sessionServiceIsEnabled: '<%=GlobalProperties.getSessionServiceUrl()%>' !== "",
    sessionServiceUrl: '<%=GlobalProperties.getSessionServiceUrl()%>',
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
    
   
};
--%>

// an empty string means the property is listed but not assigned to in portal.props 
// a null value means it was entirely missing 

<%= request.getParameter("callback") %>({

    <%
    
        // Set API root variable for cbioportal-frontend repo
        String url = request.getRequestURL().toString();
        String baseURL = url.substring(0, url.length() - request.getRequestURI().length()) + request.getContextPath();
        baseURL = baseURL.replace("https://", "").replace("http://", "");
    
        String[] propNameArray = {
            "app.version",
            "oncoprint.custom_driver_annotation.binary.menu_label",
            "disabled_tabs",            
            "civic.url",
            "oncoprint.custom_driver_annotation.default",
            "oncoprint.oncokb_hotspots.default",
            "genomenexus.url",
            "google_analytics_profile_id",
            "oncoprint.hide_vus.default",
            "mycancergenome.show",
            "oncokb.public_api.url",
            "digitalslidearchive.iframe.url",
            "digitalslidearchive.meta.url",
            "mdacc.heatmap.meta.url",
            "mdacc.heatmap.patient.url",
            "mdacc.heatmap.study.meta.url",
            "mdacc.heatmap.study.url",
            "oncoprint.custom_driver_annotation.tiers.menu_label",
            "priority_studies",
            "show.hotspot",
            "show.oncokb",
            "show.civic",
            "show.genomenexus",
            "skin.documentation.about",
            "skin.documentation.baseurl",
            "skin.blurb",
            "skin.custom_header_tabs",
            "skin.data_sets_footer",
            "skin.data_sets_header",
            "skin.documentation.markdown",
            "skin.email_contact",
            "skin.example_study_queries",            
            "skin.examples_right_column_html",
            "skin.documentation.faq",
            "skin.footer",
            "skin.login.contact_html",
            "skin.login.saml.registration_html",
            "skin.documentation.news",
            "skin.documentation.oql",
            "skin.query.max_tree_depth",
            "skin.right_logo",
            "skin.right_nav.show_data_sets",
            "skin.right_nav.show_examples",
            "skin.right_nav.show_testimonials",
            "skin.right_nav.whats_new_blurb",
            "skin.show_about_tab",
            "skin.show_data_tab",
            "skin.show_faqs_tab",
            "skin.show_news_tab",
            "skin.show_r_matlab_tab",
            "skin.show_tools_tab",
            "skin.show_tutorials_tab",
            "skin.show_web_api_tab",
            "skin.title",
            "skin.authorization_message",
            "session.service.url",
            "session.url_length_threshold"
           
        }; 
    
        // for each above, add json prop and lookup value in portal.properties
        for (int i = 0; i < propNameArray.length; i++){
            
              String value = GlobalProperties.getProperty(propNameArray[i]);
              
              if (value != null) {
                // do not stringify boolean values
                // need to improve boolean detection or use JSON writing service
                value = String.format("\"%s\"", value.replace("\"","\\\""));
              }
              
              String key = propNameArray[i].replace(".","_");
              
              out.println(String.format("\"%s\" : %s,", key, value));

        }
        
        // these are some custom props which are not read directly from portal.props
        
        out.println(String.format("\"%s\" : %s,", "enable_darwin", CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties()));
            
        out.println(String.format("\"%s\" : %s,", "query_sets_of_genes", GlobalProperties.getQuerySetsOfGenes()));
        
        out.println(String.format("\"%s\" : \"%s\",", "base_url", baseURL)); 
        
        out.println(String.format("\"%s\" : \"%s\",", "user_email_address", GlobalProperties.getAuthenticatedUserName()));     
        
        out.println(String.format("\"%s\" : %s,", "frontendConfigOverride", GlobalProperties.getFrontendConfig()));     
                
       
        String enableOncoKBandHotspots = "";
        switch(GlobalProperties.enableOncoKBandHotspots()){
            case "true":
                enableOncoKBandHotspots = "undefined";
                break;
            case "false":
                enableOncoKBandHotspots = "\"disable\"";
            case "custom":
                enableOncoKBandHotspots = "\"custom\"";
        }
        
        out.println(String.format("\"%s\" : %s,", "oncoprintOncoKbHotspotsDefault", enableOncoKBandHotspots)); 
                
                      
      
     %>

});
