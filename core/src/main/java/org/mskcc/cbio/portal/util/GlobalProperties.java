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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.util.*;
import java.net.URL;


/**
 * Utility class for getting / setting global properties.
 */
@Component
public class GlobalProperties {

    public static final String HOME_DIR = "PORTAL_HOME";
    private static final String propertiesFilename = "portal.properties";

    public static final String PATHWAY_COMMONS_URL = "pathway_commons.url";
    public static final String UCSC_CANCER_GENOMICS_URL = "ucsc_cancer_genomics.url";
	public static final String SEGFILE_URL = "segfile.url";
	public static final String OPENSSL_BINARY = "openssl.binary";
	public static final String SIGNATURE_KEY = "signature.key";
	public static final String ENCRYPTION_KEY = "encryption.key";
	public static final String BROAD_BAM_URL = "broad.bam.url";
	public static final String BROAD_BAM_CHECKING_URL = "broad.bam.checking.url";
	public static final String IGV_BAM_LINKING = "igv.bam.linking";
	public static final String IGV_BAM_LINKING_STUDIES = "igv.bam.linking.studies";
    public static final String AUTHENTICATE = "authenticate";
    public static final String AUTHORIZATION = "authorization";
    public static final String FILTER_GROUPS_BY_APPNAME = "filter_groups_by_appname";
    public static final String INCLUDE_NETWORKS = "include_networks";
    public static final String GOOGLE_ANALYTICS_PROFILE_ID = "google_analytics_profile_id";
    public static final String GENOMESPACE = "genomespace";

    public static final String APP_NAME = "app.name";
    public static final String DEFAULT_APP_NAME = "public_portal";
    
    public static final String APP_VERSION = "app.version";
    public static final String SKIN_TITLE = "skin.title";
    public static final String DEFAULT_SKIN_TITLE = "cBioPortal for Cancer Genomics";
    public static final String SKIN_BLURB = "skin.blurb";
    // blurb default
    public static final String DEFAULT_SKIN_BLURB = "The cBioPortal for Cancer Genomics provides " +
            "<b>visualization</b>, <b>analysis</b> and <b>download</b> of large-scale cancer genomics data sets. " +
            "<p>Please adhere to <u><a href=\"http://cancergenome.nih.gov/abouttcga/policies/publicationguidelines\"> " +
            "the TCGA publication guidelines</a></u> when using TCGA data in your publications.</p> " +
            "<p><b>Please cite</b> <a href=\"http://www.ncbi.nlm.nih.gov/pubmed/23550210\">Gao " +
            "et al. <i>Sci. Signal.</i> 2013</a> &amp;  " +
            "<a href=\"http://cancerdiscovery.aacrjournals.org/content/2/5/401.abstract\">" +
            "Cerami et al. <i>Cancer Discov.</i> 2012</a> when publishing results based on cBioPortal.</p>\n";
    public static final String SKIN_TAG_LINE_IMAGE = "skin.tag_line_image";
    public static final String DEFAULT_SKIN_TAG_LINE_IMAGE = "images/tag_line.png";
    public static final String SKIN_EMAIL_CONTACT = "skin.email_contact";
    public static final String DEFAULT_EMAIL_CONTACT = "cbioportal at googlegroups dot com";
    public static final String SKIN_SHOW_NEWS_TAB = "skin.show_news_tab";
    public static final String SKIN_SHOW_DATA_TAB = "skin.show_data_tab";
    public static final String SKIN_RIGHT_NAV_SHOW_DATA_SETS = "skin.right_nav.show_data_sets";
    public static final String SKIN_RIGHT_NAV_SHOW_EXAMPLES = "skin.right_nav.show_examples";
    public static final String SKIN_RIGHT_NAV_SHOW_TESTIMONIALS = "skin.right_nav.show_testimonials";
    public static final String SKIN_AUTHORIZATION_MESSAGE = "skin.authorization_message";
    public static final String DEFAULT_AUTHORIZATION_MESSAGE = "Access to this portal is only available to authorized users.";
    public static final String SKIN_EXAMPLE_STUDY_QUERIES = "skin.example_study_queries";
    public static final String DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES =
            "tcga\n" +
            "tcga -provisional\n" +
            "tcga -moratorium\n" +
            "tcga OR icgc\n" +
            "-\"cell line\"\n" +
            "prostate mskcc\n" +
            "esophageal OR stomach\n" +
            "serous\n" +
            "breast";
    public static final String SKIN_DATASETS_HEADER = "skin.data_sets_header";
    public static final String DEFAULT_SKIN_DATASETS_HEADER = "The portal currently contains data from the following " +
            "cancer genomics studies.  The table below lists the number of available samples per data type and tumor.";
    public static final String SKIN_DATASETS_FOOTER = "skin.data_sets_footer";
    public static final String DEFAULT_SKIN_DATASETS_FOOTER = "Data sets of TCGA studies were downloaded from Broad " +
            "Firehose (http://gdac.broadinstitute.org) and updated monthly. In some studies, data sets were from the " +
            "TCGA working groups directly.";

