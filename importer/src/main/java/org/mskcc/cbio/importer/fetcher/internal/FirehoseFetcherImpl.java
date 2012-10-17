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
package org.mskcc.cbio.importer.fetcher.internal;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.model.ImportData;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.DataSourceMetadata;
import org.mskcc.cbio.importer.dao.ImportDataDAO;
import org.mskcc.cbio.importer.util.Shell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;

/**
 * Class which implements the fetcher interface.
 */
final class FirehoseFetcherImpl implements Fetcher {

	// conts for run types
	private static final String ANALYSIS_RUN = "analyses";
	private static final String STDDATA_RUN = "stddata";

	// date formats
	public static final SimpleDateFormat BROAD_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd");
	public static final SimpleDateFormat PORTAL_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	// our logger
	private static final Log LOG = LogFactory.getLog(FirehoseFetcherImpl.class);

	// regex used when getting firehose run dates from the broad
    private static final Pattern FIREHOSE_GET_RUNS_LINE_REGEX = 
		Pattern.compile("^(\\w*)\\s*(\\w*)\\s*(\\w*)$");

    private static final Pattern FIREHOSE_GET_RUNS_COL_REGEX = 
		Pattern.compile("^(\\w*)__(\\w*)");

    private static final Pattern FIREHOSE_FILENAME_TUMOR_TYPE_REGEX =
		Pattern.compile("^gdac.broadinstitute.org_(\\w*)\\..*");

	// ref to configuration
	private Config config;

	// ref to file utils
	private FileUtils fileUtils;

	// ref to import data
	private ImportDataDAO importDataDAO;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	// download directories
	private DataSourceMetadata dataSourceMetadata;

	// location of firehose get
	private String firehoseGetScript;
	@Value("${firehose_get_script}")
	public void setFirehoseGetScript(String property) { this.firehoseGetScript = property; }

