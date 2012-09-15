// package
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;

import java.io.File;
import javax.swing.JTable;
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
	 * Returns the given file contents in a JTable.
	 *
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
	 * @return JTable
	 * @throws Exception
	 */
	JTable getFileContents(final PortalMetadata portalMetadata, final ImportData importData) throws Exception;

    /**
     * Reflexively creates a new instance of the given class.
     *
     * @param className String
     * @return Object
     */
    Object newInstance(final String className);
}