    public static final String PATIENT_VIEW_PLACEHOLDER = "patient_view_placeholder";
    public static final String PATIENT_VIEW_GENOMIC_OVERVIEW_CNA_CUTOFF = "patient_view_genomic_overview_cna_cutoff";
    public static final double[] DEFAULT_GENOMIC_OVERVIEW_CNA_CUTOFF = new double[]{0.2,1.5};
    public static final String PATIENT_VIEW_DIGITAL_SLIDE_IFRAME_URL = "digitalslidearchive.iframe.url";
    public static final String PATIENT_VIEW_DIGITAL_SLIDE_META_URL = "digitalslidearchive.meta.url";

    public static final String PATIENT_VIEW_MDACC_HEATMAP_META_URL = "mdacc.heatmap.meta.url";
    public static final String PATIENT_VIEW_MDACC_HEATMAP_URL = "mdacc.heatmap.patient.url";

    public static final String STUDY_VIEW_MDACC_HEATMAP_URL = "mdacc.heatmap.study.url";
    public static final String STUDY_VIEW_MDACC_HEATMAP_META_URL = "mdacc.heatmap.study.meta.url";

    public static final String ONCOKB_API_URL = "oncokb.api.url";
    public static final String ONCOKB_PUBLIC_API_URL = "oncokb.public_api.url";
    public static final String SHOW_ONCOKB = "show.oncokb";

    private static String sessionServiceURL;
    @Value("${session.service.url:}") // default is empty string
    public void setSessionServiceURL(String property) { sessionServiceURL = property; }

    // properties for showing the right logo in the header_bar and default logo
    public static final String SKIN_RIGHT_LOGO = "skin.right_logo";

    // properties for hiding/showing tabs in the header navigation bar
    public static final String SKIN_SHOW_WEB_API_TAB = "skin.show_web_api_tab";
    public static final String SKIN_SHOW_R_MATLAB_TAB = "skin.show_r_matlab_tab";
    public static final String SKIN_SHOW_TUTORIALS_TAB = "skin.show_tutorials_tab";
    public static final String SKIN_SHOW_FAQS_TAB = "skin.show_faqs_tab";
    public static final String SKIN_SHOW_TOOLS_TAB = "skin.show_tools_tab";
    public static final String SKIN_SHOW_ABOUT_TAB = "skin.show_about_tab";

    // property for setting the news blurb in the right column
    public static final String SKIN_RIGHT_NAV_WHATS_NEW_BLURB = "skin.right_nav.whats_new_blurb";
    public static final String DEFAULT_SKIN_WHATS_NEW_BLURB = 
            "<form action=\"https://groups.google.com/group/cbioportal-news/boxsubscribe\"> &nbsp;&nbsp;&nbsp;&nbsp;" +
            "<b>Sign up for low-volume email news alerts:</b></br> &nbsp;&nbsp;&nbsp;&nbsp;<input type=\"text\" " +
            "name=\"email\" title=\"Subscribe to mailing list\"> <input type=\"submit\" name=\"sub\" value=\"Subscribe\"> " +
            "</form> &nbsp;&nbsp;&nbsp;&nbsp;<b>Or follow us <a href=\"http://www.twitter.com/cbioportal\">" +
            "<i>@cbioportal</i></a> on Twitter</b>\n";

    // footer
    public static final String SKIN_FOOTER = "skin.footer";
    public static final String DEFAULT_SKIN_FOOTER = " | <a href=\"http://www.mskcc.org/mskcc/html/44.cfm\">MSKCC</a>" +
            " | <a href=\"http://cancergenome.nih.gov/\">TCGA</a>";

    // login contact
    public static final String SKIN_LOGIN_CONTACT_HTML = "skin.login.contact_html";
    public static final String DEFAULT_SKIN_LOGIN_CONTACT_HTML = "If you think you have received this message in " +
            "error, please contact us at <a style=\"color:#FF0000\" href=\"mailto:cbioportal-access@cbio.mskcc.org\">" +
            "cbioportal-access@cbio.mskcc.org</a>";

