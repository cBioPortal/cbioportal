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

import org.mskcc.cbio.maf.*;

import java.io.*;
import java.util.List;

/**
 * Main controller class for MAF processing and IO operations.
 *
 * @author Selcuk Onur Sumer
 */
public class Oncotator
{
	protected static final String SILENT_MUTATION = "Silent";
	protected static int MAX_NUM_RECORDS_TO_PROCESS = -1;

	protected OncotatorService oncotatorService;

	protected int buildNumErrors = 0;
	protected int numRecordsProcessed = 0;
	protected int totalNumRecords = 0;

	// config params (TODO create a config class instead?)
	protected boolean useCache;
	protected boolean sortColumns;
	protected boolean addMissingCols;

	/**
	 * Default constructor with the default oncotator service.
	 */
	public Oncotator()
	{
		// init default settings
		this.sortColumns = false;
		this.addMissingCols = false;
		this.useCache = true;
		this.oncotatorService = new CachedOncotatorService();
	}

	public Oncotator(boolean useCache)
	{
		this();
		this.useCache = useCache;

		// determine whether to use the DB cache or not
		if (useCache)
		{
			this.oncotatorService = new CachedOncotatorService();
		}
		else
		{
			this.oncotatorService = new BasicOncotatorService();
		}
	}

	/**
	 * Alternative constructor with a specific oncotator service.
	 */
	public Oncotator(OncotatorService oncotatorService)
	{
		// init default settings
		this();

		// update oncotator service
		this.oncotatorService = oncotatorService;
	}

	/**
	 * Instantiates a new (default) MAF processor for this oncotator.
	 *
	 * Override this method in a child class to provide a custom MAF
	 * processor for specific needs.
	 *
	 * @param headerLine    header line of the MAF containing column names
	 * @return              default MAF processor instance
	 */
	protected OncoMafProcessor initMafProcessor(String headerLine)
	{
		return new OncoMafProcessor(headerLine);
	}

	/**
	 * Oncotates the given input MAF file and creates a new MAF
	 * file with new/updated oncotator columns.
	 *
	 * @param inputMafFile  input MAF
	 * @param outputMafFile output MAF
	 * @return              number of errors (if any) during the process
	 * @throws IOException                  if an IO exception occurs
	 * @throws OncotatorServiceException    if a service exception occurs
	 */
	protected int oncotateMaf(File inputMafFile, File outputMafFile)
			throws IOException, OncotatorServiceException
	{
		this.outputFileNames(inputMafFile, outputMafFile);
		this.totalNumRecords = this.countNumRecords(inputMafFile);

		FileReader reader = new FileReader(inputMafFile);
		BufferedReader bufReader = new BufferedReader(reader);
		MafHeaderUtil headerUtil = new MafHeaderUtil();

		String headerLine = headerUtil.extractHeader(bufReader);
		MafUtil mafUtil = new MafUtil(headerLine);
		OncoMafProcessor processor = this.initMafProcessor(headerLine);

		this.numRecordsProcessed = 0;
		FileWriter writer = new FileWriter(outputMafFile);

		// write comments/metadata to the output
		FileIOUtil.writeLines(writer, headerUtil.getComments());

		// create new header line for output
		List<String> columnNames = processor.newHeaderList(
				this.sortColumns, this.addMissingCols);

		// write the header line to output
		FileIOUtil.writeLine(writer, columnNames);

		String dataLine = bufReader.readLine();

		// process the file line by line
		while (dataLine != null)
		{
			// skip empty lines
			if (dataLine.trim().length() == 0)
			{
				dataLine = bufReader.readLine();
				continue;
			}

			// update total number of records processed
			this.numRecordsProcessed++;

			MafRecord mafRecord = mafUtil.parseRecord(dataLine);
			OncotatorRecord oncotatorRecord = this.conditionallyOncotateRecord(mafRecord);
			this.conditionallyAbort(this.numRecordsProcessed);

			// get the data and update/add new oncotator columns
			List<String> data = processor.newDataList(dataLine);
			processor.updateOncotatorData(data, oncotatorRecord);

			// write data to the output file
			FileIOUtil.writeLine(writer, data);

			dataLine = bufReader.readLine();
		}

		reader.close();
		writer.close();

		return this.oncotatorService.getErrorCount();
	}

