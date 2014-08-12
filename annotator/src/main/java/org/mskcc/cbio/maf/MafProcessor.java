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

package org.mskcc.cbio.maf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helps processing and reordering of MAF file content. Designed
 * as a base MAF processing class for Oncotator and Mutation Assessor
 * tools.
 *
 * @author Selcuk Onur Sumer
 */
public class MafProcessor
{
	public static final String TAB = "\t";

	protected String headerLine;
	protected MafUtil mafUtil;
	protected List<String> standardHeaders;
	protected List<String> oncoHeaders;
	protected List<String> maHeaders;
	protected List<String> newHeaders;

	public MafProcessor(String headerLine)
	{
		this.headerLine = headerLine.trim();
		this.mafUtil = new MafUtil(this.headerLine);
		this.standardHeaders = this.initStandardHeaderList();
		this.oncoHeaders = this.initOncoHeaderList();
		this.maHeaders = this.initMaHeaderList();
	}

	/**
	 * Creates a new header list with sorted and complete standard columns.
	 *
	 * @return  a sorted list of header columns
	 */
	public List<String> newHeaderList()
	{
		return this.newHeaderList(true, true);
	}

	/**
	 * Creates a new header list. Sorting and adding missing information
	 * is optional.
	 *
	 * @param sorted        indicates whether sort the standard cols
	 * @param addMissing    indicates whether add all standard cols
	 * @return              a new header list
	 */
	public List<String> newHeaderList(boolean sorted, boolean addMissing)
	{
		List<String> headerData = new ArrayList<String>();

		// TODO allow adding missing columns without sorting? Does that make sense?

		if (sorted)
		{
			// add standard columns
			this.addStandardColsToHeader(headerData, addMissing);

			// add oncotator columns
			this.addOncoColsToHeader(headerData, addMissing);

			// add MA columns
			this.addMaColsToHeader(headerData, addMissing);

			// add all other (custom) columns
			this.addOtherColsToHeader(headerData);
		}
		else
		{
			// add existing columns to the header data
			this.addExistingColsToHeader(headerData);

			// append new columns to the header data
			this.addNewColsToHeader(headerData);
		}

		// store the sorted header data for future use
		this.newHeaders = headerData;
		return headerData;
	}

	/**
	 * Adds a new column name into the given header list. If addMissing flag
	 * is set, then the column name will be added in any case.
	 *
	 * @param headerData    list to add the column name
	 * @param header        name of the column
	 * @param addMissing    indicates if the column will be added in any case
	 */
	protected void addColumnToHeader(List<String> headerData,
			String header,
			boolean addMissing)
	{
		if (addMissing ||
		    this.mafUtil.getColumnIndex(header) != -1)
		{
			headerData.add(header);
		}
	}

