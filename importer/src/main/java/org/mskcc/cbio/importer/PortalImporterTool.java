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
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.scripts.NormalizeExpressionLevels;                                                     

import org.apache.commons.cli.*;

import org.apache.commons.logging.*;
import org.apache.log4j.PropertyConfigurator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;
import java.util.Properties;

/**
 * Class which provides command line admin capabilities 
 * to the importer tool.
 */
public class PortalImporterTool implements Runnable {

    private static final String HOME_DIR = "PORTAL_HOME";
	private static final Log LOG = LogFactory.getLog(PortalImporterTool.class);
    static {
        configureLogging();
    }
	private static final String contextFile = "classpath:applicationContext-portalImporterTool.xml";
	private static final ApplicationContext context = new ClassPathXmlApplicationContext(contextFile);
	private static final Options options = initializeOptions();

	private CommandLine commandLine;

	private static Options initializeOptions()
    {
		
		// create each option
		Option help = new Option("h", "Print this message.");

        Option annotateMAF = (OptionBuilder.withArgName("maf:output")
                              .hasArgs(2)
                              .withValueSeparator(':')
                              .withDescription("Annotates the MAF file with additional information from mutationassessor.org and Oncotator." +
                                               "If output filename is not given, input filename will be used with a '.annotated' extension.")
                              .create("a"));

        Option validateCancerStudy = (OptionBuilder.withArgName("dir")
                                      .hasArg()
                                      .withDescription("Validates cancer studies within the given cancer study directory.")
                                      .create("v"));

        Option normalizeDataFile = (OptionBuilder.withArgName("cna-file:expression-file:output-file:normal-sample-suffix")
                                    .hasArgs(4)
                                    .withValueSeparator(':')
                                    .withDescription("Given CNV & expression data for a set of samples, generate normalized expression values.")
                                    .create("n"));

        Option importCancerStudy = (OptionBuilder.withArgName("dir:skip:force")
                                    .hasArgs(3)
                                    .withValueSeparator(':')
                                    .withDescription("Import cancer study data into the database.  " +
                                                     "This command will traverse all subdirectories of cancer_study_directory " +
                                                     "looking for cancer studies to import.  If the skip argument is 't', " +
                                                     "cancer studies will not be replaced.  Set force to 't' to force a cancer study replacement.")
                                    .create("i"));

		 Option deleteCancerStudy = (OptionBuilder.withArgName("cancer_study_id")
									.hasArg()
									.withDescription("Delete a cancer study matching the given cancer study id.")
									.create("d"));

		
		// create an options instance
		Options toReturn = new Options();

		// add options
		toReturn.addOption(help);
		toReturn.addOption(annotateMAF);
        toReturn.addOption(validateCancerStudy);
        toReturn.addOption(normalizeDataFile );
		toReturn.addOption(importCancerStudy);
		toReturn.addOption(deleteCancerStudy);

		// outta here
		return toReturn;
	}

	public void setCommandParameters(String[] args)
    {
		// create our parser
		CommandLineParser parser = new PosixParser();

		// parse
		try {
			commandLine = parser.parse(options, args);
		}
		catch (Exception e) {
			Admin.usage(new PrintWriter(System.out, true));
		}
	}

	public static void usage(PrintWriter writer)
    {

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(writer, 100,
							"PortalImporterTool", "", options,
							HelpFormatter.DEFAULT_LEFT_PAD,
							HelpFormatter.DEFAULT_DESC_PAD, "");
	}

