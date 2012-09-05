// package
package org.mskcc.cbio.importer.fetcher.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.dao.ImportDataDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.ArrayList;

/**
 * Class which implements the fetcher interface.
 */
final class FirehoseFetcherImpl implements Fetcher {

	// conts for run types
	private static final String ANALYSIS_RUN = "analyses";
	private static final String STDDATA_RUN = "stddata";

	// our logger
	private static final Log LOG = LogFactory.getLog(FirehoseFetcherImpl.class);

	// regex used when getting firehose run dates from the broad
    private static final Pattern FIREHOSE_GET_RUNS_LINE_REGEX = 
		Pattern.compile("^(\\w*)\\s*(\\w*)\\s*(\\w*)$");

    private static final Pattern FIREHOSE_GET_RUNS_COL_REGEX = 
		Pattern.compile("^(\\w*)__(\\w*)");

    private static final Pattern FIREHOSE_FILENAME_CANCER_NAME_REGEX =
		Pattern.compile("^gdac.broadinstitute.org_(\\w*)\\..*");

	// date formats
	private static final SimpleDateFormat BROAD_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd");
	private static final SimpleDateFormat PORTAL_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to import data
	private ImportDataDAO importDataDAO;

	// location of firehose get
	private String firehoseGetScript;
	@Value("${firehose_get_script}")
	public void setFirehoseGetScript(String property) { this.firehoseGetScript = property; }

	// location of analysis download
	private String analysisDownloadDir;
	@Value("${analysis_download_dir}")
	public void setAnalysisDownloadDir(String property) { this.analysisDownloadDir = property; }

	// location of stddata download
	private String stddataDownloadDir;
	@Value("${stddata_download_dir}")
	public void setSTDDATADownloadDir(String property) { this.stddataDownloadDir = property; }

	/**
	 * Constructor.
     *
     * Takes a Config reference.
	 * Takes a FileUtils reference.
	 * Takes a ImportDataDAO reference.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param importDataDAO ImportDataDAO;
	 */
	public FirehoseFetcherImpl(final Config config, final FileUtils fileUtils, final ImportDataDAO importDataDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.importDataDAO = importDataDAO;
	}

