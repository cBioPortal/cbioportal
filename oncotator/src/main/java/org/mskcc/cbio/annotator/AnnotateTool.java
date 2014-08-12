package org.mskcc.cbio.annotator;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Command Line Tool to Annotate a Single Input File.
 *
 * @author Selcuk Onur Sumer
 */
public class AnnotateTool
{
	public static void main(String[] args)
	{
		// default config params
		boolean sort = false;       // sort output MAF cols or not
		boolean addMissing = false; // add missing standard cols or not

		// process program arguments

		int i;

		for (i = 0; i < args.length; i++)
		{
			if (args[i].startsWith("-"))
			{
				if (args[i].equalsIgnoreCase("-sort"))
				{
					sort = true;
				}
				else if (args[i].equalsIgnoreCase("-std"))
				{
					addMissing = true;
				}
			}
			else
			{
				break;
			}
		}

		if (args.length - i < 2)
		{
			System.out.println("command line usage: annotateMaf.sh [-sort] [-std] " +
			                   "<input_maf_file> <output_maf_file> " +
			                   "[maf2maf_script_file] [vcf2maf_script_file] [vep_path]");

			return;
		}

		String input = args[i];
		String output = args[i+1];
		String maf2maf = Annotator.DEFAULT_MAF2MAF;
		String vcf2maf = Annotator.DEFAULT_VCF2MAF;
		String vepPath = Annotator.DEFAULT_VEP_PATH;

		if (args.length > i + 2)
		{
			maf2maf = args[i + 2];
		}

		if (args.length > i + 3)
		{
			vcf2maf = args[i + 3];
		}

		if (args.length > i + 4)
		{
			vepPath = args[i + 4];
		}

		int result = 0;

		try
		{
			result = driver(input,
				output,
				sort,
				addMissing,
				maf2maf,
				vcf2maf,
				vepPath);
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

	public static int driver(String inputMaf,
			String outputMaf,
			boolean sort,
			boolean addMissing,
			String maf2MafScript,
			String vcf2MafScript,
			String vepPath)
	{
		Date start = new Date();
		int result = 0;

		Annotator annotator = new Annotator();
		annotator.setSortColumns(sort);
		annotator.setAddMissingCols(addMissing);
		annotator.setMaf2MafScript(maf2MafScript);
		annotator.setVcf2MafScript(vcf2MafScript);
		annotator.setVepPath(vepPath);

		try {
			annotator.annotateFile(new File(inputMaf), new File(outputMaf));
		} catch (IOException e) {
			System.out.println("IO error occurred: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Date end = new Date();
			double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

//			System.out.println("Total number of records processed: " + annotator.getNumRecordsProcessed());
//
//			if (annotator.getBuildNumErrors() > 0)
//			{
//				System.out.println("Number of records skipped due to incompatible build no: " +
//				                   annotator.getBuildNumErrors());
//			}

			System.out.println("Total time: " + timeElapsed + " seconds.");
		}

		return result;
	}
}