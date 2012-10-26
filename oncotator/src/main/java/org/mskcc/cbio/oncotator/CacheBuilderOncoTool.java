package org.mskcc.cbio.oncotator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Created with IntelliJ IDEA.
 * User: sos
 * Date: 10/26/12
 * Time: 3:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class CacheBuilderOncoTool extends OncotateTool
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
			File outputMafFile,
			boolean noCache) throws Exception
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
