package org.mskcc.portal.oncotator;

import org.mskcc.cgds.dao.DaoException;
import org.mskcc.portal.util.WebFileConnect;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Connects to Oncotator and Retrieves Details on a Single Mutation.
 */
public class OncotatorService {
    private static OncotatorService oncotatorService;
    private final static String ONCOTATOR_BASE_URL = "http://www.broadinstitute.org/oncotator/mutation/";
    private static final Logger logger = Logger.getLogger(OncotatorService.class);

    private OncotatorService () {
    }

    public static OncotatorService getInstance() {
        if (oncotatorService == null) {
            oncotatorService = new OncotatorService();
        }
        return oncotatorService;
    }

    public OncotatorRecord getOncotatorRecord(String chr, long start, long end, String referenceAllele,
        String observedAllele) throws IOException {
        String key = createKey(chr, start, end, referenceAllele, observedAllele);

        URL url = new URL(ONCOTATOR_BASE_URL + key);
        logger.warn("Getting live data:  " + url);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String content = WebFileConnect.readFile(in);
        return OncotatorParser.parseJSON(key, content);
    }

    public static String createKey(String chr, long start, long end, String referenceAllele,
                                   String observedAllele) {
        return chr + "_" + start + "_" + end + "_" + referenceAllele + "_" + observedAllele;
    }
}