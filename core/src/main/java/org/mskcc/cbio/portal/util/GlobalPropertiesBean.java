/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.util;

import java.util.*;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

/**
 *
 * @author ochoaa
 */
@Configuration
@PropertySources({@PropertySource(value="portal.properties", ignoreResourceNotFound=true), 
    @PropertySource(value="file:classpath/portal.properties")})
@Scope("singleton")
public class GlobalPropertiesBean {

    @Value("${pathway_commons.url}")
    private String pathwayCommonsUrl;    
    
    @Value("${ucsc_cancer_genomics.url}")
    private String ucscCancerGenomicsUrl;
    
    @Value("${segfile.url}")
    private String segFileUrl;

    @Value("${openssl.binary}")
    private String openSslBinary;
    
    @Value("${signature.key}")
    private String signatureKey;
    
    @Value("${encryption.key}")
    private String encryptionKey;
    
    @Value("${broad.bam.url}")
    private String broadBamUrl;
    
    @Value("${broad.bam.checking.url}")
    private String broadBamCheckingUrl;
    
    @Value("${igv.bam.linking:false}")
    private boolean igvBamLinking;
    
    @Value("${igv.bam.linking.studies}")
    private String igvBamLinkingStudies;
    
    // default value = "false"
    @Value("${authenticate:false}")
    private String authenticate;
    
    @Value("${authorization}")
    private boolean authorization;
    
    @Value("${filter_groups_by_appname:true}")
    private boolean filterGroupsByAppName;
    
    @Value("${include_networks}")
    private boolean includeNetworks;
    
    @Value("${google_analytics_profile_id}")
    private String googleAnalyticsProfileId;
    
    @Value("${genomespace}")
    private boolean genomeSpace;

    @Value("${app.name}")
    private String appName;
    private final String DEFAULT_APP_NAME = "public_portal";
    
    // default value = 1.0
    @Value("${app.version:1.0}")
    private String appVersion;
    
    @Value("${skin.title}")
    private String skinTitle;    
    private final String DEFAULT_SKIN_TITLE = "cBioPortal for Cancer Genomics";
    
    
    @Value("${skin.blurb}")
    private String skinBlurb;    
    private final String DEFAULT_SKIN_BLURB = "The cBioPortal for Cancer Genomics provides " +
            "<b>visualization</b>, <b>analysis</b> and <b>download</b> of large-scale cancer genomics data sets. " +
            "<p>Please adhere to <u><a href=\"http://cancergenome.nih.gov/abouttcga/policies/publicationguidelines\"> " +
            "the TCGA publication guidelines</a></u> when using TCGA data in your publications.</p> " +
            "<p><b>Please cite</b> <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/23550210\">Gao " +
            "et al. <i>Sci. Signal.</i> 2013</a> &amp;  " +
            "<a href=\"http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract\">" +
            "Cerami et al. <i>Cancer Discov.</i> 2012</a> when publishing results based on cBioPortal.</p>\n";
    
    @Value("${skin.tag_line_image}")
    private String skinTagLineImage;    
    private final String DEFAULT_SKIN_TAG_LINE_IMAGE = "images/tag_line.png";
        
    @Value("${skin.email_contact}")
    private String skinEmailContact;
    private final String DEFAULT_EMAIL_CONTACT = "cbioportal at googlegroups dot com";
    
    @Value("${skin.show_news_tab:true}")
    private boolean skinShowNewsTab;
    
    @Value("${skin.show_data_tab:true}")
    private boolean skinShowDataTab;
    
    @Value("${skin.right_nav.show_data_sets:true}")
    private boolean skinRightNavShowDataSets;
    
    @Value("${skin.right_nav.show_examples:true}")
    private boolean skinRightNavShowExamples;
    
    @Value("${skin.right_nav.show_testimonials:true}")
    private boolean skinRightNavShowTestimonials;
    
    @Value("${skin.authorization_message}")
    private String skinAuthorizationMessage;    
    private final String DEFAULT_AUTHORIZATION_MESSAGE = "Access to this portal is only available to authorized users.";
    
