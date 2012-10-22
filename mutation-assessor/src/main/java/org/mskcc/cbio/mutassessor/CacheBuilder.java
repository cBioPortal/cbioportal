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

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Utility class to process MA files and create cache build on the same key structure
 * as the oncotator cache.
 */
public class CacheBuilder
{
	// Column header names in an MA file
	public static final String MA_VARIANT = "mutation";
	public static final String MA_FIMPACT = "func. impact";
	public static final String MA_PROTEIN_CHANGE = "uniprot variant";
	public static final String MA_LINK_MSA = "msa";
	public static final String MA_LINK_PDB = "pdb";

	/**
	 * Map for column header indices (to have flexibility for column positions in the file)
	 */
	protected HashMap<String, Integer> headerIndices;

	/**
	 * Optional output SQL script filename
	 */
	protected String sqlFilename;

	/**
	 * Default constructor with no sql script option.
	 */
	public CacheBuilder()
	{
		this.sqlFilename = null;
	}

	/**
	 * Constructor to allow SQL script creation instead of
	 * direct insertion into the DB.
	 *
	 * @param sqlFilename   output SQL script filename
	 */
	public CacheBuilder(String sqlFilename)
	{
		this.sqlFilename = sqlFilename;

		// also try to clean previous content
		try
		{
			FileWriter writer = new FileWriter(sqlFilename);
			writer.write("");
			writer.flush();
			writer.close();
		}
		catch (IOException e)
		{
			System.out.println("[warning] failed to initialize SQL script file: " +
			                   sqlFilename);
		}
	}

	/**
	 * Processes all files in a given directory (assuming that all files are MA files).
	 *
	 * @param inputDirectory    target directory containing input files
	 * @throws IOException
	 * @throws SQLException
	 */
	public void processDirectory(File inputDirectory)
			throws IOException, SQLException
	{
		File[] list;

		if (inputDirectory.isDirectory())
		{
			list = inputDirectory.listFiles();

			if (list != null)
			{
				for (File file : list)
				{
					if (!file.isDirectory())
					{
						this.processFile(file);
					}
				}
			}
		}
	}

	/**
	 * Processes a single MA file, and inserts a row into DB for each line.
	 *
	 * @param inputMA       input MA file to process
	 * @throws IOException
	 * @throws SQLException
	 */
	public void processFile(File inputMA) throws IOException, SQLException
	{
		DaoMutAssessorCache dao = DaoMutAssessorCache.getInstance();

		BufferedReader reader = new BufferedReader(new FileReader(inputMA));
		BufferedWriter writer = null;

		int bufferSize = 10000;
		List<String> valueBuffer = null;

		// conditionally init writer
		if (this.sqlFilename != null)
		{
			// initialize the writer in append mode
			writer = new BufferedWriter(
					new FileWriter(this.sqlFilename, true));

			// init value buffer
			valueBuffer = new ArrayList<String>(bufferSize);
		}

		// process header line
		String line = reader.readLine();
		this.headerIndices = this.buildIndexMap(line);

		// process each data line
		while ((line = reader.readLine()) != null)
		{
			// skip empty lines
			if (line.trim().length() == 0)
			{
				continue;
			}

			MutationAssessorRecord record = this.parseDataLine(line);

			if (!record.hasNoInfo())
			{
				// sql script filename is provided, output contents to the script
				if (writer != null)
				{
					// creating an SQL script file, instead of using slower JDBC
					// (using extended insert method to make insertion even faster)
					if (valueBuffer.size() < bufferSize)
					{
						// if buffer is not full just add values to the buffer
						valueBuffer.add(dao.getInsertValues(record));
					}
					else
					{
						// write an extended insert SQL line to the output
						writer.write(dao.getInsertHead());

						// add all values in the buffer
						this.writeBufferContent(writer, valueBuffer);

						// also clear the buffer
						valueBuffer.clear();
					}
				}
				// use slower JDBC option if no output filename provided
				else
				{
					dao.put(record);
				}
			}
			else
			{
				System.out.println("[warning] no MA information for " + record.getKey());
			}
		}

		reader.close();

		if (writer != null)
		{
			// make sure latest content of value buffer is written
			if (!valueBuffer.isEmpty())
			{
				writer.write(dao.getInsertHead());
				this.writeBufferContent(writer, valueBuffer);
			}

			// finally close the writer
			writer.close();
		}
	}

