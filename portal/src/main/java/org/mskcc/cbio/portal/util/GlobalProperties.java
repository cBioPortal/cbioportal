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

/**
 * Utility class for getting / setting global properties.
 */
public class GlobalProperties {
    private static final String PATHWAY_COMMONS_URL_PARAM = "pathway_commons.url";
    private static final String UCSC_CANCER_GENOMICS_URL_PARAM = "ucsc_cancer_genomics.url";
    private static final String SEGFILE_URL_PARAM = "segfile.url";
    
    private static final String PATHWAY_COMMANS_URL;
    private static final String UCSC_CANCER_GENOMICS_URL;
	private static final String SEGFILE_URL;
    
    static {
        Config config = Config.getInstance();
        PATHWAY_COMMANS_URL = config.getProperty(PATHWAY_COMMONS_URL_PARAM);
        UCSC_CANCER_GENOMICS_URL = config.getProperty(UCSC_CANCER_GENOMICS_URL_PARAM);
        SEGFILE_URL = config.getProperty(SEGFILE_URL_PARAM);
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

    /**
     * Gets the Global IGV URL.
     *
     * @return IGV URL.
     */
    public static String getSegfileUrl() {
        return SEGFILE_URL;
    }
    
}
