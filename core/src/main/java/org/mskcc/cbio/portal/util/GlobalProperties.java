/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.servlet.QueryBuilder;

import java.net.*;
import java.util.*;
import com.google.common.base.Strings;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.*;
import java.net.HttpURLConnection;
import java.util.*;
import java.net.URL;
import org.springframework.context.ApplicationContext;

/**
 * Utility class for getting / setting global properties.
 */
//@Configuration
//@PropertySources({@PropertySource(value="portal.properties", ignoreResourceNotFound=true), 
//    @PropertySource(value="file:classpath/portal.properties")})
//@Scope("singleton")
public class GlobalProperties {
    
    private static final ApplicationContext ctx = SpringUtil.getApplicationContext();
    private static final GlobalPropertiesBean gbBean = (GlobalPropertiesBean)ctx.getBean("globalPropertiesBean");

//    @Value("${pathway_commons.url}")
//    private static String pathwayCommonsUrl;    
//    
//    @Value("${ucsc_cancer_genomics.url}")
//    private static String ucscCancerGenomicsUrl;
//    
//    @Value("${segfile.url}")
//    private static String segFileUrl;
//
//    @Value("${openssl.binary}")
//    private static String openSslBinary;
//    
//    @Value("${signature.key}")
//    private static String signatureKey;
//    
//    @Value("${encryption.key}")
//    private static String encryptionKey;
//    
//    @Value("${broad.bam.url}")
//    private static String broadBamUrl;
//    
//    @Value("${broad.bam.checking.url}")
//    private static String broadBamCheckingUrl;
//    
//    @Value("${igv.bam.linking:false}")
//    private static boolean igvBamLinking;
//    
//    @Value("${igv.bam.linking.studies}")
//    private static String igvBamLinkingStudies;
//    
//    // default value = "false"
//    @Value("${authenticate:false}")
//    private static String authenticate;
//    
//    @Value("${authorization}")
//    private static boolean authorization;
//    
//    @Value("${filter_groups_by_appname:true}")
//    private static boolean filterGroupsByAppName;
//    
//    @Value("${include_networks}")
//    private static boolean includeNetworks;
//    
//    @Value("${google_analytics_profile_id}")
//    private static String googleAnalyticsProfileId;
//    
//    @Value("${genomespace}")
//    private static boolean genomeSpace;
//
//    @Value("${app.name}")
//    private static String appName;
//    private static final String DEFAULT_APP_NAME = "public_portal";
//    
//    // default value = 1.0
//    @Value("${app.version:1.0}")
//    private static String appVersion;
//    
//    @Value("${skin.title}")
//    private static String skinTitle;    
//    private static final String DEFAULT_SKIN_TITLE = "cBioPortal for Cancer Genomics";
//    
//    
//    @Value("${skin.blurb}")
//    private static String skinBlurb;    
//    private static final String DEFAULT_SKIN_BLURB = "The cBioPortal for Cancer Genomics provides " +
//            "<b>visualization</b>, <b>analysis</b> and <b>download</b> of large-scale cancer genomics data sets. " +
//            "<p>Please adhere to <u><a href=\"http://cancergenome.nih.gov/abouttcga/policies/publicationguidelines\"> " +
//            "the TCGA publication guidelines</a></u> when using TCGA data in your publications.</p> " +
//            "<p><b>Please cite</b> <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/23550210\">Gao " +
//            "et al. <i>Sci. Signal.</i> 2013</a> &amp;  " +
//            "<a href=\"http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract\">" +
//            "Cerami et al. <i>Cancer Discov.</i> 2012</a> when publishing results based on cBioPortal.</p>\n";
//    
//    @Value("${skin.tag_line_image}")
//    private static String skinTagLineImage;    
//    private static final String DEFAULT_SKIN_TAG_LINE_IMAGE = "images/tag_line.png";
//        
//    @Value("${skin.email_contact}")
//    private static String skinEmailContact;
//    private static final String DEFAULT_EMAIL_CONTACT = "cbioportal at googlegroups dot com";
//    
//    @Value("${skin.show_news_tab:true}")
//    private static boolean skinShowNewsTab;
//    
//    @Value("${skin.show_data_tab:true}")
//    private static boolean skinShowDataTab;
//    
//    @Value("${skin.right_nav.show_data_sets:true}")
//    private static boolean skinRightNavShowDataSets;
//    
//    @Value("${skin.right_nav.show_examples:true}")
//    private static boolean skinRightNavShowExamples;
//    
//    @Value("${skin.right_nav.show_testimonials:true}")
//    private static boolean skinRightNavShowTestimonials;
//    
//    @Value("${skin.authorization_message}")
//    private static String skinAuthorizationMessage;    
//    private static final String DEFAULT_AUTHORIZATION_MESSAGE = "Access to this portal is only available to authorized users.";
//    
//    @Value("${skin.example_study_queries}")
//    private static String skinExampleStudyQueries;
//    private static final String DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES =            
//            "tcga\n" +
//            "tcga -provisional\n" +
//            "tcga -moratorium\n" +
//            "tcga OR icgc\n" +
//            "-\"cell line\"\n" +
//            "prostate mskcc\n" +
//            "esophageal OR stomach\n" +
//            "serous\n" +
//            "breast";
//
//    @Value("${skin.data_sets_header}")
//    private static String skinDataSetsHeader;
//    private static final String DEFAULT_SKIN_DATASETS_HEADER = "The portal currently contains data from the following " +
//            "cancer genomics studies.  The table below lists the number of available samples per data type and tumor.";
//    
//    @Value("${skin.data_sets_footer}")
//    private static String skinDataSetsFooter;
//    private static final String DEFAULT_SKIN_DATASETS_FOOTER = "Data sets of TCGA studies were downloaded from Broad " +
//            "Firehose (http://gdac.broadinstitute.org) and updated monthly. In some studies, data sets were from the " +
//            "TCGA working groups directly.";
//    
//    @Value("${patient_view_placeholder:false}")
//    private static boolean patientViewPlaceholder;
//    
//    // default value = 0.2,1.5
//    @Value("${patient_view_genomic_overview_cna_cutoff:0.2,1.5}")
//    private static  double[] patientViewGenomicOverviewCnaCutoff; // default value = 0.2,1.5    
//
////    public static final String PATIENT_VIEW_PLACEHOLDER = "patient_view_placeholder";
////    public static final String PATIENT_VIEW_GENOMIC_OVERVIEW_CNA_CUTOFF = "patient_view_genomic_overview_cna_cutoff";
////    public static final double[] DEFAULT_GENOMIC_OVERVIEW_CNA_CUTOFF = new double[]{0.2,1.5};
////    public static final String PATIENT_VIEW_DIGITAL_SLIDE_IFRAME_URL = "digitalslidearchive.iframe.url";
////    public static final String PATIENT_VIEW_DIGITAL_SLIDE_META_URL = "digitalslidearchive.meta.url";
////    public static final String PATIENT_VIEW_TCGA_PATH_REPORT_URL = "tcga_path_report.url";
////    public static final String ONCOKB_URL = "oncokb.url";
//    @Value("${mdacc.heatmap.meta.url}")
//    private static String patientViewMdaccHeatmapMetaUrl;
////    public static final String PATIENT_VIEW_MDACC_HEATMAP_META_URL = "mdacc.heatmap.meta.url";
//    @Value("${mdacc.heatmap.patient.url}")
//    private static String patientViewMdaccHeatmapUrl;
////    public static final String PATIENT_VIEW_MDACC_HEATMAP_URL = "mdacc.heatmap.patient.url";
////
//    @Value("${mdacc.heatmap.study.url}")
//    private static String studyViewMdaccHeatmapUrl;
////    public static final String STUDY_VIEW_MDACC_HEATMAP_URL = "mdacc.heatmap.study.url";
//    @Value("${mdacc.heatmap.study.meta.url}")
//    private static String studyViewMdaccHeatmapMetaUrl;
////    public static final String STUDY_VIEW_MDACC_HEATMAP_META_URL = "mdacc.heatmap.study.meta.url";
////
////    public static final String SESSION_SERVICE_URL = "session.service.url";
//
//    @Value("${digitalslidearchive.iframe.url}")
//    private static String digitalSlideArchiveIframeUrl;
//    
//    @Value("${digitalslidearchive.meta.url}")
//    private static String digitalSlideArchiveMetaUrl;
//    
//    @Value("${tcga_path_report.url}")
//    private static String tcgaPathReportUrl;
//    
//    @Value("${oncokb.url}")
//    private static String oncokbUrl;
//    
//    @Value("${session.service.url}")
//    private static String sessionServiceUrl;
//    
//    // properties for showing the right logo in the header_bar and default logo
//    @Value("${skin.right_logo}")
//    private static String skinRightLog;    
//    private static final String DEFAULT_SKIN_RIGHT_LOGO = "images/mskcc_logo_3d_grey.jpg";   
//
//    // properties for hiding/showing tabs in the header navigation bar
//    @Value("${skin.show_web_api_tab:true}")
//    private static boolean skinShowWebApiTab;
//    
//    @Value("${skin.show_r_matlab_tab:true}")
//    private static boolean skinShowRMatlabTab;
//    
//    @Value("${skin.show_tutorials_tab:true}")
//    private static boolean skinShowTutorialsTab;
//    
//    @Value("${skin.show_faqs_tab:true}")
//    private static boolean skinShowFaqsTab;
//    
//    @Value("${skin.show_tools_tab:true}")
//    private static boolean skinShowToolsTab;
//    
//    @Value("${skin.show_about_tab:true}")
//    private static boolean skinShowAboutTab;
//    
//    @Value("${skin.show_visualize_your_data_tab:true}")
//    private static boolean skinShowVisualizeYourDataTab;
//    
//    // property for setting the news blurb in the right column
//    @Value("${skin.right_nav.whats_new_blurb}")
//    private static String skinRightNavWhatsNewBlurb;    
//    private static final String DEFAULT_SKIN_WHATS_NEW_BLURB = 
//            "<form action=\"http://groups.google.com/group/cbioportal-news/boxsubscribe\"> &nbsp;&nbsp;&nbsp;&nbsp;" +
//            "<b>Sign up for low-volume email news alerts:</b></br> &nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" " +
//            "name=\"email\" title=\"Subscribe to mailing list\"> <input type=\"submit\" name=\"sub\" value=\"Subscribe\"> " +
//            "</form> &nbsp;&nbsp;&nbsp;&nbsp;<b>Or follow us <a href=\"http://www.twitter.com/cbioportal\">" +
//            "<i>@cbioportal</i></a> on Twitter</b>\n";
//    
//    // footer
//    @Value("${skin.footer}")
//    private static String skinFooter;
//    private static final String DEFAULT_SKIN_FOOTER = " | <a href=\"http://www.mskcc.org/mskcc/html/44.cfm\">MSKCC</a>" +
//            " | <a href=\"http://cancergenome.nih.gov/\">TCGA</a>";
//    
//    // login contact
//    @Value("${skin.login.contact_html}")
//    private static String skinLoginContactHtml;
//    private static final String DEFAULT_SKIN_LOGIN_CONTACT_HTML = "If you think you have received this message in " +
//            "error, please contact us at <a style=\"color:#FF0000\" href=\"mailto:cbioportal-access@cbio.mskcc.org\">" +
//            "cbioportal-access@cbio.mskcc.org</a>";
//    
//    // properties for hiding/showing tabs in the patient view
////    public static final String SKIN_PATIENT_VIEW_SHOW_DRUGS_TAB="skin.patient_view.show_drugs_tab";
//
//    @Value("${skin.patient_view.show_clinical_trials_tab:false}")
//    private static boolean skinPatientViewShowClinicalTrialsTab;
//    
//    @Value("${skin.patient_view.show_drugs_tab:false}")
//    private static boolean skinPatientViewShowDrugsTab;
//    
//    // property for setting the saml registration html
//    @Value("${skin.login.saml.registration_html}")
//    private static String skinLoginSamlRegistrationHtml;
//    private static final String DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML = "Sign in with MSK";
//    
//    // property for the saml entityid
//    @Value("${saml.idp.metadata.entityid}")
//    private static String samlIdpMetadataEntityId;    
//
//    // property for the custom header tabs
//    @Value("${skin.custom_header_tabs}")
//    private static String[] skinCustomHeaderTabs;
//        
//    // properties for the FAQ, about us, news and examples
//    @Value("${skin.documentation.baseurl}")
//    private static String skinDocumentationBaseUrl;
//    private static final String DEFAULT_SKIN_BASEURL = "https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/";
//    
//    @Value("${skin.documentation.markdown:true}")
//    private static boolean skinDocumentationMarkdown;
//    
//    @Value("${skin.documentation.faq}")
//    private static String skinDocumentationFaq;
//    private static final String DEFAULT_SKIN_FAQ = "FAQ.md";
//    
//    @Value("${skin.documentation.about}")
//    private static String skinDocumentationAbout;
//    private static final String DEFAULT_SKIN_ABOUT = "About-Us.md";
//        
//    @Value("${skin.documentation.news}")
//    private static String skinDocumentationNews;
//    private static final String DEFAULT_SKIN_NEWS = "News.md";
//
//    @Value("${skin.examples_right_column}")
//    private static String skinExamplesRightColumn;
//    private static final String DEFAULT_SKIN_EXAMPLES_RIGHT_COLUMN = "../../../content/examples.html";
//
//    @Value("${always_show_study_group}")
//    private static String alwaysShowStudyGroup;
//    
//    // property for text shown at the right side of the Select Patient/Case set, which
//    // links to the study view
//    @Value("${skin.study_view.link_text}")
//    private static String skinStudyViewLinkText;
//    private static final String DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT = "To build your own case set, try out our enhanced " +            
//            "Study View.";    
//
//    @Value("${mycancergenome.show:false}")
//    private static boolean myCancerGenomeShow;
//    
//    @Value("${oncokb.geneStatus}")
//    private static String oncokbGeneStatus;
//    
//    @Value("${show.hotspot:true}")
//    private static boolean showHotspot;
//        
//    @Value("${recache_study_after_update:false}")
//    private static boolean recacheStudyAfterUpdate;    
//    
//    // default value = 0
//    @Value("${db.version:0}")
//    private static String dbVersion;
//        
//    @Value("${darwin.auth_url:}")
//    private static String darwinAuthUrl;
//    
//    @Value("${priority_studies}")
//    private static String priorityStudies;
//    public static final String PRIORITY_STUDIES = "priority_studies";
//    
//    private static Log LOG = LogFactory.getLog(GlobalProperties.class);
//    private static Properties properties = initializeProperties();
//
//    private static Properties initializeProperties()
//    {
//        return loadProperties(getResourceStream());
//    }
//
//    private static InputStream getResourceStream()
//    {
//        String resourceFilename = null;
//        InputStream resourceFIS = null;
//
//        try {
//            String home = System.getenv(HOME_DIR);
//            if (home != null) {
//                 resourceFilename =
//                    home + File.separator + GlobalProperties.propertiesFilename;
//                if (LOG.isInfoEnabled()) {
//                    LOG.info("Attempting to read properties file: " + resourceFilename);
//                }
//                resourceFIS = new FileInputStream(resourceFilename);
//                if (LOG.isInfoEnabled()) {
//                    LOG.info("Successfully read properties file");
//                }
//            }
//        }
//        catch (FileNotFoundException e) {
//            if (LOG.isInfoEnabled()) {
//                LOG.info("Failed to read properties file: " + resourceFilename);
//            }
//        }
//
//        if (resourceFIS == null) {
//            if (LOG.isInfoEnabled()) {
//                LOG.info("Attempting to read properties file from classpath");
//            }
//            resourceFIS = GlobalProperties.class.getClassLoader().
//                getResourceAsStream(GlobalProperties.propertiesFilename);
//            if (LOG.isInfoEnabled()) {
//                LOG.info("Successfully read properties file");
//            }
//        }
//         
//        return resourceFIS;
//    }
//
//    private static Properties loadProperties(InputStream resourceInputStream)
//    {
//        Properties properties = new Properties();
//
//        try {
//            properties.load(resourceInputStream);
//            resourceInputStream.close();
//        }
//        catch (IOException e) {
//            if (LOG.isErrorEnabled()) {
//                LOG.error("Error loading properties file: " + e.getMessage());
//            }
//        }
//
//        return properties;
//    }
//
//    @Value("${darwin.response_url:}")
//    private static String darwinResponseUrl;
//    
//    @Value("${darwin.authority:}")
//    private static String darwinAuthority;
//    
//    @Value("${cis.user:}")
//    private static String cisUser;
//    
//    @Value("${disabled_tabs:}")
//    private static String disabledTabs;
//    
//    // css properties
//    @Value("${global_css}")
//    private String globalCss;
//    
//    @Value("${special_css}")
//    private String specialCss;
//    
//    @Value("${data_sets}")
//    private String dataSets;
//    
//    @Value("${popeye}")
//    private String popeye;
//    
//    @Value("${faq}")
//    private String faq;

