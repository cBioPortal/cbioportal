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

package org.mskcc.cbio.portal.graph;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Interaction;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility Class for converting Interaction Objects to JUNG 2.0 Graphs.
 *
 * @author Ethan Cerami.
 */
public final class InteractionToJung {
    private static final String DELIM = ":";

    /**
     * Private constructor to prevent instantiation.
     */
    private InteractionToJung() {
    }

    /**
     * Creates a JUNG Graph from the Specified List of Interactions.
     *
     * @param interactionList ArrayList of Interaction Objects.
     * @return JUNG Graph.
     * @throws DaoException Database Error.
     */
    public static Graph<String, String> createGraph(ArrayList<Interaction> interactionList)
            throws DaoException {
        Graph<String, String> graph = new UndirectedSparseMultigraph<String, String>();

        //  Add Interactions
        addInteractionsToGraph(interactionList, graph);

        //  All done.  Return the graph.
        return graph;
    }

    private static void addInteractionsToGraph(ArrayList<Interaction> interactionList,
            Graph<String, String> g) throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        Set<String> edgeSet = new HashSet<String>();
        for (Interaction interaction : interactionList) {
            long geneAId = interaction.getGeneA();
            long geneBId = interaction.getGeneB();
            CanonicalGene geneA = daoGene.getGene(geneAId);
            CanonicalGene geneB = daoGene.getGene(geneBId);

            if (geneA == null) {
                throw new NullPointerException ("Cannot identify gene for:  " + geneAId);
            }
            if (geneB == null) {
                throw new NullPointerException ("Cannot identify gene for:  " + geneBId);
            }
            g.addVertex(geneA.getHugoGeneSymbolAllCaps());
            g.addVertex(geneB.getHugoGeneSymbolAllCaps());
            String edgeParams = interaction.getInteractionType() + DELIM + interaction.getSource()
                + DELIM + interaction.getExperimentTypes() + DELIM + interaction.getPmids();
            String completeEdgeKey = createKey(edgeParams, geneA, geneB);
            if (!edgeSet.contains(completeEdgeKey)) {
                g.addEdge(completeEdgeKey, geneA.getHugoGeneSymbolAllCaps(), geneB.getHugoGeneSymbolAllCaps());
                edgeSet.add(completeEdgeKey);
            }
        }
    }

    private static String createKey (String edgeKey, CanonicalGene geneA, CanonicalGene geneB){
        long idA = geneA.getEntrezGeneId();
        long idB = geneB.getEntrezGeneId();
        if (idA < idB) {
            return edgeKey + DELIM + idA + DELIM + idB;
        } else {
            return edgeKey + DELIM + idB + DELIM + idA;
        }
    }
}
