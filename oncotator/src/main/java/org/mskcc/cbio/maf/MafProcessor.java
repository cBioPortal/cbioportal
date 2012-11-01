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

package org.mskcc.cbio.maf;

import org.mskcc.cbio.oncotator.OncotatorRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Enables re ordering of MAF files as well as adding new oncotator columns.
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

	public List<String> newHeaderList()
	{
		return this.newHeaderList(true, true);
	}

	public List<String> newHeaderList(boolean sorted, boolean addMissing)
	{
		List<String> headerData = new ArrayList<String>();

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

	protected void addStandardColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.standardHeaders)
		{
			this.addColumnToHeader(headerData, header, addMissing);
		}
	}

	protected void addOncoColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.oncoHeaders)
		{
			// always add missing oncotator columns
			// overwrite, if necessary, to allow conditional adding
			this.addColumnToHeader(headerData, header, true);
		}
	}

	protected void addMaColsToHeader(List<String> headerData,
			boolean addMissing)
	{
		for (String header : this.maHeaders)
		{
			// never add missing MA columns
			// overwrite, if necessary, to allow conditional adding
			this.addColumnToHeader(headerData, header, false);
		}
	}

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

	protected void addExistingColsToHeader(List<String> headerData)
	{
		// add all existing headers
		Collections.addAll(headerData, this.headerLine.split(TAB));
	}

	protected void addNewColsToHeader(List<String> headerData)
	{
		for (String oncoHeader : this.oncoHeaders)
		{
			if (!headerData.contains(oncoHeader))
			{
				// add missing oncotator headers
				headerData.add(oncoHeader);
			}
		}
	}

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

	public List<String> newDataList(String dataLine)
	{
		// adjust data line for consistency with the header
		dataLine = this.mafUtil.adjustDataLine(dataLine);

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
				// TODO add "NA" instead?
				dataList.add("");
			}
		}

		return dataList;
	}

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

	protected List<String> initOncoHeaderList()
	{
		List<String> headers = new ArrayList<String>();

		// Oncotator columns
		headers.add(MafUtil.ONCOTATOR_VARIANT_CLASSIFICATION);
		headers.add(MafUtil.ONCOTATOR_PROTEIN_CHANGE);
		headers.add(MafUtil.ONCOTATOR_COSMIC_OVERLAPPING);
		headers.add(MafUtil.ONCOTATOR_DBSNP_RS);
		headers.add(MafUtil.ONCOTATOR_GENE_SYMBOL);

		return headers;
	}

	protected List<String> initMaHeaderList()
	{
		List<String> headers = new ArrayList<String>();

		// Mutation Assessor columns
		headers.add(MafUtil.MA_FIMPACT);
		headers.add(MafUtil.MA_PROTEIN_CHANGE);
		headers.add(MafUtil.MA_LINK_MSA);
		headers.add(MafUtil.MA_LINK_PDB);
		headers.add(MafUtil.MA_LINK_VAR);

		return headers;
	}

	public void updateOncotatorData(List<String> data,
		OncotatorRecord oncotatorRecord)
	{
		if (oncotatorRecord == null)
		{
			oncotatorRecord = new OncotatorRecord("NA");
		}

		String proteinChange =
				oncotatorRecord.getBestEffectTranscript().getProteinChange();
		String cosmicOverlapping =
				oncotatorRecord.getCosmicOverlappingMutations();
		String dbSnpRs =
				oncotatorRecord.getDbSnpRs();
		String variantClassification =
				oncotatorRecord.getBestEffectTranscript().getVariantClassification();
		String geneSymbol =
				oncotatorRecord.getBestEffectTranscript().getGene();

		// create a new maf util for the new header line to get new oncotator indices
		String newHeaderLine = this.newHeaderLineAsString();
		MafUtil mafUtil = new MafUtil(newHeaderLine);

		// update oncotator values
		data.set(mafUtil.getOncoProteinChangeIndex(), proteinChange);
		data.set(mafUtil.getOncoCosmicOverlappingIndex(), cosmicOverlapping);
		data.set(mafUtil.getOncoDbSnpRsIndex(), dbSnpRs);
		data.set(mafUtil.getOncoVariantClassificationIndex(), variantClassification);
		data.set(mafUtil.getOncoGeneSymbolIndex(), geneSymbol);
	}

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
