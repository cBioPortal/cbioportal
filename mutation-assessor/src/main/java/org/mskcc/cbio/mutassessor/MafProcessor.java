package org.mskcc.cbio.mutassessor;

import org.mskcc.cbio.maf.MafRecord;
import org.mskcc.cbio.maf.MafUtil;

import java.io.*;
import java.sql.SQLException;
import java.util.HashMap;

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

		// check if the file already oncotated
		// (assuming if it has the first oncotator column, then it is oncotated)
		boolean oncotated = (util.getOncoVariantClassificationIndex() != -1);

		// identify MA columns
		HashMap<Integer, String> maIndices = this.getMaIndices(line);

		// remove not needed MA columns from the header
		line = this.removeMaColumns(line, maIndices);

		String newColumns = this.getNewMaColumns(maImpact,
			maProteinChange,
			maLinkMsa,
			maLinkPdb);

		// add new MA columns if necessary
		line = addNewMaColumns(line,
			newColumns,
			oncotated);

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

			MafRecord mafRecord = util.parseRecord(line);
			String key = generateKey(mafRecord);

			MutationAssessorRecord maRecord = dao.get(key);

			String[] parts = line.split("\t", -1);
			line = "";

			for (int i = 0; i < parts.length; i++)
			{
				if (oncotated &&
				    i == util.getOncoVariantClassificationIndex())
				{
					// add all required MA values just before the oncotator columns
					line += this.getNewMaData(maRecord,
						maImpact,
						maProteinChange,
						maLinkMsa,
						maLinkPdb);

					// also add the onctotator column
					line += parts[i];
				}
				// overwrite existing MA columns if MA data available
				else if (maRecord != null)
				{
					// TODO keep the old value if the data is "NA" or overwrite in any case?

					if (i == util.getMaFImpactIndex())
					{
						line += maRecord.getImpact();
					}
					else if (i == util.getMaProteinChangeIndex())
					{
						line += maRecord.getProteinChange();
					}
					else if (i == util.getMaLinkMsaIndex())
					{
						line += maRecord.getAlignmentLink();
					}
					else if (i == util.getMaLinkPdbIndex())
					{
						line += maRecord.getStructureLink();
					}
					// skip MA columns that are not needed anymore
					// (do not add if the column starts with "MA:")
					else if (maIndices.get(i) == null)
					{
						line += parts[i];
					}
				}
				// skip MA columns that are not needed anymore
				// (do not add if the column starts with "MA:")
				else if (maIndices.get(i) == null)
				{
					line += parts[i];
				}

				// add a TAB as a delimiter (except last column)
				if (maIndices.get(i) == null &&
				    i < parts.length - 1)
				{
					line += "\t";
				}
			}

			// if the file is not oncotated,
			// then append new MA data at the end of the row
			if(!oncotated)
			{
				String maData = this.getNewMaData(maRecord,
					maImpact,
					maProteinChange,
					maLinkMsa,
					maLinkPdb);

				// remove last tab, since columns will be added
				// at the end of the line
				if (maData.endsWith("\t"))
				{
					maData = maData.substring(0,
						maData.length() - 1);
				}

				line += "\t" + maData;
			}

			writer.write(line);
			writer.newLine();
		}

		reader.close();
		writer.close();
	}

	/**
	 * Add new Mutation Assessor columns to the MAF header line.
	 * @param headerline    MAF header line
	 * @param columnNames   new MA column names
	 * @param oncotated     indicated if the MAF is already oncotated
	 * @return              header line with new columns added
	 */
	private String addNewMaColumns(String headerline,
			String columnNames,
			boolean oncotated)
	{
		// if the file is already oncotated insert new column names
		// just before the oncotator columns
		if (oncotated)
		{
			// this is required to get the correct insertion index
			// for the new header line
			MafUtil util = new MafUtil(headerline);
			int insertionIndex = util.getOncoVariantClassificationIndex();

			// split and reconstruct the line with new headers
			String[] parts = headerline.split("\t", -1);
			headerline = "";

			for (int i = 0; i < parts.length; i++)
			{
				if (i == insertionIndex)
				{
					headerline += columnNames + "\t" + parts[i];
				}
				else
				{
					headerline += parts[i];
				}

				if (i < parts.length - 1)
				{
					headerline += "\t";
				}
			}
		}
		// append columns to the end of the file
		else
		{
			headerline += "\t" + columnNames;
		}

		return headerline;
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
	 * @return                  string representation of the new Mutation Assessor data
	 */
	private String getNewMaData(MutationAssessorRecord maRecord,
			boolean maImpact,
			boolean maProteinChange,
			boolean maLinkMsa,
			boolean maLinkPdb)
	{
		String maData = "";

		// get data from record if it is not null
		if (maRecord != null)
		{
			if (!maImpact)
			{
				maData += maRecord.getImpact() + "\t";
			}

			if (!maProteinChange)
			{
				maData += maRecord.getProteinChange() + "\t";
			}

			if (!maLinkMsa)
			{
				maData += maRecord.getAlignmentLink() + "\t";
			}

			if (!maLinkPdb)
			{
				maData += maRecord.getStructureLink() + "\t";
			}
		}
		// just insert TABs
		else
		{
			if (!maImpact)
			{
				maData += "\t";
			}

			if (!maProteinChange)
			{
				maData += "\t";
			}

			if (!maLinkMsa)
			{
				maData += "\t";
			}

			if (!maLinkPdb)
			{
				maData += "\t";
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
	 * @return                  new column names separated by TABs.
	 */
	private String getNewMaColumns(boolean maImpact,
			boolean maProteinChange,
			boolean maLinkMsa,
			boolean maLinkPdb)
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
