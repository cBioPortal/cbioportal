/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

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
