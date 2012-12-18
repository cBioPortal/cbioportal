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
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.ImportDataMatrix;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.model.CaseListMetadata;

import java.io.File;
import java.util.Collection;

/**
 * Interface used to access some common file utils.
 */
public interface FileUtils {

	/**
	 * Computes the MD5 digest for the given file.
	 * Returns the 32 digit hexadecimal.
	 *
	 * @param file File
	 * @return String
	 * @throws Exception
	 */
	String getMD5Digest(final File file) throws Exception;

	/**
	 * Reads the precomputed md5 digest out of a firehose .md5 file.
	 *
	 * @param file File
	 * @return String
	 * @throws Exception 
	 */
	String getPrecomputedMD5Digest(final File file) throws Exception;

    /**
     * Makes a directory, including parent directories if necessary.
     *
     * @param directory File
     */
    void makeDirectory(final File directory) throws Exception;

    /**
     * Deletes a directory recursively.
     *
     * @param directory File
     */
    void deleteDirectory(final File directory) throws Exception;

    /**
     * Lists all files in a given directory and its subdirectories.
     *
     * @param directory File
     * @param extensions String[]
     * @param recursize boolean
     * @return Collection<File>
     */
    Collection<File> listFiles(final File directory, String[] extensions, boolean recursive) throws Exception;

	/**
	 * Returns the given file contents in an ImportDataMatrix.
	 *
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
	 * @return ImportDataMatrix
	 * @throws Exception
	 */
	ImportDataMatrix getFileContents(final PortalMetadata portalMetadata, final ImportData importData) throws Exception;

	/**
	 * Get staging file header.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @return stagingFile String
	 * @throws Exception
	 */
	String getStagingFileHeader(final PortalMetadata portalMetadata, final String cancerStudy, final String stagingFile) throws Exception;

	/**
	 * Creates a temporary file with the given contents.
	 *
	 * @param filename String
	 * @param fileContent String
	 * @return File
	 */
	File createTmpFileWithContents(final String filename, final String fileContent) throws Exception;

	/**
	 * Creates (or overwrites) the given file with the given contents.
	 *
	 * @param directory String
	 * @param filename String
	 * @param fileContent String
	 * @return File
	 */
	File createFileWithContents(final String directory, final String filename, final String fileContent) throws Exception;

	/**
	 * Downloads the given file specified via url to the given canonicalDestination.
	 *
	 * @param urlString String
	 * @param canonicalDestination String
	 * @throws Exception
	 */
	void downloadFile(final String urlString, final String canonicalDestination) throws Exception;

	/**
	 * Creates a staging file (and meta file) with contents from the given ImportDataMatrix.
	 *
	 * @param dataSourceMetadata DataSourceMetadata
	 * @param datatypeMetadata DatatypeMetadata
     * @param portalMetadata PortalMetadata
	 * @param importDataMatrix ImportDataMatrix
	 * @throws Exception
	 */
	void writeStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
						  final DatatypeMetadata datatypeMetadata, final ImportDataMatrix importDataMatrix) throws Exception;

	/**
	 * Creates a staging file for mutation data (and meta file) with contents from the given ImportDataMatrix.
	 * This is called when the mutation file needs to be run through the Oncotator and Mutation Assessor Tools.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param importDataMatrix ImportDataMatrix
	 * @throws Exception
	 */
	void writeMutationStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
								  final DatatypeMetadata datatypeMetadata, final ImportDataMatrix importDataMatrix) throws Exception;

	/**
	 * Creates a z-score staging file from the given dependencies.  It assumes that the
	 * dependency - staging files have already been created.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dependencies DatatypeMetadata[]
	 * @throws Exception
	 */
	void writeZScoresStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
								 final DatatypeMetadata datatypeMetadata, final DatatypeMetadata[] dependencies) throws Exception;

	/**
	 * Create a case list file from the given case list metadata file.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param caseListMetadata CaseListMetadata
	 * @param caseList String[]
	 * @throws Exception
	 */
	void writeCaseListFile(final PortalMetadata portalMetadata, final String cancerStudy, final CaseListMetadata caseListMetadata, final String[] caseList) throws Exception;
}
