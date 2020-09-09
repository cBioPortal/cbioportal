<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.mskcc.cbio.portal.servlet.CheckDarwinAccessServlet" %>
<%@ page import="org.mskcc.cbio.portal.util.GlobalProperties" %>
<%@ page import="org.json.simple.JSONArray" %>
<%@ page import="org.json.simple.JSONObject" %>

// an empty string means the property is listed but not assigned to in portal.props
// a null value means it was entirely missing

// this jsp can either be included in index.jsp or called as a jsonp service (by decoupled frontends not using jsps)
// if there is a callback url param, we respond as jsonp callback(data), otherwise, we assign to variable

<%
    String callback = request.getParameter("callback");

    // this is to avoid CORB blocking when jsonp is called across domain
    if (callback != null) {
        response.setContentType("text/javascript; charset=UTF-8");
    }

%>

<%= (callback==null ? "window.rawServerConfig = " : "callback" ) %>(

    <%
        // Set API root variable for cbioportal-frontend repo
        String currentUrl = request.getRequestURL().toString();
        String baseURL = currentUrl.substring(0, currentUrl.length() - request.getRequestURI().length()) + request.getContextPath();
        baseURL = baseURL.replace("https://", "").replace("http://", "");

        String[] propNameArray = {
            "app.version",
            "app.name",
            "dat.method",
            "oncoprint.custom_driver_annotation.binary.menu_label",
            "disabled_tabs",
            "civic.url",
            "oncoprint.custom_driver_annotation.binary.default",
            "oncoprint.oncokb.default",
            "oncoprint.hotspots.default",
            "genomenexus.url",
            "genomenexus.url.grch38",
            "google_analytics_profile_id",
            "analytics_report_url",
            "oncoprint.hide_vus.default",
            "mycancergenome.show",
            "oncokb.public_api.url",
            "digitalslidearchive.iframe.url",
            "digitalslidearchive.meta.url",
            "mdacc.heatmap.meta.url",
            "mdacc.heatmap.patient.url",
            "mdacc.heatmap.study.meta.url",
            "mdacc.heatmap.study.url",
            "show.mdacc.heatmap",
            "oncoprint.custom_driver_annotation.tiers.menu_label",
            "patient_view.use_legacy_timeline",
            "priority_studies",
            "show.hotspot",
            "show.oncokb",
            "show.civic",
            "show.genomenexus",
            "show.genomenexus.annotation_sources",
            "show.mutation_mappert_tool.grch38",
            "show.transcript_dropdown",
            "skin.documentation.about",
            "skin.documentation.baseurl",
            "skin.example_study_queries",
            "skin.blurb",
            "skin.custom_header_tabs",
            "skin.data_sets_footer",
            "skin.data_sets_header",
            "skin.documentation.markdown",
            "skin.email_contact",
            "skin.examples_right_column_html",
            "skin.documentation.faq",
            "skin.footer",
            "skin.login.contact_html",
            "skin.login.saml.registration_html",
            "skin.documentation.news",
            "skin.documentation.oql",
            "skin.query.max_tree_depth",
            "skin.quick_select_buttons",
            "skin.right_logo",
            "skin.right_nav.show_data_sets",
            "skin.right_nav.show_examples",
            "skin.right_nav.show_testimonials",
            "skin.right_nav.show_whats_new",
            "skin.right_nav.whats_new_blurb",
            "skin.show_about_tab",
            "skin.show_data_tab",
            "skin.show_faqs_tab",
            "skin.show_news_tab",
            "skin.show_r_matlab_tab",
            "skin.show_tools_tab",
            "skin.show_tutorials_tab",
            "skin.show_web_api_tab",
            "skin.show_tweet_button",
            "skin.patientview.filter_genes_profiled_all_samples",
            "skin.patientview.show_mskcc_slide_viewer",
            "quick_search.enabled",
            "default_cross_cancer_study_session_id",
            "default_cross_cancer_study_list",
            "default_cross_cancer_study_list_name",
            "skin.description",
            "skin.title",
            "skin.authorization_message",
            "session.url_length_threshold",
            "bitly.api_key",
            "bitly.user",
            "bitly.access.token",
            "oncoprint.custom_driver_annotation.tiers.default",
            "ensembl.transcript_url",
            "enable_persistent_cache",
            "query_product_limit",
            "skin.show_gsva",
            "saml.logout.local",
            "skin.citation_rule_text"
        };


        JSONObject obj = new JSONObject();

        // for each above, add json prop and lookup value in portal.properties
        for (int i = 0; i < propNameArray.length; i++){

              String value = GlobalProperties.getProperty(propNameArray[i]);

              String key = propNameArray[i].replace(".","_");

              // booleans should be non-string in json
              if ("true".equals(value) || "false".equals(value)) {
                 obj.put(key, Boolean.parseBoolean(value));
              } else {
                 obj.put(key, value);
              }

        }

        // these are some custom props which are not read directly from portal.props
        obj.put("enable_darwin", CheckDarwinAccessServlet.CheckDarwinAccess.existsDarwinProperties());

        obj.put("query_sets_of_genes", GlobalProperties.getQuerySetsOfGenes());

        obj.put("base_url", baseURL);

        obj.put("user_email_address",GlobalProperties.getAuthenticatedUserName());

        obj.put("frontendConfigOverride",GlobalProperties.getFrontendConfig());

        obj.put("authenticationMethod",GlobalProperties.authenticationMethod());

        obj.put("mskWholeSlideViewerToken", GlobalProperties.getMskWholeSlideViewerToken());

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

        obj.put("oncoprintOncoKbHotspotsDefault",enableOncoKBandHotspots);

        obj.put("oncoKbTokenDefined", !StringUtils.isEmpty(GlobalProperties.getOncoKbToken()));

        obj.put("sessionServiceEnabled", !StringUtils.isEmpty(GlobalProperties.getSessionServiceUrl()));

        out.println(obj.toJSONString());


     %>

);

