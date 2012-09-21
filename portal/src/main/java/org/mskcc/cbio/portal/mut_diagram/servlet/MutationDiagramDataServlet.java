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

package org.mskcc.cbio.portal.mut_diagram.servlet;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static org.codehaus.jackson.map.DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.DaoGeneOptimized;
import org.mskcc.cbio.cgds.model.ExtendedMutation;
import org.mskcc.cbio.portal.mut_diagram.FeatureService;
import org.mskcc.cbio.portal.mut_diagram.IdMappingService;
import org.mskcc.cbio.portal.mut_diagram.Markup;
import org.mskcc.cbio.portal.mut_diagram.Pileup;
import org.mskcc.cbio.portal.mut_diagram.Sequence;
import org.mskcc.cbio.portal.mut_diagram.impl.CacheFeatureService;
import org.mskcc.cbio.portal.mut_diagram.impl.CgdsIdMappingService;
import org.mskcc.cbio.portal.mut_diagram.impl.PfamGraphicsCacheLoader;

import com.google.common.collect.ImmutableList;

/**
 * Mutation diagram data servlet.
 */
public final class MutationDiagramDataServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(MutationDiagramDataServlet.class);
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;
    private static final List<Sequence> EMPTY = Collections.emptyList();

    private final ObjectMapper objectMapper;
    private final FeatureService featureService;
    private final IdMappingService idMappingService;

    public MutationDiagramDataServlet() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        PfamGraphicsCacheLoader cacheLoader = new PfamGraphicsCacheLoader(objectMapper);
        featureService = new CacheFeatureService(cacheLoader);

        try {
            idMappingService = new CgdsIdMappingService(DaoGeneOptimized.getInstance());
        }
        catch (DaoException e) {
            throw new RuntimeException("could not create id mapping service", e);
        }
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // todo:  check and sanitize hugoGeneSymbol and mutations if necessary
        String hugoGeneSymbol = request.getParameter("hugoGeneSymbol");

        List<String> uniProtIds = idMappingService.getUniProtIds(hugoGeneSymbol);
        if (uniProtIds.isEmpty()) {
            writeSequencesToResponse(EMPTY, response);
            return;
        }

        String uniProtId = uniProtIds.get(0);
        List<Sequence> sequences = featureService.getFeatures(uniProtId);
        if (sequences.isEmpty()) {
            writeSequencesToResponse(EMPTY, response);
            return;
        }

        Sequence sequence = sequences.get(0);
        if (sequence.getMetadata() == null) {
            Map<String, Object> metadata = newHashMap();
            sequence.setMetadata(metadata);
        }
        sequence.getMetadata().put("hugoGeneSymbol", hugoGeneSymbol);
        sequence.getMetadata().put("uniProtId", uniProtId);
 
        List<Markup> markups = newArrayList();
        List<ExtendedMutation> mutations = readMutations(request.getParameter("mutations"));
        for (Pileup pileup : Pileup.pileup(mutations)) {
            Markup markup = new Markup();
            markup.setDisplay("true");
            markup.setStart(pileup.getLocation());
            markup.setEnd(pileup.getLocation());
            markup.setColour(
		            ImmutableList.of("#b40000", // lolipop fill color
                                     "#c00000", // missense color (histogram)
                                     "#353535")); // other than missense (histogram)
            markup.setLineColour("#babdb6");
            markup.setHeadStyle("diamond");
            markup.setV_align("top");
            markup.setType("mutation");
            markup.setMetadata(new HashMap<String, Object>());
            markup.getMetadata().put("count", pileup.getCount());
	        markup.getMetadata().put("missenseCount", pileup.getMissenseCount());
            markup.getMetadata().put("label", pileup.getLabel());
            markup.getMetadata().put("location", pileup.getLocation());
            markups.add(markup);
        }
        writeSequencesToResponse(ImmutableList.of(sequence.withMarkups(markups)), response);
    }

    private void writeSequencesToResponse(final List<Sequence> sequences, final HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), sequences);
    }

    /**
     * Read and return a list of extended mutations from the specified value in JSON format
     * or an empty list if the value cannot be read.
     *
     * @param value list of extended mutations in JSON format
     * @return a list of extended mutations from the specified value in JSON format, or an
     *    empty list if the value cannot be read
     */
    List<ExtendedMutation> readMutations(final String value) {
        List<ExtendedMutation> mutations = Collections.emptyList();
        if (value != null) {
            try {
                TypeFactory typeFactory = objectMapper.getTypeFactory();
                CollectionType sequenceList = typeFactory.constructCollectionType(List.class, ExtendedMutation.class);
                mutations = objectMapper.readValue(value, sequenceList);
            }
            catch (JsonParseException e) {
                logger.warn("could not deserialize extended mutations", e);
            }
            catch (JsonMappingException e) {
                logger.warn("could not deserialize extended mutations", e);
            }
            catch (IOException e) {
                logger.warn("could not deserialize extended mutations", e);
            }
        }
        return mutations;
    }
}
