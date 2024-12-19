package org.cbioportal.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


// Class adapted from legacy config_service.jsp and GlobalProperties.java
@Service
public class FrontendPropertiesServiceImpl implements FrontendPropertiesService {

    private final Logger log = LoggerFactory.getLogger(FrontendPropertiesServiceImpl.class);

    // This enum holds properties exposed to the frontend reactapp.
    // Format: <name of property known in frontend>("<name of property in properties file>", "<default value>")
    // There are some properties that require processing before being exposed to the frontend.
    public enum FrontendProperty {
        dat_method("dat.method", null),
        oncoprint_custom_driver_annotation_binary_menu_label(
            "oncoprint.custom_driver_annotation.binary.menu_label", null),
        oncoprint_custom_driver_annotation_binary_menu_description(
            "oncoprint.custom_driver_annotation.binary.menu_description", null),
        disabled_tabs("disabled_tabs", null),
        civic_url("civic.url", null),
        oncoprint_custom_driver_annotation_binary_default(
            "oncoprint.custom_driver_annotation.binary.default", null),
        oncoprint_oncokb_default("oncoprint.oncokb.default", null),
        oncoprint_hotspots_default("oncoprint.hotspots.default", null),
        oncoprint_clustered_default("oncoprint.clustered.default", null),
        oncokb_merge_icons_by_default("oncokb.merge_icons_by_default", null),
        oncoprint_clinical_tracks_config_json("oncoprint.clinical_tracks.config_json", null),
        genomenexus_url("genomenexus.url", null),
        genomenexus_url_grch38("genomenexus.url.grch38", null),
        genomenexus_isoform_override_source("genomenexus.isoform_override_source", null),
        google_analytics_profile_id("google_analytics_profile_id", null),
        analytics_report_url("analytics_report_url", null),
        oncoprint_hide_vus_default("oncoprint.hide_vus.default", null),
        mycancergenome_show("mycancergenome.show", null),
        oncokb_public_api_url("oncokb.public_api.url", null),
        digitalslidearchive_iframe_url("digitalslidearchive.iframe.url", null),
        digitalslidearchive_meta_url("digitalslidearchive.meta.url", null),
        mdacc_heatmap_meta_url("mdacc.heatmap.meta.url", null),
        mdacc_heatmap_patient_url("mdacc.heatmap.patient.url", null),
        mdacc_heatmap_study_meta_url("mdacc.heatmap.study.meta.url", null),
        mdacc_heatmap_study_url("mdacc.heatmap.study.url", null),
        show_mdacc_heatmap("show.mdacc.heatmap", null),
        oncoprint_custom_driver_annotation_tiers_menu_label(
            "oncoprint.custom_driver_annotation.tiers.menu_label", null),
        oncoprint_custom_driver_annotation_tiers_menu_description(
            "oncoprint.custom_driver_annotation.tiers.menu_description", null),
        patient_view_use_legacy_timeline("patient_view.use_legacy_timeline", null),
        installation_map_url("installation_map_url", null),
        priority_studies("priority_studies", null),
        show_hotspot("show.hotspot", null),
        show_oncokb("show.oncokb", null),
        show_civic("show.civic", null),
        show_genomenexus("show.genomenexus", null),
        show_genomenexus_annotation_sources("show.genomenexus.annotation_sources", null),
        show_mutation_mappert_tool_grch38("show.mutation_mappert_tool.grch38", null),
        show_transcript_dropdown("show.transcript_dropdown", null),
        show_signal("show.signal", null),
        show_cbioportal("show.cbioportal", null),
        show_cosmic("show.cosmic", null),
        show_ndex("show.ndex", null),
        survival_show_p_q_values_in_survival_type_table(
            "survival.show_p_q_values_in_survival_type_table", null),
        survival_min_group_threshold("survival.min_group_threshold", null),
        survival_initial_x_axis_limit("survival.initial_x_axis_limit", null),
        skin_documentation_about("skin.documentation.about", null),
        skin_documentation_baseurl("skin.documentation.baseurl", null),
        skin_example_study_queries("skin.example_study_queries", null),
        skin_blurb("skin.blurb", null),
        skin_custom_header_tabs("skin.custom_header_tabs", null),
        skin_data_sets_footer("skin.data_sets_footer", null),
        skin_data_sets_header("skin.data_sets_header", null),
        skin_documentation_markdown("skin.documentation.markdown", null),
        skin_email_contact("skin.email_contact", null),
        skin_examples_right_column_html("skin.examples_right_column_html", null),
        skin_documentation_faq("skin.documentation.faq", null),
        skin_footer("skin.footer", null),
        skin_footer_show_dev("skin.footer_show_dev", null),
        skin_login_contact_html("skin.login.contact_html", null),
        skin_login_saml_registration_html("skin.login.saml.registration_html", null),
        skin_documentation_news("skin.documentation.news", null),
        skin_documentation_oql("skin.documentation.oql", null),
        skin_query_max_tree_depth("skin.query.max_tree_depth", null),
        skin_quick_select_buttons("skin.quick_select_buttons", null),
        skin_right_logo("skin.right_logo", null),
        skin_left_logo("skin.left_logo", null),
        skin_right_nav_show_data_sets("skin.right_nav.show_data_sets", null),
        skin_right_nav_show_examples("skin.right_nav.show_examples", null),
        skin_right_nav_show_testimonials("skin.right_nav.show_testimonials", null),
        skin_right_nav_show_whats_new("skin.right_nav.show_whats_new", null),
        skin_right_nav_show_twitter("skin.right_nav.show_twitter", null),
        skin_right_nav_whats_new_blurb("skin.right_nav.whats_new_blurb", null),
        skin_show_about_tab("skin.show_about_tab", null),
        skin_show_data_tab("skin.show_data_tab", null),
        skin_show_faqs_tab("skin.show_faqs_tab", null),
        skin_show_news_tab("skin.show_news_tab", null),
        skin_show_r_matlab_tab("skin.show_r_matlab_tab", null),
        skin_show_tools_tab("skin.show_tools_tab", null),
        skin_show_tutorials_tab("skin.show_tutorials_tab", null),
        skin_show_web_api_tab("skin.show_web_api_tab", null),
        skin_show_tweet_button("skin.show_tweet_button", null),
        skin_show_study_help_button("skin.show_study_help_button", null),
        skin_patientview_filter_genes_profiled_all_samples(
            "skin.patientview.filter_genes_profiled_all_samples", null),
        skin_patientview_show_mskcc_slide_viewer("skin.patientview.show_mskcc_slide_viewer", null),
        skin_show_settings_menu("skin.show_settings_menu", null),
        skin_hide_logout_button("skin.hide_logout_button", null),
        quick_search_enabled("quick_search.enabled", null),
        default_cross_cancer_study_session_id("default_cross_cancer_study_session_id", null),
        default_cross_cancer_study_list("default_cross_cancer_study_list", null),
        default_cross_cancer_study_list_name("default_cross_cancer_study_list_name", null),
        skin_description("skin.description", null),
        skin_title("skin.title", null),
        app_name("app.name", null),
        skin_authorization_message("skin.authorization_message", null),
        session_url_length_threshold("session.url_length_threshold", null),
        bitly_api_key("bitly.api_key", null),
        bitly_user("bitly.user", null),
        bitly_access_token("bitly.access.token", null),
        oncoprint_custom_driver_annotation_tiers_default(
            "oncoprint.custom_driver_annotation.tiers.default", null),
        ensembl_transcript_url("ensembl.transcript_url", null),
        enable_persistent_cache("enable_persistent_cache", null),
        enable_request_body_gzip_compression("enable_request_body_gzip_compression", null),
        query_product_limit("query_product_limit", null),
        skin_show_gsva("skin.show_gsva", null),
        saml_idp_metadata_entityid("saml.idp.metadata.entityid", null),
        saml_logout_local("saml.logout.local", null),
        skin_citation_rule_text("skin.citation_rule_text", null),
        skin_geneset_hierarchy_default_p_value("skin.geneset_hierarchy.default_p_value", null),
        skin_geneset_hierarchy_default_gsva_score("skin.geneset_hierarchy.default_gsva_score", null),
        app_version("app.version", null),
        frontendSentryEndpoint("sentryjs.frontend_project_endpoint", null),