    @Value("${skin.example_study_queries}")
    private String skinExampleStudyQueries;
    private final String DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES =            
            "tcga\n" +
            "tcga -provisional\n" +
            "tcga -moratorium\n" +
            "tcga OR icgc\n" +
            "-\"cell line\"\n" +
            "prostate mskcc\n" +
            "esophageal OR stomach\n" +
            "serous\n" +
            "breast";

    @Value("${skin.data_sets_header}")
    private String skinDataSetsHeader;
    private final String DEFAULT_SKIN_DATASETS_HEADER = "The portal currently contains data from the following " +
            "cancer genomics studies.  The table below lists the number of available samples per data type and tumor.";
    
    @Value("${skin.data_sets_footer}")
    private String skinDataSetsFooter;
    private final String DEFAULT_SKIN_DATASETS_FOOTER = "Data sets of TCGA studies were downloaded from Broad " +
            "Firehose (http://gdac.broadinstitute.org) and updated monthly. In some studies, data sets were from the " +
            "TCGA working groups directly.";
    
    @Value("${patient_view_placeholder:false}")
    private boolean patientViewPlaceholder;
    
    // default value = 0.2,1.5
    @Value("${patient_view_genomic_overview_cna_cutoff:0.2,1.5}")
    private  double[] patientViewGenomicOverviewCnaCutoff; // default value = 0.2,1.5    

    @Value("${mdacc.heatmap.meta.url}")
    private String patientViewMdaccHeatmapMetaUrl;

    @Value("${mdacc.heatmap.patient.url}")
    private String patientViewMdaccHeatmapUrl;

    @Value("${mdacc.heatmap.study.url}")
    private String studyViewMdaccHeatmapUrl;

    @Value("${mdacc.heatmap.study.meta.url}")
    private String studyViewMdaccHeatmapMetaUrl;

    @Value("${digitalslidearchive.iframe.url}")
    private String digitalSlideArchiveIframeUrl;
    
    @Value("${digitalslidearchive.meta.url}")
    private String digitalSlideArchiveMetaUrl;
    
    @Value("${tcga_path_report.url}")
    private String tcgaPathReportUrl;
    
    @Value("${oncokb.url}")
    private String oncokbUrl;
    
    @Value("${session.service.url}")
    private String sessionServiceUrl;
    
    // properties for showing the right logo in the header_bar and default logo
    @Value("${skin.right_logo}")
    private String skinRightLog;    
    private final String DEFAULT_SKIN_RIGHT_LOGO = "images/mskcc_logo_3d_grey.jpg";   

    // properties for hiding/showing tabs in the header navigation bar
    @Value("${skin.show_web_api_tab:true}")
    private boolean skinShowWebApiTab;
    
    @Value("${skin.show_r_matlab_tab:true}")
    private boolean skinShowRMatlabTab;
    
    @Value("${skin.show_tutorials_tab:true}")
    private boolean skinShowTutorialsTab;
    
    @Value("${skin.show_faqs_tab:true}")
    private boolean skinShowFaqsTab;
    
    @Value("${skin.show_tools_tab:true}")
    private boolean skinShowToolsTab;
    
    @Value("${skin.show_about_tab:true}")
    private boolean skinShowAboutTab;
    
    @Value("${skin.show_visualize_your_data_tab:true}")
    private boolean skinShowVisualizeYourDataTab;
    
    // property for setting the news blurb in the right column
    @Value("${skin.right_nav.whats_new_blurb}")
    private String skinRightNavWhatsNewBlurb;    
    private final String DEFAULT_SKIN_WHATS_NEW_BLURB = 
            "<form action=\"http://groups.google.com/group/cbioportal-news/boxsubscribe\"> &nbsp;&nbsp;&nbsp;&nbsp;" +
            "<b>Sign up for low-volume email news alerts:</b></br> &nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" " +
            "name=\"email\" title=\"Subscribe to mailing list\"> <input type=\"submit\" name=\"sub\" value=\"Subscribe\"> " +
            "</form> &nbsp;&nbsp;&nbsp;&nbsp;<b>Or follow us <a href=\"http://www.twitter.com/cbioportal\">" +
            "<i>@cbioportal</i></a> on Twitter</b>\n";
    
