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
import java.text.SimpleDateFormat;

/**
 * Class which provides command line admin capabilities 
 * to the importer tool.
 */
public class Admin implements Runnable {

	// our context file
	public static final String contextFile = "classpath:applicationContext-importer.xml";

	// date format 
	public static final SimpleDateFormat PORTAL_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

	// context
	private static final ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);

	// our logger
	private static final Log LOG = LogFactory.getLog(Admin.class);

	// options var
	private static final Options options = initializeOptions();

	// identifiers for init db command
	private static final String PORTAL_DATABASE = "portal";
	private static final String IMPORTER_DATABASE = "importer";

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
		Option help = new Option("help", "Print this message.");

        Option initializeDatabase = (OptionBuilder.withArgName("db_name")
									 .hasArg()
									 .withDescription("Initialize database(s).  Valid " +
													  "database identifiers are: " +
													  "\"" + PORTAL_DATABASE + "\" and \"" +
													  IMPORTER_DATABASE + "\" or " +
													  "\"" + Config.ALL + "\".")
									 .create("init_db"));

        Option fetchData = (OptionBuilder.withArgName("data_source:run_date")
							.hasArgs(2)
							.withValueSeparator(':')
							.withDescription("Fetch data from the given data_source and the given run date (mm/dd/yyyy).  " + 
											 "Use \"" + Fetcher.LATEST_RUN_INDICATOR + "\" to retrieve the most current run.")
							.create("fetch_data"));

        Option fetchReferenceData = (OptionBuilder.withArgName("reference_data")
									  .hasArg()
									  .withDescription("Fetch the given reference data." +
													   "  Use \"" + Config.ALL + "\" to retrieve all reference data.")
									  .create("fetch_reference_data"));

        Option oncotateMAF = (OptionBuilder.withArgName("maf_file")
							  .hasArg()
							  .withDescription("Run the given MAF though the Oncotator and OMA tools.")
							  .create("oncotate_maf"));

        Option oncotateAllMAFs = (OptionBuilder.withArgName("data_source")
							  .hasArg()
							  .withDescription("Run all MAFs in the given datasource though the Oncotator and OMA tools.")
							  .create("oncotate_mafs"));

        Option convertData = (OptionBuilder.withArgName("portal:run_date:apply_overrides")
                              .hasArgs(3)
							  .withValueSeparator(':')
                              .withDescription("Convert data within the importer database " +
											   "from the given run date (mm/dd/yyyy), " +
											   "for the given portal.  If apply_overrides is 't', " +
											   "overrides will be substituted for data_source data " +
											   "before staging files are created.")
                              .create("convert_data"));

		Option applyOverrides = (OptionBuilder.withArgName("portal")		
								 .hasArg()		
								 .withDescription("Replace staging files for the given portal " +
												  "with any exisiting overrides.")
								 .create("apply_overrides"));

        Option generateCaseLists = (OptionBuilder.withArgName("portal")
									.hasArg()
									.withDescription("Generate case lists for existing " +
													 "staging files for the given portal.")
									.create("generate_case_lists"));

        Option importReferenceData = (OptionBuilder.withArgName("reference_type")
									  .hasArg()
									  .withDescription("Import reference data for the given reference_type.  "+
													   "Use \"" + Config.ALL + "\" to import all reference data.")
									  .create("import_reference_data"));

        Option importData = (OptionBuilder.withArgName("portal:init_portal_db:ref_data")
                             .hasArgs(3)
							 .withValueSeparator(':')
                             .withDescription("Import data for the given portal.  " +
											  "If init_portal_db is 't' a portal db will be created (an existing one will be clobbered.  " +
											  "If ref_data is 't', all reference data will be imported prior to importing staging files.")
                             .create("import_data"));

		// create an options instance
		Options toReturn = new Options();

		// add options
		toReturn.addOption(help);
		toReturn.addOption(initializeDatabase);
		toReturn.addOption(fetchData);
		toReturn.addOption(fetchReferenceData);
		toReturn.addOption(oncotateMAF);
		toReturn.addOption(oncotateAllMAFs);
		toReturn.addOption(convertData);
		toReturn.addOption(applyOverrides);
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
			// initialize import database
			else if (commandLine.hasOption("init_db")) {
				initializeDatabase(commandLine.getOptionValue("init_db"));
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
            // apply overrides		
			else if (commandLine.hasOption("apply_overrides")) {		
				applyOverrides(commandLine.getOptionValue("apply_overrides"));
			}
			// convert data
			else if (commandLine.hasOption("convert_data")) {
                String[] values = commandLine.getOptionValues("convert_data");
				convertData(values[0], values[1], (values.length == 3) ? values[2] : "");
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
                importData(values[0],
						   (values.length >= 2) ? values[1] : "",
						   (values.length == 3) ? values[2] : "");
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
	 * Helper function to initialize import database.
	 *
	 * @param databaseName String
	 * @throws Exception
	 */
	private void initializeDatabase(String databaseName) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("initializeDatabase(): " + databaseName);
		}

		boolean unknownDB = true;
		DatabaseUtils databaseUtils = (DatabaseUtils)getBean("databaseUtils");
		if (databaseName.equals(Config.ALL) || databaseName.equals(IMPORTER_DATABASE)) {
			unknownDB = false;
			databaseUtils.createDatabase(databaseUtils.getImporterDatabaseName(), true);
		}
		if (databaseName.equals(Config.ALL) || databaseName.equals(PORTAL_DATABASE)) {
			unknownDB = false;
			databaseUtils.createDatabase(databaseUtils.getPortalDatabaseName(), false);
			boolean success = databaseUtils.executeScript(databaseUtils.getPortalDatabaseName(),
														  databaseUtils.getPortalDatabaseSchema(),
														  databaseUtils.getDatabaseUser(),
														  databaseUtils.getDatabasePassword());
			if (!success) {
				System.err.println("Error creating database schema.");
			} 
		}
		if (unknownDB && LOG.isInfoEnabled()) {
			LOG.info("initializeDatabase(), unknown database: " + databaseName);
		}

		if (LOG.isInfoEnabled()) {
			LOG.info("initializeDatabase(), complete");
		}
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

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchData(), complete");
		}
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
		if (referenceMetadatas.isEmpty()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("fetchReferenceData(), unknown referenceType: " + referenceType);
			}
		}
		else {
			Fetcher fetcher = (Fetcher)getBean("referenceDataFetcher");
			for (ReferenceMetadata referenceMetadata : referenceMetadatas) {
				if (referenceType.equals(Config.ALL) || referenceMetadata.getReferenceType().equals(referenceType)) {
					if (LOG.isInfoEnabled()) {
						LOG.info("fetchReferenceData(), calling fetcher for: " + referenceMetadata.getReferenceType());
					}
					fetcher.fetchReferenceData(referenceMetadata);
				}
			}
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("fetchReferenceData(), complete");
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

		if (LOG.isInfoEnabled()) {
			LOG.info("oncotateMAF(), complete");
		}
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

		if (LOG.isInfoEnabled()) {
			LOG.info("oncotateAllMAFs(), complete");
		}
	}

	/**
	 * Helper function to convert data.
     *
     * @param portal String
	 * @param runDate String
	 * @param applyOverrides String
     *
	 * @throws Exception
	 */
	private void convertData(String portal, String runDate, String applyOverrides) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("convertData(), portal: " + portal);
			LOG.info("convertData(), run date: " + runDate);
			LOG.info("convertData(), apply overrides: " + applyOverrides);
		}

		Boolean applyOverridesBool = getBoolean(applyOverrides);

		// sanity check date format - doesn't work?
		PORTAL_DATE_FORMAT.setLenient(false);
		PORTAL_DATE_FORMAT.parse(runDate);

		// create an instance of Converter
		Converter converter = (Converter)getBean("converter");
		converter.convertData(portal, runDate, applyOverridesBool);

		if (LOG.isInfoEnabled()) {
			LOG.info("convertData(), complete");
		}
	}

    /**		
	 * Helper function to apply overrides to a given portal.
	 *		
	 * @param portal String		
	 * @throws Exception		
	 */		
	private void applyOverrides(String portal) throws Exception {		
			
		if (LOG.isInfoEnabled()) {		
			LOG.info("applyOverrides(), portal: " + portal);
		}
			
		Converter converter = (Converter)getBean("converter");		
		converter.applyOverrides(portal);

		if (LOG.isInfoEnabled()) {		
			LOG.info("applyOverrides(), complete");
		}
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

		if (LOG.isInfoEnabled()) {
			LOG.info("generateCaseLists(), complete");
		}
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
		Collection<ReferenceMetadata> referenceMetadatas = config.getReferenceMetadata(referenceType);
		if (referenceMetadatas.isEmpty()) {
			if (LOG.isInfoEnabled()) {
				LOG.info("importReferenceData(), unknown referenceType: " + referenceType);
			}
		}
		else {
			Importer importer = (Importer)getBean("importer");
			for (ReferenceMetadata referenceMetadata : referenceMetadatas) {
				if (referenceType.equals(Config.ALL) || referenceMetadata.getReferenceType().equals(referenceType)) {
					if (LOG.isInfoEnabled()) {
						LOG.info("importReferenceData(), calling import for: " + referenceMetadata.getReferenceType());
					}
					importer.importReferenceData(referenceMetadata);
				}
			}
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("importReferenceData(), complete");
		}
	}

	/**
	 * Helper function to import data.
     *
     * @param portal String
	 * @param initPortalDatabase String
	 * @param importReferenceData String
	 *
	 * @throws Exception
	 */
	private void importData(String portal, String initPortalDatabase, String importReferenceData) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), portal: " + portal);
			LOG.info("importData(), initPortalDatabase: " + initPortalDatabase);
			LOG.info("importData(), importReferenceData: " + importReferenceData);
		}

		// get booleans
		Boolean initPortalDatabaseBool = getBoolean(initPortalDatabase);
		Boolean importReferenceDataBool = getBoolean(importReferenceData);

		// create an instance of Importer
		Importer importer = (Importer)getBean("importer");
		importer.importData(portal, initPortalDatabaseBool, importReferenceDataBool);

		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), complete");
		}
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
