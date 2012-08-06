// package
package org.mskcc.cbio.firehose.fetcher.internal;

// imports
import org.mskcc.cbio.firehose.Config;
import org.mskcc.cbio.firehose.Fetcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

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
     *
     * @param firehoseConfig Config
	 */
	public FetcherImpl(Config firehoseConfig) {

		// set members
		this.firehoseConfig = firehoseConfig;
	}

	/**
	 * Fetchers data from the Broad.
	 *
	 * @throws ParseException - improper date format
	 * @throws IOException - reading firehose_get output
	 * @throws InterruptedException - executing process via runtime
	 */
	@Override
	public void fetch() throws ParseException, IOException, InterruptedException {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch()");
		}

		// get latest runs
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		Date ourLatestAnalysisRun = formatter.parse(firehoseConfig.getLatestAnalysisRun());
		Date ourLatestSTDDATARun = formatter.parse(firehoseConfig.getLatestSTDDATARun()); 

		if (LOG.isInfoEnabled()) {
			LOG.info("our latest analysis run: " + formatter.format(ourLatestAnalysisRun));
			LOG.info("our latest stddata run: " + formatter.format(ourLatestSTDDATARun));
		}

		// get broads latest run
		Date latestBroadAnalysisRun = getLatestBroadRun(ANALYSIS_RUN);
		Date latestBroadSTDDATARun = getLatestBroadRun(STDDATA_RUN);

		// do we need to grab a new analysis run?
		if (latestBroadAnalysisRun.after(ourLatestAnalysisRun)) {
			//fetchLatestRun(ANALYSIS_RUN, latestBroadAnalysisRun);
		}

		// do we need to grab a new analysis run?
		if (latestBroadSTDDATARun.after(ourLatestSTDDATARun)) {
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
	 */
	private Date getLatestBroadRun(String runType) throws ParseException, IOException, InterruptedException {

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
	 */
	private void fetchLatestRun(String runType, Date runDate) throws ParseException, IOException, InterruptedException {

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
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
		ProcessBuilder pb = new ProcessBuilder(firehoseGetScript, "-b",
											   runType,
											   formatter.format(runDate),
											   "-tasks " + datatypesToDownload,
											   "brca");
		pb.directory(new File(downloadDirectoryName));
		if (LOG.isInfoEnabled()) {
			LOG.info("executing: " + pb.command());
			LOG.info("this may take a while...");
		}
		process = pb.start();
		process.waitFor();
	}

	/**
	 * Helper method to clobber directory
	 *
	 * @param node File
	 */
	private void clobber(File node) {

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
}