    // footer
    @Value("${skin.footer}")
    private String skinFooter;
    private final String DEFAULT_SKIN_FOOTER = " | <a href=\"http://www.mskcc.org/mskcc/html/44.cfm\">MSKCC</a>" +
            " | <a href=\"http://cancergenome.nih.gov/\">TCGA</a>";
    
    // login contact
    @Value("${skin.login.contact_html}")
    private String skinLoginContactHtml;
    private final String DEFAULT_SKIN_LOGIN_CONTACT_HTML = "If you think you have received this message in " +
            "error, please contact us at <a style=\"color:#FF0000\" href=\"mailto:cbioportal-access@cbio.mskcc.org\">" +
            "cbioportal-access@cbio.mskcc.org</a>";
    
    // properties for hiding/showing tabs in the patient view
    @Value("${skin.patient_view.show_clinical_trials_tab:false}")
    private boolean skinPatientViewShowClinicalTrialsTab;
    
    @Value("${skin.patient_view.show_drugs_tab:false}")
    private boolean skinPatientViewShowDrugsTab;
    
    // property for setting the saml registration html
    @Value("${skin.login.saml.registration_html}")
    private String skinLoginSamlRegistrationHtml;
    private final String DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML = "Sign in with MSK";
    
    // property for the saml entityid
    @Value("${saml.idp.metadata.entityid}")
    private String samlIdpMetadataEntityId;    

    // property for the custom header tabs
    @Value("${skin.custom_header_tabs}")
    private String[] skinCustomHeaderTabs;
        
    // properties for the FAQ, about us, news and examples
    @Value("${skin.documentation.baseurl}")
    private String skinDocumentationBaseUrl;
    private final String DEFAULT_SKIN_BASEURL = "https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/";
    
    @Value("${skin.documentation.markdown:true}")
    private boolean skinDocumentationMarkdown;
    
    @Value("${skin.documentation.faq}")
    private String skinDocumentationFaq;
    private final String DEFAULT_SKIN_FAQ = "FAQ.md";
    
    @Value("${skin.documentation.about}")
    private String skinDocumentationAbout;
    private final String DEFAULT_SKIN_ABOUT = "About-Us.md";
        
    @Value("${skin.documentation.news}")
    private String skinDocumentationNews;
    private final String DEFAULT_SKIN_NEWS = "News.md";

    @Value("${skin.examples_right_column}")
    private String skinExamplesRightColumn;
    private final String DEFAULT_SKIN_EXAMPLES_RIGHT_COLUMN = "../../../content/examples.html";

    @Value("${always_show_study_group}")
    private String alwaysShowStudyGroup;
    
    // property for text shown at the right side of the Select Patient/Case set, which
    // links to the study view
    @Value("${skin.study_view.link_text}")
    private String skinStudyViewLinkText;
    private final String DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT = "To build your own case set, try out our enhanced " +            
            "Study View.";    

    @Value("${mycancergenome.show:false}")
    private boolean myCancerGenomeShow;
    
    @Value("${oncokb.geneStatus}")
    private String oncokbGeneStatus;
    
    @Value("${show.hotspot:true}")
    private boolean showHotspot;
        
    @Value("${recache_study_after_update:false}")
    private boolean recacheStudyAfterUpdate;    
    
    // default value = 0
    @Value("${db.version:0}")
    private String dbVersion;
        
    @Value("${darwin.auth_url:}")
    private String darwinAuthUrl;
    
    @Value("${priority_studies}")
    private String priorityStudies;

    @Value("${darwin.response_url:}")
    private String darwinResponseUrl;
    
    @Value("${darwin.authority:}")
    private String darwinAuthority;
    
    @Value("${cis.user:}")
    private String cisUser;
    
    @Value("${disabled_tabs:}")
    private String disabledTabs;
    
    // css properties
    @Value("${global_css}")
    private String globalCss;
    
    @Value("${special_css}")
    private String specialCss;
    
