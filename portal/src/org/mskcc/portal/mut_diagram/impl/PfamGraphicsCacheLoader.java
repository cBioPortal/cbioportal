package org.mskcc.portal.mut_diagram.impl;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.mskcc.portal.mut_diagram.Sequence;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

/**
 * Feature service cache loader that calls the Pfam graphics service.
 */
final class PfamGraphicsCacheLoader extends CacheLoader<String, List<Sequence>> {
    private static final Logger logger = Logger.getLogger(PfamGraphicsCacheLoader.class);
    static final String URL_PREFIX = "http://pfam.sanger.ac.uk/protein/";
    static final String URL_SUFFIX = "/graphic";
    private final ObjectMapper objectMapper;

    @Inject
    PfamGraphicsCacheLoader(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Sequence> load(final String uniProtId) throws Exception {
        List<Sequence> sequences = new LinkedList<Sequence>();
        try {
            TypeFactory typeFactory = objectMapper.getTypeFactory();
            CollectionType sequenceList = typeFactory.constructCollectionType(List.class, Sequence.class);

            URL url = new URL(URL_PREFIX + uniProtId + URL_SUFFIX);
            sequences = objectMapper.readValue(url, sequenceList);
        }
        catch (Exception e) {
            logger.error("could not load features for " + uniProtId, e);
        }
        return ImmutableList.copyOf(sequences);
    }
}
