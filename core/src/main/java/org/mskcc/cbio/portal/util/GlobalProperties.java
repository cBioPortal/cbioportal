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

import java.util.*;

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    public static final String PATHWAY_COMMONS_URL = "pc_url";
    public static final String UCSC_CANCER_GENOMICS_URL = "ucsc_url";
	public static final String SEGFILE_URL = "segfile_url";
	public static final String OPENSSL_BINARY = "openssl_bin";
	public static final String SIGNATURE_KEY = "sig_key";
	public static final String ENCRYPTION_KEY = "encrypt_key";
	public static final String BROAD_BAM_URL = "broad_bam_url";
	public static final String IGV_BAM_LINKING = "igv_bam_linking";
	public static final String IGV_BAM_LINKING_STUDIES = "igv_bam_linking_studies";

	private static Map<String, String> properties;
    
    static {
        Config config = Config.getInstance();
		properties = new HashMap<String, String>();
		properties.put(PATHWAY_COMMONS_URL, config.getProperty("pathway_commons.url"));
		properties.put(UCSC_CANCER_GENOMICS_URL, config.getProperty("ucsc_cancer_genomics.url"));
		properties.put(SEGFILE_URL, config.getProperty("segfile.url"));
		properties.put(OPENSSL_BINARY, config.getProperty("openssl.binary"));
		properties.put(SIGNATURE_KEY, config.getProperty("signature.key"));
		properties.put(ENCRYPTION_KEY, config.getProperty("encryption.key"));
		properties.put(BROAD_BAM_URL, config.getProperty("broad.bam.url"));
		properties.put(IGV_BAM_LINKING, config.getProperty("igv.bam.linking"));
		properties.put(IGV_BAM_LINKING_STUDIES, config.getProperty("igv.bam.linking.studies"));
    }

    public static String getPathwayCommonsUrl()
	{
		return properties.get(PATHWAY_COMMONS_URL);
    }
    
    public static String getUcscCancerGenomicsUrl()
	{
        return properties.get(UCSC_CANCER_GENOMICS_URL);
    }

    public static String getSegfileUrl()
	{
        return properties.get(SEGFILE_URL);
    }

	public static String getProperty(String property)
	{
		return (properties.containsKey(property)) ? properties.get(property) : "";
	}

	public static boolean wantIGVBAMLinking() {
                String igvBamLinking = properties.get(IGV_BAM_LINKING);
		return igvBamLinking!=null && igvBamLinking.equals("true");
	}

	public static Collection<String> getIGVBAMLinkingStudies() {
                String igvBamLinkingStudies = properties.get(IGV_BAM_LINKING_STUDIES);
                if (igvBamLinkingStudies==null) {
                    return Collections.emptyList();
                }
		String[] studies = igvBamLinkingStudies.split(":");
		return (studies.length > 0) ? Arrays.asList(studies) : Collections.<String>emptyList();
	}
}
