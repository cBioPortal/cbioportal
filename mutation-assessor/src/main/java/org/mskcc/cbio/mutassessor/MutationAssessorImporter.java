package org.mskcc.cbio.mutassessor;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Main class.
 */
public class MutationAssessorImporter
{
	public static void main(String[] args)
	{
		MafProcessor mafProcessor = new MafProcessor();
		CacheBuilder builder;

		String input;
		String output = null;

		boolean db = false;

		// validate arguments
		if (args.length == 0)
		{
			// TODO invalid number of arguments, print usage
			System.out.println("Error: Invalid number of arguments");
			System.exit(1);
		}

		if (args[0].equals("-db"))
		{
			if (args.length < 2)
			{
				// TODO no input file or directory
				System.out.println("Error: Invalid number of arguments");
				System.exit(1);
			}
		}
		else if (args.length < 2)
		{
			// TODO output file not specified
			System.out.println("Error: Invalid number of arguments");
			System.exit(1);
		}

		// if the switch -db is provided, then process MA files and insert into DB
		if (args[0].equals("-db"))
		{
			db = true;
			input = args[1];

			if (args.length >= 2)
			{
				output = args[2];
			}
		}
		// else process a single MAF file and extend with MA information
		else
		{
			input = args[0];
			output = args[1];
		}

		try
		{
			// db switch is provided
			if (db)
			{
				// no output specified, write to DB (slower)
				if (output != null)
				{
					builder = new CacheBuilder(output);
				}
				// output specified, create an SQL script file (faster)
				else
				{
					builder = new CacheBuilder();
				}

				// start cache building process
				builder.buildCache(input);
			}
			// process given MAF file and add MA info if possible
			else
			{
				mafProcessor.addMutAssessorInfo(
						new File(input), new File(output));
			}
		}
		catch (IOException e)
		{
			System.out.println("IO error: " + e.getMessage());
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			System.out.println("Database error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
