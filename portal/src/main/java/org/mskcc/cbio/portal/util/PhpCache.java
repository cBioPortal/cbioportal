package org.mskcc.cbio.portal.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.IOException;
import java.util.Date;

/**
 * PHP Cache of Global Components.
 * Enables us to retrieve, store and display global header, footer and righ column,
 * as globally defined for all pages within the cBio Cancer Genomics web site.
 */
public class PhpCache {
    private static PhpCache cache;
    private String header;
    private String footer;
    private String rightCol;
    private Date timeOfLastRetrieval;
    private static final long TWO_HOURS = 1000 * 60 * 60 * 2;

    /**
     * Gets the singleton instance of this class.
     *
     * @return PhpCache Object.
     */
    public static PhpCache getInstance() {
        if (cache == null) {
            cache = new PhpCache();
        }
        return cache;
    }

    /**
     * Private Constructor.
     * Enforces singleton pattern.
     */
    private PhpCache() {
        initCache();
    }

    /**
     * Initializes the PHP Cache.
     */
    private void initCache() {
        header = getPhpComponent("header.php");
        footer = getPhpComponent("footer.php");
        rightCol = getPhpComponent("right_column.php");
        timeOfLastRetrieval = new Date();
    }

    /**
     * Gets the header.
     *
     * @return header component.
     */
    public String getHeader() {
        conditionallyUpdateCache();
        return header;
    }

    /**
     * Gets the footer.
     *
     * @return footer component.
     */
    public String getFooter() {
        conditionallyUpdateCache();
        return footer;
    }

    /**
     * Gets the right column.
     *
     * @return right column.
     */
    public String getRightCol() {
        conditionallyUpdateCache();
        return rightCol;
    }

    /**
     * Conditionally update the cache every 2 Hours.
     */
    private void conditionallyUpdateCache() {
        Date currentDate = new Date();
        long diff = currentDate.getTime() - timeOfLastRetrieval.getTime();
        if (diff > TWO_HOURS) {
            initCache();
        }
    }

    /**
     * Retrieve the PHP Component from the cBio Web Site.
     *
     * @param component component URL.
     * @return component text/html.
     */
    private String getPhpComponent(String component) {
        String baseUrl = "http://cbio.mskcc.org/cancergenomics/components/";

        //  Create Apache HTTP Client
        HttpClient client = new HttpClient();

        try {
            GetMethod method = new GetMethod(baseUrl + component);

            //  Extract HTTP Status Code
            int statusCode = client.executeMethod(method);

            //  If all is OK, extract the response text
            if (statusCode == HttpStatus.SC_OK) {
                return ResponseUtil.getResponseString(method);
            } else {
                return ("Could not retrieve:  " + component);
            }
        } catch (HttpException e) {
            return ("Could not retrieve:  " + component);
        } catch (IOException e) {
            return ("Could not retrieve:  " + component);
        }
    }

}
