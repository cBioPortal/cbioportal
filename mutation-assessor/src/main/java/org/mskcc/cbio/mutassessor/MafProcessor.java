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

package org.mskcc.cbio.mutassessor;

import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adds or replaces Mutation Assessor columns to MAFs.
 */
public class MafProcessor
{
	// Mutation Assessor column names
	public static final String MA_F_IMPACT = "MA:FImpact";
	public static final String MA_LINK_VAR = "MA:link.var";
	public static final String MA_LINK_MSA = "MA:link.MSA";
	public static final String MA_LINK_PDB = "MA:link.PDB";
	public static final String MA_PROTEIN_CHANGE = "MA:protein.change";


	/**
	 * Adds mutation assessor information to the given input MAF
	 * by using the information in the mutation assessor cache.
	 *
	 * @param inputMaf      input MAF to process
	 * @param outputMaf     output MAF to create
	 * @throws IOException
	 * @throws SQLException
	 */
	public void addMutAssessorInfo(File inputMaf,
			File outputMaf) throws IOException, SQLException
	{
		DaoMutAssessorCache dao = DaoMutAssessorCache.getInstance();

		BufferedReader reader = new BufferedReader(new FileReader(inputMaf));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputMaf));

		// process header line
		String line = reader.readLine();
		MafUtil util = new MafUtil(line);

		// determine if a required MA columns already exist
		boolean maImpact = (util.getMaFImpactIndex() != -1);
		boolean maProteinChange = (util.getMaProteinChangeIndex() != -1);
		boolean maLinkMsa = (util.getMaLinkMsaIndex() != -1);
		boolean maLinkPdb = (util.getMaLinkPdbIndex() != -1);
		boolean maLinkVar = (util.getMaLinkVarIndex() != -1);

		// check if the file is already oncotated, insertion index will be
		// a negative number if it is not oncotated
		Integer insertionIndex = this.findInsertionIndex(util);

		// identify MA columns
		HashMap<Integer, String> maIndices = this.getMaIndices(line);

		// remove not needed MA columns from the header
		line = this.removeMaColumns(line, maIndices);

		String newColumns = this.getNewMaColumns(maImpact,
			maProteinChange,
			maLinkMsa,
			maLinkPdb,
			maLinkVar);

		// add new MA columns if necessary
		line = addNewMaColumns(line,
			newColumns,
			insertionIndex);

		// write new header
		writer.write(line);
		writer.newLine();

		// process each data line
		while ((line = reader.readLine()) != null)
		{
			// skip empty lines
			if (line.trim().length() == 0)
			{
				continue;
			}

			line = util.adjustDataLine(line);
			MafRecord mafRecord = util.parseRecord(line);
			String key = generateKey(mafRecord);

			MutationAssessorRecord maRecord = dao.get(key);

			String[] parts = line.split("\t", -1);
			List<String> data = new ArrayList<String>();

			for (int i = 0; i < parts.length; i++)
			{
				// if oncotated insert before a specific index
				if (i == insertionIndex)
				{
					// add all required MA values just before the oncotator columns
					data.addAll(this.getNewMaData(
							maRecord, maImpact, maProteinChange,
							maLinkMsa, maLinkPdb, maLinkVar));

					// also add the onctotator column
					data.add(parts[i]);
				}
				// overwrite existing MA columns if MA data available
				else if (maRecord != null)
				{
					// no need to keep the old value even if the data is "NA",
					// so overwrite in any case

					if (i == util.getMaFImpactIndex())
					{
						data.add(maRecord.getImpact());
					}
					else if (i == util.getMaProteinChangeIndex())
					{
						data.add(maRecord.getProteinChange());
					}
					else if (i == util.getMaLinkMsaIndex())
					{
						data.add(maRecord.getAlignmentLink());
					}
					else if (i == util.getMaLinkPdbIndex())
					{
						data.add(maRecord.getStructureLink());
					}
					else if (i == util.getMaLinkVarIndex())
					{
						data.add(generateLinkVar(maRecord.getKey()));
					}
					// skip MA columns that are not needed anymore
					// (do not add if the column starts with "MA:")
					else if (maIndices.get(i) == null)
					{
						data.add(parts[i]);
					}
				}
				// skip MA columns that are not needed anymore
				// (do not add if the column starts with "MA:")
				else if (maIndices.get(i) == null)
				{
					data.add(parts[i]);
				}
			}

			// if the file is not oncotated,
			// then append new MA data at the end of the row
			if(insertionIndex < 0)
			{
				data.addAll(this.getNewMaData(maRecord, maImpact, maProteinChange, maLinkMsa, maLinkPdb, maLinkVar));
			}

			// reconstruct the line by using the collected data
			line = "";

			for (String col : data)
			{
				line += col + "\t";
			}

			// remove last tab & output the new line
			writer.write(line.substring(0, line.length() - 1));
			writer.newLine();
		}

		reader.close();
		writer.close();
	}

	/**
	 * Adds new Mutation Assessor columns to the MAF header line.
	 *
	 * @param headerLine        MAF header line
	 * @param columnNames       new MA column names
	 * @param insertionIndex    insertion index (<0 if not oncotated)
	 * @return                  header line with new columns added
	 */
	private String addNewMaColumns(String headerLine,
			String columnNames,
			int insertionIndex)
	{
		// check if nothing to add
		if (columnNames == null ||
			columnNames.length() == 0)
		{
			return headerLine;
		}

		// if the file is already oncotated insert new column names
		// just before the oncotator columns
		if (insertionIndex >= 0)
		{
			// this is required to get the correct insertion index
			// for the new header line
			MafUtil util = new MafUtil(headerLine);

			// split and reconstruct the line with new headers
			String[] parts = headerLine.split("\t");
			headerLine = "";

			for (int i = 0; i < parts.length; i++)
			{
				if (i == insertionIndex)
				{
					headerLine += columnNames + "\t" + parts[i];
				}
				else
				{
					headerLine += parts[i];
				}

				if (i < parts.length - 1)
				{
					headerLine += "\t";
				}
			}
		}
		// append columns to the end of the file
		else
		{
			headerLine = headerLine.trim() + "\t" + columnNames;
		}

		return headerLine;
	}

	/**
	 * Gets the new Mutation Assessor data from the given Mutation Assessor record.
	 * It only gets data for the new columns. The returned string will contain a TAB
	 * at the end if not all of the columns already exist, if all exist then the
	 * returned string will be empty.
	 *
	 * @param maRecord          Mutation Assessor record containing data
	 * @param maImpact          indicates if functional impact column already exists
	 * @param maProteinChange   indicates if protein change column already exists
	 * @param maLinkMsa         indicates if MSA link column already exists
	 * @param maLinkPdb         indicates if PDB link column already exists
	 * @param maLinkVar         indicates if var link column already exists
	 * @return                  string representation of the new Mutation Assessor data
	 */
	private List<String> getNewMaData(MutationAssessorRecord maRecord,
			boolean maImpact,
			boolean maProteinChange,
			boolean maLinkMsa,
			boolean maLinkPdb,
			boolean maLinkVar)
	{
		List<String> maData = new ArrayList<String>();

		// get data from record if it is not null
		if (maRecord != null)
		{
			if (!maImpact)
			{
				maData.add(maRecord.getImpact());
			}

			if (!maProteinChange)
			{
				maData.add(maRecord.getProteinChange());
			}

			if (!maLinkMsa)
			{
				maData.add(maRecord.getAlignmentLink());
			}

			if (!maLinkPdb)
			{
				maData.add(maRecord.getStructureLink());
			}

			if (!maLinkVar)
			{
				maData.add(generateLinkVar(maRecord.getKey()));
			}
		}
		// just insert 'NA's
		else
		{
			if (!maImpact)
			{
				maData.add(MafRecord.NA_STRING);
			}

			if (!maProteinChange)
			{
				maData.add(MafRecord.NA_STRING);
			}

			if (!maLinkMsa)
			{
				maData.add(MafRecord.NA_STRING);
			}

			if (!maLinkPdb)
			{
				maData.add(MafRecord.NA_STRING);
			}

			if (!maLinkVar)
			{
				maData.add(MafRecord.NA_STRING);
			}
		}

		return maData;
	}

	/**
	 * Gets the new Mutation Assessor column names.
	 *
	 * @param maImpact          indicates if functional impact column already exists
	 * @param maProteinChange   indicates if protein change column already exists
	 * @param maLinkMsa         indicates if MSA link column already exists
	 * @param maLinkPdb         indicates if PDB link column already exists
	 * @param maLinkVar         indicates if var link column already exists
	 * @return                  new column names separated by TABs.
	 */
	private String getNewMaColumns(boolean maImpact,
			boolean maProteinChange,
			boolean maLinkMsa,
			boolean maLinkPdb,
			boolean maLinkVar)
	{
		String cols = "";

		if (!maImpact)
		{
			cols += MA_F_IMPACT + "\t";
		}

		if (!maProteinChange)
		{
			cols += MA_PROTEIN_CHANGE + "\t";
		}

		if (!maLinkMsa)
		{
			cols += MA_LINK_MSA + "\t";
		}

		if (!maLinkPdb)
		{
			cols += MA_LINK_PDB + "\t";
		}

		if (!maLinkVar)
		{
			cols += MA_LINK_VAR + "\t";
		}

		// remove trailing tab (if any)
		if (cols.endsWith("\t"))
		{
			cols = cols.substring(0, cols.length() - 1);
		}

		return cols;
	}

	/**
	 * Creates a map for the existing mutation assessor column names.
	 * The map is keyed on the column index (integer), and the corresponding
	 * value is the column name (string).
	 *
	 * @param headerLine    header line of the MAF file
	 * @return              a map for already existing column names
	 */
	private HashMap<Integer, String> getMaIndices(String headerLine)
	{
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		String[] parts = headerLine.split("\t");

		for (int i = 0; i < parts.length; i++)
		{
			String header = parts[i].toLowerCase();
			if (header.startsWith("ma:") &&
				!this.isRequiredMaColumn(header))
			{
				map.put(i, parts[i].toLowerCase());
			}
		}

		return map;
	}

	/**
	 * Remove unnecessary/unused column names from the header line.
	 *
	 * @param headerLine    header line to be cleaned
	 * @param maIndices     map for already existing MA columns
	 * @return              new header line with unused columns removed
	 */
	private String removeMaColumns(String headerLine, HashMap<Integer, String> maIndices)
	{
		String[] parts = headerLine.split("\t");
		String newHeader = "";

		for (int i = 0; i < parts.length; i++)
		{
			// add only if it is not in the map
			if (maIndices.get(i) == null)
			{
				newHeader += parts[i];

				if (i < parts.length - 1)
				{
					newHeader += "\t";
				}
			}
		}

		return newHeader;
	}

	/**
	 * Determines if the given header (column) name is a required MA column
	 * or not.
	 *
	 * @param header    header (column) name
	 * @return          true if required, false otherwise
	 */
	private boolean isRequiredMaColumn(String header)
	{
		return header.equalsIgnoreCase(MA_F_IMPACT) ||
		   header.equalsIgnoreCase(MA_LINK_VAR) ||
		   header.equalsIgnoreCase(MA_LINK_MSA) ||
		   header.equalsIgnoreCase(MA_LINK_PDB) ||
		   header.equalsIgnoreCase(MA_PROTEIN_CHANGE);
	}

	private Integer findInsertionIndex(MafUtil util)
	{
		Integer min = Integer.MAX_VALUE;

		List<Integer> oncoIndices = new ArrayList<Integer>();

		oncoIndices.add(util.getOncoVariantClassificationIndex());
		oncoIndices.add(util.getOncoCosmicOverlappingIndex());
		oncoIndices.add(util.getOncoGeneSymbolIndex());
		oncoIndices.add(util.getOncoProteinChangeIndex());
		oncoIndices.add(util.getOncoDbSnpRsIndex());

		for (int i : oncoIndices)
		{
			if (i != -1 && i < min)
			{
				min = i;
			}
		}

		if (min == Integer.MAX_VALUE)
		{
			min = -1;
		}

		return min;
	}

	/**
	 * Generates the link to the mutation assessor site for the given key.
	 *
	 * @param key   cache key for an MA record
	 * @return      link to MA site for the given cache key
	 */
	public static String generateLinkVar(String key)
	{
		String linkHeader = "getma.org/?cm=var&var=hg19,";

		String[] parts = key.split("_");

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

	/**
	 * Generates a cache DB key for the given MAF record. The generated key
	 * is in the form of :
	 *   [chromosome]_[startPosition]_[endPosition]_[referenceAllele]_[tumorAllele]
	 *
	 * See also CacheBuilder.generateKey format.
	 *
	 * @param record    MAF record representing a single line in a MAF
	 * @return          cache key for the given record
	 */
	public static String generateKey(MafRecord record)
	{
		String chr = record.getChr();
		Long start = record.getStartPosition();
		Long end = record.getEndPosition();
		String refAllele = record.getReferenceAllele();
		String tumAllele = "";

		// take the tumor allele that is different from the reference allele
		if (!refAllele.equalsIgnoreCase(record.getTumorSeqAllele1()))
		{
			tumAllele = record.getTumorSeqAllele1();
		}
		else if (!refAllele.equalsIgnoreCase(record.getTumorSeqAllele2()))
		{
			tumAllele = record.getTumorSeqAllele2();
		}

		return chr + "_" + start + "_" + end + "_" + refAllele + "_" + tumAllele;
	}
}