    @Value("${data_sets}")
    private String dataSets;
    
    @Value("${popeye}")
    private String popeye;
    
    @Value("${faq}")
    private String faq;

    public String getPathwayCommonsUrl() {
        return pathwayCommonsUrl;
    }
    
    public String getUcscCancerGenomicsUrl() {
        return ucscCancerGenomicsUrl;
    }

    public String getSegfileUrl() {
        return segFileUrl;
    }

    public boolean wantIGVBAMLinking() {
        return igvBamLinking;
    }

    public Collection<String> getIGVBAMLinkingStudies() {
        if (igvBamLinkingStudies==null) {
            return Collections.emptyList();
        }
        String[] studies = igvBamLinkingStudies.split(":");
        return (studies.length > 0) ? Arrays.asList(studies) : Collections.<String>emptyList();
    }

    public boolean usersMustAuthenticate() {
        return !authenticate.equals("false");
    }

    public String authenticationMethod() {
        return authenticate;
    }

    public boolean usersMustBeAuthorized() {
        return authorization;
    }

    public String getAppName() {
        return Strings.isNullOrEmpty(appName)?DEFAULT_APP_NAME:appName;
    }

    public String getAppVersion() {
        return appVersion;
    }
    
    public String getTitle() {
        return Strings.isNullOrEmpty(skinTitle)?DEFAULT_SKIN_TITLE:skinTitle;
    }
    
    // updated function to use a default if nothing is specified
    public String getBlurb() {
        return Strings.isNullOrEmpty(skinBlurb)?DEFAULT_SKIN_BLURB:skinBlurb;
    }
    // get custom FAQ html or the default
    public String getFaqHtml() {
        return Strings.isNullOrEmpty(skinDocumentationFaq)?DEFAULT_SKIN_FAQ:skinDocumentationFaq;
    }
    // get custom About html or the default
    public String getAboutHtml() {
        return Strings.isNullOrEmpty(skinDocumentationAbout)?DEFAULT_SKIN_ABOUT:skinDocumentationAbout;
    }
    // get custom News html or the default
    public String getNewsHtml() {
        return skinDocumentationNews==null?DEFAULT_SKIN_NEWS:getContentString(skinDocumentationNews);
    }
    // get custom News html or the default
    public String getBaseUrl() {
        return Strings.isNullOrEmpty(skinDocumentationBaseUrl)?DEFAULT_SKIN_BASEURL:skinDocumentationBaseUrl;
    }
    public boolean isMarkdownDocumentation() {
        return skinDocumentationMarkdown;
    }

    // get custom Example Queries for the right column html or the default
    public String getExamplesRightColumnHtml() {
        return Strings.isNullOrEmpty(skinExamplesRightColumn)?DEFAULT_SKIN_EXAMPLES_RIGHT_COLUMN:"../../../content/"+skinExamplesRightColumn;
    }

    private String getContentString(String contentString) {
        if(getBaseUrl().equalsIgnoreCase("")) return "content/"+contentString;
        return contentString;
    }

    // get the login contact html
    public String getLoginContactHtml() {
        return Strings.isNullOrEmpty(skinLoginContactHtml)?DEFAULT_SKIN_LOGIN_CONTACT_HTML:skinLoginContactHtml;
    }
    // get the text for the saml login button
    public String getLoginSamlRegistrationHtml() {
        return Strings.isNullOrEmpty(skinLoginSamlRegistrationHtml)?DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML:skinLoginSamlRegistrationHtml;
    }
    public String getSamlIdpMetadataEntityid() {
        return samlIdpMetadataEntityId;
    }
    public String getTagLineImage() {
        return Strings.isNullOrEmpty(skinTagLineImage)?DEFAULT_SKIN_TAG_LINE_IMAGE:skinTagLineImage;
    }

    // function for retrieving the right logo, used by the header_bar
    public String getRightLogo() {
        return Strings.isNullOrEmpty(skinRightLog)?DEFAULT_SKIN_RIGHT_LOGO:skinRightLog;
    }

