/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
 * Class which contains TCGA tumor type metadata.
 */
public class TCGATumorTypeMetadata {

	private String tcgaCode;
    private String oncotreeCode;

    public TCGATumorTypeMetadata(String[] properties) {
		if (properties.length < 2) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		this.tcgaCode = properties[0].trim();
        this.oncotreeCode = properties[1].trim();
	}

	public String getTCGACode() { return tcgaCode; }
    public String getOncoTreeCode() { return oncotreeCode; }
}
