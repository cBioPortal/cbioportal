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

package org.mskcc.cbio.mapping;

import java.io.*;
import java.util.*;

/**
 * Initially designed to create a gene symbol to isoform mapping. We can extend
 * it later by adding other mappings such as uniprot ID to isoform, etc.
 *
 * @author Selcuk Onur Sumer
 */
public class IsoformMapper
{
	public static final String DEFAULT_MAPPING_FILE = "gene_to_isoform.txt";

	protected Map<String, List<String>> symbolToIsoform;

	public IsoformMapper()
	{
		this.symbolToIsoform = new HashMap<String, List<String>>();
	}

	public String getCanonicalIsoformBySymbol(String geneSymbol)
	{
		List<String> list = this.symbolToIsoform.get(
				geneSymbol.toUpperCase());

		if (list != null &&
		    list.size() > 0)
		{
			return list.iterator().next();
		}
		else
		{
			return null;
		}
	}

	public Map<String, List<String>> buildSymbolToIsoformMap(String filename)
			throws IOException
	{
		return this.buildSymbolToIsoformMap(new FileInputStream(filename));
	}

	public Map<String, List<String>> buildSymbolToIsoformMap(InputStream is)
			throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line;
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();

		while ((line = reader.readLine()) != null)
		{
			if (line.trim().length() == 0)
			{
				continue;
			}

			String[] parts = line.split("\t");
			String[] isoforms = parts[1].split("\\|");

			List<String> list = new LinkedList<String>();
			// TODO sort (wrt to a well defined rule) before adding to the list
			Collections.addAll(list, isoforms);
			map.put(parts[0], list);
		}

		reader.close();

		this.symbolToIsoform = map;
		return this.symbolToIsoform;
	}
}