    // function for retrieving the footer text
    public String getFooter() {
        return Strings.isNullOrEmpty(skinFooter)?DEFAULT_SKIN_FOOTER:skinFooter;
    }
    // function for retrieving the studyview link text
    public String getStudyviewLinkText() {
        return Strings.isNullOrEmpty(skinStudyViewLinkText)?DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT:skinStudyViewLinkText;
    }

    public String getEmailContact() {
        return Strings.isNullOrEmpty(skinEmailContact)?DEFAULT_EMAIL_CONTACT:("<span class=\"mailme\" title=\"Contact us\">" + skinEmailContact + "</span>");
    }

    public boolean includeNetworks() {
        return includeNetworks;
    }

    public String getGoogleAnalyticsProfileId() {
        return googleAnalyticsProfileId;
    }

    public boolean genomespaceEnabled() {
        return genomeSpace;
    }

    public boolean showPlaceholderInPatientView() {
        return patientViewPlaceholder;
    }

    public double[] getPatientViewGenomicOverviewCnaCutoff() {
        return patientViewGenomicOverviewCnaCutoff;
    }

    public boolean showNewsTab() {
        return skinShowNewsTab;
    }

    public boolean showDataTab() {
        return skinShowDataTab;
    }

    // show or hide the web api tab in header navigation bar
    public boolean showWebApiTab() {
        return skinShowWebApiTab;
    }
    // show or hide the r matlab tab in header navigation bar
    public boolean showRMatlabTab() {
        return skinShowRMatlabTab;
    }
    // show or hide the tutorial tab in header navigation bar
    public boolean showTutorialsTab() {
        return skinShowTutorialsTab;
    }
    // show or hide the faqs tab in header navigation bar
    public boolean showFaqsTab() {
        return skinShowFaqsTab;
    }
    // show or hide the tools tab in header navigation bar
    public boolean showToolsTab() {
        return skinShowToolsTab;
    }
    // show or hide the about tab in header navigation bar
    public boolean showAboutTab() {
        return skinShowAboutTab;
    }
    // show or hide the visualize your data tab in header navigation bar
    public boolean showVisualizeYourDataTab() {
        return skinShowVisualizeYourDataTab;
    }

    // show the clinical trials tab in the patient view
    public boolean showClinicalTrialsTab() {
        return skinPatientViewShowClinicalTrialsTab;
    }

    // show the drugs tab in the patient view
    public boolean showDrugsTab() {
        return skinPatientViewShowDrugsTab;
    }
    // get the text for the What's New in the right navigation bar
    public String getRightNavWhatsNewBlurb() {
        return Strings.isNullOrEmpty(skinRightNavWhatsNewBlurb)?DEFAULT_SKIN_WHATS_NEW_BLURB:skinRightNavWhatsNewBlurb;
    }
    public boolean showRightNavDataSets() {
        return skinRightNavShowDataSets;
    }
    public boolean showRightNavExamples() {
        return skinRightNavShowExamples;
    }

    public boolean showRightNavTestimonials() {
        return skinRightNavShowTestimonials;
    }

    public String getAuthorizationMessage() {
        return Strings.isNullOrEmpty(skinAuthorizationMessage)?DEFAULT_AUTHORIZATION_MESSAGE:skinAuthorizationMessage;
    }

    public String getExampleStudyQueries() {
        return Strings.isNullOrEmpty(skinExampleStudyQueries)?DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES:skinExampleStudyQueries;
    }

    // added usage of default data sets header
    public String getDataSetsHeader() {
        return Strings.isNullOrEmpty(skinDataSetsHeader)?DEFAULT_SKIN_DATASETS_HEADER:skinDataSetsHeader;
    }

    // added usage of default data sets footer
    public String getDataSetsFooter() {
        return Strings.isNullOrEmpty(skinDataSetsFooter)?DEFAULT_SKIN_DATASETS_FOOTER:skinDataSetsFooter;
    }

    public String getDigitalSlideArchiveIframeUrl() {
        return digitalSlideArchiveIframeUrl;
    }

    public String getDigitalSlideArchiveMetaUrl() {
        return digitalSlideArchiveMetaUrl;
    }

