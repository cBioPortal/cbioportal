// package
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.Fetcher;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.CommandLineParser;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.PrintWriter;

/**
 * Class which provides command line admin capabilities 
 * to the importer tool.
 */
public class Admin implements Runnable {

	// our context file
	private static final String contextFile = "classpath:applicationContext-importer.xml";

	// our logger
	private static final Log LOG = LogFactory.getLog(Admin.class);

	// options var
	private static Options options = initializeOptions();

	// parsed command line
	private CommandLine line;
	

	/**
	 * Method to initialize our static options var
	 *
	 * @return Options
	 */
	private static Options initializeOptions() {
		
		// create each option
		Option help = new Option("help", "print this message");
		Option fetch = new Option("firehose_fetch", "fetch firehose data");

		// create an options instance
		Options toReturn = new Options();

		// add options
		toReturn.addOption(help);
		toReturn.addOption(fetch);

		// outta here
		return toReturn;
	}

	/**
	 * Parses the arguments.
	 *
	 * @param args String[]
	 * @throws Exception
	 */
	public void setCommandParameters(String[] args) throws Exception {

		// create our parser
		CommandLineParser parser = new PosixParser();

		// parse
		line = parser.parse(options, args);
	}

	/**
	 * Executes the desired portal commmand.
	 */
	@Override
	public void run() {

		// sanity check
		if (line == null) {
			return;
		}

		try {
			// usage
			if (line.hasOption("help")) {
				Admin.usage(new PrintWriter(System.out, true));
			}
			// fetch
			else if (line.hasOption("firehose_fetch")) {
				fetchFirehoseData();
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
	 * Helper function to get firehose data.
	 *
	 * @throws Exception
	 */
	private void fetchFirehoseData() throws Exception {

		if (LOG.isInfoEnabled()) {
			LOG.info("fetchFirehoseData()");
		}

		// create an instance of fetcher
		ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
		Fetcher fetcher = (Fetcher)context.getBean("firehoseFetcher");
		fetcher.fetch();
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
