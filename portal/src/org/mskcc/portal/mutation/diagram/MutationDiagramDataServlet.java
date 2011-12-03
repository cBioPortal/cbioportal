package org.mskcc.portal.mutation.diagram;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Mutation diagram data servlet.
 */
@Singleton
public final class MutationDiagramDataServlet extends HttpServlet {
    /** Default serial version UID. */
    private static final long serialVersionUID = 1L;

    private final DomainService domainService;
    private final IdMappingService idMappingService;
    private final MutationService mutationService;

    @Inject
    public MutationDiagramDataServlet(final DomainService domainService, final IdMappingService idMappingService, final MutationService mutationService) {
        this.domainService = domainService;
        this.idMappingService = idMappingService;
        this.mutationService = mutationService;
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        // todo:  check and sanitize hugoGeneSymbol if necessary
        String hugoGeneSymbol = request.getParameter("hugoGeneSymbol");
        List<String> uniProtIds = idMappingService.getUniProtIds(hugoGeneSymbol);
        String uniProtId = uniProtIds.get(0); // uh oh
        List<Domain> domains = domainService.getDomains(uniProtId);
        int length = domains.get(0).getEnd();
        List<Mutation> mutations = mutationService.getMutations(hugoGeneSymbol);
        String label = hugoGeneSymbol + "/" + uniProtId;
        MutationDiagram mutationDiagram = new MutationDiagram(hugoGeneSymbol, label, length, domains.subList(1, domains.size()), mutations);
        response.setContentType("application/json");
        response.getWriter().append(mutationDiagram.toJSONString());
    }
}
