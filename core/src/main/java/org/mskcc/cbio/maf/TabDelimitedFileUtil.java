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

/**
 * Generic utility class providing simple utility functions for tab
 * delimited data files.
 *
 * @author Selcuk Onur Sumer
 */
public class TabDelimitedFileUtil
{
	public final static String NA_STRING = "NA";
	public final static long NA_LONG = Long.MIN_VALUE;
	// TODO use MIN instead of -1, we may have fields with negative values
	public final static int NA_INT = -1;
	public final static float NA_FLOAT = -1;

	public static String getPartString(int index, String[] parts)
	{
		try
		{
			if (parts[index].length() == 0)
			{
				return NA_STRING;
			}
			else
			{
				return parts[index];
			}
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			return NA_STRING;
		}
	}

	public static Long getPartLong(int index, String[] parts) {
		try {
			String part = parts[index];
			return Long.parseLong(part);
		} catch (ArrayIndexOutOfBoundsException e) {
			return NA_LONG;
		} catch (NumberFormatException e) {
			return NA_LONG;
		}
	}

	public static Integer getPartInt(int index, String[] parts)
	{
		try {
			String part = parts[index];
			return (int)(Float.parseFloat(part));
		} catch (ArrayIndexOutOfBoundsException e) {
			return NA_INT;
		} catch (NumberFormatException e) {
			return NA_INT;
		}
	}

	public static Float getPartPercentage(int index, String[] parts)
	{
		try {
			float result = NA_FLOAT;
			String part = parts[index];
			if (part.contains("%")) {
				result = Float.parseFloat(part.replace("%", "")) / Float.parseFloat("100");
			} else {
				result = Float.parseFloat(part);
			}
			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			return NA_FLOAT;
		} catch (NumberFormatException e) {
			return NA_FLOAT;
		}
	}

	public static Float getPartFloat(int index, String[] parts)
	{
		try {
			String part = parts[index];
			return Float.parseFloat(part);
		} catch (ArrayIndexOutOfBoundsException e) {
			return NA_FLOAT;
		} catch (NumberFormatException e) {
			return NA_FLOAT;
		}
	}

	// returning MIN_VALUE instead of NA_FLOAT
	// use this one if -1 is not a safe "NA" value.
	public static Float getPartFloat2(int index, String[] parts)
	{
		try {
			String part = parts[index];
			return Float.parseFloat(part);
		} catch (ArrayIndexOutOfBoundsException e) {
			return Float.MIN_VALUE;
		} catch (NumberFormatException e) {
			return Float.MIN_VALUE;
		}
	}

	public static String adjustDataLine(String dataLine,
			int headerCount)
	{
		String line = dataLine;
		String[] parts = line.split("\t", -1);

		// diff should be zero if (# of headers == # of data cols)
		int diff = headerCount - parts.length;

		// number of header columns are more than number of data columns
		if (diff > 0)
		{
			// append appropriate number of tabs
			for (int i = 0; i < diff; i++)
			{
				line += "\t";
			}
		}
		// number of data columns are more than number of header columns
		else if (diff < 0)
		{
			line = "";

			// just truncate the data (discard the trailing columns)
			for (int i = 0; i < headerCount; i++)
			{
				line += parts[i];

				if (i < headerCount - 1)
				{
					line += "\t";
				}
			}
		}

		return line;
	}
}
