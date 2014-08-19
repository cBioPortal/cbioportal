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

package org.mskcc.cbio.oncotator;

import org.mskcc.cbio.annotator.AnnotatorConfig;

import java.io.*;
import java.util.Date;

/**
 * Command Line Tool to Oncotate a Single MAF File.
 *
 * @author Selcuk Onur Sumer
 */
public class OncotateTool
{
	// TODO remove this after updating the importer
	public static int driver(String inputMaf,
			String outputMaf,
			boolean useCache,
			boolean sort,
			boolean addMissing)
	{
		AnnotatorConfig config = new AnnotatorConfig();

		config.setInput(inputMaf);
		config.setOutput(outputMaf);
		config.setNoCache(!useCache);
		config.setSort(sort);
		config.setAddMissing(addMissing);

		return driver(config);
	}

	public static int driver(AnnotatorConfig config)
	{
		Date start = new Date();
		int oncoResult = 0;

		Oncotator tool = new Oncotator(!config.isNoCache());
		tool.setSortColumns(config.isSort());
		tool.setAddMissingCols(config.isAddMissing());

		try {
			oncoResult = tool.oncotateMaf(new File(config.getInput()),
			                              new File(config.getOutput()));
		} catch (IOException e) {
			System.out.println("IO error occurred: " + e.getMessage());
			e.printStackTrace();
		} catch (OncotatorServiceException e) {
			System.out.println("Service error occurred: " + e.getMessage());
			e.printStackTrace();
		}
		finally {
			Date end = new Date();
			double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

			System.out.println("Total number of records processed: " +
			                   tool.getNumRecordsProcessed());

			if (tool.getBuildNumErrors() > 0)
			{
				System.out.println("Number of records skipped due to incompatible build no: " +
				                   tool.getBuildNumErrors());
			}

			System.out.println("Total time: " + timeElapsed + " seconds.");
		}

		return oncoResult;
	}
}