	/**
	 * Fetchers data from the Broad.
	 *
	 * @throws Exception
	 */
	@Override
	public void fetch() throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch()");
		}

		// get latest runs
		Date ourLatestAnalysisRunDownloaded = PORTAL_DATE_FORMAT.parse(config.getLatestAnalysisRunDownloaded());
		Date ourLatestSTDDATARunDownloaded = PORTAL_DATE_FORMAT.parse(config.getLatestSTDDATARunDownloaded()); 

		if (LOG.isInfoEnabled()) {
			LOG.info("our latest analysis run: " + PORTAL_DATE_FORMAT.format(ourLatestAnalysisRunDownloaded));
			LOG.info("our latest stddata run: " + PORTAL_DATE_FORMAT.format(ourLatestSTDDATARunDownloaded));
		}

		// get broads latest run
		Date latestBroadAnalysisRun = getLatestBroadRun(ANALYSIS_RUN);
		Date latestBroadSTDDATARun = getLatestBroadRun(STDDATA_RUN);

		// do we need to grab a new analysis run?
		if (latestBroadAnalysisRun.after(ourLatestAnalysisRunDownloaded)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fresh analysis data to download." + PORTAL_DATE_FORMAT.format(latestBroadAnalysisRun));
			}
			fetchLatestRun(ANALYSIS_RUN, latestBroadAnalysisRun);
			config.setLatestAnalysisRunDownloaded(PORTAL_DATE_FORMAT.format(latestBroadAnalysisRun));
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("we have the latest analysis data.");
			}
		}

		// do we need to grab a new analysis run?
		if (latestBroadSTDDATARun.after(ourLatestSTDDATARunDownloaded)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fresh STDDATA data to download." + PORTAL_DATE_FORMAT.format(latestBroadSTDDATARun));
			}
			fetchLatestRun(STDDATA_RUN, latestBroadSTDDATARun);
			config.setLatestSTDDATARunDownloaded(PORTAL_DATE_FORMAT.format(latestBroadSTDDATARun));
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("we have the latest STDDATA data.");
			}
		}

		// outta here
	}

	/**
	 * Method determines date of latest broad run.  runType
	 * argument is one of "analyses" or "stddata".
	 *
	 * @param runType String
	 * @return Date
	 * @throws Exception
	 */
	private Date getLatestBroadRun(final String runType) throws Exception {

		// steup a default date for comparision
		Date latestRun = BROAD_DATE_FORMAT.parse("1918_05_11");

		// execute firehose get to determine available runs
		Process process = Runtime.getRuntime().exec(firehoseGetScript + " -r");
		process.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String lineOfOutput;
		while ((lineOfOutput = reader.readLine()) != null) {
			if (lineOfOutput.startsWith(runType)) {
				Matcher lineMatcher = FIREHOSE_GET_RUNS_LINE_REGEX.matcher(lineOfOutput);
				if (lineMatcher.find()) {
					// column 3 is "Available_From_Broad_GDAC"
					if (lineMatcher.group(3).equals("yes")) {
						// column one is runtype__yyyy_mm_dd
						Matcher columnMatcher = FIREHOSE_GET_RUNS_COL_REGEX.matcher(lineMatcher.group(1));
						// parse date out of column one and compare to the current latestRun
						if (columnMatcher.find()) {
							Date thisRunDate = BROAD_DATE_FORMAT.parse(columnMatcher.group(2));
							if (thisRunDate.after(latestRun)) {
								latestRun = thisRunDate;
							}
						}
					}
				}
			}
		}

		// outta here
		return latestRun;
	}

	/**
	 * Method fetches latest run.
	 *
	 * @param runType String
	 * @param runDate Date
	 * @return void
	 * @throws Exception
	 */
	private void fetchLatestRun(final String runType, final Date runDate) throws Exception {

		// vars used below
		Process process;
		ProcessBuilder processBuilder;
		Runtime rt = Runtime.getRuntime();

		// determine download directory
		String downloadDirectoryName = (runType.equals(ANALYSIS_RUN)) ?
			analysisDownloadDir : stddataDownloadDir;
		File downloadDirectory = new File(downloadDirectoryName);

		// clobber the directory
		if (downloadDirectory.exists()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("clobbering directory: " + downloadDirectoryName);
			}
			clobber(downloadDirectory);
		}

		// make the directory
		process = rt.exec("mkdir -p " + downloadDirectoryName);
		process.waitFor();

		// download the daat
		Collection<CancerStudyMetadata> cancerStudyMetadata = config.getCancerStudyMetadata();
		String cancerStudiesToDownload = getCancerStudiesToDownload(cancerStudyMetadata);
		Collection<DatatypeMetadata> datatypeMetadata = config.getDatatypeMetadata();
		String datatypesToDownload = getDatatypesToDownload(datatypeMetadata);

		ProcessBuilder pb = new ProcessBuilder(firehoseGetScript, "-b",
											   "-tasks",
											   datatypesToDownload,
											   runType,
											   BROAD_DATE_FORMAT.format(runDate),
											   cancerStudiesToDownload);
		pb.directory(new File(downloadDirectoryName));
		if (LOG.isInfoEnabled()) {
			LOG.info("executing: " + pb.command());
			LOG.info("this may take a while...");
		}
		process = pb.start();
		process.waitFor();

		// importing data
		if (LOG.isInfoEnabled()) {
			LOG.info("download complete, storing in database.");
		}
		storeData(downloadDirectory, datatypeMetadata, runDate);
	}

	/**
	 * Helper method to clobber directory
	 *
	 * @param node File
	 */
	private void clobber(final File node) {

		if(node.isDirectory()){
			String[] subNode = node.list();
			for(String filename : subNode){
				clobber(new File(node, filename));
			}
			node.delete();
		}
		else {
			node.delete();
		}
	}

	/**
	 * Helper function to get cancer studies to download.
	 *
	 * @param cancerStudyMetadata Collection<CancerStudyMetadata>
	 * @return String
	 */
	private String getCancerStudiesToDownload(final Collection<CancerStudyMetadata> cancerStudyMetadata) {

		String toReturn = "";
		for (CancerStudyMetadata csMetadata : cancerStudyMetadata) {
			if (csMetadata.getDownload()) {
				toReturn += csMetadata.getCancerStudyID() + " ";
			}
		}

		// outta here
		return toReturn.trim();
	}

	/**
	 * Helper function to get datatypes to download.
	 *
	 * @param datatypeMetadata Collection<DatatypeMetadata>
	 * @return String
	 */
	private String getDatatypesToDownload(final Collection<DatatypeMetadata> datatypeMetadata) {

		String toReturn = "";
		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
			if (dtMetadata.getDownload()) {
				toReturn += dtMetadata.getPackageFilename() + " ";
			}
		}

		// outta here
		return toReturn.trim();
	}

	/**
	 * Helper method to store downloaded data.  If md5 digest is correct,
	 * import data, else skip it
	 *
	 * @param node File
	 * @param datatypeMetadata Collection<DatatypeMetadata>
	 * @param runDate Date
	 * @throws Exception
	 */
	private void storeData(final File node, final Collection<DatatypeMetadata> datatypeMetadata, final Date runDate) throws Exception {

		if (node.isDirectory()){
			String[] subNode = node.list();
			for (String filename : subNode){
				storeData(new File(node, filename), datatypeMetadata, runDate);
			}
		}
		else {
			// we are only going to store data with md5 sum
			if (node.getName().endsWith(".md5")) {
				// get precomputed digest (from .md5)
				String precomputedDigest = fileUtils.getPrecomputedMD5Digest(node);
				// compute md5 digest from respective data file
				File dataFile = new File(node.getCanonicalPath().replace(".md5", ""));
				String computedDigest = fileUtils.getMD5Digest(dataFile);
				if (LOG.isInfoEnabled()) {
					LOG.info("checkMD5Digest(), file: " + node.getCanonicalPath());
					LOG.info("checkMD5Digest(), precomputed digest: " + precomputedDigest);
					LOG.info("checkMD5Digest(), computed digest: " + computedDigest);
				}
				// remove the data file if its corrupt
				if (!computedDigest.equalsIgnoreCase(precomputedDigest)) {
					if (LOG.isInfoEnabled()) {
						LOG.info("!!!!! Error, md5 digest not correct, removing file " + dataFile.getCanonicalPath() + "!!!!!");
						dataFile.delete();
					}		
				}
				// determine cancer type
				Matcher cancerTypeMatcher = FIREHOSE_FILENAME_CANCER_NAME_REGEX.matcher(dataFile.getName());
				String cancerType = (cancerTypeMatcher.find()) ? cancerTypeMatcher.group(1) : "";
				// determine data type(s) - may be multiple, ie CNA, LOG2CNA
				Collection<DatatypeMetadata> datatypes = getFileDatatype(dataFile.getName(), datatypeMetadata);
				// url
				String urlToData = dataFile.toURI().toURL().toString();
				// create an store a new ImportData object
				for (DatatypeMetadata datatype : datatypes) {
					ImportData importData = new ImportData(cancerType, datatype.getDatatype().toString(),
														   PORTAL_DATE_FORMAT.format(runDate), urlToData, computedDigest);
					importDataDAO.importData(importData);
				}
				// remove files?
				if (LOG.isInfoEnabled()) {
					LOG.info("Removing md5 digest file: " + node.getCanonicalPath());
				}
				node.delete();
			}
		}
	}

	/**
	 * Helper function to get datatypes to download.
	 *
	 * @param filename String
	 * @param datatypeMetadata Collection<DatatypeMetadata>
	 * @return Collection<DatatypeMetadata>
	 */
	private Collection<DatatypeMetadata> getFileDatatype(final String filename, final Collection<DatatypeMetadata> datatypeMetadata) {

		Collection<DatatypeMetadata> toReturn = new ArrayList<DatatypeMetadata>();
		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
			if (filename.contains(dtMetadata.getPackageFilename())) {
				toReturn.add(dtMetadata);
			}
		}

		// outta here
		return toReturn;
	}
}
