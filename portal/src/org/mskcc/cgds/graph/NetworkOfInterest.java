package org.mskcc.cgds.graph;

import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.Interaction;
import org.mskcc.cgds.dao.DaoInteraction;
import org.mskcc.cgds.dao.DaoException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import edu.uci.ics.jung.graph.Graph;

/**
 * Generates a Network of Interest, based on Input Set of Seed Genes.
 *
 * @author Ethan Cerami.
 */
public class NetworkOfInterest {
    private String tabDelim;
    private Graph<String, String> graph;

    /**
     * Constructor.
     *
     * @param geneList ArrayList of Seed Genes.
     * @throws DaoException Database Error.
     */
    public NetworkOfInterest (ArrayList<CanonicalGene> geneList) throws DaoException {
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        ArrayList<Interaction> interactionList = new ArrayList<Interaction>();
        HashSet<String> seedSet = new HashSet<String>();

        for (CanonicalGene gene:  geneList) {

            //  Get all interactions involving current gene.
            ArrayList<Interaction> currentInteractionList = daoInteraction.getInteractions(gene);
            interactionList.addAll(currentInteractionList);
            seedSet.add(gene.getHugoGeneSymbol());
        }

        //  Convert to JUNG to Graph
        graph = InteractionToJung.createGraph(interactionList);

        //  For now, mark all nodes that have degree = 1, and mark for removal
        Collection<String> vertexCollection = graph.getVertices();
        ArrayList<String> vertexList = new ArrayList<String>(vertexCollection);
        ArrayList<String> deleteList = new ArrayList<String>();
        for (String gene:  vertexList) {
            int seedDegree = 0;
            Iterator<String> neighborIterator = graph.getNeighbors(gene).iterator();
            while (neighborIterator.hasNext()) {
                String neighbor = neighborIterator.next();
                if (seedSet.contains(neighbor)) {
                    seedDegree++;
                }
            }
            if (seedDegree == 1 && !seedSet.contains(gene)) {
                // System.out.println ("Mark for deletion:  " + gene);
                deleteList.add(gene);
            }
        }

        //  Remove marked genes
        for (String gene:  deleteList) {
            graph.removeVertex(gene);
        }

        //  Convert to Tab Delim
        tabDelim = JungToTabDelim.convertToSif(graph);
    }

    /**
     * Gets the Tab Delim Output.
     * @return Simple Tab Delim Output.
     */
    public String getTabDelim() {
        return tabDelim;
    }

    /**
     * Gets the JUNG Graph.
     * @return JUNG Graph Object.
     */
    public Graph<String, String> getGraph() {
        return graph;
    }
}