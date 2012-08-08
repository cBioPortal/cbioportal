// package
package org.mskcc.cbio.firehose.fetcher.internal;

// imports
import org.mskcc.cbio.firehose.Config;
import org.mskcc.cbio.firehose.Fetcher;
import org.mskcc.cbio.firehose.FileUtils;

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

/**
 * Class which implements the fetcher interface.
 */
final class FetcherImpl implements Fetcher {

	// conts for run types
	private static final String ANALYSIS_RUN = "analyses";
	private static final String STDDATA_RUN = "stddata";

	// our logger
	private static final Log LOG = LogFactory.getLog(FetcherImpl.class);

	// regex used when getting firehose run dates from the broad
    private static final Pattern FIREHOSE_GET_RUNS_LINE_REGEX = 
		Pattern.compile("^(\\w*)\\s*(\\w*)\\s*(\\w*)$");

    private static final Pattern FIREHOSE_GET_RUNS_COL_REGEX = 
		Pattern.compile("^(\\w*)__(\\w*)");

	// ref to configuration
	private Config firehoseConfig;

	// ref to file utils
	private FileUtils fileUtils;

	// location of firehose get
	private String firehoseGetScript;
	@Value("${firehose_get_script}")
	public void setFirehoseGetScript(String property) { this.firehoseGetScript = property; }

	// location of analysis download
	private String analysisDownloadDir;
	@Value("${firehose_analysis_download_dir}")
	public void setAnalysisDownloadDir(String property) { this.analysisDownloadDir = property; }

	// location of stddata download
	private String stddataDownloadDir;
	@Value("${firehose_stddata_download_dir}")
	public void setSTDDATADownloadDir(String property) { this.stddataDownloadDir = property; }

	/**
	 * Constructor.
     *
     * Takes a Config reference.
	 * Takes a FileUtils reference.
     *
     * @param firehoseConfig Config
	 * @param fileUtils FileUtils
	 */
	public FetcherImpl(final Config firehoseConfig, final FileUtils fileUtils) {

		// set members
		this.firehoseConfig = firehoseConfig;
		this.fileUtils = fileUtils;
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
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		Date ourLatestAnalysisRunDownloaded = formatter.parse(firehoseConfig.getLatestAnalysisRunDownloaded());
		Date ourLatestSTDDATARunDownloaded = formatter.parse(firehoseConfig.getLatestSTDDATARunDownloaded()); 

		if (LOG.isInfoEnabled()) {
			LOG.info("our latest analysis run: " + formatter.format(ourLatestAnalysisRunDownloaded));
			LOG.info("our latest stddata run: " + formatter.format(ourLatestSTDDATARunDownloaded));
		}

		// get broads latest run
		Date latestBroadAnalysisRun = getLatestBroadRun(ANALYSIS_RUN);
		Date latestBroadSTDDATARun = getLatestBroadRun(STDDATA_RUN);

		// do we need to grab a new analysis run?
		if (latestBroadAnalysisRun.after(ourLatestAnalysisRunDownloaded)) {
			//fetchLatestRun(ANALYSIS_RUN, latestBroadAnalysisRun);
		}

		// do we need to grab a new analysis run?
		if (latestBroadSTDDATARun.after(ourLatestSTDDATARunDownloaded)) {
			fetchLatestRun(STDDATA_RUN, latestBroadSTDDATARun);
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
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
		Date latestRun = formatter.parse("1918_05_11");

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
							Date thisRunDate = formatter.parse(columnMatcher.group(2));
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

		// download the data
		String datatypesToDownload = (runType.equals(ANALYSIS_RUN)) ?
			firehoseConfig.getAnalysisDatatypes() : firehoseConfig.getSTDDATADatatypes();
		String cancerStudiesToDownload = firehoseConfig.getCancerStudiesToDownload();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
		ProcessBuilder pb = new ProcessBuilder(firehoseGetScript, "-b",
											   "-tasks",
											   datatypesToDownload,
											   runType,
											   formatter.format(runDate),
											   cancerStudiesToDownload);
		pb.directory(new File(downloadDirectoryName));
		if (LOG.isInfoEnabled()) {
			LOG.info("executing: " + pb.command());
			LOG.info("this may take a while...");
		}
		process = pb.start();
		process.waitFor();

		// check md5sums
		if (LOG.isInfoEnabled()) {
			LOG.info("download complete, checking md5 digests.");
		}
		checkMD5Digest(downloadDirectory);
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
	 * Helper method to check md5 digests for each downloaded file
	 *
	 * @param node File
	 * @throws Exception
	 */
	private void checkMD5Digest(final File node) throws Exception {

		if (node.isDirectory()){
			String[] subNode = node.list();
			for(String filename : subNode){
				checkMD5Digest(new File(node, filename));
			}
		}
		else {
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
				// remove .md5 file
				if (LOG.isInfoEnabled()) {
					LOG.info("Removing md5 digest file: " + node.getCanonicalPath());
				}
				node.delete();
			}
		}
	}
}