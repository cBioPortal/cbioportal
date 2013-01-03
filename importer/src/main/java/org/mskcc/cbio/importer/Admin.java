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
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.Fetcher;
import org.mskcc.cbio.importer.DatabaseUtils;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.model.ReferenceMetadata;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.CommandLineParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Collection;
import java.util.Properties;

/**
 * Class which provides command line admin capabilities 
 * to the importer tool.
 */
public class Admin implements Runnable {

	// our context file
	public static final String contextFile = "classpath:applicationContext-importer.xml";

	// context
	private static final ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);

	// our logger
	private static final Log LOG = LogFactory.getLog(Admin.class);

	// options var
	private static final Options options = initializeOptions();

	// parsed command line
	private CommandLine commandLine;

	/**
	 * Method to get beans by id
	 *
	 * @param String beanID
	 * @return Object
	 */
	private static Object getBean(String beanID) {
		return context.getBean(beanID);
	}

	/**
	 * Method to initialize our static options var
	 *
	 * @return Options
	 */
	private static Options initializeOptions() {
		
		// create each option
		Option help = new Option("help", "print this message");

        Option clobberImportDataRecordbase = new Option("clobber_import_database", "clobber the import database");

        Option fetchData = (OptionBuilder.withArgName("datasource:run_date")
							.hasArgs(2)
							.withValueSeparator(':')
							.withDescription("fetch data from the given datasource and the given run date (mm/dd/yyyy) " + 
											 "or use \"" + Fetcher.LATEST_RUN_INDICATOR + "\" to retrieve the most current run.")
							.create("fetch_data"));

        Option fetchReferenceData = (OptionBuilder.withArgName("reference_data")
									  .hasArg()
									  .withDescription("fetch reference data")
									  .create("fetch_reference_data"));

        Option oncotateMAF = (OptionBuilder.withArgName("maf_file")
							  .hasArg()
							  .withDescription("run the given MAF though Oncotator and OMA tools")
							  .create("oncotate_maf"));

        Option oncotateAllMAFs = (OptionBuilder.withArgName("datasource")
							  .hasArg()
							  .withDescription("run all MAFs in given datasource though Oncotator and OMA tools")
							  .create("oncotate_mafs"));

        Option convertData = (OptionBuilder.withArgName("portal:apply_overrides")
                              .hasArgs(2)
							  .withValueSeparator(':')
                              .withDescription("convert data awaiting for import for the given portal (if apply_overrides is 't', overrides will be substituted for data source data before staging files are created")
                              .create("convert_data"));

        Option generateCaseLists = (OptionBuilder.withArgName("portal")
									.hasArg()
									.withDescription("generate case lists for the given portal")
									.create("generate_case_lists"));

        Option importReferenceData = (OptionBuilder.withArgName("reference_type")
									  .hasArg()
									  .withDescription("import given reference data")
									  .create("import_reference_data"));

        Option importData = (OptionBuilder.withArgName("portal:apply_overrides")
                             .hasArgs(2)
							 .withValueSeparator(':')
                             .withDescription("import data for use in the given portal (if apply_overrides is 't', overrides will be substituted for staging files")
                             .create("import_data"));

		// create an options instance
		Options toReturn = new Options();

		// add options
		toReturn.addOption(help);
		toReturn.addOption(clobberImportDataRecordbase);
		toReturn.addOption(fetchData);
		toReturn.addOption(fetchReferenceData);
		toReturn.addOption(oncotateMAF);
		toReturn.addOption(oncotateAllMAFs);
		toReturn.addOption(convertData);
		toReturn.addOption(generateCaseLists);
		toReturn.addOption(importReferenceData);
		toReturn.addOption(importData);

		// outta here
		return toReturn;
	}

	/**
	 * Parses the arguments.
	 *
	 * @param args String[]
	 */
	public void setCommandParameters(String[] args) {

		// create our parser
		CommandLineParser parser = new PosixParser();

		// parse
		try {
			commandLine = parser.parse(options, args);
		}
		catch (Exception e) {
			Admin.usage(new PrintWriter(System.out, true));
			System.exit(-1);
		}
	}

	/**
	 * Executes the desired portal commmand.
	 */
	@Override
	public void run() {

		// sanity check
		if (commandLine == null) {
			return;
		}

		try {
			// usage
			if (commandLine.hasOption("help")) {
				Admin.usage(new PrintWriter(System.out, true));
			}
			// clobber import database
			else if (commandLine.hasOption("clobber_import_database")) {
				clobberImportDataRecordbase();
			}
			// fetch
			else if (commandLine.hasOption("fetch_data")) {
                String[] values = commandLine.getOptionValues("fetch_data");
				fetchData(values[0], values[1]);
			}
			// fetch reference data
			else if (commandLine.hasOption("fetch_reference_data")) {
				fetchReferenceData(commandLine.getOptionValue("fetch_reference_data"));
			}
			// oncotate MAF
			else if (commandLine.hasOption("oncotate_maf")) {
				oncotateMAF(commandLine.getOptionValue("oncotate_maf"));
			}
			// oncotate MAFs
			else if (commandLine.hasOption("oncotate_mafs")) {
				oncotateAllMAFs(commandLine.getOptionValue("oncotate_mafs"));
			}
			// convert data
			else if (commandLine.hasOption("convert_data")) {
                String[] values = commandLine.getOptionValues("convert_data");
				convertData(values[0], (values.length == 2) ? values[1] : "");
			}
			// generate case lists
			else if (commandLine.hasOption("generate_case_lists")) {
				generateCaseLists(commandLine.getOptionValue("generate_case_lists"));
			}
			// import reference data
			else if (commandLine.hasOption("import_reference_data")) {
				importReferenceData(commandLine.getOptionValue("import_reference_data"));
			}
			// import data
			else if (commandLine.hasOption("import_data")) {
                String[] values = commandLine.getOptionValues("import_data");
				importData(values[0], (values.length == 2) ? values[1] : "");
			}
			else {
				Admin.usage(new PrintWriter(System.out, true));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Helper function to clobber import database.
	 *
	 * @throws Exception
	 */
	private void clobberImportDataRecordbase() throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("clobberImportDataRecordbase()");
		}

		DatabaseUtils databaseUtils = (DatabaseUtils)getBean("databaseUtils");
		databaseUtils.createDatabase(databaseUtils.getImporterDatabaseName(), true);
	}

	/**
	 * Helper function to get data.
	 *
	 * @param dataSource String
	 * @param runDate String
	 * @throws Exception
	 */
	private void fetchData(String dataSource, String runDate) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchData(), dateSource:runDate: " + dataSource + ":" + runDate);
		}

		// create an instance of fetcher
		DataSourcesMetadata dataSourcesMetadata = getDataSourcesMetadata(dataSource);
		// fetch the given data source
		Fetcher fetcher = (Fetcher)getBean(dataSourcesMetadata.getFetcherBeanID());
		fetcher.fetch(dataSource, runDate);
	}

	/**
	 * Helper function to fetch reference data.
     *
     * @param referenceType String
	 *
	 * @throws Exception
	 */
	private void fetchReferenceData(String referenceType) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchReferenceData(), referenceType: " + referenceType);
		}

		// create an instance of fetcher
		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		Config config = (Config)getBean("config");
		Collection<ReferenceMetadata> referenceMetadatas = config.getReferenceMetadata(referenceType);
		if (!referenceMetadatas.isEmpty()) {
			ReferenceMetadata referenceMetadata = referenceMetadatas.iterator().next();
			Fetcher fetcher = (Fetcher)getBean(referenceMetadata.getFetcherBeanID());
			fetcher.fetchReferenceData(referenceMetadata);
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("fetchReferenceData(), unknown referenceType: " + referenceType);
			}
		}
	}

	/**
	 * Helper function to oncotate the give MAF.
     *
     * @param mafFile String
     *
	 * @throws Exception
	 */
	private void oncotateMAF(String mafFileName) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("oncotateMAF(), mafFile: " + mafFileName);
		}

		// sanity check
		File mafFile = new File(mafFileName);
		if (!mafFile.exists()) {
			throw new IllegalArgumentException("cannot find the give MAF: " + mafFileName);
		}

		// create fileUtils object
		Config config = (Config)getBean("config");
		FileUtils fileUtils = (FileUtils)getBean("fileUtils");

		// create tmp file for given MAF
		File tmpMAF = 
			org.apache.commons.io.FileUtils.getFile(org.apache.commons.io.FileUtils.getTempDirectory(),
													"tmpMAF");
		org.apache.commons.io.FileUtils.copyFile(mafFile, tmpMAF);

		// oncotate the MAF (input is tmp maf, output is original maf)
		fileUtils.oncotateMAF(FileUtils.FILE_URL_PREFIX + tmpMAF.getCanonicalPath(),
							  FileUtils.FILE_URL_PREFIX + mafFile.getCanonicalPath());

		// clean up
		org.apache.commons.io.FileUtils.forceDelete(tmpMAF);
	}

	/**
	 * Helper function to oncotate MAFs.
     *
     * @param dataSource String
     *
	 * @throws Exception
	 */
	private void oncotateAllMAFs(String dataSource) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("oncotateAllMAFs(), dataSource: " + dataSource);
		}

		// get the data source metadata object
		DataSourcesMetadata dataSourcesMetadata = getDataSourcesMetadata(dataSource);

		// oncotate all the files of the given data source
		FileUtils fileUtils = (FileUtils)getBean("fileUtils");
		fileUtils.oncotateAllMAFs(dataSourcesMetadata);
	}

	/**
	 * Helper function to convert data.
     *
     * @param portal String
	 * @param applyOverrides String
     *
	 * @throws Exception
	 */
	private void convertData(String portal, String applyOverrides) throws Exception {

		Boolean applyOverridesBool = getBoolean(applyOverrides);

		if (LOG.isInfoEnabled()) {
			LOG.info("convertData(), portal: " + portal);
			LOG.info("convertData(), apply overrides: " + applyOverridesBool);
		}

		// create an instance of Converter
		Converter converter = (Converter)getBean("converter");
		converter.convertData(portal, applyOverridesBool);
	}

	/**
	 * Helper function to generate case lists.
     *
     * @param portal String
     *
	 * @throws Exception
	 */
	private void generateCaseLists(String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("generateCaseLists(), portal: " + portal);
		}

		// create an instance of Converter
		Converter converter = (Converter)getBean("converter");
		converter.generateCaseLists(portal);
	}

	/**
	 * Helper function to import reference data.
     *
     * @param referenceType String
	 *
	 * @throws Exception
	 */
	private void importReferenceData(String referenceType) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importReferenceData(), referenceType: " + referenceType);
		}

		// create an instance of Importer
		Config config = (Config)getBean("config");
		Collection<ReferenceMetadata> referenceMetadata = config.getReferenceMetadata(referenceType);
		if (!referenceMetadata.isEmpty()) {
			Importer importer = (Importer)getBean("importer");
			importer.importReferenceData(referenceMetadata.iterator().next());
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("importReferenceData(), unknown referenceType: " + referenceType);
			}
		}
	}

	/**
	 * Helper function to import data.
     *
     * @param portal String
	 * @param applyOverrides String
	 *
	 * @throws Exception
	 */
	private void importData(String portal, String applyOverrides) throws Exception {

		Boolean applyOverridesBool = getBoolean(applyOverrides);

		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), portal: " + portal);
			LOG.info("importData(), apply overrides: " + applyOverridesBool);
		}

		// create an instance of Importer
		Importer importer = (Importer)getBean("importer");
		importer.importData(portal, applyOverridesBool);
	}

	/**
	 * Helper function to get a DataSourcesMetadata from
	 * a given datasource (name).
	 *
	 * @param dataSource String
	 * @return DataSourcesMetadata
	 */
	private DataSourcesMetadata getDataSourcesMetadata(String dataSource) {

		DataSourcesMetadata toReturn = null;
		Config config = (Config)getBean("config");
		Collection<DataSourcesMetadata> dataSources = config.getDataSourcesMetadata(dataSource);
		if (!dataSources.isEmpty()) {
			toReturn = dataSources.iterator().next();
		}

		// sanity check
		if (toReturn == null) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourcesMetadata object.");
		}

		// outta here
		return toReturn;
	}

	/**
	 * Helper function to create boolean based on argument parameter.
	 *
	 * @param parameterValue String
	 * @return Boolean
	 */
	private Boolean getBoolean(String parameterValue) {
		return (parameterValue.equalsIgnoreCase("t")) ?
			new Boolean("true") : new Boolean("false");
	}

	/**
	 * Helper function - prints usage
	 */
	public static void usage(PrintWriter writer) {

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(writer, HelpFormatter.DEFAULT_WIDTH,
							"Admin", "", options,
							HelpFormatter.DEFAULT_LEFT_PAD,
							HelpFormatter.DEFAULT_DESC_PAD, "");
	}

	/**
	 * The big deal main.
	 *
	 * @param args String[]
	 */
	public static void main(String[] args) throws Exception {

		// sanity check
		if (args.length == 0) {
			System.err.println("Missing args to Admin.");
			Admin.usage(new PrintWriter(System.err, true));
			System.exit(-1);
		}

		// configure logging
		Properties props = new Properties();
		props.load(Admin.class.getResourceAsStream("/log4j.properties"));
		PropertyConfigurator.configure(props);

		// process
		Admin admin = new Admin();
		try {
			admin.setCommandParameters(args);
			admin.run();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
