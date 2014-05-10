/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to process comment lines (meta data) of a standard text file.
 * Default comment line indicator is #
 *
 * @author Selcuk Onur Sumer
 */
public class MafHeaderUtil
{
	public static final String DEFAULT_COMMENT_CHAR = "#";

	private String headerLine;
	private List<String> comments;
	private String commentChar;

	public MafHeaderUtil(String commentChar)
	{
		this.comments = new ArrayList<String>();
		this.headerLine = null;
		this.commentChar = commentChar;
	}

	public MafHeaderUtil()
	{
		this(DEFAULT_COMMENT_CHAR);
	}

	public String extractHeader(BufferedReader reader) throws IOException
	{
		String line;
		boolean done = false;

		while (!done)
		{
			line = reader.readLine();

			if (line == null ||
			    (line.trim().length() > 0) && !line.trim().startsWith(this.commentChar))
			{
				done = true;
				this.headerLine = line;
			}
			else
			{
				this.comments.add(line);
			}
		}

		return this.headerLine;
	}

	public List<String> getComments()
	{
		return comments;
	}

	public String getHeaderLine()
	{
		return headerLine;
	}
}