	/**
	 * Creates an SQL line for extended insert (insertion of multiple values).
	 *
	 * @param writer        output file writer
	 * @param valueBuffer   buffer containing multiple values of MA info
	 * @throws IOException
	 */
	private void writeBufferContent(BufferedWriter writer,
			List<String> valueBuffer) throws IOException
	{
		for (int i = 0; i < valueBuffer.size() ; i++)
		{
			writer.write("(" + valueBuffer.get(i) + ")");

			if (i < valueBuffer.size() - 1)
			{
				writer.write(",");
			}
		}

		writer.write(";");
		writer.newLine();
	}

	/**
	 * Creates a map of indices for the MA column names.
	 *
	 * @param headerLine    header line containing column names.
	 * @return              a map keyed on column names
	 */
	protected HashMap<String, Integer> buildIndexMap(String headerLine)
	{
		HashMap<String, Integer> headerIndices = new HashMap<String, Integer>();

		String[] parts = headerLine.split("\t");

		for (int i = 0; i < parts.length ; i++)
		{
			if (parts[i].equalsIgnoreCase(MA_VARIANT))
			{
				headerIndices.put(MA_VARIANT, i);
			}
			else if (parts[i].equalsIgnoreCase(MA_FIMPACT))
			{
				headerIndices.put(MA_FIMPACT, i);
			}
			else if (parts[i].equalsIgnoreCase(MA_PROTEIN_CHANGE))
			{
				headerIndices.put(MA_PROTEIN_CHANGE, i);
			}
			else if (parts[i].equalsIgnoreCase(MA_LINK_MSA))
			{
				headerIndices.put(MA_LINK_MSA, i);
			}
			else if (parts[i].equalsIgnoreCase(MA_LINK_PDB))
			{
				headerIndices.put(MA_LINK_PDB, i);
			}
		}

		return headerIndices;
	}

	/**
	 * Parses a data line and creates a MutationAssessorRecord
	 *
	 * @param dataLine data line containing MA values
	 * @return      a MutationAssessorRecord instance
	 */
	protected MutationAssessorRecord parseDataLine(String dataLine)
	{
		String[] parts = dataLine.split("\t", -1);

		String mutation = this.getPartString(this.getHeaderIndex(MA_VARIANT), parts);
		String key = this.generateKey(mutation);
		String impact = this.getPartString(this.getHeaderIndex(MA_FIMPACT), parts);
		String proteinChange = this.getPartString(this.getHeaderIndex(MA_PROTEIN_CHANGE), parts);
		String structureLink = this.getPartString(this.getHeaderIndex(MA_LINK_PDB), parts);
		String alignmentLink = this.getPartString(this.getHeaderIndex(MA_LINK_MSA), parts);

		MutationAssessorRecord record = new MutationAssessorRecord(key);
		record.setImpact(impact);
		record.setProteinChange(proteinChange);
		record.setStructureLink(structureLink);
		record.setAlignmentLink(alignmentLink);

		return record;
	}

	/**
	 * Assuming the input string format :
	 *    [build],[chr],[startPos],[refAllele],[tumAllele]
	 *
	 * Generated key format:
	 *   [chromosome]_[startPosition]_[endPosition]_[referenceAllele]_[tumorAllele]
	 *
	 * See also MafProcessor.generateKey format.
	 *
	 * @param mutation  mutation string with necessary info
	 * @return          key generated for the given string
	 */
	protected String generateKey(String mutation)
	{
		String[] parts = mutation.split(",");

		String key = null;

		if (parts.length >= 5)
		{
			key = parts[1] + "_" + parts[2] + "_" + parts[2] + "_" +
			      parts[3] + "_" + parts[4];
		}

		return key;
	}

	protected Integer getHeaderIndex(String header)
	{
		Integer index = this.headerIndices.get(header);

		if (index == null)
		{
			index = -1;
		}

		return index;
	}

	protected String getPartString(Integer index, String[] parts)
	{
		try
		{
			if (parts[index].length() == 0)
			{
				return MutationAssessorRecord.NA_STRING;
			}
			else
			{
				return parts[index];
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return MutationAssessorRecord.NA_STRING;
		}
	}

	public void buildCache(String input) throws IOException, SQLException
	{
		File inFile = new File(input);

		if (inFile.isDirectory())
		{
			this.processDirectory(inFile);
		}
		else
		{
			this.processFile(inFile);
		}
	}
}
