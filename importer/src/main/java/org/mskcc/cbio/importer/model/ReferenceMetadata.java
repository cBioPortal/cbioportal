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
import java.net.URL;

/**
 * Class which contains reference metadata.
 */
public class ReferenceMetadata {

	// bean properties
	private String referenceType;
	private Boolean importIntoPortal;
	private URL referenceFileSource;
	private URL referenceFile;
	private String fetcherBeanID;
	private String importerClassName;

    /**
     * Create a ReferenceMetadata instance with properties in given array.
	 * Its assumed order of properties is that from google worksheet.
     *
	 * @param properties String[]
     */
    public ReferenceMetadata(String[] properties) {

		if (properties.length != 6) {
            throw new IllegalArgumentException("corrupt properties array passed to contructor");
		}

		this.referenceType = properties[0].trim();
		this.importIntoPortal = new Boolean(properties[1].trim());
		try {
			this.referenceFileSource = new URL(properties[2].trim());
			this.referenceFile = new URL(properties[3].trim());
		}
		catch (Exception e) {
			if (properties[2].trim().length() == 0) this.referenceFileSource = null;
			if (properties[3].trim().length() == 0) this.referenceFile = null;
		}
		this.fetcherBeanID = properties[4].trim();
		this.importerClassName = properties[5].trim();
	}

	public String getReferenceType() { return referenceType; }
	public Boolean importIntoPortal() { return importIntoPortal; }
	public URL getReferenceFileSource() { return referenceFileSource; }
	public URL getReferenceFile() { return referenceFile; }
	public String getFetcherBeanID() { return fetcherBeanID; }
	public String getImporterClassName() { return importerClassName; }
}
