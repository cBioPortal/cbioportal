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

import java.util.List;

/**
 * MAF Processor specific to the Oncotator tool.
 *
 * @author Selcuk Onur Sumer
 */
public class OncoMafProcessor extends MafProcessor
{
	public OncoMafProcessor(String headerLine)
	{
		super(headerLine);
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
			// always add missing oncotator columns
			this.addColumnToHeader(headerData, header, true);
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
			// never add missing MA columns
			this.addColumnToHeader(headerData, header, false);
		}
	}

	/**
	 * Adds new oncotator column names into the header list.
	 *
	 * @param headerData    list of header names
	 */
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

	/**
	 * Updates the data list by adding oncotator data by using
	 * the given oncotator record.
	 *
	 * @param data              data representing a single line in a MAF
	 * @param oncotatorRecord   record containing the oncotator data
	 */
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
}
