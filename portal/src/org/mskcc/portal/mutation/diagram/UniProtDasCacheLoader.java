package org.mskcc.portal.mutation.diagram;

import java.util.LinkedList;
import java.util.List;

import uk.ac.ebi.das.jdas.adapters.features.DasGFFAdapter;
import uk.ac.ebi.das.jdas.adapters.features.FeatureAdapter;
import uk.ac.ebi.das.jdas.client.FeaturesClient;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;

/**
 * Domain service cache loader that calls the UniProt DAS service with a feature request.
 */
final class UniProtDasCacheLoader extends CacheLoader<String, List<Domain>> {
    static final String SERVER_URL = "http://www.ebi.ac.uk/das-srv/uniprot/das/uniprot/";
    static final List<String> TYPES = ImmutableList.of("SO:0000417");

    /** {@inheritDoc} */
    public List<Domain> load(final String uniProtId) throws Exception {
        List<Domain> domains = new LinkedList<Domain>();
        List<String> segments = ImmutableList.of(uniProtId);

        FeaturesClient featuresClient = new FeaturesClient();
        // todo: four null arguments, ick
        DasGFFAdapter gffAdapter = featuresClient.fetchData(SERVER_URL, segments, TYPES, null, null, null, null);
        // todo: assumes there will be at least one segment
        DasGFFAdapter.SegmentAdapter segment = gffAdapter.getGFF().getSegment().get(0);
        for (FeatureAdapter feature : segment.getFeature()) {
            String label = feature.getLabel();
            int start = feature.getStart();
            int end = feature.getEnd();
            domains.add(new Domain(label, start, end));
        }
        return ImmutableList.copyOf(domains);
    }
}
