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

package org.mskcc.cbio.portal.scripts;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.mskcc.cbio.portal.util.ConsoleUtil;
import org.mskcc.cbio.portal.util.ProgressMonitor;

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
	 * @param incremental       indicates incremental fetching
	 * @return  total number of errors
	 */
	public static int driver(String outputFilename,
			boolean incremental) throws IOException
	{
		BufferedWriter out = new BufferedWriter(new FileWriter(outputFilename));

		int numErrors = 0;

		// TODO if incremental:
		// 1. open the file in append mode, do not overwrite
		// 2. check if a certain uniprot id is already mapped in the file
		// 3. populate key set if incremental option is selected
		Set<String> keySet = initKeySet(outputFilename, incremental);
                
                Set<String> uniprotAccs = ImportUniProtIdMapping.getSwissProtAccessionHuman();
                
                ProgressMonitor pMonitor = new ProgressMonitor();
                pMonitor.setConsoleMode(true);
                pMonitor.setMaxValue(uniprotAccs.size());

		// read all
		for (String uniprotId : uniprotAccs)
		{
                            pMonitor.incrementCurValue();
                            ConsoleUtil.showProgress(pMonitor);
                            
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

		out.close();

		return numErrors;
	}

	private static Set<String> initKeySet(String outputFilename, boolean incremental)
	{
		HashSet<String> keySet = new HashSet<String>();

		if (incremental)
		{
			// TODO populate keyset by processing output file
		}

		return keySet;
	}

	/**
	 * Fetches the JSON data from the PFAM graphics service for the
	 * specified uniprot accession.
	 *
	 * @param uniprotAcc a uniprot accession
	 * @return  pfam graphic data as a JSON string
	 * @throws  IOException
	 */
	private static String fetch(String uniprotAcc) throws IOException
	{
		URL url = new URL(URL_PREFIX + uniprotAcc + URL_SUFFIX);

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

	public static void main(String[] args) throws Exception
	{
		// default config params
		boolean noFetch = false;     // skip fetching
		boolean incremental = false; // overwrite or append data

		// process program arguments

		int i;

		// this is for program arguments starting with a dash
		// these arguments must come before IO file names
		for (i = 0; i < args.length; i++)
		{
			if (args[i].startsWith("-"))
			{
				if (args[i].equalsIgnoreCase("-nofetch"))
				{
					noFetch = true;
				}
				else if (args[i].equalsIgnoreCase("-append"))
				{
					incremental = true;
				}
			}
			else
			{
				break;
			}
		}

		// check IO file name args
		if (args.length - i < 1)
		{
			System.out.println("command line usage:  fetchPfamGraphicsData.sh <output_pfam_mapping_file>");
            return;
		}

		String output = args[i];

		if (noFetch)
		{
			// do nothing, just terminate
			System.out.println("-nofetch argument provided, terminating...");
			return;
		}

                    System.out.println("Fetching started...");
                    Date start = new Date();
                    int numErrors = driver(output, incremental);
                    Date end = new Date();
                    System.out.println("Fetching finished.");

                    double timeElapsed = (end.getTime() - start.getTime()) / 1000.0;

                    System.out.println("\nTotal time elapsed: " + timeElapsed + " seconds");

                    if (numErrors > 0)
                    {
                            System.out.println("Total number of errors: " + numErrors);
                    }
	}
}
