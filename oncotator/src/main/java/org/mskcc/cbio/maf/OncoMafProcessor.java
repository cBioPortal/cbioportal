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

		String cosmicOverlapping =
				oncotatorRecord.getCosmicOverlappingMutations();
		String dbSnpRs =
				oncotatorRecord.getDbSnpRs();
		String dbSnpValStatus =
				oncotatorRecord.getDbSnpValStatus();

		String proteinChange =
				oncotatorRecord.getBestCanonicalTranscript().getProteinChange();
		String variantClassification =
				oncotatorRecord.getBestCanonicalTranscript().getVariantClassification();
		String geneSymbol =
				oncotatorRecord.getBestCanonicalTranscript().getGene();
		String refseqMrnaId =
				oncotatorRecord.getBestCanonicalTranscript().getRefseqMrnaId();
		String refseqProtId =
				oncotatorRecord.getBestCanonicalTranscript().getRefseqProtId();
		String uniprotName =
				oncotatorRecord.getBestCanonicalTranscript().getUniprotName();
		String uniprotAccession =
				oncotatorRecord.getBestCanonicalTranscript().getUniprotAccession();
		String codonChange =
				oncotatorRecord.getBestCanonicalTranscript().getCodonChange();
		String transcriptChange =
				oncotatorRecord.getBestCanonicalTranscript().getTranscriptChange();
		String exonAffected = (oncotatorRecord.getBestCanonicalTranscript().getExonAffected() == null) ?
				null : oncotatorRecord.getBestCanonicalTranscript().getExonAffected().toString();
		String proteinPosStart = (oncotatorRecord.getBestCanonicalTranscript().getProteinPosStart() == null) ?
				null : oncotatorRecord.getBestCanonicalTranscript().getProteinPosStart().toString();
		String proteinPosEnd = (oncotatorRecord.getBestCanonicalTranscript().getProteinPosEnd() == null) ?
				null : oncotatorRecord.getBestCanonicalTranscript().getProteinPosEnd().toString();

		String proteinChangeBe =
				oncotatorRecord.getBestEffectTranscript().getProteinChange();
		String variantClassificationBe =
				oncotatorRecord.getBestEffectTranscript().getVariantClassification();
		String geneSymbolBe =
				oncotatorRecord.getBestEffectTranscript().getGene();
		String refseqMrnaIdBe =
				oncotatorRecord.getBestEffectTranscript().getRefseqMrnaId();
		String refseqProtIdBe =
				oncotatorRecord.getBestEffectTranscript().getRefseqProtId();
		String uniprotNameBe =
				oncotatorRecord.getBestEffectTranscript().getUniprotName();
		String uniprotAccessionBe =
				oncotatorRecord.getBestEffectTranscript().getUniprotAccession();
		String codonChangeBe =
				oncotatorRecord.getBestEffectTranscript().getCodonChange();
		String transcriptChangeBe =
				oncotatorRecord.getBestEffectTranscript().getTranscriptChange();
		String exonAffectedBe =(oncotatorRecord.getBestEffectTranscript().getExonAffected() == null) ?
				null : oncotatorRecord.getBestEffectTranscript().getExonAffected().toString();
		String proteinPosStartBe = (oncotatorRecord.getBestEffectTranscript().getProteinPosStart() == null) ?
				null : oncotatorRecord.getBestEffectTranscript().getProteinPosStart().toString();
		String proteinPosEndBe = (oncotatorRecord.getBestEffectTranscript().getProteinPosEnd() == null) ?
				null : oncotatorRecord.getBestEffectTranscript().getProteinPosEnd().toString();

		// create a new maf util for the new header line to get new oncotator indices
		String newHeaderLine = this.newHeaderLineAsString();
		MafUtil mafUtil = new MafUtil(newHeaderLine);

		// update oncotator values
		data.set(mafUtil.getOncoCosmicOverlappingIndex(), cosmicOverlapping);
		data.set(mafUtil.getOncoDbSnpRsIndex(), dbSnpRs);
		data.set(mafUtil.getOncoDbSnpValStatusIndex(), dbSnpValStatus);

		data.set(mafUtil.getOncoProteinChangeIndex(), proteinChange);
		data.set(mafUtil.getOncoVariantClassificationIndex(), variantClassification);
		data.set(mafUtil.getOncoGeneSymbolIndex(), geneSymbol);
		data.set(mafUtil.getOncoRefseqMrnaIdIndex(), refseqMrnaId);
		data.set(mafUtil.getOncoRefseqProtIdIndex(), refseqProtId);
		data.set(mafUtil.getOncoUniprotNameIndex(), uniprotName);
		data.set(mafUtil.getOncoUniprotAccessionIndex(), uniprotAccession);
		data.set(mafUtil.getOncoCodonChangeIndex(), codonChange);
		data.set(mafUtil.getOncoTranscriptChangeIndex(), transcriptChange);
		data.set(mafUtil.getOncoExonAffectedIndex(), exonAffected);
		data.set(mafUtil.getOncoProteinPosStartIndex(), proteinPosStart);
		data.set(mafUtil.getOncoProteinPosEndIndex(), proteinPosEnd);

		data.set(mafUtil.getOncoProteinChangeBeIndex(), proteinChangeBe);
		data.set(mafUtil.getOncoVariantClassificationBeIndex(), variantClassificationBe);
		data.set(mafUtil.getOncoGeneSymbolBeIndex(), geneSymbolBe);
		data.set(mafUtil.getOncoRefseqMrnaIdBeIndex(), refseqMrnaIdBe);
		data.set(mafUtil.getOncoRefseqProtIdBeIndex(), refseqProtIdBe);
		data.set(mafUtil.getOncoUniprotNameBeIndex(), uniprotNameBe);
		data.set(mafUtil.getOncoUniprotAccessionBeIndex(), uniprotAccessionBe);
		data.set(mafUtil.getOncoCodonChangeBeIndex(), codonChangeBe);
		data.set(mafUtil.getOncoTranscriptChangeBeIndex(), transcriptChangeBe);
		data.set(mafUtil.getOncoExonAffectedBeIndex(), exonAffectedBe);
		data.set(mafUtil.getOncoProteinPosStartBeIndex(), proteinPosStartBe);
		data.set(mafUtil.getOncoProteinPosEndBeIndex(), proteinPosEndBe);
	}
}
