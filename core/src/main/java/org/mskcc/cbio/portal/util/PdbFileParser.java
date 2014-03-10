/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
package org.mskcc.cbio.portal.util;

import org.json.simple.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class designed to parse raw PDF files returned from PDB data service,
 * and to convert data into JSON objects.
 *
 * @author Selcuk Onur Sumer
 */
public class PdbFileParser
{
	/**
	 * Parses the raw PDB file retrieved from the server and
	 * creates a mapping for each main identifier.
	 *
	 * @param rawInput  raw file contents as a String
	 * @return  data mapped on the main identifier names
	 */
	public Map<String, String> parsePdbFile(String rawInput)
	{
		String[] lines = rawInput.toLowerCase().split("\n");

		// count for distinct identifiers
		Map<String, Integer> countMap = new HashMap<String, Integer>();

		// map of builders to build content for each identifier
		Map<String, StringBuilder> contentMap = new HashMap<String, StringBuilder>();

		// actual content map to return
		Map<String, String> content = new HashMap<String, String>();

		for (String line: lines)
		{
			String[] tokens = line.split("[\\s]+");

			// first token is the identifier
			if (tokens.length == 0)
			{
				// empty line, just skip
				continue;
			}

			String identifier = tokens[0];

			// get the corresponding count
			Integer count = countMap.get(identifier);

			if (count == null)
			{
				count = 0;
			}

			// update count
			countMap.put(identifier, ++count);

			String str = line;

			// if there is more than one identifier lines,
			// than the line starts with the line number
			// we should get rid of the line number as well
			if (count > 1)
			{
				str = str.replaceFirst(count.toString(), "");
			}

			// get the corresponding string builder
			StringBuilder sb = contentMap.get(identifier);

			if (sb == null)
			{
				sb = new StringBuilder();
				contentMap.put(identifier, sb);
			}

			// get rid of the identifier itself
			str = str.replaceFirst(identifier, "").trim();

			// append the parsed line
			sb.append(str);
			sb.append("\n");
		}

		for (String identifier: contentMap.keySet())
		{
			String value = contentMap.get(identifier).toString().trim();
			content.put(identifier, value);
		}

		return content;
	}

	/**
	 * Parses the raw compound/source content returned by the service,
	 * and creates a nested map structure for each sub-field.
	 *
	 * @param rawInput  raw data from the service
	 * @return  nested map structure
	 */
	public Map<String, Object> parseCompound(String rawInput)
	{
		String[] lines = rawInput.split("\n");
		Map<String, Object> content = new HashMap<String, Object>();
		Map<String, Object> mol = null;

		// buffering lines (just in case if an entity consists of multiple lines)
		StringBuilder buffer = new StringBuilder();

		for (String line: lines)
		{
			buffer.append(line);

			// process the buffer if line end with a semicolon
			if (line.trim().endsWith(";"))
			{
				String[] tokens = buffer.toString().split(":");

				if (tokens.length > 1)
				{
					String field = tokens[0].trim();
					String value = tokens[1].trim();
					// remove the semicolon
					value = value.substring(0, value.length() - 1);
					JSONArray list = null;

					// create a new mapping object for each mol_id
					if (field.equals("mol_id"))
					{
						mol = new HashMap<String, Object>();
						content.put(value, mol);
					}
					// convert comma separated chain and gene lists into an array
					else if (field.equals("chain") ||
					         field.equals("gene"))
					{
						String[] values = value.split("[\\s,]+");
						list = new JSONArray();
						list.addAll(Arrays.asList(values));
					}

					// add the field for the current mol
					if (mol != null)
					{
						if (list != null)
						{
							mol.put(field, list);
						}
						else
						{
							mol.put(field, value);
						}
					}
				}

				// reset buffer for the next entity
				buffer = new StringBuilder();
			}
			else
			{
				// add a whitespace before adding the next line
				buffer.append(" ");
			}
		}

		return content;
	}

	/**
	 * Parses the raw PDB title content returned by the service,
	 * and creates a human readable info string.
	 *
	 * @param rawTitle   data lines to process
	 * @return  a human readable info string
	 */
	public String parseTitle(String rawTitle)
	{
		String[] lines = rawTitle.split("\n");
		StringBuilder sb = new StringBuilder();

		for (String line: lines)
		{
			sb.append(line);

			// whether to add a space at the end or not
			if (!line.endsWith("-"))
			{
				sb.append(" ");
			}
		}

		return sb.toString().trim();
	}
}