    public static String getPathwayCommonsUrl() {
        return gbBean.getPathwayCommonsUrl(); //pathwayCommonsUrl;
    }
    
    public static String getUcscCancerGenomicsUrl() {
        return gbBean.getUcscCancerGenomicsUrl(); //ucscCancerGenomicsUrl;
    }

    public static String getSegfileUrl() {
        return gbBean.getSegfileUrl(); //segFileUrl;
    }

    public static boolean wantIGVBAMLinking() {
        return gbBean.wantIGVBAMLinking(); //igvBamLinking;
    }

    public static Collection<String> getIGVBAMLinkingStudies() {
        return gbBean.getIGVBAMLinkingStudies();
//        if (igvBamLinkingStudies==null) {
//            return Collections.emptyList();
//        }
//        String[] studies = igvBamLinkingStudies.split(":");
//        return (studies.length > 0) ? Arrays.asList(studies) : Collections.<String>emptyList();
    }

    public static boolean usersMustAuthenticate() {
        return gbBean.usersMustAuthenticate(); //!authenticate.equals("false");
    }

    public static String authenticationMethod() {
        return gbBean.authenticationMethod(); //authenticate;
    }

    /**
     * Return authenticated username
     * @return String userName 
     * Return authenticated username. If the user is not authenticated, 'anonymousUser' will be returned.
     */
    public static String getAuthenticatedUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if(authentication != null) {
            return authentication.getName();
        }else {
            return "anonymousUser";
        }
    }
    public static boolean usersMustBeAuthorized() {
        return gbBean.usersMustBeAuthorized(); //authorization;
    }

    public static String getAppName() {
        return gbBean.getAppName(); //Strings.isNullOrEmpty(appName)?DEFAULT_APP_NAME:appName;
    }

    public static String getAppVersion() {
        return gbBean.getAppVersion(); //appVersion;
    }
    
    public static String getTitle() {
        return gbBean.getTitle(); //Strings.isNullOrEmpty(skinTitle)?DEFAULT_SKIN_TITLE:skinTitle;
    }
    
    // updated function to use a default if nothing is specified
    public static String getBlurb() {
        return gbBean.getBlurb(); //Strings.isNullOrEmpty(skinBlurb)?DEFAULT_SKIN_BLURB:skinBlurb;
    }
    // get custom FAQ html or the default
    public static String getFaqHtml() {
        return gbBean.getFaqHtml(); //Strings.isNullOrEmpty(skinDocumentationFaq)?DEFAULT_SKIN_FAQ:skinDocumentationFaq;
    }
    // get custom About html or the default
    public static String getAboutHtml() {
        return gbBean.getAboutHtml(); //Strings.isNullOrEmpty(skinDocumentationAbout)?DEFAULT_SKIN_ABOUT:skinDocumentationAbout;
    }
    // get custom News html or the default
    public static String getNewsHtml() {
        return gbBean.getNewsHtml(); //skinDocumentationNews==null?DEFAULT_SKIN_NEWS:getContentString(skinDocumentationNews);
    }
    // get custom News html or the default
    public static String getBaseUrl() {
        return gbBean.getBaseUrl(); //Strings.isNullOrEmpty(skinDocumentationBaseUrl)?DEFAULT_SKIN_BASEURL:skinDocumentationBaseUrl;
    }
    public static boolean isMarkdownDocumentation() {
        return gbBean.isMarkdownDocumentation(); //skinDocumentationMarkdown;
    }

    // get custom Example Queries for the right column html or the default
    public static String getExamplesRightColumnHtml() {
        return gbBean.getExamplesRightColumnHtml(); //Strings.isNullOrEmpty(skinExamplesRightColumn)?DEFAULT_SKIN_EXAMPLES_RIGHT_COLUMN:"../../../content/"+skinExamplesRightColumn;
    }