	/**
	 * Constructor.
     *
     * @param config Config
	 * @param fileUtils FileUtils
	 * @param databaseUtils DatabaseUtils
	 * @param importDataDAO ImportDataDAO;
	 */
	public FirehoseFetcherImpl(final Config config, final FileUtils fileUtils,
							   final DatabaseUtils databaseUtils, final ImportDataDAO importDataDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataDAO = importDataDAO;
        this.dataSourceMetadata = config.getDataSourceMetadata("firehose");

		// sanity check
		if (this.dataSourceMetadata == null) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourceMetadata object.");
		}
	}

	/**
	 * Fetchers genomic data from an external datasource and
	 * places in database for processing.
	 *
	 * @param clobberDatabase boolean
	 * @throws Exception
	 */
	@Override
	public void fetch(final boolean clobberDatabase) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch()");
		}

		// get latest runs
		String[] latestRuns = dataSourceMetadata.getLatestRunDownload().split(":");
		Date ourLatestAnalysisRunDownloaded = PORTAL_DATE_FORMAT.parse(latestRuns[0]);
		Date ourLatestSTDDATARunDownloaded = PORTAL_DATE_FORMAT.parse(latestRuns[1]); 

		if (LOG.isInfoEnabled()) {
			LOG.info("our latest analysis run: " + PORTAL_DATE_FORMAT.format(ourLatestAnalysisRunDownloaded));
			LOG.info("our latest stddata run: " + PORTAL_DATE_FORMAT.format(ourLatestSTDDATARunDownloaded));
		}

		// get broads latest run
		Date latestBroadAnalysisRun = getLatestBroadRun(ANALYSIS_RUN);
		Date latestBroadSTDDATARun = getLatestBroadRun(STDDATA_RUN);

		// do we need to grab a new analysis run?
		Boolean newAnalysisRun = false;
		if (latestBroadAnalysisRun.after(ourLatestAnalysisRunDownloaded)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fresh analysis data to download." + PORTAL_DATE_FORMAT.format(latestBroadAnalysisRun));
			}
			fetchLatestRun(ANALYSIS_RUN, latestBroadAnalysisRun, clobberDatabase);
			newAnalysisRun = true;
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("we have the latest analysis data.");
			}
		}

		// do we need to grab a new analysis run?
		Boolean newSTDDataRun = false;
		if (latestBroadSTDDATARun.after(ourLatestSTDDATARunDownloaded)) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fresh STDDATA data to download." + PORTAL_DATE_FORMAT.format(latestBroadSTDDATARun));
			}
			fetchLatestRun(STDDATA_RUN, latestBroadSTDDATARun, clobberDatabase);
			newSTDDataRun = true;
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("we have the latest STDDATA data.");
			}
		}

		// updata run dates
		if (newAnalysisRun || newSTDDataRun) {
			dataSourceMetadata.setLatestRunDownload((PORTAL_DATE_FORMAT.format(latestBroadAnalysisRun) +
													 ":" +
													 PORTAL_DATE_FORMAT.format(latestBroadSTDDATARun)));
			config.setDataSourceMetadata(dataSourceMetadata);
		}
	}

	/**
	 * Fetchers reference data from an external datasource.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	@Override
	public void fetchReferenceData(final ReferenceMetadata referenceMetadata) throws Exception {
		throw new UnsupportedOperationException();
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

		Process process = Runtime.getRuntime().exec(firehoseGetScript + " -r");
		process.waitFor();
		if (process.exitValue() != 0) { return latestRun; }
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
	 * @param clobberDatabase boolean
	 * @throws Exception
	 */
	private void fetchLatestRun(final String runType, final Date runDate, final boolean clobberDatabase) throws Exception {

		// determine download directory
		String[] downloadDirectories = dataSourceMetadata.getDownloadDirectory().split(":");
		String downloadDirectoryName = (runType.equals(ANALYSIS_RUN)) ?
			downloadDirectories[0] : downloadDirectories[1];
		File downloadDirectory = new File(downloadDirectoryName);

		// clobber the directory
		if (downloadDirectory.exists()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("clobbering directory: " + downloadDirectoryName);
			}
            fileUtils.deleteDirectory(downloadDirectory);
		}

		// make the directory
        fileUtils.makeDirectory(downloadDirectory);

		// download the data
		Collection<TumorTypeMetadata> tumorTypeMetadata = config.getTumorTypeMetadata();
		String tumorTypesToDownload = getTumorTypesToDownload(tumorTypeMetadata);
		Collection<DatatypeMetadata> datatypeMetadata = config.getDatatypeMetadata();
		String firehoseDatatypesToDownload = getFirehoseDatatypesToDownload(datatypeMetadata);
		String[] command = new String[] { firehoseGetScript, "-b",
										  "-tasks",
										  firehoseDatatypesToDownload,
										  runType,
										  BROAD_DATE_FORMAT.format(runDate),
										  tumorTypesToDownload };
		if (LOG.isInfoEnabled()) {
			LOG.info("executing: " + Arrays.asList(command));
			LOG.info("this may take a while...");
		}

		if (Shell.exec(Arrays.asList(command), downloadDirectoryName)) {
			// importing data
			if (LOG.isInfoEnabled()) {
				LOG.info("download complete, storing in database.");
			}
			// clobber current import 
			if (clobberDatabase) {
				databaseUtils.createDatabase(databaseUtils.getImporterDatabaseName(), true);
			}
			storeData(downloadDirectory, datatypeMetadata, runDate);
		}
	}

	/**
	 * Helper function to get tumor types to download.
	 *
	 * @param tumorTypeMetadata Collection<TumorTypeMetadata>
	 * @return String
	 */
	private String getTumorTypesToDownload(final Collection<TumorTypeMetadata> tumorTypeMetadata) {

		String toReturn = "";
		for (TumorTypeMetadata ttMetadata : tumorTypeMetadata) {
			if (ttMetadata.getDownload()) {
				toReturn += ttMetadata.getTumorTypeID() + " ";
			}
		}

		// outta here
		return toReturn.trim();
	}

	/**
	 * Helper function to get firehose datatypes to download.
	 *
	 * @param datatypeMetadata Collection<DatatypeMetadata>
	 * @return String
	 */
	private String getFirehoseDatatypesToDownload(final Collection<DatatypeMetadata> datatypeMetadata) {

		String toReturn = "";
		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
			if (dtMetadata.getDownload()) {
				toReturn += dtMetadata.getFirehoseDownloadArchive() + " ";
			}
		}

		// outta here
		return toReturn.trim();
	}

	/**
	 * Helper method to store downloaded data.  If md5 digest is correct,
	 * import data, else skip it
	 *
	 * @param downloadDirectory File
	 * @param datatypeMetadata Collection<DatatypeMetadata>
	 * @param runDate Date
	 * @throws Exception
	 */
	private void storeData(final File downloadDirectory, final Collection<DatatypeMetadata> datatypeMetadata, final Date runDate) throws Exception {

        // we only want to process files with md5 checksums
        String exts[] = {"md5"};
        for (File md5File : fileUtils.listFiles(downloadDirectory, exts, true)) {

            // get precomputed digest (from .md5)
            String precomputedDigest = fileUtils.getPrecomputedMD5Digest(md5File);
            // compute md5 digest from respective data file
            File dataFile = new File(md5File.getCanonicalPath().replace(".md5", ""));
            String computedDigest = fileUtils.getMD5Digest(dataFile);
            if (LOG.isInfoEnabled()) {
                LOG.info("checkMD5Digest(), file: " + md5File.getCanonicalPath());
                LOG.info("checkMD5Digest(), precomputed digest: " + precomputedDigest);
                LOG.info("checkMD5Digest(), computed digest: " + computedDigest);
            }
            // if file is corrupt, skip it
            if (!computedDigest.equalsIgnoreCase(precomputedDigest)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("!!!!! Error, md5 digest not correct, file: " + dataFile.getCanonicalPath() + "!!!!!");
                }
                continue;
            }
            // determine cancer type
            Matcher tumorTypeMatcher = FIREHOSE_FILENAME_TUMOR_TYPE_REGEX.matcher(dataFile.getName());
            String tumorType = (tumorTypeMatcher.find()) ? tumorTypeMatcher.group(1) : "";
            // determine data type(s) - may be multiple, ie CNA, LOG2CNA
            Collection<DatatypeMetadata> datatypes = getFileDatatype(dataFile.getName(), datatypeMetadata);
            // url
            String canonicalPath = dataFile.getCanonicalPath();
            // create an store a new ImportData object
            for (DatatypeMetadata datatype : datatypes) {
                ImportData importData = new ImportData(tumorType, datatype.getDatatype(),
                                                       PORTAL_DATE_FORMAT.format(runDate), canonicalPath, computedDigest,
                                                       true, datatype.getFirehoseDownloadFilename(),
                                                       getDatatypeOverrideFilename(datatype.getDatatype(), datatypeMetadata));
                importDataDAO.importData(importData);
            }
		}
	}

	/**
	 * Helper function to determine the datatype of the firehose file.
	 *
	 * @param filename String
	 * @param datatypeMetadata Collection<datatypeMetadata>
	 * @return Collection<DatatypeMetadata>
	 */
	private Collection<DatatypeMetadata> getFileDatatype(final String filename, final Collection<DatatypeMetadata> datatypeMetadata) {

		Collection<DatatypeMetadata> toReturn = new ArrayList<DatatypeMetadata>();
		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
			if (filename.contains(dtMetadata.getFirehoseDownloadArchive())) {
				toReturn.add(dtMetadata);
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Helper function to get datatype override file.
	 *
	 * @param datatype String
	 * @param datatypeMetadata Collection<DatatypeMetadata>
	 * @return String
	 */
	private String getDatatypeOverrideFilename(final String datatype, final Collection<DatatypeMetadata> datatypeMetadata) {

        String toReturn = "";

		for (DatatypeMetadata dtMetadata : datatypeMetadata) {
            if (dtMetadata.getDatatype().toLowerCase().equals(datatype.toLowerCase())) {
                toReturn = dtMetadata.getOverrideFilename();
                break;
            }
		}

		// outta here
		return toReturn;
	}
}
