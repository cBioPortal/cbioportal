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
package org.mskcc.cbio.importer.model;

// imports
import org.mskcc.cbio.importer.util.MetadataUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * Class which contains reference metadata.
 */
public class ReferenceMetadata {

    // delimiter between tumor type and center (used for find the path)
	public static final String REFERENCE_DATA_ARGS_DELIMITER = ":";

	// bean properties
	private String referenceType;
	private String fetcherName;
	private List<String> fetcherArgs;
	private String importerName;
	private List<String> importerArgs;

    /**
     * Create a ReferenceMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public ReferenceMetadata(String[] properties) {

		if (properties.length < 5) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		this.referenceType = properties[0].trim();
		this.fetcherName = MetadataUtils.getCanonicalPath(properties[1].trim());
		this.fetcherArgs = new ArrayList<String>();
		for (String fetcherArg : properties[2].trim().split(REFERENCE_DATA_ARGS_DELIMITER)) {
			this.fetcherArgs.add(MetadataUtils.getCanonicalPath(fetcherArg));
		}
		this.importerName = MetadataUtils.getCanonicalPath(properties[3].trim());
		this.importerArgs = new ArrayList<String>();
		for (String importerArg : properties[4].trim().split(REFERENCE_DATA_ARGS_DELIMITER)) {
			this.importerArgs.add(MetadataUtils.getCanonicalPath(importerArg));
		}
	}

	public String getReferenceType() { return referenceType; }
	public String getFetcherName() { return fetcherName; }
	public List<String> getFetcherArgs() { return fetcherArgs; }
	public String getImporterName() { return importerName; }
	public List<String> getImporterArgs() { return importerArgs; }
}
