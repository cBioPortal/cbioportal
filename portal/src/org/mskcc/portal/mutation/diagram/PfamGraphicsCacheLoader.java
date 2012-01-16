package org.mskcc.portal.mutation.diagram;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;

/**
 * Domain service cache loader that calls the Pfam graphics service.
 */
final class PfamGraphicsCacheLoader extends CacheLoader<String, List<Domain>> {
    private static final Logger logger = Logger.getLogger(PfamGraphicsCacheLoader.class);
    static final String URL_PREFIX = "http://pfam.sanger.ac.uk/protein/";
    static final String URL_SUFFIX = "/graphic";

    @Override
    public List<Domain> load(final String uniProtId) throws Exception {
        List<Domain> domains = new LinkedList<Domain>();
        try {
            JSONParser parser = new JSONParser();
            URL url = new URL(URL_PREFIX + uniProtId + URL_SUFFIX);
            JSONArray root = (JSONArray) parser.parse(Resources.toString(url, Charset.defaultCharset()));
            JSONObject graphics = (JSONObject) root.get(0);
            domains.add(new Domain("length", 1, Integer.parseInt((String) graphics.get("length"))));
            JSONArray regions = (JSONArray) graphics.get("regions");
            for (Object o : regions) {
                JSONObject region = (JSONObject) o;
                String label = (String) region.get("text");
                int start = parseInt(region.get("start"));
                int end = parseInt(region.get("end"));
                domains.add(new Domain(label, start, end));
            }
        }
        catch (Exception e) {
            logger.error("could not load domains for " + uniProtId, e);
        }
        return ImmutableList.copyOf(domains);
    }

    static final int parseInt(final Object value) {
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        else {
            throw new NumberFormatException("cannot parse " + value + " as int");
        }
    }
}
