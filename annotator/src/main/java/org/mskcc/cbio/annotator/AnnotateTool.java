package org.mskcc.cbio.annotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.CmdLineException;
import org.mskcc.cbio.maf.MafHeaderUtil;
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
			System.out.println("Error: ");
			System.out.println(e.getMessage());
			System.out.println("");
			// print the usage
			System.out.println("Usage: ");
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

				if (result != 0)
				{
					System.out.println("Process completed with " + result + " error(s).");
				}
			}
			else
			{
				result = driver(config);

				if (result != 0)
				{
					System.out.println("[ERROR] Process completed with exit code " + result);
				}
			}
		}
		catch (RuntimeException e)
		{
			System.out.println("Fatal error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static int driver(AnnotatorConfig config)
	{
		Date start = new Date();
		int result = 0;

		Annotator annotator = new Annotator(config);

		try {
			System.out.println("[" + start + "] Started annotating: " + config.getInput());
			result = annotator.annotateFile(new File(config.getInput()),
			                       new File(config.getOutput()));
			int diff = compareFiles(config.getInput(), config.getOutput());

			if (diff != 0)
			{
				System.out.println("Possible error processing the input file: " + config.getInput());
			}
		} catch (IOException e) {
			System.out.println("IO error occurred: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Date end = new Date();
			double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

//			System.out.println("Total number of records processed: " + annotator.getNumRecordsProcessed());
			System.out.println("[" + end + "] Total time: " + timeElapsed + " seconds.");
		}

		return result;
	}

	/**
	 * Compare the given 2 files with respect to number of total data lines.
	 *
	 * @param file1
	 * @param file2
	 * @return difference between the two files wrt total number of data lines
	 */
	private static int compareFiles(String file1, String file2)
	{
		int diff;

		try
		{
			diff = calcLineCount(file1) - calcLineCount(file2);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return -1;
		}

		return diff;
	}

	/**
	 * Calculates the total number of data lines: Lines excluding the comments,
	 * header line and empty lines.
	 *
	 * @param filename  name of the data file
	 * @return  total number of data lines
	 * @throws IOException
	 */
	private static int calcLineCount(String filename) throws IOException
	{
		BufferedReader bufReader = new BufferedReader(new FileReader(filename));
		MafHeaderUtil headerUtil = new MafHeaderUtil();

		// this is to exclude comments and header lines from the count
		String headerLine = headerUtil.extractHeader(bufReader);

		String dataLine;
		int count = 0;

		// process the file line by line
		while ((dataLine = bufReader.readLine()) != null)
		{
			// skip empty lines
			if (dataLine.trim().length() == 0)
			{
				continue;
			}

			// update total number of lines
			count++;
		}

		bufReader.close();

		return count;
	}
}