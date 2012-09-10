// package
package org.mskcc.cbio.importer;

// imports
import java.io.File;
import java.nio.ByteBuffer;

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
	String getMD5Digest(File file) throws Exception;

	/**
	 * Reads the precomputed md5 digest out of a firehose .md5 file.
	 *
	 * @param file File
	 * @return String
	 * @throws Exception 
	 */
	String getPrecomputedMD5Digest(File file) throws Exception;

	/**
	 * Returns the given file contents.
	 *
	 * @param file File
	 * @return ByteBuffer
	 * @throws Exception
	 */
	ByteBuffer getFileContents(File file) throws Exception;
}
