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

package org.mskcc.cbio.mutassessor;

import org.mskcc.cbio.maf.*;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adds or replaces Mutation Assessor columns to MAFs.
 */
public class DataImporter
{
	protected MutationAssessorService maService;

	// config params (TODO create a config class instead?)
	protected boolean sortColumns;
	protected boolean addMissingCols;

	/**
	 * Default constructor with the default oncotator service.
	 */
	public DataImporter()
	{
		// init default (cache) mutation assessor service
		this.maService = new MutationAssessorService();

		// init default settings
		this.sortColumns = false;
		this.addMissingCols = false;
	}

	/**
	 * Alternative constructor with a specific oncotator service.
	 */
	public DataImporter(MutationAssessorService maService)
	{
		// init default settings
		this();

		// update mutation assessor service
		this.maService = maService;
	}
	/**
	 * Adds mutation assessor information to the given input MAF
	 * by using the information in the mutation assessor cache.
	 *
	 * @param inputMaf      input MAF to process
	 * @param outputMaf     output MAF to create
	 * @throws IOException
	 * @throws MutationAssessorServiceException
	 */
	public void addMutAssessorInfo(File inputMaf,
			File outputMaf) throws IOException, MutationAssessorServiceException
	{
		this.outputFileNames(inputMaf, outputMaf);

		BufferedReader reader = new BufferedReader(new FileReader(inputMaf));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputMaf));

		// process header line
		MafHeaderUtil headerUtil = new MafHeaderUtil();
		String line = headerUtil.extractHeader(reader);
		MafUtil util = new MafUtil(line);
		MaMafProcessor processor = new MaMafProcessor(line);

		// write metadata/comments to the output
		FileIOUtil.writeLines(writer, headerUtil.getComments());

		// create new header line for output
		List<String> columnNames = processor.newHeaderList(
				this.sortColumns, this.addMissingCols);

		// write the header line to output
		FileIOUtil.writeLine(writer, columnNames);

		int numRecordsProcessed = 0;

		// process each data line
		while ((line = reader.readLine()) != null)
		{
			// skip empty lines
			if (line.trim().length() == 0)
			{
				continue;
			}

			//line = util.adjustDataLine(line);
			MafRecord mafRecord = util.parseRecord(line);
			String key = MafUtil.generateKey(mafRecord);

			MutationAssessorRecord maRecord = this.maService.getMaRecord(key);

			// get the data and update/add new mutation assessor columns
			List<String> data = processor.newDataList(line);
			processor.updateMaData(data, maRecord);

			// write data to the output file
			FileIOUtil.writeLine(writer, data);

			numRecordsProcessed++;
		}

		System.out.println("Total number of records processed: " +
		                   numRecordsProcessed);

		reader.close();
		writer.close();
	}

	// Getters and Setters

	public boolean isSortColumns()
	{
		return sortColumns;
	}

	public void setSortColumns(boolean sortColumns)
	{
		this.sortColumns = sortColumns;
	}

	public boolean isAddMissingCols()
	{
		return addMissingCols;
	}

	public void setAddMissingCols(boolean addMissingCols)
	{
		this.addMissingCols = addMissingCols;
	}

	protected void outputFileNames(File inputMaf, File outputMaf) {
		System.out.println("Reading MAF from: " + inputMaf.getAbsolutePath());
		System.out.println("Writing new MAF to: " + outputMaf.getAbsolutePath());
	}
}
