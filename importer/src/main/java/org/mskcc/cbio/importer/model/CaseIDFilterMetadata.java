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
package org.mskcc.cbio.importer.model;

// imports

/**
 * Class which contains caseID filter metadata.
 */
public class CaseIDFilterMetadata {

	// bean properties
	private String filterName;
	private String regex;

    /**
     * Create a CaseIDFilterMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public CaseIDFilterMetadata(String[] properties) {

		if (properties.length < 2) {
            throw new IllegalArgumentException("corrupt properties array passed to constructor");
		}

		this.filterName = properties[0].trim();
		this.regex = properties[1].trim();
	}

	public String getFilterName() { return filterName; }
	public String getRegex() { return regex; }
}
