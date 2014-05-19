/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/

// package
package org.mskcc.cbio.importer.io.internal;

// imports
import org.mskcc.cbio.importer.*;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.portal.scripts.*;
import org.mskcc.cbio.importer.util.*;
import org.mskcc.cbio.portal.model.CopyNumberSegmentFile;
import org.mskcc.cbio.importer.converter.internal.MethylationConverterImpl;

import org.mskcc.cbio.liftover.Hg18ToHg19;
import org.mskcc.cbio.oncotator.OncotateTool;
import org.mskcc.cbio.mutassessor.MutationAssessorTool;

import org.apache.commons.io.*;
import org.apache.commons.codec.digest.DigestUtils;

import org.apache.commons.logging.*;

import org.apache.commons.compress.compressors.gzip.GzipUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.lang.reflect.Constructor;
import java.util.zip.GZIPInputStream;

/**
 * Class which implements the FileUtils interface.
 */
class FileUtilsImpl implements org.mskcc.cbio.importer.FileUtils {

    // used in unzip method
    private static int BUFFER = 2048;

	// our logger
	private static Log LOG = LogFactory.getLog(FileUtilsImpl.class);

	// ref to config
	private Config config;

	// ref to caseids
	private CaseIDs caseIDs;

	// location of lift over binary
	private String liftoverBinary;
	@Value("${liftover_binary}")
	public void setLiftOverBinary(String property) { this.liftoverBinary = property; }
	public String getLiftOverBinary() { return MetadataUtils.getCanonicalPath(liftoverBinary); }

	// location of lift over chain
	private String liftoverChain;
	@Value("${liftover_chain_file}")
	public void setLiftOverChain(String property) { this.liftoverChain = property; }
	public String getLiftOverChain() { return MetadataUtils.getCanonicalPath(liftoverChain); }

	public FileUtilsImpl(Config config, CaseIDs caseIDs) {

		// set members
		this.config = config;
		this.caseIDs = caseIDs;
	}

