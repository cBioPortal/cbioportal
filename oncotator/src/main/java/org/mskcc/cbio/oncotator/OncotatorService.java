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

	private int errorCode = 0;

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
            OncotatorRecord record = cache.get(key);
            
            // if record is null, then it is not cached yet
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
                	cache.put(record);
                }
                else
                {
                	record = new OncotatorRecord(key);
	                this.errorCode = 2;
                }
                
                return record;
            }
            else
            {
                return record;
            }
        } catch (IOException e) {
            System.out.println("Got IO Error:  " + e.getMessage());
            return new OncotatorRecord(key);
        } finally {
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

	public int getErrorCode()
	{
		return errorCode;
	}
}