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
import org.mskcc.cbio.importer.model.DatatypeMetadata;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class which provides DatatypeMetadata utils.
 */
public final class DatatypeMetadataUtils {

	/**
	 * Function to get datatype metadata object for given datatype.
	 *
	 * @param datatype String
	 * @param datatypeMetadata Collection<datatypeMetadata>
	 * @return DatatypeMetadata
	 */
	public static DatatypeMetadata getDatatypeMetadata(final String datatype,
													   final Collection<DatatypeMetadata> datatypeMetadata) {
		
		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
            if (dtMetadata.getDatatype().toLowerCase().equals(datatype.toLowerCase())) {
				return dtMetadata;
            }
		}

		// outta here
		return null;
	}

	/**
	 * Function to determine the datatype(s)
	 * of the datasource file (the file that was fetched from a datasource).
	 *
	 * @param filename String
	 * @param datatypeMetadata Collection<datatypeMetadata>
	 * @return Collection<DatatypeMetadata>
	 */
	public static Collection<DatatypeMetadata> getFileDatatype(final String filename, final Collection<DatatypeMetadata> datatypeMetadata) {

		Collection<DatatypeMetadata> toReturn = new ArrayList<DatatypeMetadata>();
		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
			for (String archive : dtMetadata.getDownloadArchives()) {
				if (filename.contains(archive)) {
					toReturn.add(dtMetadata);
				}
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Function to get datatype override file.
	 *
	 * @param datatype String
	 * @param datatypeMetadata Collection<DatatypeMetadata>
	 * @return String
	 */
	public static String getDatatypeOverrideFilename(final String datatype, final Collection<DatatypeMetadata> datatypeMetadata) {

        String toReturn = "";

		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
            if (dtMetadata.getDatatype().toLowerCase().equals(datatype.toLowerCase())) {
                toReturn = dtMetadata.getOverrideFilename();
                break;
            }
		}

		// outta here
		return toReturn;
	}
}