	@Override
	public String getMD5Digest(File file) throws Exception {

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

	@Override
	public String getPrecomputedMD5Digest(File file) throws Exception {

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

    @Override
    public void makeDirectory(File directory) throws Exception {
        
        org.apache.commons.io.FileUtils.forceMkdir(directory);
    }

	@Override
	public boolean directoryIsEmpty(File directory) throws Exception {
		return (listFiles(directory, null, true).isEmpty());
	}

    @Override
    public void deleteDirectory(File directory) throws Exception {

        org.apache.commons.io.FileUtils.deleteDirectory(directory);
    }

    @Override
    public void deleteFile(File file) throws Exception {
		if (file.exists()) org.apache.commons.io.FileUtils.forceDelete(file);
	}

    @Override
    public Collection<File> listFiles(File directory, String[] extensions, boolean recursive) throws Exception {

        return org.apache.commons.io.FileUtils.listFiles(directory, extensions, recursive);
    }

    @Override
	public List<DataMatrix> getDataMatrices(ImportDataRecord importDataRecord, DataMatrix methylationCorrelation) throws Exception {

        List<DataMatrix> dataMatrices = new ArrayList<DataMatrix>();

		if (LOG.isInfoEnabled()) {
			LOG.info("getDataMatrices(): " + importDataRecord);
		}

        // determine path to file (does override file exist?)
        String fileCanonicalPath = importDataRecord.getCanonicalPathToData();

        // data can be compressed
		if (GzipUtils.isCompressedFilename(fileCanonicalPath.toLowerCase())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("getDataMatrices(): processing file: " + fileCanonicalPath);
            }
            dataMatrices.addAll(getDataMatricesFromArchive(importDataRecord, methylationCorrelation));
        }
        else {
            if (LOG.isInfoEnabled()) {
                LOG.info("getDataMatrices(): processing file: " + fileCanonicalPath);
            }
            File dataFile = new File(fileCanonicalPath);
            InputStream is = org.apache.commons.io.FileUtils.openInputStream(dataFile);
            DataMatrix m = getDataMatrix(dataFile.getName(), is, methylationCorrelation);
            if (m != null) {
	            dataMatrices.add(m);
	        }
            IOUtils.closeQuietly(is);
        }

        // outta here
        return dataMatrices;
    }

	@Override
	public List<String> getMissingCaseListFilenames(String rootDirectory, CancerStudyMetadata cancerStudyMetadata) throws Exception {

		ArrayList toReturn = new ArrayList<String>();
		String caseListDirectory = (rootDirectory + File.separator + cancerStudyMetadata.getStudyPath() + File.separator + "case_lists");
		for (CaseListMetadata caseListMetadata : config.getCaseListMetadata(Config.ALL)) {
			String caseListFilename = caseListDirectory + File.separator + caseListMetadata.getCaseListFilename();
			File caseListFile = new File(caseListFilename);
			if (!caseListFile.exists()) toReturn.add(caseListFilename);
		}
		return toReturn;
	}

	@Override
	public void generateCaseLists(boolean overwrite, boolean strict, String stagingDirectory, CancerStudyMetadata cancerStudyMetadata) throws Exception {

		// iterate over case lists
		for (CaseListMetadata caseListMetadata : config.getCaseListMetadata(Config.ALL)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("generateCaseLists(), processing cancer study: " + cancerStudyMetadata + ", case list: " + caseListMetadata.getCaseListFilename());
			}
			File caseListFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																		cancerStudyMetadata.getStudyPath(),
																		"case_lists",
																		caseListMetadata.getCaseListFilename());
			if (caseListFile.exists() && !overwrite) {
				if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), caseListFile exists and overwrite is false, skipping caselist...");
				}
				continue;
			}
			// how many staging files are we working with?
			String[] stagingFilenames = null;
			// setup union/intersection bools
			boolean unionCaseList = 
				caseListMetadata.getStagingFilenames().contains(CaseListMetadata.CASE_LIST_UNION_DELIMITER);
			boolean intersectionCaseList = 
				caseListMetadata.getStagingFilenames().contains(CaseListMetadata.CASE_LIST_INTERSECTION_DELIMITER);
			// union (like all cases)
			if (unionCaseList) {
				stagingFilenames = caseListMetadata.getStagingFilenames().split("\\" + CaseListMetadata.CASE_LIST_UNION_DELIMITER);
			}
			// intersection (like complete or cna-seq)
			else if (intersectionCaseList) {
				stagingFilenames = caseListMetadata.getStagingFilenames().split("\\" + CaseListMetadata.CASE_LIST_INTERSECTION_DELIMITER);
			}
			// just a single staging file
			else {
				stagingFilenames = new String[] { caseListMetadata.getStagingFilenames() };
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("generateCaseLists(), stagingFilenames: " + java.util.Arrays.toString(stagingFilenames));
			}
			// this is the set we will pass to writeCaseListFile
			LinkedHashSet<String> caseSet = new LinkedHashSet<String>();
			// this indicates the number of staging files processed -
			// used to verify that an intersection should be written
			int numStagingFilesProcessed = 0;
			for (String stagingFilename : stagingFilenames) {
				if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), processing stagingFile: " + stagingFilename);
				}
				// compute the case set
				List<String> caseList = getCaseListFromStagingFile(strict, caseIDs, cancerStudyMetadata, stagingDirectory, stagingFilename);
				// we may not have this datatype in study
				if (caseList.size() == 0) {
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), stagingFileHeader is empty: " + stagingFilename + ", skipping...");
					}
					continue;
				}
				// intersection 
				if (intersectionCaseList) {
					if (caseSet.isEmpty()) {
						caseSet.addAll(caseList);
					}
					else {
						caseSet.retainAll(caseList);
					}
				}
				// otherwise union or single staging (treat the same)
				else {
					caseSet.addAll(caseList);
				}
				++numStagingFilesProcessed;
			}
			// write the case list file (don't make empty case lists)
			if (caseSet.size() > 0) {
				if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), calling writeCaseListFile()...");
				}
				// do not write out complete cases file unless we've processed all the files required
				if (intersectionCaseList && (numStagingFilesProcessed != stagingFilenames.length)) {
					if (LOG.isInfoEnabled()) {
						LOG.info("generateCaseLists(), number of staging files processed != number staging files required for cases_complete.txt, skipping call to writeCaseListFile()...");
					}
					continue;
				}
				writeCaseListFile(stagingDirectory, cancerStudyMetadata, caseListMetadata, caseSet.toArray(new String[0]));
			}
			else if (LOG.isInfoEnabled()) {
				LOG.info("generateCaseLists(), caseSet.size() <= 0, skipping call to writeCaseListFile()...");
			}
			// if union, write out the cancer study metadata file & patient list
			if (overwrite && caseSet.size() > 0 && caseListMetadata.getCaseListFilename().equals(CaseListMetadata.ALL_CASES_FILENAME)) {
				if (LOG.isInfoEnabled()) {
					LOG.info("generateCaseLists(), processed all cases list, we can now update cancerStudyMetadata file()...");
				}
				writeCancerStudyMetadataFile(stagingDirectory, cancerStudyMetadata, caseSet.size());
			}
		}
	}

	@Override
	public List<String> getCaseListFromStagingFile(boolean strict, CaseIDs caseIDs, CancerStudyMetadata cancerStudyMetadata, String stagingDirectory, String stagingFilename) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("getCaseListFromStagingFile(): " + stagingFilename);
		}

		// we use set here
		HashSet<String> caseSet = new HashSet<String>();

		// if we are processing mutations data and a sequencedSamplesFile exists, use it
		if (stagingFilename.equals(DatatypeMetadata.MUTATIONS_STAGING_FILENAME)) {
			File sequencedSamplesFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																				cancerStudyMetadata.getStudyPath(),
																				DatatypeMetadata.SEQUENCED_SAMPLES_FILENAME);
			if (sequencedSamplesFile.exists()) {
				if (LOG.isInfoEnabled()) LOG.info("getCaseListFromStagingFile(), sequenceSamplesFile exists, calling getCaseListFromSequencedSamplesFile()");
				return getCaseListFromSequencedSamplesFile(sequencedSamplesFile);
			}
		}

		// staging file
		File stagingFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																   cancerStudyMetadata.getStudyPath(),
																   stagingFilename);
		// sanity check
		if (!stagingFile.exists()) {
			return new ArrayList<String>();
		}

		// iterate over all rows in file
		org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(stagingFile);
		try {
			int mafCaseIDColumnIndex = 0;
			boolean processHeader = true;
			while (it.hasNext()) {
                                String line = it.next();
                                if (line.startsWith("#")) {
                                    if (line.startsWith("#"+Converter.MUTATION_CASE_LIST_META_HEADER+":")) {
                                        return Arrays.asList(line.substring(Converter.MUTATION_CASE_LIST_META_HEADER.length()+2).trim().split("[ \t]+"));
                                    }
                                    continue;
                                }
                                
				// create a string list from row in file
				List<String> thisRow = Arrays.asList(line.split(Converter.VALUE_DELIMITER));
				// is this the header file?
				if (processHeader) {
					// look for MAF file case id column header
					mafCaseIDColumnIndex = thisRow.indexOf(Converter.MUTATION_CASE_ID_COLUMN_HEADER);
					// this is not a MAF file, header contains the case ids, return here
					if (mafCaseIDColumnIndex  == -1) {
						if (LOG.isInfoEnabled()) LOG.info("getCaseListFromStagingFile(), this is not a MAF header contains sample ids...");
						for (String potentialCaseID : thisRow) {
							if (!strict || caseIDs.isSampleId(potentialCaseID)) {
								// check to filter out column headers other than sample ids
								if (Converter.NON_CASE_IDS.contains(potentialCaseID.toUpperCase())) {
									continue;
								}
								caseSet.add(caseIDs.getPatientId(potentialCaseID));
							}
						}
						break;
					}
					else {
						if (LOG.isInfoEnabled()) LOG.info("getCaseListFromStagingFile(), this is a MAF, samples ids in col: " + mafCaseIDColumnIndex);
					}
					processHeader = false;
					continue;
				}
				// we want to add the value at mafCaseIDColumnIndex into return set - this is a case ID
				String potentialCaseID = thisRow.get(mafCaseIDColumnIndex);
				if (!strict || caseIDs.isSampleId(potentialCaseID)) {
					caseSet.add(caseIDs.getPatientId(potentialCaseID));
				}
			}
		} finally {
			it.close();
		}

		// outta here
		return new ArrayList<String>(caseSet);
	}

	@Override
	public File createTmpFileWithContents(String filename, String fileContent) throws Exception {

		File tmpFile = org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectoryPath(),
															   filename);
		org.apache.commons.io.FileUtils.writeStringToFile(tmpFile, fileContent, false);
		return tmpFile;
	}

	@Override
	public File createFileWithContents(String filename, String fileContent) throws Exception {

		File file = org.apache.commons.io.FileUtils.getFile(filename);
		org.apache.commons.io.FileUtils.writeStringToFile(file, fileContent, false);

		// outta here
		return file;
	}

	@Override
	public File createFileFromStream(String filename, InputStream is) throws Exception {

		File file = org.apache.commons.io.FileUtils.getFile(filename);
		org.apache.commons.io.FileUtils.copyInputStreamToFile(is, file);

		// outta here
		return file;
	}

	@Override
	public void downloadFile(String urlSource, String urlDestination) throws Exception {
        
		// sanity check
		if (urlSource == null || urlSource.length() == 0 ||
			urlDestination == null || urlDestination.length() == 0) {
			throw new IllegalArgumentException("downloadFile(): urlSource or urlDestination argument is null...");
		}

		// URLs for given parameters
		URL source = new URL(urlSource);
		URL destination = new URL(urlDestination);

		// we have a compressed file
		if (GzipUtils.isCompressedFilename(urlSource)) {
			// downlod to temp destination
			File tempDestinationFile = org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
																			   ""+System.currentTimeMillis()+"."+new File(source.getFile()).getName());
			if (LOG.isInfoEnabled()) {
				LOG.info("downloadFile(), " + urlSource + ", this may take a while...");
			}
			org.apache.commons.io.FileUtils.copyURLToFile(source, tempDestinationFile);
			if (LOG.isInfoEnabled()) {
				LOG.info("downloadFile(), gunzip: we have compressed file, decompressing...");
			}
			// decompress the file
			gunzip(tempDestinationFile.getCanonicalPath());
			if (LOG.isInfoEnabled()) {
				LOG.info("downloadFile(), gunzip complete...");
			}
			// move temp/decompressed file to final destination
			File destinationFile = new File(destination.getFile());
			if (destinationFile.exists()) {
				org.apache.commons.io.FileUtils.forceDelete(destinationFile);
			}
			org.apache.commons.io.FileUtils.moveFile(org.apache.commons.io.FileUtils.getFile(GzipUtils.getUncompressedFilename(tempDestinationFile.getCanonicalPath())),
													 destinationFile);

			// lets cleanup after ourselves - remove compressed file
			tempDestinationFile.delete();
		}
		// uncompressed file, download directry to urlDestination
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("downloadFile(), " + urlSource + ", this may take a while...");
			}
			org.apache.commons.io.FileUtils.copyURLToFile(source,
														  org.apache.commons.io.FileUtils.getFile(destination.getFile()));
		}
	}

	@Override
	public LineIterator getFileContents(String urlFile) throws Exception {
		return org.apache.commons.io.FileUtils.lineIterator(new File(new URL(urlFile).getFile()));
	}

	@Override
	public void writeCancerStudyMetadataFile(String stagingDirectory, CancerStudyMetadata cancerStudyMetadata, int numCases) throws Exception {

		File metaFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																cancerStudyMetadata.getStudyPath(),
																cancerStudyMetadata.getCancerStudyMetadataFilename());
			if (LOG.isInfoEnabled()) {
				LOG.info("writeCancerStudyMetadataFile(), meta file: " + metaFile);
			}
			PrintWriter writer = new PrintWriter(org.apache.commons.io.FileUtils.openOutputStream(metaFile, false));
			writer.print("type_of_cancer: " + cancerStudyMetadata.getTumorType() + "\n");
			writer.print("cancer_study_identifier: " + cancerStudyMetadata + "\n");
			String name = (cancerStudyMetadata.getName().length() > 0) ?
				cancerStudyMetadata.getName() : cancerStudyMetadata.getTumorTypeMetadata().getName();
			name = name.replaceAll(CancerStudyMetadata.TUMOR_TYPE_NAME_TAG,
								   cancerStudyMetadata.getTumorTypeMetadata().getName());
			writer.print("name: " + name + "\n");
            		writer.print("short_name: " + cancerStudyMetadata.getShortName() + "\n");
			String description = cancerStudyMetadata.getDescription();
			description = description.replaceAll(CancerStudyMetadata.NUM_CASES_TAG, Integer.toString(numCases));
			description = description.replaceAll(CancerStudyMetadata.TUMOR_TYPE_TAG,
												 cancerStudyMetadata.getTumorTypeMetadata().getType().toUpperCase());
			description = description.replaceAll(CancerStudyMetadata.TUMOR_TYPE_NAME_TAG,
												 cancerStudyMetadata.getTumorTypeMetadata().getName());
			writer.print("description: " + description + "\n");
			if (cancerStudyMetadata.getCitation().length() > 0) {
				writer.print("citation: " + cancerStudyMetadata.getCitation() + "\n");
			}
			if (cancerStudyMetadata.getPMID().length() > 0) {
				writer.print("pmid: " + cancerStudyMetadata.getPMID() + "\n");
			}
			if (cancerStudyMetadata.getGroups().length() > 0) {
				writer.print("groups: " + cancerStudyMetadata.getGroups() + "\n");
			}

			writer.flush();
			writer.close();
	}

	public void writeMetadataFile(String stagingDirectory,
			CancerStudyMetadata cancerStudyMetadata,
			DatatypeMetadata datatypeMetadata,
			int numCases) throws Exception
	{
		String filename = stagingDirectory + File.separator + datatypeMetadata.getMetaFilename();
		File metaFile = new File(filename);

		if (LOG.isInfoEnabled()) {
			LOG.info("writeMetadataFile(), meta file: " + metaFile);
		}

		PrintWriter writer = new PrintWriter(org.apache.commons.io.FileUtils.openOutputStream(metaFile, false));
		writer.print("cancer_study_identifier: " + cancerStudyMetadata + "\n");
		writer.print("genetic_alteration_type: " + datatypeMetadata.getMetaGeneticAlterationType() + "\n");
		String stableID = datatypeMetadata.getMetaStableID();
		stableID = stableID.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
		writer.print("stable_id: " + stableID + "\n");
		writer.print("show_profile_in_analysis_tab: " + datatypeMetadata.getMetaShowProfileInAnalysisTab() + "\n");
		String profileDescription = datatypeMetadata.getMetaProfileDescription();

		if (numCases < 0)
		{
			numCases = 0;
		}

		//profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_GENES_TAG, Integer.toString(dataMatrix.getGeneIDs().size()));
		profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_CASES_TAG,
			Integer.toString(numCases));

		profileDescription = profileDescription.replaceAll(DatatypeMetadata.TUMOR_TYPE_TAG,
			cancerStudyMetadata.getTumorType());

		writer.print("profile_description: " + profileDescription + "\n");
		writer.print("profile_name: " + datatypeMetadata.getMetaProfileName() + "\n");

		writer.flush();
		writer.close();
	}

	@Override
	public void writeMetadataFile(String stagingDirectory, CancerStudyMetadata cancerStudyMetadata,
								   DatatypeMetadata datatypeMetadata, DataMatrix dataMatrix) throws Exception {

			File metaFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																	cancerStudyMetadata.getStudyPath(),
																	datatypeMetadata.getMetaFilename());
			if (LOG.isInfoEnabled()) {
				LOG.info("writeMetadataFile(), meta file: " + metaFile);
			}
			PrintWriter writer = new PrintWriter(org.apache.commons.io.FileUtils.openOutputStream(metaFile, false));
			writer.print("cancer_study_identifier: " + cancerStudyMetadata + "\n");
			writer.print("genetic_alteration_type: " + datatypeMetadata.getMetaGeneticAlterationType() + "\n");
			writer.print("datatype: " + datatypeMetadata.getMetaDatatypeType() + "\n");
			String stableID = datatypeMetadata.getMetaStableID();
			stableID = stableID.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
			writer.print("stable_id: " + stableID + "\n");
			writer.print("show_profile_in_analysis_tab: " + datatypeMetadata.getMetaShowProfileInAnalysisTab() + "\n");
			String profileDescription = datatypeMetadata.getMetaProfileDescription();
			if (dataMatrix != null) {
				profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_GENES_TAG, Integer.toString(dataMatrix.getGeneIDs().size()));
				profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_CASES_TAG, Integer.toString(dataMatrix.getCaseIDs().size()));
			}
			profileDescription = profileDescription.replaceAll(DatatypeMetadata.TUMOR_TYPE_TAG, cancerStudyMetadata.getTumorType());
			writer.print("profile_description: " + profileDescription + "\n");
			writer.print("profile_name: " + datatypeMetadata.getMetaProfileName() + "\n");
			writer.flush();
			writer.close();
	}

	@Override
	public void writeCopyNumberSegmentMetadataFile(String stagingDirectory, CancerStudyMetadata cancerStudyMetadata,
								   DatatypeMetadata datatypeMetadata, DataMatrix dataMatrix) throws Exception {

			String metaFilename = datatypeMetadata.getMetaFilename().replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
			File metaFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																	cancerStudyMetadata.getStudyPath(),
																	metaFilename);
			if (LOG.isInfoEnabled()) {
				LOG.info("writeCopyNumberSegmentMetadataFile(), meta file: " + metaFile);
			}
			PrintWriter writer = new PrintWriter(org.apache.commons.io.FileUtils.openOutputStream(metaFile, false));
			writer.print("cancer_study_identifier: " + cancerStudyMetadata + "\n");
			if (datatypeMetadata.getDatatype().contains(CopyNumberSegmentFile.ReferenceGenomeId.hg18.toString())){
				writer.print("reference_genome_id: " + CopyNumberSegmentFile.ReferenceGenomeId.hg18.toString() + "\n");
			}	
			else {
				writer.print("reference_genome_id: " + CopyNumberSegmentFile.ReferenceGenomeId.hg19.toString() + "\n");
			}
			String profileDescription = datatypeMetadata.getMetaProfileDescription();
			if (dataMatrix != null) {
				profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_GENES_TAG, Integer.toString(dataMatrix.getGeneIDs().size()));
				profileDescription = profileDescription.replaceAll(DatatypeMetadata.NUM_CASES_TAG, Integer.toString(dataMatrix.getCaseIDs().size()));
			}
			profileDescription = profileDescription.replaceAll(DatatypeMetadata.TUMOR_TYPE_TAG, cancerStudyMetadata.getTumorType());
			writer.print("description: " + profileDescription + "\n");
			String cnaSegFilename = datatypeMetadata.getStagingFilename().replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
			writer.print("data_filename: " + cnaSegFilename + "\n");
			writer.flush();
			writer.close();
	}	

	@Override
	public void writeStagingFile(String stagingDirectory, CancerStudyMetadata cancerStudyMetadata,
								 DatatypeMetadata datatypeMetadata, DataMatrix dataMatrix) throws Exception {

		// staging file
		String stagingFilename = datatypeMetadata.getStagingFilename();
		stagingFilename = stagingFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
		File stagingFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																   cancerStudyMetadata.getStudyPath(),
																   stagingFilename);

		if (LOG.isInfoEnabled()) {
			LOG.info("writingStagingFile(), staging file: " + stagingFile);
		}
																   
		FileOutputStream out = org.apache.commons.io.FileUtils.openOutputStream(stagingFile, false);
		dataMatrix.write(out);
		IOUtils.closeQuietly(out);
	}

	@Override
	public void writeMutationStagingFile(String stagingDirectory, CancerStudyMetadata cancerStudyMetadata,
										 DatatypeMetadata datatypeMetadata, DataMatrix dataMatrix) throws Exception {

		// we only have data matrix at this point, we need to create a temp with its contents
		File oncotatorInputFile =
			org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
													""+System.currentTimeMillis()+".oncotatorInputFile");
		FileOutputStream out = org.apache.commons.io.FileUtils.openOutputStream(oncotatorInputFile);
		dataMatrix.write(out);
		IOUtils.closeQuietly(out);

		// output should be the path/name of staging file
		String stagingFilename = datatypeMetadata.getStagingFilename();
		stagingFilename = stagingFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
		File stagingFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																   cancerStudyMetadata.getStudyPath(),
																   stagingFilename);

		// call oncotateAF
		oncotateMAF(org.mskcc.cbio.importer.FileUtils.FILE_URL_PREFIX + oncotatorInputFile.getCanonicalPath(),
					org.mskcc.cbio.importer.FileUtils.FILE_URL_PREFIX + stagingFile.getCanonicalPath());

		// clean up
		if (oncotatorInputFile.exists()) {
			org.apache.commons.io.FileUtils.forceDelete(oncotatorInputFile);
		}
	}

	@Override
	public boolean writeZScoresStagingFile(String stagingDirectory, CancerStudyMetadata cancerStudyMetadata,
                                           DatatypeMetadata datatypeMetadata, DatatypeMetadata[] dependencies) throws Exception {

		// sanity check
		if (dependencies.length != 2) {
			throw new IllegalArgumentException("writeZScoresStagingFile(), datatypeMetadatas.length != 2, aborting...");
		}

		// check for existence of dependencies
		if (LOG.isInfoEnabled()) {
			LOG.info("writeZScoresStagingFile(), checking for existence of dependencies: " + Arrays.asList(dependencies));
		}
		File cnaFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
															   cancerStudyMetadata.getStudyPath(),
															   dependencies[0].getStagingFilename());
		if (!cnaFile.exists()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("writeZScoresStagingFile(), cannot find cna file dependency: " + cnaFile.getCanonicalPath());
			}
			return false;
		}

		File expressionFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																	  cancerStudyMetadata.getStudyPath(),
																	  dependencies[1].getStagingFilename());
		if (!expressionFile.exists()) { 
			if (LOG.isInfoEnabled()) {
				LOG.info("writeZScoresStagingFile(), cannot find expression file dependency: " + expressionFile.getCanonicalPath());
			}
			return false;
		}

		// we need a zscore file
		File zScoresFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																   cancerStudyMetadata.getStudyPath(),
																   datatypeMetadata.getStagingFilename());
		
		// call NormalizeExpressionLevels
		String[] args = { cnaFile.getCanonicalPath(), expressionFile.getCanonicalPath(),
                          zScoresFile.getCanonicalPath(), NormalizeExpressionLevels.TCGA_NORMAL_SUFFIX };
		if (LOG.isInfoEnabled()) {
			LOG.info("writingZScoresStagingFlie(), calling NormalizeExpressionLevels: " + Arrays.toString(args));
		}
		try {
			NormalizeExpressionLevels.driver(args);
		}
		catch (RuntimeException e) {
			// houston we have a problem...
			if (LOG.isInfoEnabled()) {
				LOG.info("writingZScoresStagingFlie(), exception thrown by NormalizeExpressionLevels: " +
						 e.getMessage() + ", aborting...");
			}
			if (zScoresFile.exists()) {
				org.apache.commons.io.FileUtils.forceDelete(zScoresFile);
			}
			return false;
		}
		
        return true;
	}

	@Override
	public File getOverrideFile(PortalMetadata portalMetadata, CancerStudyMetadata cancerStudyMetadata, String filename) throws Exception {

		File overrideFile = org.apache.commons.io.FileUtils.getFile(portalMetadata.getOverrideDirectory(),
																	cancerStudyMetadata.getStudyPath(),
																	filename);
		return (overrideFile.exists()) ? overrideFile : null;
	}

	@Override
	public void applyOverride(String overrideDirectory, String stagingDirectory, CancerStudyMetadata cancerStudyMetadata,
							  String overrideFilename, String stagingFilename) throws Exception {

		// check for override file
		File overrideFile = org.apache.commons.io.FileUtils.getFile(overrideDirectory,
																	cancerStudyMetadata.getStudyPath(),
																	overrideFilename);
		if (overrideFile.exists()) {
			File stagingFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																	   cancerStudyMetadata.getStudyPath(),
																	   stagingFilename);

			if (LOG.isInfoEnabled()) {
				LOG.info("applyOverride(), override file exists for " + stagingFile.getCanonicalPath() + ": " + 
						 overrideFile.getCanonicalPath());
			}

			// copy override file to staging area
			if (overrideFile.isFile()) {
				org.apache.commons.io.FileUtils.copyFile(overrideFile, stagingFile);
			}
			else {
				org.apache.commons.io.FileUtils.copyDirectory(overrideFile, stagingFile);
			}
		}
	}

	@Override
	public void writeCaseListFile(String stagingDirectory, CancerStudyMetadata cancerStudyMetadata, CaseListMetadata caseListMetadata, String[] caseList) throws Exception {

		File caseListFile = org.apache.commons.io.FileUtils.getFile(stagingDirectory,
																	cancerStudyMetadata.getStudyPath(),
																	"case_lists",
																	caseListMetadata.getCaseListFilename());

		if (LOG.isInfoEnabled()) {
			LOG.info("writeCaseListFile(), case list file: " + caseListFile.getCanonicalPath());
		}
		PrintWriter writer = new PrintWriter(org.apache.commons.io.FileUtils.openOutputStream(caseListFile, false));
		writer.print("cancer_study_identifier: " + cancerStudyMetadata + "\n");
		String stableID = caseListMetadata.getMetaStableID();
		stableID = stableID.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
		writer.print("stable_id: " + stableID + "\n");
		writer.print("case_list_name: " + caseListMetadata.getMetaCaseListName() + "\n");
		String caseListDescription = caseListMetadata.getMetaCaseListDescription();
		caseListDescription = caseListDescription.replaceAll(DatatypeMetadata.NUM_CASES_TAG, Integer.toString(caseList.length));
		writer.print("case_list_description: " + caseListDescription + "\n");
		writer.print("case_list_category: " + caseListMetadata.getMetaCaseListCategory() + "\n");
		writer.print("case_list_ids: ");
		for (String caseID : caseList) {
			writer.print(caseID + Converter.VALUE_DELIMITER);
		}
		writer.println();
		writer.flush();
		writer.close();
	}

	/**
	 * Runs all MAFs for the given dataaSourcesMetadata through
	 * the Oncotator and OMA tools.
	 *
	 * @param dataSourcesMetadata DataSourcesMetadata
	 * @throws Exception
	 */
	@Override
	public void oncotateAllMAFs(DataSourcesMetadata dataSourcesMetadata) throws Exception {

		// iterate over datasource download directory and process all MAFs
		String[] extensions = new String[] { DatatypeMetadata.MAF_FILE_EXT };
		for (File maf : listFiles(new File(dataSourcesMetadata.getDownloadDirectory()), extensions, true)) {
			// create temp for given maf
			File oncotatorInputFile =
				org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
														""+System.currentTimeMillis()+".oncotatorInputFile");
			org.apache.commons.io.FileUtils.copyFile(maf, oncotatorInputFile);
			// input is tmp file we just created, we want output to go into the original maf
			oncotateMAF(org.mskcc.cbio.importer.FileUtils.FILE_URL_PREFIX + oncotatorInputFile.getCanonicalPath(),
						org.mskcc.cbio.importer.FileUtils.FILE_URL_PREFIX + maf.getCanonicalPath());
			// clean up
			org.apache.commons.io.FileUtils.forceDelete(oncotatorInputFile);
		}
	}

	@Override
	public void oncotateMAF(String inputMAFURL, String outputMAFURL) throws Exception {

		// sanity check
		if (inputMAFURL == null || inputMAFURL.length() == 0 ||
			outputMAFURL == null || outputMAFURL.length() == 0) {
			throw new IllegalArgumentException("oncotateMAFdownloadFile(): url or urlDestination argument is null...");
		}

		URL inputMAF = new URL(inputMAFURL);
		URL outputMAF = new URL(outputMAFURL);

		// determine if we have to call liftover
		boolean cleanOncotatorInputFile = false;
		File oncotatorInputFile = new File(inputMAF.getFile());
		org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(oncotatorInputFile);
		String[] parts = it.nextLine().split("\t");
                int ixNcbiBuild = -1;
                for (int ix = 0; ix < parts.length; ix++) {
                    if (parts[ix].equalsIgnoreCase("NCBI_Build")) {
                        ixNcbiBuild = ix;
                        break;
                    }
                }
                
                if (ixNcbiBuild!=-1&&it.hasNext()) {
                    parts = it.nextLine().split("\t");
                    if (parts[ixNcbiBuild].contains("36") || parts[ixNcbiBuild].equals("hg18")) {
                            it.close();
                            File liftoverInputFile = org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
                                                                                                                                                             ""+System.currentTimeMillis()+".liftoverInputFile");
                            org.apache.commons.io.FileUtils.copyFile(oncotatorInputFile, liftoverInputFile);
                            oncotatorInputFile = new File(inputMAF.getFile());
                            // call lift over
                            if (LOG.isInfoEnabled()) {
                                    LOG.info("oncotateMAF(), calling Hg18ToHg19...");
                            }
                            Hg18ToHg19.driver(liftoverInputFile.getCanonicalPath(), oncotatorInputFile.getCanonicalPath(), getLiftOverBinary(), getLiftOverChain());
                            org.apache.commons.io.FileUtils.forceDelete(liftoverInputFile);
                            cleanOncotatorInputFile = true;
                    }
                }

		// create a temp output file from the oncotator
		File oncotatorOutputFile = 
			org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
													""+System.currentTimeMillis()+".oncotatorOutputFile");
		// call oncotator
		if (LOG.isInfoEnabled()) {
			LOG.info("oncotateMAF(), calling OncotateTool...");
		}
		OncotateTool.driver(oncotatorInputFile.getCanonicalPath(), oncotatorOutputFile.getCanonicalPath(), true, true, true);
		// we call OMA here -
		// we use output from oncotator as input file
		if (LOG.isInfoEnabled()) {
			LOG.info("oncotateMAF(), calling MutationAssessorTool...");
		}
		File outputMAFFile = new File(outputMAF.getFile());
		outputMAFFile.createNewFile();
		MutationAssessorTool.driver(oncotatorOutputFile.getCanonicalPath(), outputMAFFile.getCanonicalPath(), false, true, true);

		// clean up
		org.apache.commons.io.FileUtils.forceDelete(oncotatorOutputFile);
		if (cleanOncotatorInputFile) org.apache.commons.io.FileUtils.forceDelete(oncotatorInputFile);
	}

	/**
	 * Copy's the given portal's seg files to location used for linking to IGV from cBio Portal web site.
	 *
	 * @param portalMetadata PortalMetadata
	 * @param datatypeMetadata DatataypeMetadata
	 * @param remoteUserName String
	 * @throws Exception
	 */
	@Override
	public void copySegFiles(PortalMetadata portalMetadata, DatatypeMetadata datatypeMetadata, String remoteUserName) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("copySegFiles()");
		}

        // check args
        if (portalMetadata == null || remoteUserName == null) {
            throw new IllegalArgumentException("portal or remoteUserName must not be null");
		}

		// seg file location
		URL segFileLocation = portalMetadata.getIGVSegFileLinkingLocation();

		// we need this to determine location 
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(Config.ALL);

		// iterate over all cancer studies
		for (CancerStudyMetadata cancerStudyMetadata : config.getCancerStudyMetadata(portalMetadata.getName())) {

			// lets determine if cancer study is in staging directory or studies directory
			String rootDirectory = MetadataUtils.getCancerStudyRootDirectory(portalMetadata, dataSourcesMetadata, cancerStudyMetadata);

			if (rootDirectory == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("loadStagingFiles(), cannot find root directory for study: " + cancerStudyMetadata + " skipping...");
				}
				continue;
			}

			// construct staging filename for seg
			String sourceFilename = (rootDirectory + File.separator +
									  cancerStudyMetadata.getStudyPath() +
									  File.separator + datatypeMetadata.getStagingFilename());
			sourceFilename = sourceFilename.replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());
			String destinationFilename = datatypeMetadata.getStagingFilename().replaceAll(DatatypeMetadata.CANCER_STUDY_TAG, cancerStudyMetadata.toString());

			String[] command = new String[] { "scp",
											  sourceFilename,
											  remoteUserName + "@" + segFileLocation.getHost() + ":" +
											  segFileLocation.getFile() + destinationFilename };
			if (LOG.isInfoEnabled()) {
				LOG.info("executing: " + Arrays.asList(command));
			}
			if (Shell.exec(Arrays.asList(command), ".")) {
				if (LOG.isInfoEnabled()) {
					LOG.info("copy successful.");
				}
			}
			else if (LOG.isInfoEnabled()) {
				LOG.info("copy unsucessful.");
			}
		}
	}

    /*
     * Given a zip stream, unzips it and returns an input stream to the desired data file.
     *
     * @param importDataRecord ImportDataRecord
     * @param is InputStream
     * @return InputStream
     */
    private List<DataMatrix> getDataMatricesFromArchive(ImportDataRecord importDataRecord, DataMatrix methylationCorrelation) throws Exception {

        List<DataMatrix> toReturn = new ArrayList<DataMatrix>();

        try {
            File dataFile = new File(importDataRecord.getCanonicalPathToData());
            InputStream is = org.apache.commons.io.FileUtils.openInputStream(dataFile);
            // decompress .gz file
            if (LOG.isInfoEnabled()) {
                LOG.info("getDataMatricesFromArchive(), decompressing: " + importDataRecord.getCanonicalPathToData());
            }

            InputStream unzippedContent = new GzipCompressorInputStream(is);
            // if tarball, untar
            if (GzipUtils.isCompressedFilename(importDataRecord.getCanonicalPathToData().toLowerCase())) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("getDataMatricesFromArchive(), gzip file is a tarball, untarring");
                }
                TarArchiveInputStream tis = new TarArchiveInputStream(unzippedContent);
                TarArchiveEntry entry = null;
                while ((entry = tis.getNextTarEntry()) != null) {
                    String entryName = entry.getName();
                    String dataFilename = importDataRecord.getDataFilename();
                    if (dataFilename.contains(DatatypeMetadata.TUMOR_TYPE_TAG)) {
                        dataFilename = dataFilename.replaceAll(DatatypeMetadata.TUMOR_TYPE_TAG, importDataRecord.getTumorTypeLabel());
                    }
                    if (dataFilename.contains(DatatypeMetadata.CLINICAL_FOLLOWUP_VERSION)) {
                        Matcher clinicalPatientFollowupMatcher = DatatypeMetadata.CLINICAL_FOLLOWUP_FILE_REGEX.matcher(entryName);
                        if (clinicalPatientFollowupMatcher.find()) {
                            dataFilename = dataFilename.replace(DatatypeMetadata.CLINICAL_FOLLOWUP_VERSION,
                                                                clinicalPatientFollowupMatcher.group(1));
                        }
                    }
                    if (entryName.contains(dataFilename)) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Processing tar-archive: " + importDataRecord.getDataFilename());
                        }
                        DataMatrix m = getDataMatrix(entryName, tis, methylationCorrelation);
                        if (m != null) {
	                        toReturn.add(m);
	                    }
                    }
                }
                IOUtils.closeQuietly(tis);
            }
            else {
            	DataMatrix m = getDataMatrix(dataFile.getName(), unzippedContent, methylationCorrelation);
            	if (m != null) {
                	toReturn.add(m);
            	}
                IOUtils.closeQuietly(unzippedContent);
            }
        }
        catch (Exception e) {
            throw e;
        }
        
        // outta here
        return toReturn;
    }

    /**
     * Helper function to create DataMatrix.
     *
     * @param data InputStream
	 * @param methylationCorrelation DataMatrix
     * @return DataMatrix
     */
    private DataMatrix getDataMatrix(String dataFilename, InputStream data, DataMatrix methylationCorrelation) throws Exception {

        // iterate over all lines in byte[]
        List<String> columnNames = null;
        List<LinkedList<String>> rowData = null;
        LineIterator it = IOUtils.lineIterator(data, null);
		Map<String,String> probeIdMap = initProbMap(methylationCorrelation);

        int count = -1;
        while (it.hasNext()) {
            // first row is our column heading, create column vector
            if (++count == 0) {
                columnNames = new LinkedList(Arrays.asList(it.nextLine().split(Converter.VALUE_DELIMITER, -1)));
            }
            // all other rows are rows in the table
            else {
                rowData = (rowData == null) ? new LinkedList<LinkedList<String>>() : rowData;
                LinkedList<String> thisRow = new LinkedList(Arrays.asList(it.nextLine().split(Converter.VALUE_DELIMITER, -1)));
                if (processingBCRClinicalFile(dataFilename) && skipClinicalDataRow(thisRow)) {
                    continue;
                }
                if (methylationCorrelation == null) {
                    rowData.add(thisRow);
                }
                // first line in methylation file is probeID
                else if (probeIdMap.containsKey(thisRow.getFirst())) {
                    rowData.add(thisRow);
                }
            }
        }

        // problem reading from data?
        if (columnNames == null || rowData == null) {
            if (LOG.isInfoEnabled()) {
                LOG.info("getDataMatrix(), problem creating DataMatrix from file, data file probably missing data, returning null");
            }
            return null;
        }

        // made it here, we can create DataMatrix
        if (LOG.isInfoEnabled()) {
            LOG.info("creating new DataMatrix(), from file data, num rows: " + rowData.size());
        }

        // outta here
        return new DataMatrix(dataFilename, rowData, columnNames);
    }

	/**
	 * Helper function to gunzip file.  gzipFile param is canonical path.
	 *
	 * @param gzipFile String
	 */
	private static void gunzip(String gzipFile) throws Exception {

		// setup our gzip inputs tream
		FileOutputStream fos = null;
		String outFilePath = GzipUtils.getUncompressedFilename(gzipFile);
		GZIPInputStream gis = new GZIPInputStream(new FileInputStream(gzipFile));
 
		try {
			// unzip into file less the .gz
			fos = new FileOutputStream(outFilePath);
			IOUtils.copy(gis, fos);
		}
		finally {
			// close up our streams
			IOUtils.closeQuietly(gis);
			if (fos != null) IOUtils.closeQuietly(fos);
		}
 	}

	private static Map initProbMap(DataMatrix methylationCorrelation) {
		Map<String,String> toReturn = new HashMap<String,String>();
		if (methylationCorrelation == null) return toReturn;
		for (String probeId : methylationCorrelation.
				 getColumnData(MethylationConverterImpl.CORRELATE_METH_PROBE_COLUMN_HEADER_NAME).get(0)) {
			toReturn.put(probeId, probeId);
		}
		return toReturn;
	}

	private List<String> getCaseListFromSequencedSamplesFile(File sequencedSamplesFile) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("getCaseListFromSequencedSamplesFile(): " + sequencedSamplesFile);
		}

		LinkedHashSet<String> caseSet = new LinkedHashSet<String>();
		org.apache.commons.io.LineIterator it = org.apache.commons.io.FileUtils.lineIterator(sequencedSamplesFile);
		try {
			while (it.hasNext()) {
				caseSet.add(it.nextLine());
			}
		} finally {
			it.close();
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("caseSet size: " + caseSet.size());
		}

		return new ArrayList<String>(caseSet);
	}

    private boolean processingBCRClinicalFile(String dataFilename)
    {
        return (dataFilename.startsWith(DatatypeMetadata.BCR_CLINICAL_FILENAME_PREFIX));
    }

    private boolean skipClinicalDataRow(LinkedList<String> row)
    {
        return (!row.getFirst().startsWith("TCGA"));
    }
}
