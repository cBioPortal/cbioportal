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
 * Class which contains reference metadata.
 */
public final class ReferenceMetadata {

	// bean properties
	private String referenceType;
	private String referenceFile;
	private String referenceFileDestination;
	private String importerClassName;

    /**
     * Create a ReferenceMetadata instance with specified properties.
     *
	 * @param referenceType String
	 * @param referenceFile String
	 * @param referenceFileDestination String
	 * @param importerClassname String
     */
    public ReferenceMetadata(final String referenceType, final String referenceFile,
							 final String referenceFileDestination, final String importerClassName) {

		if (referenceType == null) {
            throw new IllegalArgumentException("referenceType must not be null");
		}
		this.referenceType = referenceType.trim();

		if (referenceFile == null) {
            throw new IllegalArgumentException("referenceFile must not be null");
		}
		this.referenceFile = referenceFile.trim();

		if (referenceFileDestination == null) {
            throw new IllegalArgumentException("referenceFileDestination must not be null");
		}
		this.referenceFileDestination = referenceFileDestination.trim();

		if (importerClassName == null) {
            throw new IllegalArgumentException("importerClassName must not be null");
		}
		this.importerClassName = importerClassName.trim();
	}

	public String getReferenceType() { return referenceType; }
	public String getReferenceFile() { return referenceFile; }
	public String getReferenceFileDestination() { return referenceFileDestination; }
	public String getImporterClassName() { return importerClassName; }
}
