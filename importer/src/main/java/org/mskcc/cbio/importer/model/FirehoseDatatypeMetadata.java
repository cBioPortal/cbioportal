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

/**
 * Class which contains firehose datatype metadata.
 */
public final class FirehoseDatatypeMetadata {

	// bean properties
	private String archive;
	private String datatype;
	private Boolean download;
	private String dataFilename;

    /**
     * Create a FirehoseDatatypeMetadata instance with specified properties.
     *
	 * @param datatype String
	 * @param download Boolean
	 * @param archive String
	 * @param dataFilename String
     */
    public FirehoseDatatypeMetadata(final String datatype, final Boolean download,
                                    final String archive, final String dataFilename) {

		if (datatype == null) {
            throw new IllegalArgumentException("datatype must not be null");
		}
		this.datatype = datatype;

		if (archive == null) {
            throw new IllegalArgumentException("archive must not be null");
		}
		this.archive = archive;

		if (dataFilename == null) {
            throw new IllegalArgumentException("dataFilename must not be null");
		}
		this.dataFilename = dataFilename;

		if (download == null) {
            throw new IllegalArgumentException("download must not be null");
		}
		this.download = download;
	}

	public String getDatatype() { return datatype; }
	public String getArchiveFilename() { return archive; }
	public String getDataFilename() { return dataFilename; }
	public Boolean getDownload() { return download; }
}
