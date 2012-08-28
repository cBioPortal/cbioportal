package org.mskcc.cbio.mutassessor;

import java.io.File;

/**
 * Adds or replaces Mutation Assessor columns to MAFs.
 */
public class MafProcessor
{
	public void addAssessorInfo(File input, File output)
	{
		// TODO use DB (dao) to get MA values (by key), and update given MAF

		// TODO also purge old/unused MA columns from the oncotated MAF.
	}
}
