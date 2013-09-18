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

package org.mskcc.cbio.portal.graph;

import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Interaction;
import org.mskcc.cbio.portal.dao.DaoInteraction;
import org.mskcc.cbio.portal.dao.DaoException;

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
        this(geneList, null); 
    }

    /**
     * Constructor.
     *
     * @param geneList ArrayList of Seed Genes.
     * @throws DaoException Database Error.
     */
    public NetworkOfInterest (ArrayList<CanonicalGene> geneList,
            Collection<String> dataSources) throws DaoException {
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        ArrayList<Interaction> interactionList = new ArrayList<Interaction>();
        HashSet<String> seedSet = new HashSet<String>();

        for (CanonicalGene gene:  geneList) {

            //  Get all interactions involving current gene.
            ArrayList<Interaction> currentInteractionList = 
                    daoInteraction.getInteractions(gene, dataSources);
            interactionList.addAll(currentInteractionList);
            seedSet.add(gene.getHugoGeneSymbolAllCaps());
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