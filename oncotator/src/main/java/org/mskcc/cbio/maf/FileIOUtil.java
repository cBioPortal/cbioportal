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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Utility class for MAF file IO operations.
 *
 * @author Selcuk Onur Sumer
 */
public class FileIOUtil
{
	public static final String TAB = "\t";

	/**
	 * Writes a single line of data to the output MAF.
	 *
	 * @param writer    writer for the output MAF
	 * @param data      list of data to write
	 * @throws java.io.IOException
	 */
	public static void writeLine(Writer writer,
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
	 * Writes multiple lines of data to the output MAF.
	 *
	 * @param writer    writer for the output MAF
	 * @param data      list of data to write
	 * @throws java.io.IOException
	 */
	public static void writeLines(Writer writer,
			List<String> data) throws IOException
	{
		for (String line : data)
		{
			writer.write(outputField(line));
			writer.write("\n");
		}
	}

	/**
	 * Returns a string representation of a single field for
	 * the output.
	 *
	 * @param field field as a string
	 * @return      string to output
	 */
	public static String outputField(String field)
	{
		if (field == null) {
			return "";
		} else {
			return field;
		}
	}
}
