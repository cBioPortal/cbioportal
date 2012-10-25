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
package org.mskcc.cbio.importer.io.internal;

// imports
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.ImportDataMatrix;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;

import org.apache.commons.io.*;
import org.apache.commons.codec.digest.DigestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayInputStream;

import java.lang.reflect.Constructor;

import java.net.URL;
import java.util.Arrays;
import java.util.Vector;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 * Class which implements the FileUtils interface.
 */
final class FileUtilsImpl implements org.mskcc.cbio.importer.FileUtils {

    // used in unzip method
    private static final int BUFFER = 2048;

	// our logger
	private static final Log LOG = LogFactory.getLog(FileUtilsImpl.class);

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

        String toReturn = "";
        InputStream is = org.apache.commons.io.FileUtils.openInputStream(file);
        try {
            toReturn = DigestUtils.md5Hex(is);
        }
        finally {
            IOUtils.closeQuietly(is);
        }

        // outta here
        return toReturn;
	}

	/**
	 * Reads the precomputed md5 digest out of a .md5 file (firehose).
     * Assume the file only contains one line wit checksum.
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
        LineIterator it = org.apache.commons.io.FileUtils.lineIterator(file);
        try {
            while (it.hasNext()) {
                String content = it.nextLine();
                if (content.split(" ").length == 2) {
                    toReturn = content.split(" ")[0].toUpperCase();
                }   
            }
        }
        finally {
            LineIterator.closeQuietly(it);
        }

		// outta here
		return toReturn;
    }

    /**
     * Makes a directory, including parent directories if necessary.
     *
     * @param directory File
     */
    @Override
    public void makeDirectory(final File directory) throws Exception {
        
        org.apache.commons.io.FileUtils.forceMkdir(directory);
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory File
     */
    @Override
    public void deleteDirectory(final File directory) throws Exception {

        org.apache.commons.io.FileUtils.deleteDirectory(directory);
    }

    /**
     * Lists all files in a given directory and its subdirectories.
     *
     * @param directory File
     * @param extensions String[]
     * @param recursize boolean
     * @return Collection<File>
     */
    @Override
    public Collection<File> listFiles(final File directory, String[] extensions, boolean recursive) throws Exception {

        return org.apache.commons.io.FileUtils.listFiles(directory, extensions, recursive);
    }

	/**
	 * Returns the contents of the datafile as specified by ImportData
     * in an ImportDataMatrix.  PortalMetadata is used to help determine if an "override"
     * file exists.
	 *
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
	 * @return ImportDataMatrix
	 * @throws Exception
	 */
    @Override
	public ImportDataMatrix getFileContents(final PortalMetadata portalMetadata, final ImportData importData) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("getFileContents(): " + importData);
		}

        // determine path to file (does override file exist?)
        String fileCanonicalPath = getCanonicalPath(portalMetadata, importData);

        // get filedata inputstream
        byte[] fileContents;

        // data can be compressed
		if (GzipUtils.isCompressedFilename(fileCanonicalPath.toLowerCase())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("getFileContents(): processing file: " + fileCanonicalPath);
            }
            fileContents = readContent(importData,
                                       org.apache.commons.io.FileUtils.openInputStream(new File(fileCanonicalPath)));
        }
        else {
            if (LOG.isInfoEnabled()) {
                LOG.info("getFileContents(): processing file: " + fileCanonicalPath);
            }
            fileContents = org.apache.commons.io.FileUtils.readFileToByteArray(new File(fileCanonicalPath));
        }

        // outta here
        return getImportDataMatrix(fileContents);
    }

	/**
	 * Downloads the given file specified via url to the given canonicalDestination.
	 *
	 * @param urlString String
	 * @param canonicalDestination String
	 * @throws Exception
	 */
	@Override
	public void downloadFile(final String urlString, final String canonicalDestination) throws Exception {

		// sanity check
		if (urlString == null || urlString.length() == 0 ||
			canonicalDestination == null || canonicalDestination.length() == 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("downloadFile(): url or canonicalDestination argument is null, returning...");
            }
			return;
		}

		URL url = new URL(urlString);
		File destinationFile = org.apache.commons.io.FileUtils.getFile(canonicalDestination);
		if (LOG.isInfoEnabled()) {
			LOG.info("downloadFile(), destination: " + destinationFile.getCanonicalPath());
			LOG.info("downloadFile(), this may take a while...");
		}
		org.apache.commons.io.FileUtils.copyURLToFile(url, destinationFile);

		// unzip if necessary
		if (GzipUtils.isCompressedFilename(urlString)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("downloadFile(), gunzip: " + destinationFile.getCanonicalPath());
			}
			String unzipFile = gunzip(destinationFile.getCanonicalPath());
			if (LOG.isInfoEnabled()) {
				LOG.info("downloadFile(), gunzip complete: " + (new File(unzipFile)).getCanonicalPath());
			}
		}
	}

    /*
     * Given a zip stream, unzips it and gets contents of desired data file.
     * This routine will attempt to close the given input stream.
     *
     * @param importData ImportData
     * @param is InputStream
     * @return byte[]
     */
    private byte[] readContent(final ImportData importData, final InputStream is) throws Exception {

        byte[] toReturn = null;
        TarArchiveInputStream tis = null;
        GzipCompressorInputStream gzis = new GzipCompressorInputStream(is);

        try {
            // decompress .gz file
            if (LOG.isInfoEnabled()) {
                LOG.info("readContent(), decompressing: " + importData.getCanonicalPathToData());
            }

            InputStream unzippedContent = IOUtils.toBufferedInputStream((InputStream)gzis);
            // if tarball, untar
            if (importData.getCanonicalPathToData().toLowerCase().endsWith("tar.gz")) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("readContent(), gzip file is a tarball, untarring");
                }
                tis = new TarArchiveInputStream(unzippedContent);
                TarArchiveEntry entry = null;
                while ((entry = tis.getNextTarEntry()) != null) {
                    String entryName = entry.getName();
                    String dataFile = importData.getDatafile();
                    if (dataFile.contains(TumorTypeMetadata.TUMOR_TYPE_REGEX)) {
                        dataFile = dataFile.replace(TumorTypeMetadata.TUMOR_TYPE_REGEX, importData.getTumorType().toUpperCase());
                    }
                    if (entryName.contains(dataFile)) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Processing tar-archive: " + importData.getDatafile());
                        }
                        toReturn = IOUtils.toByteArray(tis, entry.getSize());
                        break;
                    }
                }
            }
            else {
                toReturn = IOUtils.toByteArray(gzis);
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            IOUtils.closeQuietly(tis);
            IOUtils.closeQuietly(gzis);
            IOUtils.closeQuietly(is);
        }
        
        // outta here
        return toReturn;
    }

    /**
     * Helper function to create ImportDataMatrix.
     *
     * @param data byte[]
     * @return ImportDataMatrix
     */
    private ImportDataMatrix getImportDataMatrix(final byte[] data) throws Exception {

        // iterate over all lines in byte[]
        Vector<String> columnNames = null;
        Vector<Vector<String>> rowData = null;
        LineIterator it = IOUtils.lineIterator(new ByteArrayInputStream(data), null);
        try {
            int count = -1;
            while (it.hasNext()) {
                // first row is our column heading, create column vector
                if (++count == 0) {
                    columnNames = new Vector(Arrays.asList(it.nextLine().split("\t", -1)));
                }
                // all other rows are rows in the table
                else {
                    rowData = (rowData == null) ? new Vector<Vector<String>>() : rowData;
                    rowData.add(new Vector(Arrays.asList(it.nextLine().split("\t", -1))));
                }
            }
        }
        finally {
            LineIterator.closeQuietly(it);
        }

        // problem reading from data?
        if (columnNames == null && rowData == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("getImportDataMatrix(), problem creating ImportDataMatrix from file");
            }
            return new ImportDataMatrix();
        }

        // made it here, we can create ImportDataMatrix
        if (LOG.isInfoEnabled()) {
            LOG.info("creating new ImportDataMatrix(), from file data");
        }

        // outta here
        return new ImportDataMatrix(rowData, columnNames);
    }

    /**
     * Helper function to get canonical path to data.
     * PortalMetadata is used to help determine if an "override"
     * file exists.  This assumes that override directory names
     * are cancer study names, like brca_tcga.
     *
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
     * @return String
     */
    private String getCanonicalPath(final PortalMetadata portalMetadata, final ImportData importData) throws Exception {

        // by default return importData's canonical path
        String toReturn = importData.getCanonicalPathToData();

        // look for override file
        String potentialOverrideCancerStudyDir = null;
        String tumorType = importData.getTumorType().toLowerCase();
        // look for a cancer study that matches the tumor type
        for (String cancerStudy : portalMetadata.getCancerStudies()) {
            if (cancerStudy.toLowerCase().contains(tumorType)) {
                potentialOverrideCancerStudyDir = cancerStudy.toLowerCase();
                break;
            }
        }

        if (potentialOverrideCancerStudyDir != null) {
            // construct override filename
            String overrideFilename = importData.getOverrideFilename();
            if (overrideFilename.contains(TumorTypeMetadata.TUMOR_TYPE_REGEX)) {
                    overrideFilename = overrideFilename.replace(TumorTypeMetadata.TUMOR_TYPE_REGEX,
                                                                tumorType.toUpperCase());
            }
            // check for existence of override file
            File potentialOverrideFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getConvertOverrideDirectory(),
                                                                                 potentialOverrideCancerStudyDir,
                                                                                 overrideFilename);
            // if file exists, return its canonical path
            if (potentialOverrideFile.exists()) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("getCanonicalPath(), we found an override file: " + potentialOverrideFile.getCanonicalPath());
                }
                toReturn = potentialOverrideFile.getCanonicalPath();
            }
            else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("getCanonicalPath(), we did not find an override file");
                }
            }
        }

        // outta here
        return toReturn;
    }


	/**
	 * Helper function to gunzip file.
	 *
	 * @param inFilePath String
	 * @return String
	 */
	private static String gunzip(final String inFilePath) throws Exception {

		// setup our gzip inputs tream
		FileOutputStream out = null;
		String outFilePath = GzipUtils.getUncompressedFilename(inFilePath);
		GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(inFilePath));
 
		try {
			// unzip into file less the .gz
			out = new FileOutputStream(outFilePath);
			IOUtils.copy(gzipInputStream, out);
		}
		finally {
			// close up our streams
			IOUtils.closeQuietly(gzipInputStream);
			if (out != null) IOUtils.closeQuietly(out);
			// delete gzipped file
			new File(inFilePath).delete();
		}

		// outta here
		return outFilePath;
 	}
}