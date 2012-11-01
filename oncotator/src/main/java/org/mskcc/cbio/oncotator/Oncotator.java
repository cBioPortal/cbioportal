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

import org.mskcc.cbio.maf.MafProcessor;
import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.*;
import java.util.List;

/**
 * Main controller class for MAF processing and IO operations.
 */
public class Oncotator
{
	protected static final String TAB = "\t";
	protected static final String SILENT_MUTATION = "Silent";
	protected static int MAX_NUM_RECORDS_TO_PROCESS = -1;
	private static final int DEFAULT_ONCO_HEADERS_COUNT = 5;

	protected OncotatorService oncotatorService;

	protected int buildNumErrors = 0;

	// config params (TODO create a config class instead?)
	protected boolean useCache;
	protected boolean sortColumns;
	protected boolean addMissingCols;

//	private HashMap<String, Integer> genomicCountMap = new HashMap<String, Integer>();

	/**
	 * Default constructor with the default oncotator service.
	 */
	public Oncotator()
	{
		// init default (HTTP) oncotator service
		this.oncotatorService = new OncotatorService();

		// init default settings
		this.useCache = true;
		this.sortColumns = false;
		this.addMissingCols = false;
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
		// determine whether to use the DB cache or not
		this.oncotatorService.setUseCache(this.useCache);

		this.outputFileNames(inputMafFile, outputMafFile);

		FileReader reader = new FileReader(inputMafFile);
		BufferedReader bufReader = new BufferedReader(reader);
		String headerLine = bufReader.readLine();
		MafUtil mafUtil = new MafUtil(headerLine);
		MafProcessor processor = new MafProcessor(headerLine);

		int numRecordsProcessed = 0;
		FileWriter writer = new FileWriter(outputMafFile);

		// create new header line for output
		List<String> columnNames = processor.newHeaderList(
				this.sortColumns, this.addMissingCols);

		// write the header line to output
		this.writeLine(writer, columnNames);

		String dataLine = bufReader.readLine();

		// process the file line by line
		while (dataLine != null)
		{
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
			this.writeLine(writer, data);

			dataLine = bufReader.readLine();
		}

		System.out.println("Total Number of Records Processed:  " + numRecordsProcessed);
//		for (String coords:  genomicCountMap.keySet()) {
//			Integer count = genomicCountMap.get(coords);
//			if (count > 1) {
//				System.out.println(coords + "\t" + (count-1));
//			}
//		}

		reader.close();
		writer.close();

		return this.oncotatorService.getErrorCount();
	}

	/**
	 * Writes a single line of data to the output MAF.
	 *
	 * @param writer    writer for the output MAF
	 * @param data      list of data to write
	 * @throws IOException
	 */
	protected void writeLine(Writer writer,
			List<String> data) throws IOException
	{
		for (int i = 0; i < data.size(); i++)
		{
			String field = data.get(i);
			writer.write(outputField(field));

			if (i < data.size() - 1)
			{
				writer.write(TAB);
			}
		}

		writer.write("\n");
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
		String chr = mafRecord.getChr();
		long start = mafRecord.getStartPosition();
		long end = mafRecord.getEndPosition();
		String refAllele = mafRecord.getReferenceAllele();
		String tumorAllele = determineTumorAllele(mafRecord, refAllele);

		OncotatorRecord oncotatorRecord = null;

		if (tumorAllele != null)
		{
			oncotatorRecord = oncotatorService.getOncotatorRecord(
					chr, start, end, refAllele, tumorAllele);

			// print coordinate info to stdout
			String coords = createCoordinates(chr, start, end, refAllele, tumorAllele);
			System.out.println(coords);
//            if (genomicCountMap.containsKey(coords)) {
//                Integer count = genomicCountMap.get(coords);
//                genomicCountMap.put(coords, count+1);
//            } else {
//                genomicCountMap.put(coords, 1);
//            }
		}

		return oncotatorRecord;
	}

	/**
	 * Determines the tumor allele by using reference allele and
	 * tumor seq allele information.
	 *
	 * @param mafRecord represents a single line of a MAF file
	 * @param refAllele reference allele for that mutation
	 * @return          tumor allele as a string
	 */
	protected String determineTumorAllele(MafRecord mafRecord,
			String refAllele)
	{
		String tumorAllel1 = mafRecord.getTumorSeqAllele1();
		String tumorAllel2 = mafRecord.getTumorSeqAllele2();
		String tumorAllele = null;

		// choose the one that is different from the reference allele
		if (!refAllele.equalsIgnoreCase(tumorAllel1)) {
			tumorAllele = tumorAllel1;
		} else if(!refAllele.equalsIgnoreCase(tumorAllel2)) {
			tumorAllele = tumorAllel2;
		}

		return tumorAllele;
	}

	/**
	 * Returns a string representation of a single field for
	 * the output.
	 *
	 * @param field field as a string
	 * @return      string to output
	 */
	protected String outputField(String field)
	{
		if (field == null) {
			return "";
		} else {
			return field;
		}
	}

	protected String createCoordinates(String chr, long start, long end,
			String refAllele, String tumorAllele)
	{
		return chr + "_" + start + "_" + end + "_" + refAllele
		       + "_" + tumorAllele;
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


	// getters and setters

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
