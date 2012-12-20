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

/**
 * Class which contains tumor type  metadata.
 */
public final class TumorTypeMetadata {

    // delimiter between tumor type & name within the reference file
	public static final String TUMOR_TYPE_META_FILE_DELIMITER = " : ";

    // name of reference file created to import into portal
	public static final String TUMOR_TYPE_META_FILE_NAME = "cancers.txt"; 

	// bean properties
	private String tumorType;
	private String tumorTypeName;
	private Boolean download; // download?

    /**
     * Create a TumorTypeMetadata instance with specified properties.
     *
	 * @param tumorType String
	 * @param tumorTypeName String
	 * @param download Boolean
     */
    public TumorTypeMetadata(final String tumorType,
							 final String tumorTypeName, final Boolean download) {

		if (tumorType == null) {
            throw new IllegalArgumentException("tumorType must not be null");
		}
		this.tumorType = tumorType.trim();

		if (tumorTypeName == null) {
            throw new IllegalArgumentException("tumorTypeName must not be null");
		}
		this.tumorTypeName = tumorTypeName.trim();

		if (download == null) {
            throw new IllegalArgumentException("download must not be null");
		}
		this.download = download;
	}

	public String getType() { return tumorType; }
	public String getName() { return tumorTypeName; }
	public Boolean getDownload() { return download; }
}
