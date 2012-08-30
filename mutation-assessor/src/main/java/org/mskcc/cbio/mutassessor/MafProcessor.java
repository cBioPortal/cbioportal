package org.mskcc.cbio.mutassessor;

import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.*;

/**
 * Adds or replaces Mutation Assessor columns to MAFs.
 */
public class MafProcessor
{
	public void addAssessorInfo(File inputMaf, File outputMaf) throws IOException
	{
		// TODO use DB (dao) to get MA values (by key), and update given MAF

		// TODO also purge old/unused MA columns from the oncotated MAF.

		BufferedReader reader = new BufferedReader(new FileReader(inputMaf));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputMaf));

		// process header line
		String line = reader.readLine();
		MafUtil util = new MafUtil(line);

		// process each data line
		while ((line = reader.readLine()) != null)
		{
			// skip empty lines
			if (line.trim().length() == 0)
			{
				continue;
			}

			MafRecord record = util.parseRecord(line);

			// TODO if required MA columns already exist, overwrite the data
			// else add new columns just before the oncotator columns

			// TODO remove columns starting with "MA:" but not one of the required MA cols.
		}

		reader.close();
		writer.close();
	}
}
