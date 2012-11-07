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
import org.mskcc.cbio.importer.model.DataSourceMetadata;
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

	// our logger
	private static final Log LOG = LogFactory.getLog(Admin.class);

	// options var
	private static Options options = initializeOptions();

	// parsed command line
	private CommandLine commandLine;
	

	/**
	 * Method to initialize our static options var
	 *
	 * @return Options
	 */
	private static Options initializeOptions() {
		
		// create each option
		Option help = new Option("help", "print this message");

        Option clobberImportDatabase = new Option("clobber_import_database", "clobber the import database");

        Option fetchData = (OptionBuilder.withArgName("datasource:run_date")
							.hasArgs(2)
							.withValueSeparator(':')
							.withDescription("fetch data from the given datasource and the given run date (mm/dd/yyyy) " + 
											 "or use \"latest\" to retrieve the latest.")
							.create("fetch_data"));

        Option fetchReferenceData = (OptionBuilder.withArgName("reference_data")
									  .hasArg()
									  .withDescription("fetch reference data")
									  .create("fetch_reference_data"));

        Option convertData = (OptionBuilder.withArgName("portal")
                              .hasArg()
                              .withDescription("convert data awaiting for import for the given portal")
                              .create("convert_data"));

        Option importReferenceData = (OptionBuilder.withArgName("reference_type")
									  .hasArg()
									  .withDescription("import given reference data")
									  .create("import_reference_data"));

        Option importData = (OptionBuilder.withArgName("portal")
                             .hasArg()
                             .withDescription("import data for use in the given portal")
                             .create("import_data"));

		// create an options instance
		Options toReturn = new Options();

		// add options
		toReturn.addOption(help);
		toReturn.addOption(clobberImportDatabase);
		toReturn.addOption(fetchData);
		toReturn.addOption(fetchReferenceData);
		toReturn.addOption(convertData);
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
	public void setCommandParameters(final String[] args) {

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
				clobberImportDatabase();
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
			// convert data
			else if (commandLine.hasOption("convert_data")) {
				convertData(commandLine.getOptionValue("convert_data"));
			}
			// import reference data
			else if (commandLine.hasOption("import_reference_data")) {
				importReferenceData(commandLine.getOptionValue("import_reference_data"));
			}
			// import data
			else if (commandLine.hasOption("import_data")) {
				importData(commandLine.getOptionValue("import_data"));
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
	private void clobberImportDatabase() throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("clobberImportDatabase()");
		}

		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		DatabaseUtils databaseUtils = (DatabaseUtils)context.getBean("databaseUtils");
		databaseUtils.createDatabase(databaseUtils.getImporterDatabaseName(), true);
	}

	/**
	 * Helper function to get data.
	 *
	 * @param dataSource String
	 * @param runDate String
	 * @throws Exception
	 */
	private void fetchData(final String dataSource, final String runDate) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchData(), dateSource:runDate: " + dataSource + ":" + runDate);
		}

		// create an instance of fetcher
		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		Config config = (Config)context.getBean("config");
		DataSourceMetadata dataSourceMetadata = null;
		Collection<DataSourceMetadata> dataSources = config.getDataSourceMetadata(dataSource);
		if (!dataSources.isEmpty()) {
			dataSourceMetadata = dataSources.iterator().next();
		}
		// sanity check
		if (dataSourceMetadata == null) {
			throw new IllegalArgumentException("cannot instantiate a proper DataSourceMetadata object.");
		}
		Fetcher fetcher = (Fetcher)context.getBean(dataSourceMetadata.getFetcherBeanID());
		fetcher.fetch(dataSource, runDate);
	}

	/**
	 * Helper function to fetch reference data.
     *
     * @param referenceType String
	 *
	 * @throws Exception
	 */
	private void fetchReferenceData(final String referenceType) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchReferenceData(), referenceType: " + referenceType);
		}

		// create an instance of Importer
		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		Config config = (Config)context.getBean("config");
		ReferenceMetadata referenceMetadata = config.getReferenceMetadata(referenceType);
		if (referenceMetadata != null) {
			Fetcher fetcher = (Fetcher)context.getBean("referenceDataFetcher");
			fetcher.fetchReferenceData(referenceMetadata);
		}
		else {
			if (LOG.isInfoEnabled()) {
				LOG.info("fetchReferenceData(), unknown referenceType: " + referenceType);
			}
		}
	}

	/**
	 * Helper function to convert data.
     *
     * @param portal String
     *
	 * @throws Exception
	 */
	private void convertData(final String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("convertData(), portal: " + portal);
		}

		// create an instance of Converter
		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		Converter converter = (Converter)context.getBean("converter");
		converter.convertData(portal);
	}

	/**
	 * Helper function to import reference data.
     *
     * @param referenceType String
	 *
	 * @throws Exception
	 */
	private void importReferenceData(final String referenceType) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importReferenceData(), referenceType: " + referenceType);
		}

		// create an instance of Importer
		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		Config config = (Config)context.getBean("config");
		ReferenceMetadata referenceMetadata = config.getReferenceMetadata(referenceType);
		if (referenceMetadata != null) {
			Importer importer = (Importer)context.getBean("importer");
			importer.importReferenceData(referenceMetadata);
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
	 *
	 * @throws Exception
	 */
	private void importData(final String portal) throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("importData(), portal: " + portal);
		}

		// create an instance of Importer
		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		Importer importer = (Importer)context.getBean("importer");
		importer.importData(portal);
	}

	/**
	 * Helper function - prints usage
	 */
	public static void usage(final PrintWriter writer) {

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
		String home = System.getenv("PORTAL_HOME");
		if (home == null) {
			System.err.println("Please set PORTAL_HOME environment variable " +
							   " (point to a directory where portal.properties exists).");
		}
		PropertyConfigurator.configure(home + File.separator + "log4j.properties");

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
