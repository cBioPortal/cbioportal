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

// package
package org.mskcc.cbio.importer.util;

// imports
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.portal.model.ClinicalAttribute;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.Method;

/**
 * Class which provides utilities shared across metadata objects (yes this could be in an abstract class).
 */
public class MetadataUtils {

	private static final Log LOG = LogFactory.getLog(MetadataUtils.class);
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

	public static List<Boolean> getHeadersMissingMetadata(Config config, List<String> normalizedColumnHeaderNames)
	{
		List<Boolean> headersWithMissingMetadata = new ArrayList<Boolean>();
        Map<String, ClinicalAttributesMetadata> clinicalAttributesMetadata =
        	getClinicalAttributesMetadata(config, normalizedColumnHeaderNames);

        int lc = -1;
        for (String columnHeader : normalizedColumnHeaderNames) {
            Collection<ClinicalAttributesMetadata> metadata = config.getClinicalAttributesMetadata(columnHeader.toUpperCase());
            if (!metadata.isEmpty() && !metadata.iterator().next().missingAttributes()) {
                headersWithMissingMetadata.add(++lc, false);
            }
            else {
            	headersWithMissingMetadata.add(++lc, true);
            }
        }
        return headersWithMissingMetadata;
	}

    public static String getClinicalMetadataHeaders(Config config, List<String> normalizedColumnHeaderNames) throws Exception
    {
		StringBuilder clinicalDataHeader = new StringBuilder();
        Map<String, ClinicalAttributesMetadata> clinicalAttributesMetadata = getClinicalAttributesMetadata(config, normalizedColumnHeaderNames);

        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getDisplayName"));
        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getDescription"));
        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getDatatype"));
        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getAttributeType"));
        clinicalDataHeader.append(addClinicalDataHeader(normalizedColumnHeaderNames, clinicalAttributesMetadata, "getPriority"));
        clinicalDataHeader.append(addClinicalDataColumnHeaders(normalizedColumnHeaderNames, clinicalAttributesMetadata));
        return clinicalDataHeader.toString();
    }

    private static Map <String, ClinicalAttributesMetadata> getClinicalAttributesMetadata(Config config, List<String> normalizedColumnHeaderNames)
    {
        Map<String, ClinicalAttributesMetadata> toReturn = new HashMap<String, ClinicalAttributesMetadata>();
        for (String columnHeader : normalizedColumnHeaderNames) {
            Collection<ClinicalAttributesMetadata> metadata = config.getClinicalAttributesMetadata(columnHeader.toUpperCase());
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
        header.append(ImportClinicalData.METADATA_PREFIX);
        for (String columnHeader : normalizedColumnHeaderNames) {
            ClinicalAttributesMetadata metadata = clinicalAttributesMetadata.get(columnHeader);
            if (metadata != null && !metadata.missingAttributes()) {
                Method m = clinicalAttributesMetadata.get(columnHeader).getClass().getMethod(metadataAccessor);
                header.append((String)m.invoke(metadata) + ImportClinicalData.DELIMITER);
            }
            else {
                logMessage(String.format("Unknown clinical attribute (or missing metadata): %s", columnHeader));
                continue;
            }
        }
        return header.toString().trim() + "\n";
    }

    private static String addClinicalDataColumnHeaders(List<String> normalizedColumnHeaderNames,
                                                       Map<String, ClinicalAttributesMetadata> clinicalAttributesMetadata)
    {
        StringBuilder header = new StringBuilder();
    	for (String columnHeader : normalizedColumnHeaderNames) {
            ClinicalAttributesMetadata metadata = clinicalAttributesMetadata.get(columnHeader);
            if (metadata != null && !metadata.missingAttributes()) {
            	header.append(columnHeader + ImportClinicalData.DELIMITER);
            }
        }
        return header.toString().trim() + "\n";
    }

    private static void logMessage(String message)
    {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
    }
}
