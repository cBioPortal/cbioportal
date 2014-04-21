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

package org.mskcc.cbio.portal.mut_diagram.impl;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.mskcc.cbio.portal.mut_diagram.Sequence;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;

/**
 * Feature service cache loader that calls the Pfam graphics service.
 */
public final class PfamGraphicsCacheLoader extends CacheLoader<String, List<Sequence>> {
    private static final Logger logger = Logger.getLogger(PfamGraphicsCacheLoader.class);
    static final String URL_PREFIX = "http://pfam.sanger.ac.uk/protein/";
    static final String URL_SUFFIX = "/graphic";
    private final ObjectMapper objectMapper;

    public PfamGraphicsCacheLoader(final ObjectMapper objectMapper) {
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
