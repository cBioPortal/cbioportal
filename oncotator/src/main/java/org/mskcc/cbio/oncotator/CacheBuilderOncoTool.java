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

package org.mskcc.cbio.oncotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Oncotate Tool altered to build JSON cache for existing oncotator key values.
 */
public class CacheBuilderOncoTool extends Oncotator
{
	/**
	 * Default constructor with the default oncotator service.
	 */
	public CacheBuilderOncoTool()
	{
		super();
		OncotatorCacheService cacheService = new DaoJsonCache();
		this.oncotatorService = new OncotatorService(cacheService);
	}

	protected int oncotateMaf(File inputMafFile,
			File outputMafFile) throws Exception
	{
		// always use cache, this is a cache builder.
		this.oncotatorService.setUseCache(true);

		FileReader reader = new FileReader(inputMafFile);
		BufferedReader bufReader = new BufferedReader(reader);

		String dataLine;

		int numRecordsProcessed = 0;

		// skip header line (which is assumed to be CACHE_KEY)
		bufReader.readLine();

		while ((dataLine = bufReader.readLine()) != null)
		{
			System.out.println("(" + numRecordsProcessed + ") " + dataLine);

			if (dataLine.trim().length() < 0)
			{
				continue;
			}

			String[] parts = dataLine.trim().split("_");

			OncotatorRecord oncotatorRecord =
				oncotatorService.getOncotatorRecord(parts[0],
					Long.parseLong(parts[1]),
					Long.parseLong(parts[2]),
					parts[3],
					parts[4]);

			numRecordsProcessed++;
		}

		System.out.println("Total Number of Records Processed:  " + numRecordsProcessed);

		reader.close();

		return this.oncotatorService.getErrorCount();
	}
}