        // These properties require additional processing.
        // Names refer to the property that requires processing.
        frontendConfigOverride("frontend.config", null),
        query_sets_of_genes("querypage.setsofgenes.location", null),
        authenticationMethod("authenticate", "false"),
        mskWholeSlideViewerToken("msk.whole.slide.viewer.secret.key", null),
        oncoprintOncoKbHotspotsDefault("oncoprint.oncokb_hotspots.default", "true"),
        oncoKbTokenDefined("oncokb.token", ""),
        sessionServiceEnabled("session.service.url", ""),
        frontendUrl("frontend.url", null),
        skin_hide_download_controls("skin.hide_download_controls", "show"),
        study_download_url("study_download_url", "https://cbioportal-datahub.s3.amazonaws.com/"), 
        enable_cross_study_expression("enable_cross_study_expression", ""),
        studyview_max_samples_selected("studyview.max_samples_selected", null),
        skin_home_page_show_reference_genome("skin.home_page.show_reference_genome", null),
        vaf_sequential_mode_default("vaf.sequential_mode.default", null),
        vaf_log_scale_default("vaf.log_scale.default", null),
        skin_patient_view_custom_sample_type_colors_json("skin.patient_view.custom_sample_type_colors_json", null),
        skin_study_view_show_sv_table("skin.study_view.show_sv_table", null),
        skin_home_page_show_unauthorized_studies("skin.home_page.show_unauthorized_studies", null),
        skin_home_page_unauthorized_studies_global_message("skin.home_page.unauthorized_studies_global_message", null),
        skin_mutation_table_namespace_column_show_by_default("skin.mutation_table.namespace_column.show_by_default", null),
        skin_geneset_hierarchy_collapse_by_default("skin.geneset_hierarchy.collapse_by_default", null),
        skin_patient_view_mutation_table_columns_show_on_init("skin.patient_view.mutation_table.columns.show_on_init", null),
        skin_results_view_mutation_table_columns_show_on_init("skin.results_view.mutation_table.columns.show_on_init", null),
        skin_patient_view_copy_number_table_columns_show_on_init("skin.patient_view.copy_number_table.columns.show_on_init", null),
        skin_patient_view_structural_variant_table_columns_show_on_init("skin.patient_view.structural_variant_table.columns.show_on_init", null),
        skin_results_view_tables_default_sort_column("skin.results_view.tables.default_sort_column", null),
        skin_survival_plot_clinical_event_types_show_on_init("skin.survival_plot.clinical_event_types.show_on_init", null),
        skin_show_donate_button("skin.show_donate_button", "false"),
        