	/**
	 * Adds standard MAF column names into the given header list.
	 *
	 * @param headerData    list of header names
	 * @param addMissing    indicates if the missing columns will also be added
	 */
	protected void addStandardColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.standardHeaders)
		{
			this.addColumnToHeader(headerData, header, addMissing);
		}
	}

	/**
	 * Adds oncotator column names into the given header list.
	 *
	 * @param headerData    list of header names
	 * @param addMissing    indicates if the missing columns will also be added
	 */
	protected void addOncoColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.oncoHeaders)
		{
			this.addColumnToHeader(headerData, header, addMissing);
		}
	}

	/**
	 * Adds MA column names into the given header list.
	 *
	 * @param headerData    list of header names
	 * @param addMissing    indicates if the missing columns will also be added
	 */
	protected void addMaColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.maHeaders)
		{
			this.addColumnToHeader(headerData, header, addMissing);
		}
	}

	/**
	 * Adds other (custom) column names into the header list.
	 * @param headerData    list of header names
	 */
	protected void addOtherColsToHeader(List<String> headerData)
	{
		String[] parts = this.headerLine.split(TAB);

		for (String header : parts)
		{
			if (this.isCustomHeader(header))
			{
				headerData.add(header);
			}
		}
	}

	/**
	 * Adds all existing column names into the header list.
	 *
	 * @param headerData    list of header names
	 */
	protected void addExistingColsToHeader(List<String> headerData)
	{
		// add all existing headers
		Collections.addAll(headerData, this.headerLine.split(TAB));
	}

	/**
	 * Adds new column names into the header list. This method
	 * should be overwritten in child classes.
	 *
	 * @param headerData    list of header names
	 */
	protected void addNewColsToHeader(List<String> headerData)
	{
		// empty method body
		// override to add application specific columns
	}

	/**
	 * Checks if the header is a custom header or not.
	 *
	 * @param header    name of the header
	 * @return          true if the header is custom, false otherwise
	 */
	protected boolean isCustomHeader(String header)
	{
		for (String knownHeader : this.standardHeaders)
		{
			if (header.equalsIgnoreCase(knownHeader))
			{
				return false;
			}
		}

		for (String knownHeader : this.oncoHeaders)
		{
			if (header.equalsIgnoreCase(knownHeader))
			{
				return false;
			}
		}

		for (String knownHeader : this.maHeaders)
		{
			if (header.equalsIgnoreCase(knownHeader))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Creates a new data list matching the order of the new header list
	 * by parsing the given data line.
	 *
	 * @param dataLine  a single line of a MAF file
	 * @return          list of data representing a single line
	 */
	public List<String> newDataList(String dataLine)
	{
		// adjust data line for consistency with the header
		dataLine = TabDelimitedFileUtil.adjustDataLine(dataLine,
			this.mafUtil.getHeaderCount());

		String[] parts = dataLine.split(TAB, -1);
		List<String> dataList = new ArrayList<String>();


		// re-order the parts using the new header data
		for (String header : this.newHeaders)
		{
			int index = this.mafUtil.getColumnIndex(header);

			if (index != -1)
			{
				dataList.add(parts[index]);
			}
			else
			{
				// TODO add "NA" or null instead of an empty string?
				dataList.add("");
			}
		}

		return dataList;
	}

	/**
	 * Initializes standard MAF columns list.
	 * The order of the elements in this list is directly related
	 * to the order of the oncotator columns in the output MAF file.
	 *
	 * @return  a list of standard column names
	 */
	protected List<String> initStandardHeaderList()
	{
		List<String> headers = new ArrayList<String>();

		// standard MAF columns
		headers.add(MafUtil.HUGO_SYMBOL);
		headers.add(MafUtil.ENTREZ_GENE_ID);
		headers.add(MafUtil.CENTER);
		headers.add(MafUtil.NCBI_BUILD);
		headers.add(MafUtil.CHROMOSOME);
		headers.add(MafUtil.START_POSITION);
		headers.add(MafUtil.END_POSITION);
		headers.add(MafUtil.STRAND);
		headers.add(MafUtil.VARIANT_CLASSIFICATION);
		headers.add(MafUtil.VARIANT_TYPE);
		headers.add(MafUtil.REFERENCE_ALLELE);
		headers.add(MafUtil.TUMOR_SEQ_ALLELE_1);
		headers.add(MafUtil.TUMOR_SEQ_ALLELE_2);
		headers.add(MafUtil.DBSNP_RS);
		headers.add(MafUtil.DBSNP_VAL_STATUS);
		headers.add(MafUtil.TUMOR_SAMPLE_BARCODE);
		headers.add(MafUtil.MATCHED_NORM_SAMPLE_BARCODE);
		headers.add(MafUtil.MATCH_NORM_SEQ_ALLELE1);
		headers.add(MafUtil.MATCH_NORM_SEQ_ALLELE2);
		headers.add(MafUtil.TUMOR_VALIDATION_ALLELE1);
		headers.add(MafUtil.TUMOR_VALIDATION_ALLELE2);
		headers.add(MafUtil.MATCH_NORM_VALIDATION_ALLELE1);
		headers.add(MafUtil.MATCH_NORM_VALIDATION_ALLELE2);
		headers.add(MafUtil.VERIFICATION_STATUS);
		headers.add(MafUtil.VALIDATION_STATUS);
		headers.add(MafUtil.MUTATION_STATUS);
		headers.add(MafUtil.SEQUENCING_PHASE);
		headers.add(MafUtil.SEQUENCE_SOURCE);
		headers.add(MafUtil.VALIDATION_METHOD);
		headers.add(MafUtil.SCORE);
		headers.add(MafUtil.BAM_FILE);
		headers.add(MafUtil.SEQUENCER);

		return headers;
	}


	/**
	 * Initializes the Oncotator header list.
	 * The order of the elements in this list is directly related
	 * to the order of the oncotator columns in the output MAF file.
	 *
	 * @return  a list of oncotator column names
	 */
	protected List<String> initOncoHeaderList()
	{
		List<String> headers = new ArrayList<String>();

		// Oncotator columns

		headers.add(MafUtil.ONCOTATOR_COSMIC_OVERLAPPING);
		headers.add(MafUtil.ONCOTATOR_DBSNP_RS);
		headers.add(MafUtil.ONCOTATOR_DBSNP_VAL_STATUS);

		headers.add(MafUtil.ONCOTATOR_VARIANT_CLASSIFICATION);
		headers.add(MafUtil.ONCOTATOR_PROTEIN_CHANGE);
		headers.add(MafUtil.ONCOTATOR_GENE_SYMBOL);
		headers.add(MafUtil.ONCOTATOR_REFSEQ_MRNA_ID);
		headers.add(MafUtil.ONCOTATOR_REFSEQ_PROT_ID);
		headers.add(MafUtil.ONCOTATOR_UNIPROT_ENTRY_NAME);
		headers.add(MafUtil.ONCOTATOR_UNIPROT_ACCESSION);
		headers.add(MafUtil.ONCOTATOR_CODON_CHANGE);
		headers.add(MafUtil.ONCOTATOR_TRANSCRIPT_CHANGE);
		headers.add(MafUtil.ONCOTATOR_EXON_AFFECTED);
		headers.add(MafUtil.ONCOTATOR_PROTEIN_POS_START);
		headers.add(MafUtil.ONCOTATOR_PROTEIN_POS_END);

		headers.add(MafUtil.ONCOTATOR_VARIANT_CLASSIFICATION_BE);
		headers.add(MafUtil.ONCOTATOR_PROTEIN_CHANGE_BE);
		headers.add(MafUtil.ONCOTATOR_GENE_SYMBOL_BE);
		headers.add(MafUtil.ONCOTATOR_REFSEQ_MRNA_ID_BE);
		headers.add(MafUtil.ONCOTATOR_REFSEQ_PROT_ID_BE);
		headers.add(MafUtil.ONCOTATOR_UNIPROT_ENTRY_NAME_BE);
		headers.add(MafUtil.ONCOTATOR_UNIPROT_ACCESSION_BE);
		headers.add(MafUtil.ONCOTATOR_CODON_CHANGE_BE);
		headers.add(MafUtil.ONCOTATOR_TRANSCRIPT_CHANGE_BE);
		headers.add(MafUtil.ONCOTATOR_EXON_AFFECTED_BE);
		headers.add(MafUtil.ONCOTATOR_PROTEIN_POS_START_BE);
		headers.add(MafUtil.ONCOTATOR_PROTEIN_POS_END_BE);

		return headers;
	}

	/**
	 * Initializes the Mutation Assessor header list.
	 * The order of the elements in this list is directly related
	 * to the order of the MA columns in the output MAF file.
	 *
	 * @return  a list of MA column names
	 */
	protected List<String> initMaHeaderList()
	{
		List<String> headers = new ArrayList<String>();

		// Mutation Assessor columns
		headers.add(MafUtil.MA_FIMPACT);
		headers.add(MafUtil.MA_FIS);
		headers.add(MafUtil.MA_PROTEIN_CHANGE);
		headers.add(MafUtil.MA_LINK_MSA);
		headers.add(MafUtil.MA_LINK_PDB);
		headers.add(MafUtil.MA_LINK_VAR);

		return headers;
	}

	/**
	 * String representation of the new header line.
	 *
	 * @return  header line as single string
	 */
	protected String newHeaderLineAsString()
	{
		String newHeaderLine = "";

		for (String header : this.newHeaders)
		{
			newHeaderLine += header + TAB;
		}

		return newHeaderLine.trim();
	}
}
