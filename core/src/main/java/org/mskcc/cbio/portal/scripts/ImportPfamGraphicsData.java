/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.scripts;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.util.*;

import java.io.*;
import java.util.*;

/**
 * Imports a pfam graphics mapping file.
 * First column is a uniprot id, second column is a JSON string.
 *
 * @author Selcuk Onur Sumer
 */
public class ImportPfamGraphicsData
{
	private File pfamFile;

	/**
	 * Constructor.
	 *
	 * @param pfamFile  pfam file containing (uniprot id -> json data) mapping
	 */
	public ImportPfamGraphicsData(File pfamFile)
	{
		this.pfamFile = pfamFile;
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
            return;
		}

		SpringUtil.initDataSource();
		File input = new File(args[0]);
		ImportPfamGraphicsData importer = new ImportPfamGraphicsData(input);

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
