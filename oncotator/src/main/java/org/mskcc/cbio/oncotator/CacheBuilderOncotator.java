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
import java.io.IOException;

/**
 * Oncotator altered to build JSON cache for existing oncotator key values.
 *
 * @author Selcuk Onur Sumer
 */
public class CacheBuilderOncotator extends Oncotator
{
	/**
	 * Default constructor with the default oncotator service.
	 */
	public CacheBuilderOncotator()
	{
		super();
		OncotatorCacheService cacheService = new DaoJsonCache();

		// use a cached oncotator service with a custom cache service.
		this.oncotatorService = new CachedOncotatorService(cacheService);
	}

	protected int oncotateMaf(File inputMafFile, File outputMafFile)
			throws IOException, OncotatorServiceException
	{
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

			String key = dataLine.trim();

			OncotatorRecord oncotatorRecord =
				oncotatorService.getOncotatorRecord(key);

			numRecordsProcessed++;
		}

		System.out.println("Total Number of Records Processed:  " + numRecordsProcessed);

		reader.close();

		return this.oncotatorService.getErrorCount();
	}
}
