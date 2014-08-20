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

package org.mskcc.cbio.oncotator;

import java.io.IOException;

/**
 * Oncotator service implementation with caching option.
 *
 * @author Selcuk Onur Sumer
 */
public class CachedOncotatorService extends OncotatorService
{
	protected OncotatorCacheService cache;

	/**
	 * Default constructor with the default cache DAO.
	 */
	public CachedOncotatorService()
	{
		this.cache = new DaoJsonCache();
	}

	/**
	 * Alternative constructor with a cache service option.
	 *
	 * @param cache     cache service instance
	 */
	public CachedOncotatorService(OncotatorCacheService cache)
	{
		this.cache = cache;
	}

	/**
	 * Retrieves the data from the Oncotator service for the given query key.
	 *
	 * @param key   key for the service query
	 * @return      oncotator record containing the query result
	 */
	public OncotatorRecord getOncotatorRecord(String key) throws OncotatorServiceException
	{
		boolean addToCache = false;

		// first try to get the record from cache
		OncotatorRecord record = null;

		try {
			record = cache.get(key);
		} catch (OncotatorCacheException e) {
			//e.printStackTrace();
			throw new OncotatorServiceException(e.getMessage());
		}

		// if record is null, then it is not cached yet
		if (record == null)
		{
			try {
				record = getRecordFromService(key);
			} catch (IOException e) {
				//e.printStackTrace();
				throw new OncotatorServiceException(e.getMessage());
			}

			addToCache = true;
		}

		if (addToCache &&
		    record != null)
		{
			// surrounded with try/catch just to ignore duplicate
			// key error (race condition if parallel apps accessing
			// the DB at the same time)
			// an exception can also occur if the data is too long
			// to fit the cache (truncation error)
			try {
				cache.put(record);
			} catch (OncotatorCacheException e) {
				System.out.println("Cache error: " + e.getMessage());
				this.errorCount++;
			}
		}
		// if record is null, then there is an error with JSON parsing
		else if (record == null)
		{
			record = new OncotatorRecord(key);
			this.errorCount++;
		}

		return record;
	}
}