//    private static String getContentString(String contentString) {
//        if(getBaseUrl().equalsIgnoreCase("")) return "content/"+contentString;
//        return contentString;
//    }

    // get the login contact html
    public static String getLoginContactHtml() {
        return gbBean.getLoginContactHtml(); //Strings.isNullOrEmpty(skinLoginContactHtml)?DEFAULT_SKIN_LOGIN_CONTACT_HTML:skinLoginContactHtml;
    }
    // get the text for the saml login button
    public static String getLoginSamlRegistrationHtml() {
        return gbBean.getLoginSamlRegistrationHtml(); //Strings.isNullOrEmpty(skinLoginSamlRegistrationHtml)?DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML:skinLoginSamlRegistrationHtml;
    }
    public static String getSamlIdpMetadataEntityid() {
        return gbBean.getSamlIdpMetadataEntityid(); //samlIdpMetadataEntityId;
    }
    public static String getTagLineImage() {
        return gbBean.getTagLineImage(); //Strings.isNullOrEmpty(skinTagLineImage)?DEFAULT_SKIN_TAG_LINE_IMAGE:skinTagLineImage;
    }

    // function for retrieving the right logo, used by the header_bar
    public static String getRightLogo() {
        return gbBean.getRightLogo(); //Strings.isNullOrEmpty(skinRightLog)?DEFAULT_SKIN_RIGHT_LOGO:skinRightLog;
    }

    // function for retrieving the footer text
    public static String getFooter() {
        return gbBean.getFooter(); //Strings.isNullOrEmpty(skinFooter)?DEFAULT_SKIN_FOOTER:skinFooter;
    }
    // function for retrieving the studyview link text
    public static String getStudyviewLinkText() {
        return gbBean.getStudyviewLinkText(); //Strings.isNullOrEmpty(skinStudyViewLinkText)?DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT:skinStudyViewLinkText;
    }

    public static String getEmailContact() {
        return gbBean.getEmailContact(); //Strings.isNullOrEmpty(skinEmailContact)?DEFAULT_EMAIL_CONTACT:("<span class=\"mailme\" title=\"Contact us\">" + skinEmailContact + "</span>");
    }

    public static boolean includeNetworks() {
//        return Boolean.parseBoolean(properties.getProperty(INCLUDE_NETWORKS));
        return gbBean.includeNetworks(); //includeNetworks;
    }

    public static String getGoogleAnalyticsProfileId() {
        return gbBean.getGoogleAnalyticsProfileId(); //googleAnalyticsProfileId;
    }

    public static boolean genomespaceEnabled() {
        return gbBean.genomespaceEnabled(); //genomeSpace;
    }

    public static boolean showPlaceholderInPatientView() {
        return gbBean.showPlaceholderInPatientView(); //patientViewPlaceholder;
    }

    public static double[] getPatientViewGenomicOverviewCnaCutoff() {
        return gbBean.getPatientViewGenomicOverviewCnaCutoff(); //patientViewGenomicOverviewCnaCutoff;
    }

    public static boolean showNewsTab() {
        return gbBean.showNewsTab(); //skinShowNewsTab;
    }

    public static boolean showDataTab() {
        return gbBean.showDataTab(); //skinShowDataTab;
    }

    // show or hide the web api tab in header navigation bar
    public static boolean showWebApiTab() {
        return gbBean.showWebApiTab(); //skinShowWebApiTab;
    }
    // show or hide the r matlab tab in header navigation bar
    public static boolean showRMatlabTab() {
        return gbBean.showRMatlabTab(); //skinShowRMatlabTab;
    }
    // show or hide the tutorial tab in header navigation bar
    public static boolean showTutorialsTab() {
        return gbBean.showTutorialsTab(); //skinShowTutorialsTab;
    }
    // show or hide the faqs tab in header navigation bar
    public static boolean showFaqsTab() {
        return gbBean.showFaqsTab(); //skinShowFaqsTab;
    }
    // show or hide the tools tab in header navigation bar
    public static boolean showToolsTab() {
        return gbBean.showToolsTab(); //skinShowToolsTab;
    }
    // show or hide the about tab in header navigation bar
    public static boolean showAboutTab() {
        return gbBean.showAboutTab(); //skinShowAboutTab;
    }
    // show or hide the visualize your data tab in header navigation bar
    public static boolean showVisualizeYourDataTab() {
        return gbBean.showVisualizeYourDataTab(); //skinShowVisualizeYourDataTab;
    }

    // show the clinical trials tab in the patient view
    public static boolean showClinicalTrialsTab() {
        return gbBean.showClinicalTrialsTab(); //skinPatientViewShowClinicalTrialsTab;
    }

    // show the drugs tab in the patient view
    public static boolean showDrugsTab() {
        return gbBean.showDrugsTab(); //skinPatientViewShowDrugsTab;
    }
    // get the text for the What's New in the right navigation bar
    public static String getRightNavWhatsNewBlurb() {
        return gbBean.getRightNavWhatsNewBlurb(); //Strings.isNullOrEmpty(skinRightNavWhatsNewBlurb)?DEFAULT_SKIN_WHATS_NEW_BLURB:skinRightNavWhatsNewBlurb;
    }
    public static boolean showRightNavDataSets() {
        return gbBean.showRightNavDataSets(); //skinRightNavShowDataSets;
    }
    public static boolean showRightNavExamples() {
        return gbBean.showRightNavExamples(); //skinRightNavShowExamples;
    }

    public static boolean showRightNavTestimonials() {
        return gbBean.showRightNavTestimonials(); //skinRightNavShowTestimonials;
    }

    public static String getAuthorizationMessage() {
        return gbBean.getAuthorizationMessage(); //Strings.isNullOrEmpty(skinAuthorizationMessage)?DEFAULT_AUTHORIZATION_MESSAGE:skinAuthorizationMessage;
    }

    public static String getExampleStudyQueries() {
        return gbBean.getExampleStudyQueries(); //Strings.isNullOrEmpty(skinExampleStudyQueries)?DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES:skinExampleStudyQueries;
    }

    // added usage of default data sets header
    public static String getDataSetsHeader() {
        return gbBean.getDataSetsHeader(); //Strings.isNullOrEmpty(skinDataSetsHeader)?DEFAULT_SKIN_DATASETS_HEADER:skinDataSetsHeader;
    }

    // added usage of default data sets footer
    public static String getDataSetsFooter() {
        return gbBean.getDataSetsFooter(); //Strings.isNullOrEmpty(skinDataSetsFooter)?DEFAULT_SKIN_DATASETS_FOOTER:skinDataSetsFooter;
    }

    public static String getLinkToPatientView(String caseId, String cancerStudyId) {
        return "case.do?" + QueryBuilder.CANCER_STUDY_ID + "=" + cancerStudyId
                 //+ "&"+ org.mskcc.cbio.portal.servlet.PatientView.PATIENT_ID + "=" + caseId;
                 + "&"+ org.mskcc.cbio.portal.servlet.PatientView.SAMPLE_ID + "=" + caseId;
    }

    public static String getLinkToSampleView(String caseId, String cancerStudyId) {
        return "case.do?" + QueryBuilder.CANCER_STUDY_ID + "=" + cancerStudyId
                 + "&"+ org.mskcc.cbio.portal.servlet.PatientView.SAMPLE_ID + "=" + caseId;
    }

    public static String getLinkToCancerStudyView(String cancerStudyId) {
        return "study?" + org.mskcc.cbio.portal.servlet.CancerStudyView.ID
                + "=" + cancerStudyId;
    }

    public static String getLinkToIGVForBAM(String cancerStudyId, String caseId, String locus) {
        return ("igvlinking.json?" +
				org.mskcc.cbio.portal.servlet.IGVLinkingJSON.CANCER_STUDY_ID +
                "=" + cancerStudyId +
				"&" + org.mskcc.cbio.portal.servlet.IGVLinkingJSON.CASE_ID +
				"=" + caseId +
				"&" + org.mskcc.cbio.portal.servlet.IGVLinkingJSON.LOCUS +
				"=" + locus);
    }

    public static String getDigitalSlideArchiveIframeUrl(String caseId) {
        return gbBean.getDigitalSlideArchiveIframeUrl() + caseId; //digitalSlideArchiveIframeUrl+caseId;
    }

    public static String getDigitalSlideArchiveMetaUrl(String caseId) {
        return gbBean.getDigitalSlideArchiveMetaUrl() + caseId; //digitalSlideArchiveMetaUrl+caseId;
    }

    public static String getStudyHeatmapMetaUrl()
    {
//        String url = properties.getProperty(STUDY_VIEW_MDACC_HEATMAP_META_URL);
        return gbBean.getStudyHeatmapMetaUrl(); //studyViewMdaccHeatmapMetaUrl;
    }

    public static String getStudyHeatmapViewerUrl()
    {
//        String url = properties.getProperty(STUDY_VIEW_MDACC_HEATMAP_URL);
        return gbBean.getStudyHeatmapViewerUrl(); //studyViewMdaccHeatmapUrl;
    }

    public static String getPatientHeatmapMetaUrl(String caseId)
    {
//        String url = properties.getProperty(PATIENT_VIEW_MDACC_HEATMAP_META_URL);
        String url = gbBean.getPatientHeatmapMetaUrl();
        if (url == null || url.length() == 0) return null;
        
        return url + caseId;
    }

    public static String getPatientHeatmapViewerUrl(String caseId)
    {
//        String url = properties.getProperty(PATIENT_VIEW_MDACC_HEATMAP_URL);
        String url = gbBean.getPatientHeatmapViewerUrl();
        if (url == null || url.length() == 0) return null;
        return url + caseId;
    }

    public static String getTCGAPathReportUrl() {
        return gbBean.getTCGAPathReportUrl(); //tcgaPathReportUrl;
    }

    // function for getting the custom tabs for the header
    public static String[] getCustomHeaderTabs() {
        return gbBean.getCustomHeaderTabs(); //skinCustomHeaderTabs;
    }
   
    public static String getSessionServiceUrl() {
        return gbBean.getSessionServiceUrl(); //sessionServiceUrl;
    }
 
    public static String getOncoKBUrl() {
        //Test connection of OncoKB website.
        String oncoKbUrl = gbBean.getOncoKBUrl();
        if(!Strings.isNullOrEmpty(oncoKbUrl)) {
            try {
                URL url = new URL(oncoKbUrl+"access");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() != 200) {
                    oncoKbUrl = "";
                }
                conn.disconnect();
                return oncoKbUrl;
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    public static boolean showHotspot() {
        return gbBean.showHotspot(); //showHotspot;
    }

    public static boolean filterGroupsByAppName() {
        return gbBean.filterGroupsByAppName(); //filterGroupsByAppName;
    }
    
    public static String getAlwaysShowStudyGroup() {
        return gbBean.getAlwaysShowStudyGroup(); //alwaysShowStudyGroup!=null && alwaysShowStudyGroup.trim().isEmpty())?null:alwaysShowStudyGroup;
    }
    
    public static boolean showMyCancerGenomeUrl() {
        return gbBean.showMyCancerGenomeUrl(); //myCancerGenomeShow;
    }
    
    public static String getOncoKBGeneStatus() {
        return gbBean.getOncoKBGeneStatus(); //oncokbGeneStatus;
    }
    
    public static boolean getRecacheStudyAfterUpdate() {
        return gbBean.getRecacheStudyAfterUpdate(); //recacheStudyAfterUpdate;
    }
    
    public static String getDbVersion() {
        return gbBean.getDbVersion(); //dbVersion;
    }
    
    public static String getDarwinAuthCheckUrl() {
        return gbBean.getDarwinAuthCheckUrl(); //darwinAuthUrl;
    }
    
    public static String getDarwinResponseUrl() {
        return gbBean.getDarwinResponseUrl(); //darwinResponseUrl;
    }
    
    public static String getDarwinAuthority() { 
        return gbBean.getDarwinAuthority(); //darwinAuthority;
    }
    
    public static Map<String, Set<String>> getPriorityStudies() {        
//        Map<String, Set<String>> priorityStudiesObject = new HashMap<>();
//        if (!Strings.isNullOrEmpty(priorityStudies)) {
//            try {
//                for (String priorityStudyCategory: priorityStudies.split(";")) {
//                        String[] elements = priorityStudyCategory.split("[#,]");
//                        String category = elements[0];
//                        Set<String> studies = new HashSet<>();
//                        for (int i=1; i<elements.length; i++) {
//                                studies.add(elements[i]);
//                        }
//                        if (studies.size() > 0) {
//                            priorityStudiesObject.put(category, studies);
//                        }
//                }
//            } catch (NullPointerException e) {}                
//        }

        return gbBean.getPriorityStudies(); //priorityStudiesObject;
    }
    
    public static String getCisUser() {
        return gbBean.getCisUser(); //cisUser;         
    }
    
    public static List<String> getDisabledTabs() {
//        String[] tabs = disabledTabs.trim().split("\\|");
//        return (tabs.length > 0 && !disabledTabs.isEmpty())?Arrays.asList(tabs):new ArrayList<String>();
        return gbBean.getDisabledTabs(); 
    }

    public static void main(String[] args) {
        System.out.println(getAppVersion());
    }

    /**
     * @return the openSslBinary
     */
    public static String getOpenSslBinary() {
        return gbBean.getOpenSslBinary(); //openSslBinary;
    }

    /**
     * @return the signatureKey
     */
    public static String getSignatureKey() {
        return gbBean.getSignatureKey(); //signatureKey;
    }

    /**
     * @return the encryptionKey
     */
    public static String getEncryptionKey() {
        return gbBean.getEncryptionKey(); //encryptionKey;
    }

    /**
     * @return the broadBamUrl
     */
    public static String getBroadBamUrl() {
        return gbBean.getBroadBamUrl(); //broadBamUrl;
    }

    /**
     * @return the broadBamCheckingUrl
     */
    public static String getBroadBamCheckingUrl() {
        return gbBean.getBroadBamCheckingUrl(); //broadBamCheckingUrl;
    }

    /**
     * @return the globalCss
     */
    public String getGlobalCss() {
        return gbBean.getGlobalCss(); //globalCss;
    }

    /**
     * @return the specialCss
     */
    public String getSpecialCss() {
        return gbBean.getSpecialCss(); //specialCss;
    }

    /**
     * @return the dataSets
     */
    public String getDataSets() {
        return gbBean.getDataSets(); //dataSets;
    }

    /**
     * @return the popeye
     */
    public String getPopeye() {
        return gbBean.getPopeye(); //popeye;
    }

    /**
     * @return the faq
     */
    public String getFaq() {
        return gbBean.getFaq(); //faq;
    }

}
