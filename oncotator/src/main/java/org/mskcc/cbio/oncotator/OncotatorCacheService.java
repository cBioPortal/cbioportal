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

/**
 * Interface to define a Cache Service for Oncotator records.
 *
 * @author Selcuk Onur Sumer
 */
public interface OncotatorCacheService
{
	/**
	 * Adds a new oncotator record to the cache.
	 *
	 * @param record    oncotator record to add
	 * @return          number of records successfully added
	 */
	public int put(OncotatorRecord record) throws OncotatorCacheException;


	/**
	 * Gets the oncotator record for the given key.
	 *
	 * @param key   cache key
	 * @return      corresponding record for the given key
	 */
	public OncotatorRecord get(String key) throws OncotatorCacheException;

}
