// package
package org.mskcc.cbio.firehose.util.internal;

// imports
import org.mskcc.cbio.firehose.FileUtils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.DefaultResourceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import java.security.MessageDigest;

/**
 * Class which implements the FileUtils interface.
 */
final class FileUtilsImpl implements FileUtils {

    // used for md5 digest display
	static final byte[] HEX_CHAR_TABLE = {
		(byte)'0', (byte)'1', (byte)'2', (byte)'3',
		(byte)'4', (byte)'5', (byte)'6', (byte)'7',
		(byte)'8', (byte)'9', (byte)'a', (byte)'b',
		(byte)'c', (byte)'d', (byte)'e', (byte)'f'
	};

	// our logger
	private static final Log LOG = LogFactory.getLog(FileUtilsImpl.class);

	// LOADER can handle file://
	private static final ResourceLoader LOADER = new DefaultResourceLoader();

	/**
	 * Computes the MD5 digest for the given file.
	 * Returns the 32 digit hexadecimal.
	 *
	 * @param filename String
	 * @return String
	 * @throws Exception
	 */
	@Override
	public String getMD5Digest(final File file) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("getMD5Digest(): " + file.getCanonicalPath());
		}

		// compute the size of the file
		Resource resource = LOADER.getResource(file.toURI().toURL().toString());
		long size = 0;
		if (resource.isReadable()) {
			size = resource.contentLength();
			if (LOG.isInfoEnabled()) {
				LOG.info("file length: " + size);
			}
		}
		// read the file content into a bytebuffer
		ReadableByteChannel source = Channels.newChannel(resource.getInputStream());
		ByteBuffer byteBuffer = ByteBuffer.allocate((int)size);
		source.read(byteBuffer);
		
		// outta here
		return computeMD5Digest(byteBuffer.array());
	}

	/**
	 * Reads the precomputed md5 digest out of a firehose .md5 file.
	 *
	 * @param file File
	 * @return String
	 * @throws Exception 
	 */
	@Override
	public String getPrecomputedMD5Digest(final File file) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("getPrecomputedMD5Digest(): " + file.getCanonicalPath());
		}

		String toReturn = "";
		InputStream inputStream = LOADER.getResource(file.toURI().toURL().toString()).getInputStream();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String content = reader.readLine();
			if (content != null && content.split(" ").length == 2) {
				toReturn = content.split(" ")[0].toUpperCase();
			}
		}
		finally {
			closeQuietly(inputStream);
		}

		// outta here
		return toReturn;
    }

	/**
	 * Given the following string, computes an MD5 digest.
	 *
	 * @param data byte[]
	 * @return String
	 * @throws Exception
	 */
	private String computeMD5Digest(final byte[] data) throws Exception {

		// setup the digest
		MessageDigest digest = null;
		digest = MessageDigest.getInstance("MD5");
		digest.reset();

		// outta here
		return getHexString(digest.digest(data));
	}

	/**
	 * Converts byte[] to displayable string.
	 *
	 * @param raw byte[]
	 * @return String
	 * @throws Exception
	 */
	public static String getHexString(final byte[] raw) throws Exception {

        byte[] hex = new byte[2 * raw.length];
        int index = 0;
        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

		// outta here
        return new String(hex, "ASCII").toUpperCase();
	}

    /*
	 * Close the specified Input Stream.
	 *
	 * @param is InputStream
	 */
    private static void closeQuietly(final InputStream is) {

        try {
            is.close();
        }
        catch (Exception e) {
			LOG.warn("FileUtilsImpl.closeQuietly() failed." + e);
        }
    }
}