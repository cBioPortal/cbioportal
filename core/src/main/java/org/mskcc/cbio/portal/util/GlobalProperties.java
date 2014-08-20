/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.servlet.QueryBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.util.*;

/**
 * Utility class for getting / setting global properties.
 */
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
    public static final String BITLY_USER = "bitly.user";
    public static final String BITLY_API_KEY = "bitly.api_key";
    public static final String INCLUDE_NETWORKS = "include_networks";
    public static final String GOOGLE_ANALYTICS_PROFILE_ID = "google_analytics_profile_id";

    public static final String APP_NAME = "app.name";
    public static final String DEFAULT_APP_NAME = "public_portal";
    
    public static final String APP_VERSION = "app.version";

    public static final String SKIN_TITLE = "skin.title";
    public static final String DEFAULT_SKIN_TITLE = "cBioPortal for Cancer Genomics";
    public static final String SKIN_BLURB = "skin.blurb";
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
    public static final String SKIN_DATASETS_HEADER = "skin.data_sets_header";
    public static final String SKIN_DATASETS_FOOTER = "skin.data_sets_footer";

    public static final String PATIENT_VIEW_PLACEHOLDER = "patient_view_placeholder";
    public static final String PATIENT_VIEW_CNA_TUMORMAP_CNA_CUTOFF = "patient_view_genomic_overview_cna_cutoff";
    public static final double[] DEFAULT_TUMORMAP_CNA_CUTOFF = new double[]{0.2,1.5};
    public static final String PATIENT_VIEW_DIGITAL_SLIDE_IFRAME_URL = "digitalslidearchive.iframe.url";
    public static final String PATIENT_VIEW_DIGITAL_SLIDE_META_URL = "digitalslidearchive.meta.url";
    public static final String PATIENT_VIEW_TCGA_PATH_REPORT_URL = "tcga_path_report.url";
    
    public static final String TEMPORARY_DIR = "temporary_dir";

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
		return Boolean.parseBoolean(properties.getProperty(AUTHENTICATE));
    }

	public static boolean usersMustBeAuthorized()
    {
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

    public static String getBlurb()
    {
        return properties.getProperty(SKIN_BLURB);
    }

    public static String getTagLineImage()
    {
        String tagLineImage = properties.getProperty(SKIN_TAG_LINE_IMAGE);
        return (tagLineImage == null) ? DEFAULT_SKIN_TAG_LINE_IMAGE : "images/" + tagLineImage;
    }

    public static String getEmailContact()
    {
        String emailAddress = properties.getProperty(SKIN_EMAIL_CONTACT);
        return (emailAddress == null) ? DEFAULT_EMAIL_CONTACT :
            ("<span class=\"mailme\" title=\"Contact us\">" + emailAddress + "</span>");
    }

    public static String getBitlyUser()
    {
        return properties.getProperty(BITLY_USER);
    }

    public static String getBitlyApiKey()
    {
        return properties.getProperty(BITLY_API_KEY);
    }

    public static boolean includeNetworks()
    {
        return Boolean.parseBoolean(properties.getProperty(INCLUDE_NETWORKS));
    }

    public static String getGoogleAnalyticsProfileId()
    {
        return properties.getProperty(GOOGLE_ANALYTICS_PROFILE_ID);
    }

    public static boolean showPlaceholderInPatientView()
    {
        return Boolean.parseBoolean(properties.getProperty(PATIENT_VIEW_PLACEHOLDER));
    }

    public static double[] getPatientViewGenomicOverviewCnaCutoff()
    {
        String cutoff = properties.getProperty(PATIENT_VIEW_CNA_TUMORMAP_CNA_CUTOFF);
        if (cutoff==null) {
            return DEFAULT_TUMORMAP_CNA_CUTOFF;
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

    public static String getDataSetsHeader()
    {
        return properties.getProperty(SKIN_DATASETS_HEADER);
    }

    public static String getDataSetsFooter()
    {
        return properties.getProperty(SKIN_DATASETS_FOOTER);
    }

    public static String getLinkToPatientView(String caseId, String cancerStudyId)
    {
        return "case.do?" + QueryBuilder.CANCER_STUDY_ID + "=" + cancerStudyId
                 + "&"+ org.mskcc.cbio.portal.servlet.PatientView.CASE_ID + "=" + caseId;
    }

    public static String getLinkToCancerStudyView(String cancerStudyId)
    {
        return "study.do?" + org.mskcc.cbio.portal.servlet.QueryBuilder.CANCER_STUDY_ID
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

    public static String getTCGAPathReportUrl(String typeOfCancer)
    {
        String url = GlobalProperties.getProperty(PATIENT_VIEW_TCGA_PATH_REPORT_URL);
        return (url==null) ? null : url.replace("{cancer.type}", typeOfCancer);
    }
    
    public static String getTemporaryDir() {
        String tmp = GlobalProperties.getProperty(TEMPORARY_DIR);
        return tmp == null || tmp.isEmpty() ? "/tmp" : tmp;
    }
}
