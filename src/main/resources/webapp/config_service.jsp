        // Set API root variable for cbioportal-frontend repo
        String currentUrl = request.getRequestURL().toString();
        String baseURL = currentUrl.substring(0, currentUrl.length() - request.getRequestURI().length()) + request.getContextPath();
        baseURL = baseURL.replace("https://", "").replace("http://", "");

        String[] propNameArray = {
            "app.version",
            "app.name",
            "oncoprint.custom_driver_annotation.binary.menu_label",
            "disabled_tabs",
            "civic.url",
            "oncoprint.custom_driver_annotation.binary.default",
            "oncoprint.oncokb.default",
            "oncoprint.hotspots.default",
            "oncoprint.clustered.default",
            "genomenexus.url",
            "genomenexus.url.grch38",
            "google_analytics_profile_id",
            "analytics_report_url",
            "oncoprint.hide_vus.default",
            "mycancergenome.show",
            "oncokb.public_api.url",
            "oncokb.merge_icons_by_default",
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
            "show.cbioportal",
            "show.cosmic",
            "show.civic",
            "show.genomenexus",
            "show.genomenexus.annotation_sources",
            "genomenexus.isoform_override_source",
            "show.mutation_mappert_tool.grch38",
            "show.transcript_dropdown",
            "show.signal",
            "show.ndex",
            "survival.show_p_q_values_in_survival_type_table",
            "survival.min_group_threshold",
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
            "skin.left_logo",
            "skin.right_nav.show_data_sets",
            "skin.right_nav.show_examples",
            "skin.right_nav.show_testimonials",
            "skin.right_nav.show_whats_new",
            "skin.right_nav.show_twitter",
            "skin.right_nav.whats_new_blurb",
            "skin.right_nav.show_web_tours",
            "skin.show_about_tab",
            "skin.show_data_tab",
            "skin.show_faqs_tab",
            "skin.show_news_tab",
            "skin.show_r_matlab_tab",
            "skin.show_tools_tab",
            "skin.show_tutorials_tab",
            "skin.show_web_api_tab",
            "skin.show_tweet_button",
            "skin.show_donate_button",
            "skin.patientview.filter_genes_profiled_all_samples",
            "skin.patientview.show_mskcc_slide_viewer",
            "skin.home_page.show_unauthorized_studies",
            "skin.home_page.unauthorized_studies_global_message",
            "skin.show_settings_menu",
            "skin.hide_logout_button",
            "enable_cross_study_expression",
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
            "oncoprint.clinical_tracks.config_json",
            "ensembl.transcript_url",
            "enable_persistent_cache",
            "enable_request_body_gzip_compression",
            "enable_treatment_groups",
            "query_product_limit",
            "studyview.max_samples_selected",
            "clinical_attribute_product_limit",
            "saml.logout.local",
            "skin.citation_rule_text",
            "skin.show_gsva",
            "skin.geneset_hierarchy.default_p_value",
            "skin.geneset_hierarchy.default_gsva_score",
            "skin.geneset_hierarchy.collapse_by_default",
            "skin.mutation_table.namespace_column.show_by_default",
            "skin.patient_view.mutation_table.columns.show_on_init",
            "skin.results_view.mutation_table.columns.show_on_init",
            "skin.patient_view.copy_number_table.columns.show_on_init",
            "skin.patient_view.structural_variant_table.columns.show_on_init",
            "skin.study_view.show_sv_table",
            "comparison.categorical_na_values",
            "study_download_url",
            "skin.home_page.show_reference_genome",
            "vaf.sequential_mode.default",
            "vaf.log_scale.default",
            "download_custom_buttons_json",
        };


        JSONObject obj = new JSONObject();

        // for each above, add json prop and lookup value in application.properties
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

        obj.put("user_display_name", GlobalProperties.getAuthenticatedDisplayName());

        obj.put("frontendConfigOverride",GlobalProperties.getFrontendConfig());

        obj.put("oncoprint_clinical_tracks_config_json",GlobalProperties.getOncoprintClinicalTracksConfigJson());

        obj.put("skin_patient_view_custom_sample_type_colors_json", GlobalProperties.getSkinPatientViewCustomSampleTypeColorsJson());

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

        obj.put("oncoKbTokenDefined", !ObjectUtils.isEmpty(GlobalProperties.getOncoKbToken()));

        obj.put("sessionServiceEnabled", !ObjectUtils.isEmpty(GlobalProperties.getSessionServiceUrl()));

        obj.put("skin_hide_download_controls", GlobalProperties.getDownloadControl());
        
        obj.put("dat_method", GlobalProperties.getDataAccessTokenMethod());

        out.println(obj.toJSONString());


     %>

);

