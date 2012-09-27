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
import java.sql.SQLException;

/**
 * Connects to Oncotator and Retrieves Details on a Single Mutation.
 */
public class OncotatorService {
    private static OncotatorService oncotatorService;
    private final static String ONCOTATOR_BASE_URL = "http://www.broadinstitute.org/oncotator/mutation/";
    //private static final Logger logger = Logger.getLogger(OncotatorService.class);
    private DaoOncotatorCache cache;
    private final static long SLEEP_PERIOD = 0;  // in ms

	private int errorCount = 0;
	private boolean useCache = true;

    private OncotatorService () {
        cache = DaoOncotatorCache.getInstance();
    }

    public static OncotatorService getInstance() {
        if (oncotatorService == null) {
            oncotatorService = new OncotatorService();
        }
        return oncotatorService;
    }

    public OncotatorRecord getOncotatorRecord(String chr,
		    long start,
		    long end,
		    String referenceAllele,
            String observedAllele) throws IOException, SQLException
    {
        String key = createKey(chr, start, end, referenceAllele, observedAllele);

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
		                cache.put(record);
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
            System.out.println("Got IO Error:  " + e.getMessage());
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

    public static String createKey(String chr, long start, long end, String referenceAllele,
                                   String observedAllele) {
        return chr + "_" + start + "_" + end + "_" + referenceAllele + "_" + observedAllele;
    }

	public int getErrorCount()
	{
		return errorCount;
	}

	public void setUseCache(boolean useCache)
	{
		this.useCache = useCache;
	}
}