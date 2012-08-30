package org.mskcc.cbio.mutassessor;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 *
 */
public class MutationAssessorImporter
{
	public static void main(String[] args)
	{
		CacheBuilder builder = new CacheBuilder();
		MafProcessor mafProcessor = new MafProcessor();

		String input, output;
		boolean db = false;

		// if the switch -db is provided, then process MA files and insert into DB
		if (args[0].equals("-db"))
		{
			db = true;
			input = args[1];
			output = args[2];
		}
		// else process MAF files and extend with MA information
		else
		{
			input = args[0];
			output = args[1];
		}

		try
		{
			if (db)
			{
				builder.processFile(new File(input), new File(output));
			}
			else
			{
				mafProcessor.addAssessorInfo(new File(input), new File(output));
			}

		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

	}
}
