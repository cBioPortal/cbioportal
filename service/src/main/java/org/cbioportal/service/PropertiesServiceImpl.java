package org.cbioportal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.cbioportal.service.util.MskWholeSlideViewerTokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

// Class adapted from config_service.jsp and GlobalProperties.java

@Service
public class PropertiesServiceImpl implements PropertiesService {

    private Logger LOG = LoggerFactory.getLogger(PropertiesServiceImpl.class);

    String[] propsSentToFrontend = {
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
        "installation_map_url",
        "priority_studies",
        "show.hotspot",
        "show.oncokb",
        "show.civic",
        "show.genomenexus",
        "show.genomenexus.annotation_sources",
        "show.mutation_mappert_tool.grch38",
        "show.transcript_dropdown",
        "show.signal",
        "survival.show_p_q_values_in_survival_type_table",
        "survival.initial_x_axis_limit",
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
        "skin.footer_show_dev",
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
        "skin.right_nav.show_twitter",
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
        "skin.show_settings_menu",
        "skin.hide_logout_button",
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
        "enable_request_body_gzip_compression",
        "query_product_limit",
        "skin.show_gsva",
        "saml.logout.local",
        "skin.citation_rule_text",
        "skin.geneset_hierarchy.default_p_value",
        "skin.geneset_hierarchy.default_gsva_score",
    };

    @Value("${authenticate:false}")
    private String authenticate;
    
    @Value("${oncokb.token:}")
    private String oncoKbToken;
    
    @Value("${session.service.url:}")
    private String sessionServiceURL;

    @Autowired
    private Environment env;

    private static Map<String, String> serverConfigProperties;
    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        serverConfigProperties = Arrays.stream(propsSentToFrontend)
            .collect(
                HashMap::new,
                (out,property) -> {
                    String value = env.getProperty(property);
                    out.put(property, value);
                }, HashMap::putAll
            );
        serverConfigProperties.put("frontendConfigOverride", readFile(env.getProperty("frontend.config")));
        serverConfigProperties.put("query_sets_of_genes", readFile(env.getProperty("querypage.setsofgenes.location")));
        serverConfigProperties.put("authenticationMethod", authenticate);
        serverConfigProperties.put("mskWholeSlideViewerToken", getMskWholeSlideViewerToken());
        serverConfigProperties.put("oncoprintOncoKbHotspotsDefault", enableOncoKBandHotspotsParamValue());
        serverConfigProperties.put("oncoKbTokenDefined", String.valueOf(!oncoKbToken.isEmpty()));
        serverConfigProperties.put("sessionServiceEnabled", String.valueOf(!sessionServiceURL.isEmpty()));
    }

    private String getMskWholeSlideViewerToken() {
        // this token is for the msk portal 
        // the token is generated based on users' timestamp to let the slide viewer know whether the token is expired and then decide whether to allow the user to login the viewer
        // every time when we refresh the page or goto the new page, a new token should be generated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String secretKey = env.getProperty("msk.whole.slide.viewer.secret.key");
        String timeStamp = String.valueOf(System.currentTimeMillis());

        if(authentication != null && authentication.isAuthenticated() && secretKey != null && !secretKey.isEmpty()) {
            return "{ \"token\":\"" + MskWholeSlideViewerTokenGenerator.generateTokenByHmacSHA256(authentication.getName(), secretKey, timeStamp) + "\", \"time\":\"" + timeStamp + "\"}";
        } else {
            return null;
        }
    }

    private String enableOncoKBandHotspots() {
        String enableOncoKBandHotspots = env.getProperty("oncoprint.oncokb_hotspots.default", "true").trim();
        if (enableOncoKBandHotspots.equalsIgnoreCase("custom")) {
            return "custom";
        } else if (enableOncoKBandHotspots.equalsIgnoreCase("false")) {
            return "false";
        }
        return "true";
    }

    private String enableOncoKBandHotspotsParamValue() {
        switch(enableOncoKBandHotspots()){
            case "true":
                return "undefined";
            case "false":
                return  "\"disable\"";
            case "custom":
                return "\"custom\"";
        }
        return null;
    }

    public String getFrontendProperty(String property) {
        // TODO does not work for base_url and user_email_address props
        return serverConfigProperties.get(property);
    }

    public Map<String, String> getFrontendProperties(String baseUrl, String username) {

        serverConfigProperties.put("base_url", baseUrl);
        serverConfigProperties.put("user_email_address", username != null ? username : "anonymousUser");  
    
        return serverConfigProperties;
    }

    public String getFrontendUrl() {
        return parseUrl(env.getProperty("${frontend.url"));
    }

    private String readFile(String fileName)
    {
        String result = new String();

        if (fileName == null || fileName.trim().equals("")) {
            result = null;
        } else {
            try {
                BufferedReader br = new BufferedReader(new FileReader(ResourceUtils.getFile(fileName)));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    line = line.trim();
                    result = result + line;
                }
                br.close();
            } catch (FileNotFoundException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("frontend config file not found: " + e.getMessage());
                }
                return null;
            } catch (IOException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error reading frontend config file: " + e.getMessage());
                }
                return null;
            }
        }
        return result;
    }

    /*
     * Trim whitespace of url and append / if it does not exist. Return empty
     * string otherwise.
     */
    public static String parseUrl(String url)
    {
        String rv;

        if (url != null && !url.isEmpty()) {
            rv = url.trim();

            if (!rv.endsWith("/")) {
                rv += "/";
            }
        } else {
            rv = "";
        }

        return rv;
    }

}