        skin_patient_view_tables_default_sort_column("skin.patient_view.tables.default_sort_column", null),
        enable_treatment_groups("enable_treatment_groups", null),
        comparison_categorical_na_values("comparison.categorical_na_values", null),
        clinical_attribute_product_limit("clinical_attribute_product_limit", null),
        skin_right_nav_show_web_tours("skin.right_nav.show_web_tours", "false"),

        download_custom_buttons_json("download_custom_buttons_json", null),
      
        enable_study_tags("enable_study_tags", null),
        enable_darwin("enable_darwin", null),

        clickhouse_mode("clickhouse_mode", "false");
        
        
      
        private final String propertyName;
        private final String defaultValue;

        FrontendProperty(String name, String defaultValue) {
            this.propertyName = name;
            this.defaultValue = defaultValue;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getFrontendName() {
            return this.name();
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    @Autowired
    private Environment env;

    private static Map<String, String> serverConfigProperties;

    @PostConstruct
    public void init() {
        serverConfigProperties = Arrays.stream(FrontendProperty.values())
            // do not use toMap here because null values are problematic
            .collect(
                HashMap::new,
                (out, property) -> out.put(property.getFrontendName(), getPropertyValue(property)),
                HashMap::putAll
            );
    }

    private String getPropertyValue(FrontendProperty property) {
        String propertyValue = env.getProperty(property.getPropertyName(), property.getDefaultValue());
        if (propertyValue != null)
            propertyValue = propertyValue.trim();
        switch (property.getFrontendName()) {
            // First, add properties that require pre-processing.
            case "frontendConfigOverride":
            case "query_sets_of_genes":
            case "skin.patient_view.custom_sample_type_colors_json":
            case "oncoprint.clinical_tracks.config_json":
            case "download_custom_buttons_json":
                return readFile(propertyValue);
            case "oncoprintOncoKbHotspotsDefault":
                return enableOncoKBandHotspotsParamValue(propertyValue);
            case "oncoKbTokenDefined":
            case "sessionServiceEnabled":
                return String.valueOf(!propertyValue.isEmpty());
            case "frontendUrl":
                return getFrontendUrl(propertyValue);
            case "enable_darwin":
                return enableDarwin();
            // For others, just return the value in the properties file.
            default:
                return propertyValue;
        }
    }

    private String enableOncoKBandHotspots(String enableOncoKBandHotspots) {
        if (enableOncoKBandHotspots.equalsIgnoreCase("custom")) {
            return "custom";
        } else if (enableOncoKBandHotspots.equalsIgnoreCase("false")) {
            return "false";
        }
        return "true";
    }

    private String enableOncoKBandHotspotsParamValue(String enableOncoKBandHotspots) {
        switch (enableOncoKBandHotspots(enableOncoKBandHotspots)) {
            case "true":
                return "undefined";
            case "false":
                return "\"disable\"";
            case "custom":
                return "\"custom\"";
        }
        return null;
    }

    public String getFrontendProperty(FrontendProperty property) {
        return serverConfigProperties.get(property.getFrontendName());
    }

    public Map<String, String> getFrontendProperties() {
        // Make sure that requests work on individual instances of this data.
        return cloneProperties();
    }
    
    private Map<String,String> cloneProperties() {
        return serverConfigProperties.entrySet().stream()
            .collect(
                HashMap::new,
                (out, entry) -> out.put(entry.getKey(), entry.getValue()),
                HashMap::putAll
            );
    }

    /**
     * Find the file, either on the file system or in a .jar, and return as an InputStream.
     * @propertiesFileName: the file path 
     * @return: a valid InputStream (not null), otherwise throws FileNotFoundException
     * TECH: file system locations have precedence over classpath
     * REF: based on getResourceStream() in WebServletContextListener.java
     */
    private InputStream locateFile(String filePath) throws FileNotFoundException {
        // try absolute or relative to working directory
        File file = new File(filePath);
        if (file.exists()) {
            // throws if is a directory or cannot be opened
            log.info("Found frontend config file: {}", file.getAbsolutePath());
            return new FileInputStream(file);
        } 
        
        // try relative to PORTAL_HOME
        String home = System.getenv("PORTAL_HOME");
        if (home != null) {
            file = new File(Paths.get(home, filePath).toString());
            if (file.exists()) {
                log.info("Found frontend config file: {}", file.getAbsolutePath());
                return new FileInputStream(file);
            }
        } 

        // try resource (e.g. app.jar)
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
        if (inputStream != null) {
            log.info("Found frontend config resource: {}", filePath);
            return inputStream;
        } else {
            throw new FileNotFoundException("File not found in system or classpath: " + filePath);
        }
    }

    /**
     * Read the file, either on the file system or in a .jar, and return the content as a single-line string. 
     * @propertiesFileName: the file path 
     */
    private String readFile(String propertiesFileName) {
        if (propertiesFileName == null || propertiesFileName.isEmpty()) {
            return null;
        }

        // strip off classpath prefix and always check all locations (ClassLoader and file system)
        String filePath = propertiesFileName.startsWith("classpath:") 
            ? propertiesFileName.substring("classpath:".length()) 
            : propertiesFileName;

        try {
            InputStream inputStream = locateFile(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            return br.lines().map(String::trim).collect(Collectors.joining(""));            
        } catch (Exception e) {
            log.error("Error reading frontend config file: {}", e.getMessage());
            return null;
        }
    }
   
    public String getFrontendUrl(String propertyValue) {
        String frontendUrlRuntime = env.getProperty("frontend.url.runtime", "");
        if (frontendUrlRuntime.length() > 0) {
            try {
                String url = parseUrl(Files.readString(Paths.get(frontendUrlRuntime)).replaceAll("[\\r\\n]+", ""));
                if (log.isInfoEnabled()) {
                    log.info("Using frontend from {}: {}", frontendUrlRuntime, url);
                }
                return url;
            } catch (IOException e) {
                // error reading file, use existing frontendUrl
                if (log.isErrorEnabled()) {
                    log.error("Can't read frontend.url.runtime: {}", frontendUrlRuntime);
                }
            }
        }
        return propertyValue;
    }

    public String enableDarwin() {
        String darwinAuthUrl = env.getProperty("darwin.auth_url", "");
        String ddpResponseUrl = env.getProperty("ddp.response_url", "");
        String cisUser = env.getProperty("cis.user", "");
        String darwinRegex = env.getProperty("darwin.regex", "");
        if (!darwinAuthUrl.isBlank() && !ddpResponseUrl.isBlank() && !cisUser.isBlank() && !darwinRegex.isBlank()) {
            return "true";
        } else {
            return "false";
        }
    }
    
    /*
     * Trim whitespace of url and append / if it does not exist. Return empty
     * string otherwise.
     */
    public static String parseUrl(String url) {
        String rv = "";
        if (url != null && !url.isEmpty()) {
            rv = url.trim();
            if (!rv.endsWith("/")) {
                rv += "/";
            }
        }
        return rv;
    }

}
