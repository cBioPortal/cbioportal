package org.mskcc.portal.mutation.diagram.pfam;

/**
 * Options, derived from Pfram graphics response in JSON format.
 */
final class Options {
    private String baseUrl;

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
