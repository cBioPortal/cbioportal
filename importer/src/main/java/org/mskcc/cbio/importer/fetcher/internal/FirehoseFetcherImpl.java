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
import org.mskcc.cbio.importer.model.ImportDataRecord;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.model.TumorTypeMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.dao.ImportDataRecordDAO;
import org.mskcc.cbio.importer.util.Shell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Value;

import java.util.Date;
import java.text.SimpleDateFormat;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.Set;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.lang.reflect.Method;

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
	private ImportDataRecordDAO importDataRecordDAO;

	// ref to database utils
	private DatabaseUtils databaseUtils;

	// download directories
	private DataSourcesMetadata dataSourceMetadata;

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
	 * @param importDataRecordDAO ImportDataRecordDAO;
	 */
	public FirehoseFetcherImpl(final Config config, final FileUtils fileUtils,
							   final DatabaseUtils databaseUtils, final ImportDataRecordDAO importDataRecordDAO) {

		// set members
		this.config = config;
		this.fileUtils = fileUtils;
		this.databaseUtils = databaseUtils;
		this.importDataRecordDAO = importDataRecordDAO;
	}

	/**
	 * Fetchers genomic data from an external datasource and
	 * places in database for processing.
	 *
	 * @param dataSource String
	 * @param desiredRunDate String
	 * @throws Exception
	 */
	@Override
	public void fetch(final String dataSource, final String desiredRunDate) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetch(), dateSource:runDate: " + dataSource + ":" + desiredRunDate);
		}

		// get our DataSourcesMetadata object
		Collection<DataSourcesMetadata> dataSourcesMetadata = config.getDataSourcesMetadata(dataSource);
		if (dataSourcesMetadata.isEmpty()) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourcesMetadata object.");			
		}
		this.dataSourceMetadata = dataSourcesMetadata.iterator().next();

		// is the data source an analysis or stddata run?
		String runType = null;
		if (dataSource.contains(ANALYSIS_RUN)) {
			runType = ANALYSIS_RUN;
		}
		else if (dataSource.contains(STDDATA_RUN)) {
			runType = STDDATA_RUN;
		}
		// sanity check
		if (runType == null) {
			throw new IllegalArgumentException("cannot determine runtype from dataSource: " + dataSource);
		}

		// get broad latest run
		Date latestBroadRun = getLatestBroadRun(runType);

		// process runDate argument
		Date desiredRunDateDate = (desiredRunDate.equalsIgnoreCase(Fetcher.LATEST_RUN_INDICATOR)) ?
			latestBroadRun : PORTAL_DATE_FORMAT.parse(desiredRunDate);

		fetchLatestRun(runType, desiredRunDateDate);

		dataSourceMetadata.setLatestRunDownload(PORTAL_DATE_FORMAT.format(latestBroadRun));
		config.setDataSourcesMetadata(dataSourceMetadata);
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
	 * @throws Exception
	 */
	private void fetchLatestRun(final String runType, final Date runDate) throws Exception {

		// determine download directory
		String downloadDirectoryName = dataSourceMetadata.getDownloadDirectory();
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
		String[] tumorTypesToDownload = config.getTumorTypesToDownload();
		String[] firehoseDatatypesToDownload = config.getDatatypesToDownload(dataSourceMetadata);
		String[] command = new String[] { firehoseGetScript, "-b",
										  "-tasks",
										  Arrays.toString(firehoseDatatypesToDownload),
										  runType,
										  BROAD_DATE_FORMAT.format(runDate),
										  Arrays.toString(tumorTypesToDownload) };
		if (LOG.isInfoEnabled()) {
			LOG.info("executing: " + Arrays.asList(command));
			LOG.info("this may take a while...");
		}

		if (Shell.exec(Arrays.asList(command), downloadDirectoryName)) {
			// importing data
			if (LOG.isInfoEnabled()) {
				LOG.info("download complete, storing in database.");
			}
			storeData(dataSourceMetadata.getDataSource(), downloadDirectory, runDate);
		}
	}

	/**
	 * Helper method to store downloaded data.  If md5 digest is correct,
	 * import data, else skip it
	 *
	 * @param dataSource String
	 * @param downloadDirectory File
	 * @param runDate Date
	 * @throws Exception
	 */
	private void storeData(final String dataSource, final File downloadDirectory, final Date runDate) throws Exception {

		String center = dataSource.split(DataSourcesMetadata.DATA_SOURCE_NAME_DELIMITER)[0].toLowerCase();

		// first delete records in db with givin dataSource
		// we do this in the event that the desired datatypes to download have changed
		importDataRecordDAO.deleteByDataSource(dataSource);

        // we only want to process files with md5 checksums
        String exts[] = {"md5"};
        for (File md5File : fileUtils.listFiles(downloadDirectory, exts, true)) {

            // get precomputed digest (from .md5)
            String precomputedDigest = fileUtils.getPrecomputedMD5Digest(md5File);
            // compute md5 digest from respective data file
            File dataFile = new File(md5File.getCanonicalPath().replace(".md5", ""));
            String computedDigest = fileUtils.getMD5Digest(dataFile);
            if (LOG.isInfoEnabled()) {
                LOG.info("storeData(), file: " + md5File.getCanonicalPath());
                LOG.info("storeData(), precomputed digest: " + precomputedDigest);
                LOG.info("storeData(), computed digest: " + computedDigest);
            }
            // if file is corrupt, skip it
            if (!computedDigest.equalsIgnoreCase(precomputedDigest)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("!!!!! storeData(), Error - md5 digest not correct, file: " + dataFile.getCanonicalPath() + "!!!!!");
                }
                continue;
            }
            // determine cancer type
            Matcher tumorTypeMatcher = FIREHOSE_FILENAME_TUMOR_TYPE_REGEX.matcher(dataFile.getName());
            String tumorType = (tumorTypeMatcher.find()) ? tumorTypeMatcher.group(1) : "";
            // determine data type(s) - may be multiple, ie CNA, LOG2CNA
			if (LOG.isInfoEnabled()) {
				LOG.info("storeData(), getting datatypes for dataFile: " + dataFile.getName());
			}
            Collection<DatatypeMetadata> datatypes = config.getFileDatatype(dataSourceMetadata, dataFile.getName());
			if (LOG.isInfoEnabled()) {
				LOG.info("storeData(), found " + datatypes.size() + " datatypes found for dataFile: " + dataFile.getName());
				if (datatypes.size() > 0) {
					for (DatatypeMetadata datatype : datatypes) { LOG.info("--- " + datatype.getDatatype()); }
				}
			}
            // url
            String canonicalPath = dataFile.getCanonicalPath();
            // create an store a new ImportDataRecord object
            for (DatatypeMetadata datatype : datatypes) {
				Method archivedFilesMethod = datatype.getArchivedFilesMethod(dataSource);
				Set<String> archivedFiles = (Set<String>)archivedFilesMethod.invoke(datatype, (Object)dataFile.getName());
				if (archivedFiles.size() == 0 && LOG.isInfoEnabled()) {
					LOG.info("storeData(), cannot find any archivedFiles for archive: " + dataFile.getName());
				}
				for (String downloadFile : archivedFiles) {
					ImportDataRecord importDataRecord = new ImportDataRecord(dataSource, center,
																			 tumorType.toLowerCase(), datatype.getDatatype(),
                                                                             PORTAL_DATE_FORMAT.format(runDate), canonicalPath,
																			 computedDigest, downloadFile);
					importDataRecordDAO.importDataRecord(importDataRecord);
				}
            }
		}
	}
}
