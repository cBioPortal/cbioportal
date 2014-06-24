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

import java.util.HashMap;

/**
 * Utility Class for Parsing Fusion Files.
 *
 * This utility class handles variable columns and column orderings
 * within Fusion files.
 *
 * @author Selcuk Onur Sumer
 */
public class FusionFileUtil
{
	public static final String FUSION = "Fusion";
	public static final String DNA_SUPPORT = "DNA support";
	public static final String RNA_SUPPORT = "RNA support";
	public static final String METHOD = "Method";
	public static final String FRAME = "Frame";

	// number of headers in the header line
	private int headerCount;

	// mapping for all column names (both standard and custom columns)
	private HashMap<String, Integer> columnIndexMap;

	/**
	 * Constructor.
	 *
	 * @param headerLine    Header Line.
	 */
	public FusionFileUtil(String headerLine)
	{
		// init column index map
		this.columnIndexMap = new HashMap<String, Integer>();

		// split header names
		String parts[] = headerLine.split("\t");

		// update header count
		this.headerCount = parts.length;

		// find required header indices
		for (int i=0; i<parts.length; i++)
		{
			String header = parts[i];

			// put the index to the map
			this.columnIndexMap.put(header.toLowerCase(), i);
		}
	}

	public FusionRecord parseRecord(String line)
	{
		String parts[] = line.split("\t", -1);

		FusionRecord record = new FusionRecord();

		record.setHugoGeneSymbol(TabDelimitedFileUtil.getPartString(this.getColumnIndex(MafUtil.HUGO_SYMBOL), parts));
		record.setEntrezGeneId(TabDelimitedFileUtil.getPartLong(this.getColumnIndex(MafUtil.ENTREZ_GENE_ID), parts));
		record.setCenter(TabDelimitedFileUtil.getPartString(this.getColumnIndex(MafUtil.CENTER), parts));
		record.setTumorSampleID(TabDelimitedFileUtil.getPartString(this.getColumnIndex(MafUtil.TUMOR_SAMPLE_BARCODE), parts));
		record.setFusion(TabDelimitedFileUtil.getPartString(this.getColumnIndex(FUSION), parts));
		record.setDnaSupport(TabDelimitedFileUtil.getPartString(this.getColumnIndex(DNA_SUPPORT), parts));
		record.setRnaSupport(TabDelimitedFileUtil.getPartString(this.getColumnIndex(RNA_SUPPORT), parts));
		record.setMethod(TabDelimitedFileUtil.getPartString(this.getColumnIndex(METHOD), parts));
		record.setFrame(TabDelimitedFileUtil.getPartString(this.getColumnIndex(FRAME), parts));

		return record;
	}

	// TODO this is a duplicate (MafUtil has the same method)
	// try to factor out it into TabDelimitedFileUtil
	public int getColumnIndex(String colName)
	{
		Integer index = this.columnIndexMap.get(colName.toLowerCase());

		if (index == null)
		{
			index = -1;
		}

		return index;
	}
}
