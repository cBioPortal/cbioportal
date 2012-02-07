package org.mskcc.portal.mutation.diagram.pfam;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.mskcc.portal.mutation.diagram.IdMappingService;
import org.mskcc.portal.mutation.diagram.Mutation;
import org.mskcc.portal.mutation.diagram.MutationService;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Mutation diagram data servlet.
 */
@Singleton
public final class MutationDiagramDataServlet extends HttpServlet {
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper;
    private final FeatureService featureService;
    private final IdMappingService idMappingService;
    private final MutationService mutationService;

    @Inject
    public MutationDiagramDataServlet(final ObjectMapper objectMapper, final FeatureService featureService, final IdMappingService idMappingService, final MutationService mutationService) {
        this.objectMapper = objectMapper;
        this.featureService = featureService;
        this.idMappingService = idMappingService;
        this.mutationService = mutationService;
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // todo:  check and sanitize hugoGeneSymbol if necessary
        String hugoGeneSymbol = request.getParameter("hugoGeneSymbol");
        List<String> uniProtIds = idMappingService.getUniProtIds(hugoGeneSymbol);

        String uniProtId = uniProtIds.get(0); // uh oh
        List<Sequence> sequences = featureService.getFeatures(uniProtId);
        if (!sequences.isEmpty()) {
            Sequence sequence = sequences.get(0);
            if (sequence.getMetadata() == null) {
                sequence.setMetadata(new HashMap<String, Object>());
            }
            sequence.getMetadata().put("hugoGeneSymbol", hugoGeneSymbol);
            sequence.getMetadata().put("uniProtId", uniProtId);
            List<Mutation> mutations = mutationService.getMutations(hugoGeneSymbol);

            for (Mutation mutation : mutations) {
                Markup markup = new Markup();
                markup.setDisplay("true"); // may need to be boolean
                markup.setStart(mutation.getLocation());
                markup.setColour(ImmutableList.of("#f36"));
                markup.setLineColour("#666");
                markup.setHeadStyle("diamond");
                markup.setV_align("top");
                markup.setType("mutation");
                markup.setMetadata(new HashMap<String, Object>());
                markup.getMetadata().put("count", mutation.getCount());
                markup.getMetadata().put("type", mutation.getLabel());
                markup.getMetadata().put("description", "Mutation: " + mutation.getLabel() + " (N=" + mutation.getCount() + ")");
                markup.getMetadata().put("start", mutation.getLocation());
                markup.getMetadata().put("database", "cBio Portal");

                sequence.getMarkups().add(markup);
            }
        }
        response.setContentType("application/json");
        objectMapper.writeValue(response.getWriter(), sequences);
    }
}
