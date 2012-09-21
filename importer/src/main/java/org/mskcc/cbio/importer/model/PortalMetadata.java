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
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Class which contains portal metadata.
 */
public final class PortalMetadata {

    // some statics
    private static final String DATATYPES_DELIMITER = ":";
    private static final String CANCER_STUDIES_DELIMITER = ":";

	// bean properties
    private String name;
    private Collection<String> cancerStudies;
    private Collection<String> datatypes;
    private String stagingDirectory;
    private String convertOverrideDirectory;
    private String importOverrideDirectory;

    /**
     * Create a PortalMetadata instance with specified properties.
     *
     * @param name String
     * @param cancerStudies String
     * @param datatypes String
     * @param stagingDirectory String
	 * @param convertOverrideDirectory String
	 * @param importOverrideDirectory String
     */
    public PortalMetadata(final String name, final String cancerStudies,
                          final String datatypes, final String stagingDirectory,
                          final String convertOverrideDirectory, final String importOverrideDirectory) {

        // name
		if (name == null) {
            throw new IllegalArgumentException("name must not be null");
		}
        this.name = name;

        // cancer studies
		if (cancerStudies == null || cancerStudies.length() == 0) {
            throw new IllegalArgumentException("cancerStudies must not be null or empty");
		}
        else if (cancerStudies.contains(CANCER_STUDIES_DELIMITER)) {
            this.cancerStudies = Arrays.asList(cancerStudies.split(CANCER_STUDIES_DELIMITER));
        }
        else {
            this.cancerStudies = new ArrayList<String>();
            this.cancerStudies.add(cancerStudies);
        }

        // datatypes
		if (datatypes == null || datatypes.length() == 0) {
            throw new IllegalArgumentException("datatypes must not be null or empty");
		}
        else if (datatypes.contains(DATATYPES_DELIMITER)) {
            this.datatypes = Arrays.asList(datatypes.split(DATATYPES_DELIMITER));
        }
        else {
            this.datatypes = new ArrayList<String>();
            this.datatypes.add(datatypes);
        }

        // staging directory
		if (stagingDirectory == null) {
            throw new IllegalArgumentException("stagingDirectory must not be null");
		}
		this.stagingDirectory = stagingDirectory;

        // convertOverride directory
		this.convertOverrideDirectory = (convertOverrideDirectory == null) ? "" : convertOverrideDirectory;

        // importOverride directory
		this.importOverrideDirectory = (importOverrideDirectory == null) ? "" : importOverrideDirectory;

	}

	public String getName() { return name; }
	public Collection<String> getCancerStudies() { return cancerStudies; }
	public Collection<String> getDatatypes() { return datatypes; }
	public String getStagingDirectory() { return stagingDirectory; }
	public String getConvertOverrideDirectory() { return convertOverrideDirectory; }
	public String getImportOverrideDirectory() { return importOverrideDirectory; }
}
