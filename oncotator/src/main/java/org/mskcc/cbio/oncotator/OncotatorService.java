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
 * Base class for Oncotator Service implementations.
 * This class provides a method to connect to Oncotator Web service and
 * retrieve details on a single mutation.
 *
 */
public abstract class OncotatorService
{
	//private static final Logger logger = Logger.getLogger(OncotatorService.class);
    //private static OncotatorService oncotatorService;
    protected final static String ONCOTATOR_BASE_URL = "http://www.broadinstitute.org/oncotator/mutation/";
	protected final static long SLEEP_PERIOD = 0;  // in ms

	protected int errorCount = 0;

	/**
	 * Retrieves the data from the Oncotator service for the given query key.
	 *
	 * @param key   key for the service query
	 * @return      oncotator record containing the query result
	 */
    public abstract OncotatorRecord getOncotatorRecord(String key) throws Exception;

	/**
	 * Retrieves the record from the oncotator web service.
	 *
	 * @param key   oncotator key representing a single mutation
	 * @return      query result as an OncototatorRecord instance
	 * @throws IOException
	 */
	protected OncotatorRecord getRecordFromService(String key) throws IOException
	{
		BufferedReader in = null;
		InputStream inputStream = null;
		OncotatorRecord record;

		try
		{
			URL url = new URL(ONCOTATOR_BASE_URL + key);
			inputStream = url.openStream();
			in = new BufferedReader(new InputStreamReader(inputStream));
			String content = WebFileConnect.readFile(in);
			record = OncotatorParser.parseJSON(key, content);
		}
		catch (IOException e)
		{
			System.out.println("IO error: " + e.getMessage());
			this.errorCount++;
			record = new OncotatorRecord(key);
		}
		finally
		{
			// Must close input stream!  Otherwise, we maintain too many open connections
			// to the Broad.
			if (inputStream != null)
			{
				inputStream.close();
			}
		}

		return record;
	}

	// Getters and Setters

	public int getErrorCount()
	{
		return errorCount;
	}
}