    public String getStudyHeatmapMetaUrl()
    {
        return studyViewMdaccHeatmapMetaUrl;
    }

    public String getStudyHeatmapViewerUrl()
    {
        return studyViewMdaccHeatmapUrl;
    }

    public String getPatientHeatmapMetaUrl()
    {
        return patientViewMdaccHeatmapMetaUrl;
    }

    public String getPatientHeatmapViewerUrl()
    {
        return patientViewMdaccHeatmapUrl;
    }

    public String getTCGAPathReportUrl() {
        return tcgaPathReportUrl;
    }

    // function for getting the custom tabs for the header
    public String[] getCustomHeaderTabs() {
        return skinCustomHeaderTabs;
    }
   
    public String getSessionServiceUrl() {
        return sessionServiceUrl;
    }
 
    public String getOncoKBUrl() {
        return oncokbUrl;
    }

    public boolean showHotspot() {
        return showHotspot;
    }

    public boolean filterGroupsByAppName() {
        return filterGroupsByAppName;
    }
    
    public String getAlwaysShowStudyGroup() {
        return (alwaysShowStudyGroup!=null && alwaysShowStudyGroup.trim().isEmpty())?null:alwaysShowStudyGroup;
    }
    
    public boolean showMyCancerGenomeUrl() {
        return myCancerGenomeShow;
    }
    
    public String getOncoKBGeneStatus() {
        return oncokbGeneStatus;
    }
    
    public boolean getRecacheStudyAfterUpdate() {
        return recacheStudyAfterUpdate;
    }
    
    public String getDbVersion() {
        return dbVersion;
    }
    
    public String getDarwinAuthCheckUrl() {
        return darwinAuthUrl;
    }
    
    public String getDarwinResponseUrl() {
        return darwinResponseUrl;
    }
    
    public String getDarwinAuthority() { 
        return darwinAuthority;
    }
    
    public Map<String, Set<String>> getPriorityStudies() {        
        Map<String, Set<String>> priorityStudiesObject = new HashMap<>();
        if (!Strings.isNullOrEmpty(priorityStudies)) {
            try {
                for (String priorityStudyCategory: priorityStudies.split(";")) {
                    String[] elements = priorityStudyCategory.split("[#,]");
                    String category = elements[0];
                    Set<String> studies = new HashSet<>();
                    for (int i=1; i<elements.length; i++) {
                            studies.add(elements[i]);
                    }
                    if (studies.size() > 0) {
                        priorityStudiesObject.put(category, studies);
                    }
                }
            } catch (NullPointerException e) {}                
        }

        return priorityStudiesObject;
    }
    
    public String getCisUser() {
        return cisUser;         
    }
    
    public List<String> getDisabledTabs() {
        String[] tabs = disabledTabs.trim().split("\\|");
        return (tabs.length > 0 && !disabledTabs.isEmpty())?Arrays.asList(tabs):new ArrayList<String>();
    }

    public void main(String[] args) {
        System.out.println(getAppVersion());
    }

    /**
     * @return the openSslBinary
     */
    public String getOpenSslBinary() {
        return openSslBinary;
    }

    /**
     * @return the signatureKey
     */
    public String getSignatureKey() {
        return signatureKey;
    }

    /**
     * @return the encryptionKey
     */
    public String getEncryptionKey() {
        return encryptionKey;
    }

    /**
     * @return the broadBamUrl
     */
    public String getBroadBamUrl() {
        return broadBamUrl;
    }

    /**
     * @return the broadBamCheckingUrl
     */
    public String getBroadBamCheckingUrl() {
        return broadBamCheckingUrl;
    }

    /**
     * @return the globalCss
     */
    public String getGlobalCss() {
        return globalCss;
    }

    /**
     * @return the specialCss
     */
    public String getSpecialCss() {
        return specialCss;
    }

    /**
     * @return the dataSets
     */
    public String getDataSets() {
        return dataSets;
    }

    /**
     * @return the popeye
     */
    public String getPopeye() {
        return popeye;
    }

    /**
     * @return the faq
     */
    public String getFaq() {
        return faq;
    }
}
