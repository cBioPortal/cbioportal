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

package org.mskcc.cbio.cgds.scripts;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Fetches PFAM graphic data.
 *
 * @author Selcuk Onur Sumer
 */
public class FetchPfamGraphicsData
{
	public static final String URL_PREFIX = "http://pfam.sanger.ac.uk/protein/";
	public static final String URL_SUFFIX = "/graphic";

	/**
	 * Parses the given input file and creates an output with pfam graphics data
	 * for each uniprot id.
	 *
	 * @param inputFilename     name of the uniprot id mapping file
	 * @param outputFilename    name of the output pfam graphics file
	 */
	public static int driver(String inputFilename, String outputFilename) throws IOException
	{
		BufferedReader in = new BufferedReader(new FileReader(inputFilename));
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));

		String line;
		int numLines = 0;
		int numErrors = 0;

		Set<String> keySet = new HashSet<String>();

		// read all
		while ((line = in.readLine()) != null)
		{
			if (line.trim().length() == 0)
			{
				continue;
			}

			String[] parts = line.split("\t");

			if (parts.length > 1)
			{
				String uniprotId = parts[1];

				// avoid to add a duplicate entry
				if (keySet.contains(uniprotId))
				{
					continue;
				}

				String pfamJson = fetch(uniprotId);
				keySet.add(uniprotId);

				// replace all tabs and new lines with a single space
				pfamJson = pfamJson.trim().replaceAll("\t", " ").replaceAll("\n", " ");

				// verify if it is really a JSON object
				// TODO this verification may not be safe...
				if (pfamJson.startsWith("[") || pfamJson.startsWith("{"))
				{
					out.write(uniprotId);
					out.write("\t");
					out.write(pfamJson);
					out.write("\n");
				}
				else
				{
					System.out.println("Invalid data for: " + uniprotId);
					numErrors++;
				}
			}

			numLines++;
		}

		System.out.println("Total number of lines processed: " + numLines);

		out.close();
		in.close();

		return numErrors;
	}

	/**
	 * Fetches the JSON data from the PFAM graphics service for the
	 * specified uniprot id.
	 *
	 * @param uniprotId a uniprot id
	 * @return  pfam graphic data as a JSON string
	 * @throws  IOException
	 */
	public static String fetch(String uniprotId) throws IOException
	{
		URL url = new URL(URL_PREFIX + uniprotId + URL_SUFFIX);

		URLConnection pfamConn = url.openConnection();

		BufferedReader in = new BufferedReader(
				new InputStreamReader(pfamConn.getInputStream()));

		String line;
		StringBuilder sb = new StringBuilder();

		// read all
		while((line = in.readLine()) != null)
		{
			sb.append(line);
		}

		in.close();

		return sb.toString();
	}

	public static void main(String[] args)
	{
		// check args
		if (args.length < 2)
		{
			System.out.println("command line usage:  fetchPfamGraphicsData.sh " +
			                   "<uniprot_id_mapping_file> <output_pfam_mapping_file>");
			System.exit(1);
		}

		String input = args[0];
		String output = args[1];

		try
		{
			System.out.println("Fetching started...");
			Date start = new Date();
			int numErrors = driver(input, output);
			Date end = new Date();
			System.out.println("Fetching finished.");

			double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

			System.out.println("\nTotal time elapsed: " + timeElapsed);

			if (numErrors > 0)
			{
				System.out.println("Total number of errors: " + numErrors);
			}
		}
		catch (IOException e)
		{
			System.out.println("error processing IO files.");
			System.exit(1);
		}
	}
}