    // property for setting the saml registration html
    public static final String SKIN_LOGIN_SAML_REGISTRATION_HTML = "skin.login.saml.registration_html";
    public static final String DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML = "Sign in with MSK";

    // property for the saml entityid
    public static final String SAML_IDP_METADATA_ENTITYID="saml.idp.metadata.entityid";
    // property for whether the SAML logout should be local (at SP level) or global (at IDP level). Default: false (global)
    public static final String SAML_IS_LOGOUT_LOCAL="saml.logout.local";

    // property for the custom header tabs
    public static final String SKIN_CUSTOM_HEADER_TABS="skin.custom_header_tabs";

    // properties for the FAQ, about us, news and examples
    public static final String SKIN_BASEURL="skin.documentation.baseurl";
    public static final String DEFAULT_SKIN_BASEURL="https://raw.githubusercontent.com/cBioPortal/cbioportal/master/docs/";
    public static final String SKIN_DOCUMENTATION_MARKDOWN="skin.documentation.markdown";

    public static final String SKIN_FAQ="skin.documentation.faq";
    public static final String DEFAULT_SKIN_FAQ="FAQ.md";
    public static final String SKIN_ABOUT="skin.documentation.about";
    public static final String DEFAULT_SKIN_ABOUT="About-Us.md";
    public static final String SKIN_NEWS="skin.documentation.news";
    public static final String DEFAULT_SKIN_NEWS="News.md";

    public static final String SKIN_EXAMPLES_RIGHT_COLUMN_HTML="skin.examples_right_column_html";
    
    public static final String ALWAYS_SHOW_STUDY_GROUP="always_show_study_group";

    // property for query component
    public static final String SKIN_QUERY_MAX_TREE_DEPTH="skin.query.max_tree_depth";
    public static final Number DEFAULT_QUERY_MAX_TREE_DEPTH=3;
    
    // property for text shown at the right side of the Select Patient/Case set, which
    // links to the study view
    public static final String SKIN_STUDY_VIEW_LINK_TEXT="skin.study_view.link_text";
    public static final String DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT="To build your own case set, try out our enhanced " +
            "Study View.";

    public static final String MYCANCERGENOME_SHOW = "mycancergenome.show";
    public static final String ONCOKB_GENE_STATUS = "oncokb.geneStatus";
    public static final String SHOW_HOTSPOT = "show.hotspot";
    
    public static final String RECACHE_STUDY_AFTER_UPDATE = "recache_study_after_update";
    
    public static final String DB_VERSION = "db.version";
    private static boolean suppressSchemaVersionMismatchErrors;
    @Value("${db.suppress_schema_version_mismatch_errors:false}") // default is false
    public void setSuppressSchemaVersionMismatchErrors(String property) { suppressSchemaVersionMismatchErrors = Boolean.parseBoolean(property); }
    
    public static final String DARWIN_AUTH_URL = "darwin.auth_url";
    public static final String DARWIN_RESPONSE_URL = "darwin.response_url";
    public static final String CIS_USER = "cis.user";
    public static final String DISABLED_TABS = "disabled_tabs";
    public static final String BITLY_USER = "bitly.user";
    public static final String DARWIN_REGEX = "darwin.regex";
    
    public static final String PRIORITY_STUDIES = "priority_studies";
    public static final String SPECIES = "species";
    public static final String DEFAULT_SPECIES = "human";
    public static final String NCBI_BUILD = "ncbi.build";
    public static final String DEFAULT_NCBI_BUILD = "37";
    public static final String UCSC_BUILD = "ucsc.build";
    public static final String DEFAULT_UCSC_BUILD = "hg19";

    public static final String ONCOPRINT_DEFAULTVIEW = "oncoprint.defaultview";

    private static boolean showCivic;
    @Value("${show.civic:false}") // default is false
    public void setShowCivic(String property) { showCivic = Boolean.parseBoolean(property); }

	/*
     * Trim whitespace of url and append / if it does not exist. Return empty
     * string otherwise.
     */
	public static String parseUrl(String url)
    {
		String rv;

        if (!url.isEmpty()) {
            rv = url.trim();

            if (!rv.endsWith("/")) {
                rv += "/";
            }
        } else {
			rv = "";
		}

		return rv;
	}

	private static String civicUrl;
	@Value("${civic.url:https://civic.genome.wustl.edu/api/}") // default
	public void setCivicUrl(String property) { civicUrl = parseUrl(property); }

    private static String frontendUrl;
    @Value("${frontend.url:}") // default is empty string
    public void setFrontendUrl(String property) { frontendUrl = parseUrl(property); }

