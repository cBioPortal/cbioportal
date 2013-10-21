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
import org.apache.commons.cli.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

        Option validateCancerStudy = (OptionBuilder.withArgName("dir")
                                      .hasArg()
                                      .withDescription("Validates cancer studies within the given cancer study directory")
                                      .create("v"));

        Option importCancerStudy = (OptionBuilder.withArgName("dir:skip:force")
                                    .hasArgs(3)
                                    .withValueSeparator(':')
                                    .withDescription("Import cancer study data into the database.  " +
                                                     "This command will traverse all subdirectories of cancer_study_directory " +
                                                     "looking for cancer studies to import.  If the skip argument is 't', " +
                                                     "cancer studies will not be replaced.  Set force to 't' to force a cancer study replacement.")
                                    .create("i"));

		// create an options instance
		Options toReturn = new Options();

		// add options
		toReturn.addOption(help);
        toReturn.addOption(validateCancerStudy);
		toReturn.addOption(importCancerStudy);

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
			System.exit(-1);
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
			else if (commandLine.hasOption("i")) {
                String[] values = commandLine.getOptionValues("i");
				importCancerStudy(values[0], (values.length >= 2) ? values[1] : "", (values.length == 3) ? values[2] : "");
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

	public static void main(String[] args) throws Exception
    {
		if (args.length == 0) {
			System.err.println("Missing args to PortalImporterTool.");
			PortalImporterTool.usage(new PrintWriter(System.err, true));
			System.exit(-1);
		}

		// process
		PortalImporterTool importer = new PortalImporterTool();
		try {
			importer.setCommandParameters(args);
			importer.run();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
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
