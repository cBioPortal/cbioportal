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

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoPfamGraphics;
import org.mskcc.cbio.portal.util.ProgressMonitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Imports a pfam graphics mapping file.
 * First column is a uniprot id, second column is a JSON string.
 *
 * @author Selcuk Onur Sumer
 */
public class ImportPfamGraphicsData
{
	private File pfamFile;
	private ProgressMonitor pMonitor;

	/**
	 * Constructor.
	 *
	 * @param pfamFile  pfam file containing (uniprot id -> json data) mapping
	 * @param pMonitor  progress monitor
	 */
	public ImportPfamGraphicsData(File pfamFile, ProgressMonitor pMonitor)
	{
		this.pfamFile = pfamFile;
		this.pMonitor = pMonitor;
	}

	/**
	 * Imports pfam graphics data into the portal DB by processing the
	 * input mapping file.
	 *
	 * @throws IOException
	 * @throws DaoException
	 */
	public void importData() throws IOException, DaoException
	{
		DaoPfamGraphics dao = new DaoPfamGraphics();
		BufferedReader in = new BufferedReader(new FileReader(pfamFile));
		String line;
		int rows = 0;
		Set<String> keySet = new HashSet<String>();

		while ((line = in.readLine()) != null)
		{
			// parts[0]: uniprot id, parts[1]: pfam data as JSON
			String[] parts = line.split("\t");

			if (parts.length > 1)
			{
				String uniprotId = parts[0];
				String jsonString = parts[1];

				// avoid to add a duplicate entry
				if (!keySet.contains(uniprotId))
				{
					keySet.add(uniprotId);
					dao.addPfamGraphics(uniprotId, jsonString);
					rows++;
				}
			}
		}

		System.out.println("Total number of pfam graphics saved: " + rows);

		in.close();
	}

	/**
	 * args[0]: input pfam mapping file
	 */
	public static void main(String[] args)
	{
		if (args.length < 1)
		{
			System.out.println("error: no input file specified");
			System.exit(1);
		}

		ProgressMonitor pMonitor = new ProgressMonitor(); // TODO pMonitor is not used at all
		File input = new File(args[0]);
		ImportPfamGraphicsData importer = new ImportPfamGraphicsData(input, pMonitor);

		try
		{
			importer.importData();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