    /* read frontendUrl from this file at runtime (TODO: read from URL),
     * overrides frontend.url */
    private static String frontendUrlRuntime;
    @Value("${frontend.url.runtime:}") 
    public void setFrontendUrlRuntime(String property) { frontendUrlRuntime = property; }

    private static Log LOG = LogFactory.getLog(GlobalProperties.class);
    private static Properties properties = initializeProperties();

    private static Properties initializeProperties()
    {
        return loadProperties(getResourceStream());
    }

    private static InputStream getResourceStream()
    {
        String resourceFilename = null;
        InputStream resourceFIS = null;

        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                 resourceFilename =
                    home + File.separator + GlobalProperties.propertiesFilename;
                if (LOG.isInfoEnabled()) {
                    LOG.info("Attempting to read properties file: " + resourceFilename);
                }
                resourceFIS = new FileInputStream(resourceFilename);
                if (LOG.isInfoEnabled()) {
                    LOG.info("Successfully read properties file");
                }
            }
        }
        catch (FileNotFoundException e) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed to read properties file: " + resourceFilename);
            }
        }

        if (resourceFIS == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Attempting to read properties file from classpath");
            }
            resourceFIS = GlobalProperties.class.getClassLoader().
                getResourceAsStream(GlobalProperties.propertiesFilename);
            if (LOG.isInfoEnabled()) {
                LOG.info("Successfully read properties file");
            }
        }
         
        return resourceFIS;
    }

    private static Properties loadProperties(InputStream resourceInputStream)
    {
        Properties properties = new Properties();

        try {
            properties.load(resourceInputStream);
            resourceInputStream.close();
        }
        catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error loading properties file: " + e.getMessage());
            }
        }

        return properties;
    }

    public static String getPathwayCommonsUrl()
	{
		return properties.getProperty(PATHWAY_COMMONS_URL);
    }
    
    public static String getUcscCancerGenomicsUrl()
	{
        return properties.getProperty(UCSC_CANCER_GENOMICS_URL);
    }

    public static String getSegfileUrl()
	{
        return properties.getProperty(SEGFILE_URL);
    }

	public static String getProperty(String property)
	{
		return (properties.containsKey(property)) ? properties.getProperty(property) : null;
	}

	public static boolean wantIGVBAMLinking() {
        String igvBamLinking = properties.getProperty(IGV_BAM_LINKING);
		return igvBamLinking!=null && igvBamLinking.equals("true");
	}

	public static Collection<String> getIGVBAMLinkingStudies() {
        String igvBamLinkingStudies = properties.getProperty(IGV_BAM_LINKING_STUDIES);
        if (igvBamLinkingStudies==null) {
            return Collections.emptyList();
        }
		String[] studies = igvBamLinkingStudies.split(":");
		return (studies.length > 0) ? Arrays.asList(studies) : Collections.<String>emptyList();
	}

    public static boolean usersMustAuthenticate()
    {
        String prop = properties.getProperty(AUTHENTICATE);
        return (!prop.isEmpty() && !prop.equals("false"));
    }

    public static String authenticationMethod()
    {
        return properties.getProperty(AUTHENTICATE);
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
        return Boolean.parseBoolean(properties.getProperty(AUTHORIZATION));
	}

    public static String getAppName()
    {
        String appName = properties.getProperty(APP_NAME);
        return (appName == null) ? DEFAULT_APP_NAME : appName;
    }

    public static String getAppVersion()
    {
        String appVersion = properties.getProperty(APP_VERSION);
        return (appVersion == null) ? "1.0" : appVersion;
    }
    
    public static String getTitle()
    {
        String skinTitle = properties.getProperty(SKIN_TITLE);
        return (skinTitle == null) ? DEFAULT_SKIN_TITLE : skinTitle;
    }
    // updated function to use a default if nothing is specified
    public static String getBlurb()
    {
        String skinBlurb = properties.getProperty(SKIN_BLURB);
        return (skinBlurb == null) ? DEFAULT_SKIN_BLURB : skinBlurb;
    }
    // get custom FAQ html or the default
    public static String getFaqHtml()
    {
        String faqHtml = properties.getProperty(SKIN_FAQ);
        return (faqHtml == null) ? DEFAULT_SKIN_FAQ : getContentString(faqHtml);
    }
    // get custom About html or the default
    public static String getAboutHtml()
    {
        String aboutHtml = properties.getProperty(SKIN_ABOUT);
        return (aboutHtml == null) ? DEFAULT_SKIN_ABOUT : getContentString(aboutHtml);
    }
    // get custom News html or the default
    public static String getNewsHtml()
    {
        String newsHtml = properties.getProperty(SKIN_NEWS);
        return (newsHtml == null) ? DEFAULT_SKIN_NEWS : getContentString(newsHtml);
    }
    // get custom News html or the default
    public static String getBaseUrl()
    {
        String baseUrl = properties.getProperty(SKIN_BASEURL);
        return (baseUrl == null) ? DEFAULT_SKIN_BASEURL : baseUrl;
    }
    public static boolean isMarkdownDocumentation()
    {
        String markdownFlag = properties.getProperty(SKIN_DOCUMENTATION_MARKDOWN);
        return markdownFlag == null || Boolean.parseBoolean(markdownFlag);
    }

    //get max tree depth
    public static Number getMaxTreeDepth()
    {
        String maxTreeDepth = properties.getProperty(SKIN_QUERY_MAX_TREE_DEPTH);
        return (maxTreeDepth == null) ? DEFAULT_QUERY_MAX_TREE_DEPTH : Integer.parseInt(maxTreeDepth);
    }

    // get custom Example Queries for the right column html or the default
    public static String getExamplesRightColumnHtml()
    {
        String examplesRightColumnHtml = properties.getProperty(SKIN_EXAMPLES_RIGHT_COLUMN_HTML);
        return examplesRightColumnHtml == null? "": examplesRightColumnHtml;
    }

    private static String getContentString(String contentString){
        if(getBaseUrl().equalsIgnoreCase("")) return "content/"+contentString;
        return contentString;
    }

    // get the login contact html
    public static String getLoginContactHtml()
    {
        String loginContactHtml = properties.getProperty(SKIN_LOGIN_CONTACT_HTML);
        return (loginContactHtml == null) ? DEFAULT_SKIN_LOGIN_CONTACT_HTML : loginContactHtml;
    }
    // get the text for the saml login button
    public static String getLoginSamlRegistrationHtml()
    {
        String loginSamlRegistrationHtml = properties.getProperty(SKIN_LOGIN_SAML_REGISTRATION_HTML);
        return (loginSamlRegistrationHtml == null) ? DEFAULT_SKIN_LOGIN_SAML_REGISTRATION_HTML : loginSamlRegistrationHtml;
    }
    public static String getSamlIdpMetadataEntityid()
    {
        return getProperty(SAML_IDP_METADATA_ENTITYID);
    }
    
    // returns whether the SAML logout should be local (at SP level) or global (at IDP level). Default: false (global)
    public static String getSamlIsLogoutLocal()
    {
    	return properties.getProperty(SAML_IS_LOGOUT_LOCAL, "false");
    }
    
    public static String getTagLineImage()
    {
        String tagLineImage = properties.getProperty(SKIN_TAG_LINE_IMAGE);
        return (tagLineImage == null) ? DEFAULT_SKIN_TAG_LINE_IMAGE : "images/" + tagLineImage;
    }

    // function for retrieving the right logo, used by the header_bar
    public static String getRightLogo()
    {
        String rightLogo = properties.getProperty(SKIN_RIGHT_LOGO);
        return (rightLogo == null || "".equals(rightLogo)) ? "" : "images/" + rightLogo;
    }

    // function for retrieving the footer text
    public static String getFooter(){
        String footer = properties.getProperty(SKIN_FOOTER);
        return (footer == null) ? DEFAULT_SKIN_FOOTER : footer;
    }
    // function for retrieving the studyview link text
    public static String getStudyviewLinkText(){
        String studyviewLinkText = properties.getProperty(SKIN_STUDY_VIEW_LINK_TEXT);
        return (studyviewLinkText == null) ? DEFAULT_SKIN_STUDY_VIEW_LINK_TEXT : studyviewLinkText;
    }

    public static String getEmailContact()
    {
        String emailAddress = properties.getProperty(SKIN_EMAIL_CONTACT);
        return (emailAddress == null) ? DEFAULT_EMAIL_CONTACT :
            ("<span class=\"mailme\" title=\"Contact us\">" + emailAddress + "</span>");
    }

    public static boolean includeNetworks()
    {
        return Boolean.parseBoolean(properties.getProperty(INCLUDE_NETWORKS));
    }

    public static String getGoogleAnalyticsProfileId()
    {
        return properties.getProperty(GOOGLE_ANALYTICS_PROFILE_ID);
    }

    public static boolean genomespaceEnabled()
    {
        return Boolean.parseBoolean(properties.getProperty(GENOMESPACE));
    }

    public static boolean showPlaceholderInPatientView()
    {
        return Boolean.parseBoolean(properties.getProperty(PATIENT_VIEW_PLACEHOLDER));
    }

    public static double[] getPatientViewGenomicOverviewCnaCutoff()
    {
        String cutoff = properties.getProperty(PATIENT_VIEW_GENOMIC_OVERVIEW_CNA_CUTOFF);
        if (cutoff==null) {
            return DEFAULT_GENOMIC_OVERVIEW_CNA_CUTOFF;
        }

        String[] strs = cutoff.split(",");
        return new double[]{Double.parseDouble(strs[0]), Double.parseDouble(strs[1])};
    }

    public static boolean showNewsTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_NEWS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    public static boolean showDataTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_DATA_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    // show or hide the web api tab in header navigation bar
    public static boolean showWebApiTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_WEB_API_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the r matlab tab in header navigation bar
    public static boolean showRMatlabTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_R_MATLAB_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the tutorial tab in header navigation bar
    public static boolean showTutorialsTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_TUTORIALS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the faqs tab in header navigation bar
    public static boolean showFaqsTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_FAQS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the tools tab in header navigation bar
    public static boolean showToolsTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_TOOLS_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // show or hide the about tab in header navigation bar
    public static boolean showAboutTab()
    {
        String showFlag = properties.getProperty(SKIN_SHOW_ABOUT_TAB);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    // get the text for the What's New in the right navigation bar
    public static String getRightNavWhatsNewBlurb(){
        String whatsNewBlurb = properties.getProperty(SKIN_RIGHT_NAV_WHATS_NEW_BLURB);
        return (whatsNewBlurb == null) ? DEFAULT_SKIN_WHATS_NEW_BLURB : whatsNewBlurb;
    }
    public static boolean showRightNavDataSets()
    {
        String showFlag = properties.getProperty(SKIN_RIGHT_NAV_SHOW_DATA_SETS);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }
    public static boolean showRightNavExamples()
    {
        String showFlag = properties.getProperty(SKIN_RIGHT_NAV_SHOW_EXAMPLES);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    public static boolean showRightNavTestimonials()
    {
        String showFlag = properties.getProperty(SKIN_RIGHT_NAV_SHOW_TESTIMONIALS);
        return showFlag == null || Boolean.parseBoolean(showFlag);
    }

    public static String getAuthorizationMessage()
    {
        String authMessage = properties.getProperty(SKIN_AUTHORIZATION_MESSAGE);
        return authMessage == null ? DEFAULT_AUTHORIZATION_MESSAGE : authMessage;
    }

    public static String getExampleStudyQueries() {
        return properties.getProperty(
                SKIN_EXAMPLE_STUDY_QUERIES,
                DEFAULT_SKIN_EXAMPLE_STUDY_QUERIES);
    }

    // added usage of default data sets header
    public static String getDataSetsHeader()
    {
        String dataSetsHeader = properties.getProperty(SKIN_DATASETS_HEADER);
        return dataSetsHeader == null ? DEFAULT_SKIN_DATASETS_HEADER : dataSetsHeader;
    }

    // added usage of default data sets footer
    public static String getDataSetsFooter()
    {
        String dataSetsFooter = properties.getProperty(SKIN_DATASETS_FOOTER);
        return dataSetsFooter == null ? DEFAULT_SKIN_DATASETS_FOOTER : dataSetsFooter;
    }
    
    public static String getSpecies(){
    	String species = properties.getProperty(SPECIES);
    	return species == null ? DEFAULT_SPECIES : species;
    	}

    public static String getNCBIBuild(){
    	String NCBIBuild = properties.getProperty(NCBI_BUILD);
    	return NCBIBuild == null ? DEFAULT_NCBI_BUILD : NCBIBuild;
    	}
   
    public static String getGenomicBuild(){
    	String genomicBuild = properties.getProperty(UCSC_BUILD);
    	return genomicBuild == null ? DEFAULT_UCSC_BUILD : genomicBuild;
    	}

    public static String getLinkToPatientView(String caseId, String cancerStudyId)
    {
        return "case.do#/patient?caseId=" + caseId
                 + "&studyId=" + cancerStudyId;
    }

    public static String getLinkToSampleView(String caseId, String cancerStudyId)
    {
        return "case.do#/patient?sampleId=" + caseId
                 + "&studyId=" + cancerStudyId;
    }

    public static String getLinkToCancerStudyView(String cancerStudyId)
    {
        return "study?" + org.mskcc.cbio.portal.servlet.CancerStudyView.ID
                + "=" + cancerStudyId;
    }

    public static String getLinkToIGVForBAM(String cancerStudyId, String caseId, String locus)
    {
        return ("igvlinking.json?" +
				org.mskcc.cbio.portal.servlet.IGVLinkingJSON.CANCER_STUDY_ID +
                "=" + cancerStudyId +
				"&" + org.mskcc.cbio.portal.servlet.IGVLinkingJSON.CASE_ID +
				"=" + caseId +
				"&" + org.mskcc.cbio.portal.servlet.IGVLinkingJSON.LOCUS +
				"=" + locus);
    }

    public static String getDigitalSlideArchiveIframeUrl(String caseId)
    {
        String url = properties.getProperty(PATIENT_VIEW_DIGITAL_SLIDE_IFRAME_URL);
        return url+caseId;
    }

    public static String getDigitalSlideArchiveMetaUrl(String caseId)
    {
        String url = properties.getProperty(PATIENT_VIEW_DIGITAL_SLIDE_META_URL);
        return url+caseId;
    }

    public static String getStudyHeatmapMetaUrl()
    {
        String url = properties.getProperty(STUDY_VIEW_MDACC_HEATMAP_META_URL);
        return url;
    }

    public static String getStudyHeatmapViewerUrl()
    {
        String url = properties.getProperty(STUDY_VIEW_MDACC_HEATMAP_URL);
        return url;
    }

    public static String getPatientHeatmapMetaUrl(String caseId)
    {
        String url = properties.getProperty(PATIENT_VIEW_MDACC_HEATMAP_META_URL);
        if (url == null || url.length() == 0) return null;
        return url + caseId;
    }

    public static String getPatientHeatmapViewerUrl(String caseId)
    {
        String url = properties.getProperty(PATIENT_VIEW_MDACC_HEATMAP_URL);
        if (url == null || url.length() == 0) return null;
        return url + caseId;
    }

    // function for getting the custom tabs for the header
    public static String[] getCustomHeaderTabs(){
        String customPagesString = GlobalProperties.getProperty(SKIN_CUSTOM_HEADER_TABS);
        if(customPagesString!=null){
            // split by comma and return the String array
            return customPagesString.split(",");
        }
        return null;
    }
   
    public static String getSessionServiceUrl()
    {
        return sessionServiceURL;
    }

    public static String getOncoKBPublicApiUrl()
    {
        String oncokbApiUrl = properties.getProperty(ONCOKB_PUBLIC_API_URL);
        String showOncokb = properties.getProperty(SHOW_ONCOKB);
        
        if(showOncokb == null || showOncokb.isEmpty()) {
                    showOncokb = "true";
        }
        
        // This only applies if there is no oncokb.api.url property in the portal.properties file.
        // Empty string should be used if you want to disable the OncoKB annotation.
        if(oncokbApiUrl == null || oncokbApiUrl.isEmpty()) {
            oncokbApiUrl = "oncokb.org/api/v1";
        }
        
        if(showOncokb.equals("true")) {
           return oncokbApiUrl;
        } else {
           return "";
        }
        
    }
 
    public static String getOncoKBApiUrl()
    {
        String oncokbApiUrl = properties.getProperty(ONCOKB_API_URL);
        String showOncokb = properties.getProperty(SHOW_ONCOKB);

        if(showOncokb == null || showOncokb.isEmpty()) {
            showOncokb = "true";
        }
        // This only applies if there is no oncokb.api.url property in the portal.properties file.
        // Empty string should be used if you want to disable the OncoKB annotation.
        if(oncokbApiUrl == null) {
            oncokbApiUrl = "http://oncokb.org/legacy-api/";
        }

        //Test connection of OncoKB website.
        if(!oncokbApiUrl.isEmpty() && showOncokb.equals("true")) {
            try {
                URL url = new URL(oncokbApiUrl+"access");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() != 200) {
                    oncokbApiUrl = "";
                }
                conn.disconnect();
                return oncokbApiUrl;
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    public static String getCivicUrl() {
        return civicUrl;
    }

    public static boolean showOncoKB() {
        String showOncokb = properties.getProperty(SHOW_ONCOKB);
        if (showOncokb==null || showOncokb.isEmpty()) {
            return true; // show oncoKB by default
        } else {
            return Boolean.parseBoolean(showOncokb);
        }
    }

    public static boolean showHotspot() {
        String hotspot = properties.getProperty(SHOW_HOTSPOT);
        if (hotspot==null) {
            return true; // show hotspots by default
        }
        
        if(!hotspot.isEmpty()) {
            return Boolean.parseBoolean(hotspot);
        }else{
            return true;
        }
    }

    public static boolean showCivic() {
        return showCivic;
    }

    public static String getFrontendUrl() {
        if (frontendUrlRuntime.length() > 0) {
            try {
                String url = parseUrl(new String(Files.readAllBytes(Paths.get(frontendUrlRuntime)), StandardCharsets.UTF_8).replaceAll("[\\r\\n]+", ""));
                if (LOG.isInfoEnabled()) {
                    LOG.info("Using frontend from " + frontendUrlRuntime + ": " + url);
                }
                return url;
            } catch (IOException e) {
                // error reading file, use existing frontendUrl
                if (LOG.isErrorEnabled()) {
                    LOG.error("Can't read frontend.url.runtime: " + frontendUrlRuntime);
                }
            }
        }
        return frontendUrl;
    }

    public static boolean showMyCancerGenomeUrl()
    {
        String show = properties.getProperty(MYCANCERGENOME_SHOW);
        return show != null && Boolean.parseBoolean(show);
    }
    
    public static String getOncoKBGeneStatus()
    {
        return properties.getProperty(ONCOKB_GENE_STATUS);
    }
    
    public static boolean getRecacheStudyAfterUpdate() {
        String recacheStudyAfterUpdate = properties.getProperty(RECACHE_STUDY_AFTER_UPDATE);
        if (recacheStudyAfterUpdate==null || recacheStudyAfterUpdate.isEmpty()) {
            return false;
        }
        return Boolean.parseBoolean(recacheStudyAfterUpdate);
    }
    
    public static String getBitlyUser() {
        String bitlyUser = properties.getProperty(BITLY_USER);
        if (bitlyUser == null || bitlyUser.trim().equals(""))
        {
            return null;
        }
        return bitlyUser;
    }
    
    public static String getDbVersion() {
        String version = properties.getProperty(DB_VERSION);
        if (version == null)
        {
            return "0";
        }
        return version;
    }
    
    public static boolean isSuppressSchemaVersionMismatchErrors() {
        return suppressSchemaVersionMismatchErrors;
    }

    public static String getDarwinAuthCheckUrl() {
        String darwinAuthUrl = "";
        if (properties.containsKey(DARWIN_AUTH_URL)) {
            try{
                darwinAuthUrl = properties.getProperty(DARWIN_AUTH_URL);
            }
            catch (NullPointerException e){}
        }        
        return darwinAuthUrl;
    }
    
    public static String getDarwinResponseUrl() {
        String darwinResponseUrl = "";
        if (properties.containsKey(DARWIN_RESPONSE_URL)) {
            try{
                darwinResponseUrl = properties.getProperty(DARWIN_RESPONSE_URL);
            }
            catch (NullPointerException e) {}
        }
        return darwinResponseUrl;
    }
    
    public static List<String[]> getPriorityStudies() {
	    List<String[]> priorityStudiesObject = new LinkedList<>();
	    try {
		    String priorityStudies = properties.getProperty(PRIORITY_STUDIES).trim();
		    for (String priorityStudyCategory: priorityStudies.split(";")) {
			    String[] elements = priorityStudyCategory.split("[#,]");
			    elements = Arrays.stream(elements).filter(s -> s.length() > 0).toArray(String[]::new);
			    if (elements.length > 1) {
				    priorityStudiesObject.add(elements);
			    }
		    }
	    } catch (NullPointerException e) {}
	    return priorityStudiesObject;
    }
    
    public static String getCisUser() {
        String cisUser = "";
        if (properties.containsKey(CIS_USER)) {
            try{
                cisUser = properties.getProperty(CIS_USER);
            }
            catch (NullPointerException e) {}            
        }
        return cisUser;         
    }
    
    
    public static String getDarwinRegex() {
        String darwinRegex = "";
        if (properties.containsKey(DARWIN_REGEX)) {
            try {
                darwinRegex = properties.getProperty(DARWIN_REGEX);
            }
            catch (NullPointerException e) {}   
        }
        return darwinRegex;
    }
    
    public static List<String> getDisabledTabs() {
        String disabledTabs = "";
        try {
            disabledTabs = properties.getProperty(DISABLED_TABS).trim();
        }
        catch (NullPointerException e) {}
        
        String[] tabs = disabledTabs.split("\\|");
        return (tabs.length > 0 && disabledTabs.length() > 0) ? Arrays.asList(tabs) : new ArrayList<String>();
    }
    
    public static String getDefaultOncoprintView() {
        String defaultOncoprintView = properties.getProperty(ONCOPRINT_DEFAULTVIEW);
        if (defaultOncoprintView == null || defaultOncoprintView.isEmpty()) {
            return "patient";
        }
        return defaultOncoprintView.trim();
    }

    public static void main(String[] args)
    {
        System.out.println(getAppVersion());
    }
}
