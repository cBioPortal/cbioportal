package org.mskcc.portal.mut_diagram;

/**
 * Options, derived from Pfam graphics response in JSON format.
 */
public final class Options {
    private String baseUrl;

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
