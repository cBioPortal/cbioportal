package org.mskcc.portal.util;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    private static final String PATHWAY_COMMONS_URL_PARAM = "pathway_commons.url";
    private static final String UCSC_CANCER_GENOMICS_URL_PARAM = "ucsc_cancer_genomics.url";
    
    private static final String PATHWAY_COMMANS_URL;
    private static final String UCSC_CANCER_GENOMICS_URL;
    
    static {
        Config config = Config.getInstance();
        PATHWAY_COMMANS_URL = config.getProperty(PATHWAY_COMMONS_URL_PARAM);
        UCSC_CANCER_GENOMICS_URL = config.getProperty(UCSC_CANCER_GENOMICS_URL_PARAM);
    }

    /**
     * Gets the Global Pathway Commons URL.
     *
     * @return Pathway Commons URL.
     */
    public static String getPathwayCommonsUrl() {
        return PATHWAY_COMMANS_URL;
    }
    

    /**
     * Gets the Global UCSC Cancer Genomics URL.
     *
     * @return Pathway Commons URL.
     */
    public static String getUcscCancerGenomicsUrl() {
        return UCSC_CANCER_GENOMICS_URL;
    }
    
}
