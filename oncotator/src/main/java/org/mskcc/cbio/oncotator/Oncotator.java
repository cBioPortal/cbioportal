/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.oncotator;

import org.mskcc.cbio.maf.FileIOUtil;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;
import org.mskcc.cbio.maf.OncoMafProcessor;

import java.io.*;
import java.util.List;

/**
 * Main controller class for MAF processing and IO operations.
 */
public class Oncotator
{
	protected static final String SILENT_MUTATION = "Silent";
	protected static int MAX_NUM_RECORDS_TO_PROCESS = -1;

	protected OncotatorService oncotatorService;

	protected int buildNumErrors = 0;

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
	 * @throws Exception    if an (IO or service) Exception occurs
	 */
	protected int oncotateMaf(File inputMafFile,
			File outputMafFile) throws Exception
	{
		this.outputFileNames(inputMafFile, outputMafFile);

		FileReader reader = new FileReader(inputMafFile);
		BufferedReader bufReader = new BufferedReader(reader);
		String headerLine = bufReader.readLine();
		MafUtil mafUtil = new MafUtil(headerLine);
		OncoMafProcessor processor = this.initMafProcessor(headerLine);

		int numRecordsProcessed = 0;
		FileWriter writer = new FileWriter(outputMafFile);

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

			MafRecord mafRecord = mafUtil.parseRecord(dataLine);
			String variantClassification = mafRecord.getVariantClassification();
			OncotatorRecord oncotatorRecord = null;

			// do not oncotate silent mutations
			if (!variantClassification.equalsIgnoreCase(SILENT_MUTATION))
			{
				oncotatorRecord = this.conditionallyOncotateRecord(mafRecord);
				numRecordsProcessed++;
				this.conditionallyAbort(numRecordsProcessed);
			}
			else
			{
				// just set the mutation type, all other data will be empty
				oncotatorRecord = new OncotatorRecord("NA");
				oncotatorRecord.getBestEffectTranscript().setVariantClassification("Silent");
			}

			// get the data and update/add new oncotator columns
			List<String> data = processor.newDataList(dataLine);
			processor.updateOncotatorData(data, oncotatorRecord);

			// write data to the output file
			FileIOUtil.writeLine(writer, data);

			dataLine = bufReader.readLine();
		}

		System.out.println("Total number of records processed: " +
		                   numRecordsProcessed);

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
			throws Exception
	{
		String ncbiBuild = mafRecord.getNcbiBuild();
		OncotatorRecord oncotatorRecord = null;

		if (!ncbiBuild.equals("37") &&
		    !ncbiBuild.equalsIgnoreCase("hg19") &&
		    !ncbiBuild.equalsIgnoreCase("GRCh37"))
		{
			outputBuildNumErrorMessage(ncbiBuild);
			buildNumErrors++;

			if (buildNumErrors > 10) {
				abortDueToBuildNumErrors();
			}
		}
		else
		{
			oncotatorRecord = oncotateRecord(mafRecord);
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
	protected OncotatorRecord oncotateRecord(MafRecord mafRecord) throws Exception
	{
		String key = MafUtil.generateKey(mafRecord);

		OncotatorRecord oncotatorRecord = null;

		if (key != null)
		{
			oncotatorRecord = oncotatorService.getOncotatorRecord(key);

			// print coordinate info to stdout
			System.out.println(key);
		}

		return oncotatorRecord;
	}

	protected void abortDueToBuildNumErrors() {
		System.out.println("Too many records with wrong build #.  Aborting...");
		System.exit(1);
	}

	protected void outputBuildNumErrorMessage(String ncbiBuild) {
		System.out.println("Record uses NCBI Build:  " + ncbiBuild);
		System.out.println("-->  Oncotator only works with Build 37/hg19.");
	}

	protected void outputFileNames(File inputMafFile, File outputMafFile) {
		System.out.println("Reading MAF From:  " + inputMafFile.getAbsolutePath());
		System.out.println("Writing new MAF To:  " + outputMafFile.getAbsolutePath());
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
}
