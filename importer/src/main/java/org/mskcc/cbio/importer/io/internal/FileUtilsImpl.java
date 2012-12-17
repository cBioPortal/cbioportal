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
import org.mskcc.cbio.importer.Converter;
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.PortalMetadata;
import org.mskcc.cbio.importer.model.DataMatrix;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.CaseListMetadata;
import org.mskcc.cbio.importer.util.NormalizeExpressionLevels;

import org.mskcc.cbio.oncotator.OncotateTool;
import org.mskcc.cbio.mutassessor.MutationAssessorTool;

import org.apache.commons.io.*;
import org.apache.commons.codec.digest.DigestUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.File;
import java.io.PrintWriter;
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
    public Collection<File> listFiles(final File directory, final String[] extensions, final boolean recursive) throws Exception {

        return org.apache.commons.io.FileUtils.listFiles(directory, extensions, recursive);
    }

	/**
	 * Returns the contents of the datafile as specified by ImportData
     * in an DataMatrix.  PortalMetadata is used to help determine if an "override"
     * file exists.  May return null if there is a problem reading the file.
	 *
     * @param portalMetadata PortalMetadata
	 * @param importData ImportData
	 * @return DataMatrix
	 * @throws Exception
	 */
    @Override
	public DataMatrix getFileContents(final PortalMetadata portalMetadata, final ImportData importData) throws Exception {

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
        return getDataMatrix(fileContents);
    }

	/**
	 * Get staging file header.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @return stagingFilename String
	 * @throws Exception
	 */
	@Override
	public String getStagingFileHeader(final PortalMetadata portalMetadata, final String cancerStudy, final String stagingFilename) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("getStagingFileHeader(): " + stagingFilename);
		}

		String toReturn = "";

		// staging file
		File stagingFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
																   cancerStudy,
																   stagingFilename);
		// sanity check
		if (!stagingFile.exists()) {
			return toReturn;
		}

		org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(stagingFile);
		try {
			while (it.hasNext()) {
				toReturn = it.nextLine();
				break;
			}
		} finally {
			it.close();
		}

		// outta here
		return toReturn;
	}

	/**
	 * Creates a temporary file with the given contents.
	 *
	 * @param filename String
	 * @param fileContent String
	 * @return File
	 */
	@Override
	public File createTmpFileWithContents(final String filename, final String fileContent) throws Exception {

		return createFileWithContents(org.apache.commons.io.FileUtils.getTempDirectoryPath(), filename, fileContent);
	}

	/**
	 * Creates (or overwrites) the given file with the given contents.
	 *
	 * @param directory String
	 * @param filename String
	 * @param fileContent String
	 * @return File
	 */
	@Override
	public File createFileWithContents(final String directory, final String filename, final String fileContent) throws Exception {

		File file = org.apache.commons.io.FileUtils.getFile(directory, filename);
		org.apache.commons.io.FileUtils.writeStringToFile(file, fileContent, false);

		// outta here
		return file;
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

	/**
	 * Creates a staging file with contents from the given DataMatrix.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dataMatrix DataMatrix
	 * @throws Exception
	 */
	@Override
	public void writeStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
								 final DatatypeMetadata datatypeMetadata, final DataMatrix dataMatrix) throws Exception {

		// staging file
		String stagingFilename = datatypeMetadata.getStagingFilename();
		stagingFilename = stagingFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudy);
		File stagingFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
																   cancerStudy,
																   stagingFilename);

		if (LOG.isInfoEnabled()) {
			LOG.info("writingStagingFile(), staging file: " + stagingFile);
		}
																   
		FileOutputStream out = org.apache.commons.io.FileUtils.openOutputStream(stagingFile, false);
		dataMatrix.write(out);
		IOUtils.closeQuietly(out);

		// meta file
		if (datatypeMetadata.requiresMetafile()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("writingStagingFile(), creating metadata file for staging file: " + stagingFile);
			}
			writeMetadataFile(portalMetadata, cancerStudy, datatypeMetadata, dataMatrix);
		}
	}

	/**
	 * Creates a staging file for mutation data (and meta file) with contents from the given DataMatrix.
	 * This is called when the mutation file needs to be run through the Oncotator and Mutation Assessor Tools.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dataMatrix DataMatrix
	 * @throws Exception
	 */
	public void writeMutationStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
										 final DatatypeMetadata datatypeMetadata, final DataMatrix dataMatrix) throws Exception {

		// we only have data matrix at this point, we need to create a temp with its contents
		File oncotatorInputFile =
			org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
													"oncotatorInputFile");
		FileOutputStream out = org.apache.commons.io.FileUtils.openOutputStream(oncotatorInputFile);
		dataMatrix.write(out);
		IOUtils.closeQuietly(out);
		// create a temp output file from the oncotator
		File oncotatorOutputFile = 
			org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
													"oncotatorOutputFile");
		// call oncotator
		String[] oncotatorArgs = { oncotatorInputFile.getCanonicalPath(),
								   oncotatorOutputFile.getCanonicalPath() };
		if (LOG.isInfoEnabled()) {
			LOG.info("writingMutationStagingFile(), calling OncotateTool: " + Arrays.toString(oncotatorArgs));
		}
		OncotateTool.main(oncotatorArgs);
		// we call OMA here -
		// we already have input (oncotatorOutputFile)
		// output should be the path/name of staging file
		String stagingFilename = datatypeMetadata.getStagingFilename();
		stagingFilename = stagingFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudy);
		File stagingFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
																   cancerStudy,
																   stagingFilename);
		String[] omaArgs = { oncotatorOutputFile.getCanonicalPath(),
							 stagingFile.getCanonicalPath() };
		if (LOG.isInfoEnabled()) {
			LOG.info("writingMutationStagingFile(), calling MutationAssessorTool: " + Arrays.toString(omaArgs));
		}
		MutationAssessorTool.main(omaArgs);

		// clean up
		org.apache.commons.io.FileUtils.forceDelete(oncotatorInputFile);
		org.apache.commons.io.FileUtils.forceDelete(oncotatorOutputFile);

		// meta file
		if (datatypeMetadata.requiresMetafile()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("writingMutationStagingFile(), creating metadata file for staging file: " + stagingFile);
			}
			writeMetadataFile(portalMetadata, cancerStudy, datatypeMetadata, dataMatrix);
		}
	}

	/**
	 * Creates a z-score staging file from the given dependencies.  It assumes that the
	 * dependency - staging files have already been created.  This code also assumes
	 * that the dependencies are ordered by cna, then expression.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dependencies DatatypeMetadata[]
	 * @throws Exception
	 */
	public void writeZScoresStagingFile(final PortalMetadata portalMetadata, final String cancerStudy,
										final DatatypeMetadata datatypeMetadata, final DatatypeMetadata[] dependencies) throws Exception {

		// sanity check
		if (dependencies.length != 2) {
			throw new IllegalArgumentException("writeZScoresStagingFile(), datatypeMetadatas.length != 2, aborting...");
		}

		// check for existence of dependencies
		if (LOG.isInfoEnabled()) {
			LOG.info("writeZScoresStagingFile(), checking for existence of dependencies: " + Arrays.asList(dependencies));
		}
		File cnaFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
															   cancerStudy,
															   dependencies[0].getStagingFilename());
		if (!cnaFile.exists()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("writeZScoresStagingFile(), cannot find cna file dependency: " + cnaFile.getCanonicalPath());
			}
			return;
		}

		File expressionFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
																	  cancerStudy,
																	  dependencies[1].getStagingFilename());
		if (!expressionFile.exists()) { 
			if (LOG.isInfoEnabled()) {
				LOG.info("writeZScoresStagingFile(), cannot find expression file dependency: " + expressionFile.getCanonicalPath());
			}
			return;
		}

		// we need a zscore file
		File zScoresFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
																   cancerStudy,
																   datatypeMetadata.getStagingFilename());
		
		// call NormalizeExpressionLevels
		String[] args = { cnaFile.getCanonicalPath(), expressionFile.getCanonicalPath(), zScoresFile.getCanonicalPath() };
		if (LOG.isInfoEnabled()) {
			LOG.info("writingZScoresStagingFlie(), calling NormalizeExpressionLevels: " + Arrays.toString(args));
		}
		NormalizeExpressionLevels.main(args);
		
		// meta file
		if (datatypeMetadata.requiresMetafile()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("writingZScoresStagingFile(), creating metadata file for staging file: " + zScoresFile.getCanonicalPath());
			}
			writeMetadataFile(portalMetadata, cancerStudy, datatypeMetadata, null);
		}
	}

	/**
	 * Create a case list file from the given case list metadata file.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param caseListMetadata CaseListMetadata
	 * @param caseList String[]
	 * @throws Exception
	 */
	public void writeCaseListFile(final PortalMetadata portalMetadata, final String cancerStudy, final CaseListMetadata caseListMetadata, final String[] caseList) throws Exception {

		File caseListFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
																	cancerStudy,
																	"case_lists",
																	caseListMetadata.getCaseListFilename());

		if (LOG.isInfoEnabled()) {
			LOG.info("writeCaseListFile(), case list file: " + caseListFile.getCanonicalPath());
		}
		PrintWriter writer = new PrintWriter(org.apache.commons.io.FileUtils.openOutputStream(caseListFile, false));
		writer.print("cancer_study_identifier: " + cancerStudy + "\n");
		String stableID = caseListMetadata.getMetaStableID();
		stableID = stableID.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudy);
		writer.print("stable_id: " + stableID + "\n");
		writer.print("case_list_name: " + caseListMetadata.getMetaCaseListName() + "\n");
		String caseListDescription = caseListMetadata.getMetaCaseListDescription();
		caseListDescription = caseListDescription.replaceAll(DatatypeMetadata.NUM_CASES_TAG, Integer.toString(caseList.length));
		writer.print("case_list_description: " + caseListDescription + "\n");
		writer.print("case_list_category: " + caseListMetadata.getMetaCaseListCategory() + "\n");
		writer.print("case_list_ids: ");
		for (String caseID : caseList) {
			writer.print(caseID + Converter.CASE_DELIMITER);
		}
		writer.println();
		writer.flush();
		writer.close();
	}

	/**
	 * Helper method which writes a metadata file for the
	 * given DatatypeMetadata.  DataMatrix may be null.
	 *
     * @param portalMetadata PortalMetadata
	 * @param cancerStudy String
	 * @param datatypeMetadata DatatypeMetadata
	 * @param dataMatrix DataMatrix
	 * @throws Exception
	 *
	 */
	private void writeMetadataFile(final PortalMetadata portalMetadata, final String cancerStudy,
								   final DatatypeMetadata datatypeMetadata, final DataMatrix dataMatrix) throws Exception {

			File metaFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getStagingDirectory(),
																	cancerStudy,
																	datatypeMetadata.getMetaFilename());
			if (LOG.isInfoEnabled()) {
				LOG.info("writeMetadataFile(), meta file: " + metaFile);
			}
			PrintWriter writer = new PrintWriter(org.apache.commons.io.FileUtils.openOutputStream(metaFile, false));
			writer.print("cancer_study_identifier: " + cancerStudy + "\n");
			writer.print("genetic_alteration_type: " + datatypeMetadata.getMetaGeneticAlterationType() + "\n");
			String stableID = datatypeMetadata.getMetaStableID();
			stableID = stableID.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudy);
			writer.print("stable_id: " + stableID + "\n");
			writer.print("show_profile_in_analysis_tab: " + datatypeMetadata.getMetaShowProfileInAnalysisTab() + "\n");
			String profileDescription = datatypeMetadata.getMetaProfileDescription();
			if (dataMatrix != null) {
				profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_GENES_TAG, Integer.toString(dataMatrix.getGeneIDs().size()));
				profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_CASES_TAG, Integer.toString(dataMatrix.getCaseIDs().size()));
			}
			profileDescription = profileDescription.replaceAll(DatatypeMetadata.TUMOR_TYPE_TAG, cancerStudy.split("_")[0]);
			writer.print("profile_description: " + profileDescription + "\n");
			writer.print("profile_name: " + datatypeMetadata.getMetaProfileName() + "\n");
			writer.flush();
			writer.close();
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
                    String dataFile = importData.getDataFilename();
                    if (dataFile.contains(TumorTypeMetadata.TUMOR_TYPE_REGEX)) {
                        dataFile = dataFile.replaceAll(TumorTypeMetadata.TUMOR_TYPE_REGEX, importData.getTumorType().toUpperCase());
                    }
                    if (entryName.contains(dataFile)) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Processing tar-archive: " + importData.getDataFilename());
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
     * Helper function to create DataMatrix.
     *
     * @param data byte[]
     * @return DataMatrix
     */
    private DataMatrix getDataMatrix(final byte[] data) throws Exception {

        // iterate over all lines in byte[]
        Vector<String> columnNames = null;
        Vector<Vector<String>> rowData = null;
        LineIterator it = IOUtils.lineIterator(new ByteArrayInputStream(data), null);
        try {
            int count = -1;
            while (it.hasNext()) {
                // first row is our column heading, create column vector
                if (++count == 0) {
                    columnNames = new Vector(Arrays.asList(it.nextLine().split(Converter.CASE_DELIMITER, -1)));
                }
                // all other rows are rows in the table
                else {
                    rowData = (rowData == null) ? new Vector<Vector<String>>() : rowData;
                    rowData.add(new Vector(Arrays.asList(it.nextLine().split(Converter.CASE_DELIMITER, -1))));
                }
            }
        }
        finally {
            LineIterator.closeQuietly(it);
        }

        // problem reading from data?
        if (columnNames == null && rowData == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("getDataMatrix(), problem creating DataMatrix from file");
            }
            return null;
        }

        // made it here, we can create DataMatrix
        if (LOG.isInfoEnabled()) {
            LOG.info("creating new DataMatrix(), from file data");
        }

        // outta here
        return new DataMatrix(rowData, columnNames);
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
		String overrideFilename = importData.getOverrideFilename();

		// no need to continue if we don't have an override filename
		if (overrideFilename == null || overrideFilename.length() == 0) {
			return toReturn;
		}

        // we have to contruct the path to the override file
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
            if (overrideFilename.contains(TumorTypeMetadata.TUMOR_TYPE_REGEX)) {
                    overrideFilename = overrideFilename.replaceAll(TumorTypeMetadata.TUMOR_TYPE_REGEX,
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