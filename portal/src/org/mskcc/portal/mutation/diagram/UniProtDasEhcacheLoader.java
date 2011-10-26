package org.mskcc.portal.mutation.diagram;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import uk.ac.ebi.das.jdas.adapters.features.DasGFFAdapter;
import uk.ac.ebi.das.jdas.adapters.features.FeatureAdapter;
import uk.ac.ebi.das.jdas.client.FeaturesClient;

import com.google.common.collect.ImmutableList;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.loader.CacheLoader;

/**
 * Domain service Ehcache loader that calls the UniProt DAS service with a feature request.
 */
final class UniProtDasEhcacheLoader implements CacheLoader {
    private static final Logger logger = Logger.getLogger(UniProtDasEhcacheLoader.class);
    static final String SERVER_URL = "http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/";
    static final List<String> TYPES = ImmutableList.of("SO:0000417");

    /** {@inheritDoc} */
    public CacheLoader clone(final Ehcache cache) throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /** {@inheritDoc} */
    public void dispose() throws CacheException {
        // empty
    }

    /** {@inheritDoc} */
    public String getName() {
        return "uniProtDasEhcacheLoader";
    }

    /** {@inheritDoc} */
    public Status getStatus() {
        return null;
    }

    /** {@inheritDoc} */
    public void init() {
        // empty
    }

    /** {@inheritDoc} */
    public Object load(final Object key) throws CacheException {
        String uniProtId = (String) key;
        List<Domain> domains = new LinkedList<Domain>();
        List<String> segments = ImmutableList.of(uniProtId);

        try {
            FeaturesClient featuresClient = new FeaturesClient();
            DasGFFAdapter gffAdapter = featuresClient.fetchData(SERVER_URL, segments, TYPES, null, null, null, null);
            DasGFFAdapter.SegmentAdapter segment = gffAdapter.getGFF().getSegment().get(0);
            for (FeatureAdapter feature : segment.getFeature()) {
                String label = feature.getLabel();
                int start = feature.getStart();
                int end = feature.getEnd();
                domains.add(new Domain(label, start, end));
            }
        }
        catch (Exception e) {
            logger.error("could not load domains from cache for " + uniProtId, e);
        }
        return ImmutableList.copyOf(domains);
    }

    /** {@inheritDoc} */
    public Object load(final Object key, final Object value) {
        return load(key);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Map loadAll(final Collection keys) {
        Map result = new HashMap(keys.size());
        for (Object key : keys) {
            result.put(key, load(key));
        }
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    public Map loadAll(final Collection keys, final Object values) {
        return loadAll(keys);
    }
}