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

// package
package org.mskcc.cbio.importer.util;

// imports
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;

import java.io.File;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.Method;

/**
 * Class which provides utilities shared across metadata objects (yes this could be in an abstract class).
 */
public class MetadataUtils {

	// the reference metadata worksheet can contain environment vars
	private static final Pattern ENVIRONMENT_VAR_REGEX = Pattern.compile("\\$(\\w*)");

	/**
	 * Helper function to determine root directory for cancer study to install.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param dataSourcesMetadata Collection<DataSourcesMetadata>
	 * @param cancerStudyMetadata CancerStudyMetadata
	 * @return String
	 */
	public static String getCancerStudyRootDirectory(PortalMetadata portalMetadata,
													 Collection<DataSourcesMetadata> dataSourcesMetadata,
													 CancerStudyMetadata cancerStudyMetadata) {

		// check portal staging area - should work for all tcga
		File cancerStudyDirectory =
			new File(portalMetadata.getStagingDirectory() + File.separator + cancerStudyMetadata.getStudyPath());
		if (cancerStudyDirectory.exists()) {
			return portalMetadata.getStagingDirectory();
		}

		// made it here, check other datasources 
		for (DataSourcesMetadata dataSourceMetadata : dataSourcesMetadata) {
			if (dataSourceMetadata.isAdditionalStudiesSource()) {
				cancerStudyDirectory =
					new File(dataSourceMetadata.getDownloadDirectory() + File.separator + cancerStudyMetadata.getStudyPath());
				if (cancerStudyDirectory.exists()) {
					return dataSourceMetadata.getDownloadDirectory();
				}
			}
		}

		// outta here
		return null;
	}

	/**
	 * Helper function used to get canonical path for given path. It will translate
	 * environment variables.
	 *
	 * @param path String
	 */
	public static String getCanonicalPath(String path) {
	
		String toReturn = path;

		Matcher lineMatcher = ENVIRONMENT_VAR_REGEX.matcher(path);
		if (lineMatcher.find()) {
			String envValue = System.getenv(lineMatcher.group(1));
			if (envValue != null) {
				toReturn = path.replace("$" + lineMatcher.group(1), envValue);
			}
		}

		// outta here
		return toReturn;
	}

    public static String getClinicalMetadataHeaders(Config config, List<String> normalizedColumnHeaderNames) throws Exception
    {
		StringBuilder clinicalDataHeader = new StringBuilder();
        Map<String, ClinicalAttributesMetadata> clinicalAttributesMetadata = getClinicalAttributesMetadata(config, normalizedColumnHeaderNames);

        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getDisplayName"));
        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getDescription"));
        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getDatatype"));

        return clinicalDataHeader.toString().trim() + "\n";
    }

    private static Map <String, ClinicalAttributesMetadata> getClinicalAttributesMetadata(Config config, List<String> normalizedColumnHeaderNames)
    {
        Map<String, ClinicalAttributesMetadata> toReturn = new HashMap<String, ClinicalAttributesMetadata>();
        for (String columnHeader : normalizedColumnHeaderNames) {
            Collection<ClinicalAttributesMetadata> metadata = config.getClinicalAttributesMetadata(columnHeader);
            if (!metadata.isEmpty()) {
                toReturn.put(columnHeader, metadata.iterator().next());
            }
        }
        return toReturn;
    }

    private static String addClinicalDataHeader(List<String> normalizedColumnHeaderNames,
                                                Map<String, ClinicalAttributesMetadata> clinicalAttributesMetadata,
                                                String metadataAccessor) throws Exception
    {
        StringBuilder header = new StringBuilder();
        for (String columnHeader : normalizedColumnHeaderNames) {
            ClinicalAttributesMetadata metadata = clinicalAttributesMetadata.get(columnHeader);
            if (metadata != null) {
                Method m = clinicalAttributesMetadata.get(columnHeader).getClass().getMethod(metadataAccessor);
                header.append((String)m.invoke(clinicalAttributesMetadata) + "\t");
            }
            else if (metadataAccessor.equals("getDatatype")) {
                header.append(Converter.CLINICAL_DATA_DATATYPE_PLACEHOLDER);
            }
            else {
                header.append(Converter.CLINICAL_DATA_PLACEHOLDER);
            }

        }
        return header.toString().trim() + "\n";
    }
}
