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

import org.mskcc.cbio.io.WebFileConnect;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Connects to Oncotator and Retrieves Details on a Single Mutation.
 */
public class OncotatorService
{
	//private static final Logger logger = Logger.getLogger(OncotatorService.class);
    //private static OncotatorService oncotatorService;
    private final static String ONCOTATOR_BASE_URL = "http://www.broadinstitute.org/oncotator/mutation/";
	private final static long SLEEP_PERIOD = 0;  // in ms

    private OncotatorCacheService cache;
	private int errorCount = 0;
	private boolean useCache = true;

	/**
	 * Default constructor with the default cache DAO.
	 */
    public OncotatorService()
    {
        this.cache = DaoOncotatorCache.getInstance();
    }

	/**
	 * Alternative constructor with a cache service option.
	 *
	 * @param cache     cache service instance
	 */
	public OncotatorService(OncotatorCacheService cache)
	{
		this.cache = cache;
	}

	/**
	 * Retrieves the data from the Oncotator service for the given query key.
	 *
	 * @param key   key for the service query
	 * @return      oncotator record containing the query result
	 */
    public OncotatorRecord getOncotatorRecord(String key) throws Exception
    {
        BufferedReader in = null;
        InputStream inputStream = null;

        try 
        {
            OncotatorRecord record = null;

	        if (this.useCache)
	        {
		        record = cache.get(key);
	        }
            
            // if record is null, then it is not cached yet
	        // or the caching option is disabled
            if (record == null)
            {
                try {
                    //  Must go to sleep;  otherwise, we trigger the Broad's Limit.
                    Thread.sleep(SLEEP_PERIOD);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                URL url = new URL(ONCOTATOR_BASE_URL + key);
                inputStream = url.openStream();
                in = new BufferedReader(new InputStreamReader(inputStream));
                String content = WebFileConnect.readFile(in);
                record = OncotatorParser.parseJSON(key, content);
                
                // if record is null, then there is an error with JSON parsing
                if (record != null)
                {
                	if (this.useCache)
	                {
		                // surrounded with try/catch just to ignore duplicate
		                // key error (race condition if parallel apps accessing
		                // the DB at the same time)
		                try {
			                cache.put(record);
		                } catch (Exception e) {
			                System.out.println("Cache error: " + e.getMessage());
			                this.errorCount++;
		                }
	                }
                }
                else
                {
                	record = new OncotatorRecord(key);
	                this.errorCount++;
                }
                
                return record;
            }
            else
            {
                return record;
            }
        }
        catch (IOException e)
        {
            System.out.println("IO error: " + e.getMessage());
	        this.errorCount++;
            return new OncotatorRecord(key);
        }
        finally
        {
            // Must close input stream!  Otherwise, we maintain too many open connections
            // to the Broad.
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

	// Getters and Setters

	public int getErrorCount()
	{
		return errorCount;
	}

	public void setUseCache(boolean useCache)
	{
		this.useCache = useCache;
	}
}