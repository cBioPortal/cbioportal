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

import java.io.*;
import java.util.*;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {

    public static final String HOME_DIR = "PORTAL_HOME";
    public static final String propertiesFilename = "portal.properties";

    public static final String PATHWAY_COMMONS_URL = "pathway_commons.url";
    public static final String UCSC_CANCER_GENOMICS_URL = "ucsc_cancer_genomics.url";
	public static final String SEGFILE_URL = "segfile.url";
	public static final String OPENSSL_BINARY = "openssl.binary";
	public static final String SIGNATURE_KEY = "signature.key";
	public static final String ENCRYPTION_KEY = "encryption.key";
	public static final String BROAD_BAM_URL = "broad.bam.url";
	public static final String IGV_BAM_LINKING = "igv.bam.linking";
	public static final String IGV_BAM_LINKING_STUDIES = "igv.bam.linking.studies";
    public static final String AUTHENTICATE = "authenticate";
    public static final String AUTHORIZATION = "authorization";

    private static Properties properties = initializeProperties();
    
    private static Properties initializeProperties()
    {
        return loadProperties(getResource());
    }

    private static String getResource()
    {
        String home = System.getenv(HOME_DIR);
        return (home == null || home.isEmpty()) ?
            GlobalProperties.propertiesFilename :
            (home + File.separator + GlobalProperties.propertiesFilename);
    }

    private static Properties loadProperties(String resource)
    {
        Properties properties = new Properties();

        try {
            properties.load(new FileReader(resource));
        }
        catch (IOException e) {
            System.err.println("Error loading properties file'" +
                               resource + "', aborting\n");
            System.exit(1);
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
		return (properties.containsKey(property)) ? properties.getProperty(property) : "";
	}

	public static boolean wantIGVBAMLinking() {
		return properties.getProperty(IGV_BAM_LINKING).equals("true");
	}

	public static Collection<String> getIGVBAMLinkingStudies() {
		String[] studies = properties.getProperty(IGV_BAM_LINKING_STUDIES).split(":");
		return (studies.length > 0) ? Arrays.asList(studies) : Collections.<String>emptyList();
	}

    public static boolean usersMustAuthenticate()
    {
		return Boolean.valueOf(properties.getProperty(AUTHENTICATE));
    }

	public static boolean usersMustBeAuthorized()
    {
		return Boolean.valueOf(properties.getProperty(AUTHORIZATION));
	}
}