	/**
	 * Conditionally oncotates a single line of a MAF file.
	 *
	 * @param mafRecord MAF record representing a single line of a MAF file
	 * @return          oncotator data retrieved from oncotator service
	 */
	protected OncotatorRecord conditionallyOncotateRecord(MafRecord mafRecord)
			throws OncotatorServiceException
	{
		String ncbiBuild = mafRecord.getNcbiBuild();
		OncotatorRecord oncotatorRecord = null;

		if (!ncbiBuild.equals("37") &&
		    !ncbiBuild.equalsIgnoreCase("hg19") &&
		    !ncbiBuild.equalsIgnoreCase("GRCh37"))
		{
			this.outputBuildNumErrorMessage(ncbiBuild);
			this.buildNumErrors++;
		}
		else
		{
			oncotatorRecord = this.oncotateRecord(mafRecord);
		}

		return oncotatorRecord;
	}

	/**
	 * Oncotates a single line of a MAF file, and returns an OncotatorRecord
	 * instance containing the data retrieved from oncotator service.
	 *
	 * @param mafRecord MAF record representing a single line of a MAF file
	 * @return          oncotator data retrieved from oncotator service
	 */
	protected OncotatorRecord oncotateRecord(MafRecord mafRecord)
			throws OncotatorServiceException
	{
		String key = MafUtil.generateKey(mafRecord);

		OncotatorRecord oncotatorRecord = null;

		if (key != null)
		{
			oncotatorRecord = oncotatorService.getOncotatorRecord(key);

			String progress = "(" + this.numRecordsProcessed + "/" + this.totalNumRecords + ")";

			// print percentage complete and coordinate info (key) to stdout
			System.out.println(progress + " " + key);
		}

		return oncotatorRecord;
	}

	/**
	 * Counts the number of records (number of nonempty lines) for
	 * the given MAF file.
	 *
	 * @param inputMaf      input MAF file
	 * @return              total number of records
	 * @throws IOException  if an IO error occurs
	 */
	protected int countNumRecords(File inputMaf) throws IOException
	{
		int count = 0;

		FileReader reader = new FileReader(inputMaf);
		BufferedReader bufReader = new BufferedReader(reader);

		// skip header line
		bufReader.readLine();

		String dataLine;

		// process the file line by line
		while ((dataLine = bufReader.readLine()) != null)
		{
			// skip empty lines
			if (dataLine.trim().length() > 0)
			{
				count++;
			}
		}

		bufReader.close();

		return count;
	}

	protected void abortDueToBuildNumErrors()
	{
		throw new RuntimeException("Too many records with wrong build #.  Aborting...");
		//System.out.println("Too many records with wrong build #.  Aborting...");
	}

	protected void outputBuildNumErrorMessage(String ncbiBuild) {
		System.out.println("Record uses NCBI Build:  " + ncbiBuild);
		System.out.println("-->  Oncotator only works with Build 37/hg19.");
	}

	protected void outputFileNames(File inputMafFile, File outputMafFile) {
		System.out.println("Reading MAF from: " + inputMafFile.getAbsolutePath());
		System.out.println("Writing new MAF to: " + outputMafFile.getAbsolutePath());
	}

	protected void conditionallyAbort(int numRecordsProcessed) {
		if (MAX_NUM_RECORDS_TO_PROCESS > 0 && numRecordsProcessed > MAX_NUM_RECORDS_TO_PROCESS) {
			throw new IllegalStateException("Aborting at " + MAX_NUM_RECORDS_TO_PROCESS + " records");
		}
	}


	// Getters and Setters

	public boolean isUseCache()
	{
		return useCache;
	}

	public void setUseCache(boolean useCache)
	{
		this.useCache = useCache;
	}

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

	public int getBuildNumErrors()
	{
		return buildNumErrors;
	}

	public int getNumRecordsProcessed()
	{
		return numRecordsProcessed;
	}
}
