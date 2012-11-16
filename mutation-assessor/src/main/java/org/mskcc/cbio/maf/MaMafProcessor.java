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

import org.mskcc.cbio.mutassessor.MutationAssessorRecord;

import java.util.List;

/**
 * MAF Processor specific to the Mutation Assessor tool.
 *
 * @author Selcuk Onur Sumer
 */
public class MaMafProcessor extends MafProcessor
{
	public MaMafProcessor(String headerLine)
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
			// never add missing oncotator columns
			this.addColumnToHeader(headerData, header, false);
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
			// always add missing MA columns
			this.addColumnToHeader(headerData, header, true);
		}
	}

	/**
	 * Adds all existing column names into the header list except
	 * not needed MA columns.
	 *
	 * @param headerData    list of header names
	 */
	protected void addExistingColsToHeader(List<String> headerData)
	{
		// iterate over the header line and add all columns
		// except the ones starting with "MA:"
		for (String header : this.headerLine.split(TAB))
		{
			if (header.startsWith("MA:"))
			{
				// add only known MA columns,
				// discard all other columns starting with "MA:"
				if(this.maHeaders.contains(header))
				{
					headerData.add(header);
				}
			}
			// add all non MA columns
			else
			{
				headerData.add(header);
			}
		}
	}

	/**
	 * Adds new nutation assessor column names into the header list.
	 *
	 * @param headerData    list of header names
	 */
	protected void addNewColsToHeader(List<String> headerData)
	{
		for (String maHeader : this.maHeaders)
		{
			if (!headerData.contains(maHeader))
			{
				// add missing oncotator headers
				headerData.add(maHeader);
			}
		}
	}

	/**
	 * Adds custom column names into the header list
	 * except unknown MA column names.
	 *
	 * @param headerData    list of header names
	 */
	protected void addOtherColsToHeader(List<String> headerData)
	{
		String[] parts = this.headerLine.split(TAB);

		for (String header : parts)
		{
			boolean unknownMaCol = header.startsWith("MA:") &&
				!this.maHeaders.contains(header);

			if (this.isCustomHeader(header) &&
			    !unknownMaCol)
			{
				headerData.add(header);
			}
		}
	}

	/**
	 * Updates the data list by adding mutation assessor data by using
	 * the given mutation assessor record.
	 *
	 * @param data      data representing a single line in a MAF
	 * @param maRecord  record containing the mutation assessor data
	 */
	public void updateMaData(List<String> data,
			MutationAssessorRecord maRecord)
	{
		if (maRecord == null)
		{
			maRecord = new MutationAssessorRecord("NA");
		}

		// create a new maf util for the new header line to get new oncotator indices
		String newHeaderLine = this.newHeaderLineAsString();
		MafUtil mafUtil = new MafUtil(newHeaderLine);

		String impact = maRecord.getImpact();
		String proteinChange = maRecord.getProteinChange();
		String msa = maRecord.getAlignmentLink();
		String pdb = maRecord.getStructureLink();
		String var = generateLinkVar(maRecord.getKey());

		// update mutation assessor values
		data.set(mafUtil.getMaFImpactIndex(), impact);
		data.set(mafUtil.getMaProteinChangeIndex(), proteinChange);
		data.set(mafUtil.getMaLinkMsaIndex(), msa);
		data.set(mafUtil.getMaLinkPdbIndex(), pdb);
		data.set(mafUtil.getMaLinkVarIndex(), var);
	}

	/**
	 * Generates the link to the mutation assessor site for the given key.
	 *
	 * @param key   cache key for an MA record
	 * @return      link to MA site for the given cache key
	 */
	protected String generateLinkVar(String key)
	{
		String linkHeader = "getma.org/?cm=var&var=hg19,";

		String[] parts = key.split("_");

		if (parts.length < 5)
		{
			return null;
		}

		String chr = parts[0];
		String startPos = parts[1];
		String refAllele = parts[3];
		String tumAllele = parts[4];

		return linkHeader +
		       chr + "," +
		       startPos + "," +
		       refAllele + "," + tumAllele +
		       "&fts=all";
	}
}
