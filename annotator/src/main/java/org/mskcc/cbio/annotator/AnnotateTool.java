package org.mskcc.cbio.annotator;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;
import org.mskcc.cbio.oncotator.OncotateTool;

/**
 * Command Line Tool to Annotate a Single Input File.
 *
 * @author Selcuk Onur Sumer
 */
public class AnnotateTool
{
	public static void main(String[] args)
	{
		AnnotatorConfig config = new AnnotatorConfig();
		CmdLineParser parser = new CmdLineParser(config);

		// try to parse the command line arguments
		try {
			parser.parseArgument(args);
		} catch( CmdLineException e ) {
			// print the error message
			System.out.println(e.getMessage());
			// print the usage
			parser.printUsage(System.out);
			// terminate
			return;
		}

		int result = 0;

		try
		{
			if (config.getAnnotator().equalsIgnoreCase("oncotator"))
			{
				result = OncotateTool.driver(config);
			}
			else
			{
				result = driver(config);
			}
		}
		catch (RuntimeException e)
		{
			System.out.println("Fatal error: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			// check errors at the end
			if (result != 0)
			{
				System.out.println("Process completed with " + result + " error(s).");
			}
		}
	}

	public static int driver(AnnotatorConfig config)
	{
		Date start = new Date();
		int result = 0;

		Annotator annotator = new Annotator(config);

		try {
			annotator.annotateFile(new File(config.getInput()),
			                       new File(config.getOutput()));
		} catch (IOException e) {
			System.out.println("IO error occurred: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Date end = new Date();
			double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

//			System.out.println("Total number of records processed: " + annotator.getNumRecordsProcessed());
			System.out.println("Total time: " + timeElapsed + " seconds.");
		}

		return result;
	}
}