	@Override
	public void run()
    {
		if (commandLine == null) return;

		try {
			if (commandLine.hasOption("h")) {
				Admin.usage(new PrintWriter(System.out, true));
			}
            else if (commandLine.hasOption("v")) {
                validateCancerStudy(commandLine.getOptionValue("v"));
            }
            else if (commandLine.hasOption("n")) {
                String[] values = commandLine.getOptionValues("n");
				normalizeExpressionLevels(values[0], values[1], values[2], values[3]);
            }
			else if (commandLine.hasOption("i")) {
                String[] values = commandLine.getOptionValues("i");
				importCancerStudy(values[0], (values.length >= 2) ? values[1] : "", (values.length == 3) ? values[2] : "");
			}
            else if (commandLine.hasOption("a")) {
                String[] values = commandLine.getOptionValues("a");
                annotateMAF(values[0], (values.length == 2) ? values[1] : values[0] + ".annotated");
            }
            else if (commandLine.hasOption("d")) {
				deleteCancerStudy(commandLine.getOptionValue("d"));
			} 
			else {
				Admin.usage(new PrintWriter(System.out, true));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception
    {
		if (args.length == 0) {
			System.err.println("Missing args to PortalImporterTool.");
			PortalImporterTool.usage(new PrintWriter(System.err, true));
                        return;
		}

		// process
		PortalImporterTool importer = new PortalImporterTool();
		try {
			importer.setCommandParameters(args);
			importer.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

    private static void configureLogging()
    {
        String propertyFilename = "log4j.properties";

        try {
            String home = System.getenv(HOME_DIR);
            if (home != null) {
                propertyFilename = home + File.separator + "log4j.properties";
                InputStream fis = new FileInputStream(propertyFilename);
                Properties props = new Properties();
                props.load(fis);
                fis.close();
                PropertyConfigurator.configure(props);
            }
        }
        catch(IOException e) {
			System.err.println("Error loading: " + propertyFilename);
        }
    }

	private void validateCancerStudy(String cancerStudyDirectory) throws Exception
    {
        logMessage("validateCancerStudy(), cancer study directory: " + cancerStudyDirectory);

		Validator validator = (Validator)context.getBean("cancerStudyValidator");
		validator.validateCancerStudy(cancerStudyDirectory);

        logMessage("validateCancerStudy(), complete");
    }

	private void importCancerStudy(String cancerStudyDirectory, String skip, String force) throws Exception
    {

        logMessage("importCancerStudy(), cancer study directory: " + cancerStudyDirectory);

        boolean skipBool = getBoolean(skip);
        boolean forceBool = getBoolean(force);
		Importer importer = (Importer)context.getBean("cancerStudyImporter");
		importer.importCancerStudy(cancerStudyDirectory, skipBool, forceBool);

        logMessage("importCancerStudy(), complete");
	}

	private void annotateMAF(String inputFilename, String outputFilename) throws Exception {

        logMessage("annotateMAF(), mafFile: " + inputFilename);

		// sanity check
		File mafFile = new File(inputFilename);
		if (!mafFile.exists()) {
			throw new IllegalArgumentException("cannot find the give MAF: " + inputFilename);
		}

		// create fileUtils object
		FileUtils fileUtils = (FileUtils)context.getBean("fileUtils");

		// create output file
		File outputMAF = 
			org.apache.commons.io.FileUtils.getFile(outputFilename);

		fileUtils.oncotateMAF(FileUtils.FILE_URL_PREFIX + mafFile.getCanonicalPath(),
                              FileUtils.FILE_URL_PREFIX + outputMAF.getCanonicalPath());

        logMessage("annotateMAF(), complete");
	}

    private void normalizeExpressionLevels(String cnaFile, String expressionFile, String normalizedFile, String normalSampleSuffix) throws Exception
    {
        logMessage("normalizeExpressionLevels()");
        logMessage("cnaFile: " + cnaFile);
        logMessage("expressionFile: " + expressionFile);
        logMessage("outputFile: " + normalizedFile);
        logMessage("normalSampleSuffix: " + normalSampleSuffix);

		String[] args = { cnaFile, expressionFile, normalizedFile, normalSampleSuffix };
        NormalizeExpressionLevels.driver(args);

        logMessage("normalizeExpressionLevels(), complete");
    }

    private void deleteCancerStudy(String cancerStudyStableId) throws Exception
	{
		if (LOG.isInfoEnabled()) {
			LOG.info("deleteCancerStudy(), study id: " + cancerStudyStableId);
		}
		DaoCancerStudy.deleteCancerStudy(cancerStudyStableId);
		if (LOG.isInfoEnabled()) {
			LOG.info("deleteCancerStudy(), complete");
		}
	} 

	private boolean getBoolean(String parameterValue)
    {
		return (parameterValue.equalsIgnoreCase("t")) ? Boolean.TRUE : Boolean.FALSE;
	}

    private void logMessage(String message)
    {
        if (LOG.isInfoEnabled()) {
            LOG.info(message);
        }
        System.err.println(message);
    }
}
