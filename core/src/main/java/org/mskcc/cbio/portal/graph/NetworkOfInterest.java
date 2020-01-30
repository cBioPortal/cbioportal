/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.graph;

import edu.uci.ics.jung.graph.Graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoInteraction;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Interaction;

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
    public NetworkOfInterest(ArrayList<CanonicalGene> geneList)
        throws DaoException {
        this(geneList, null);
    }

    /**
     * Constructor.
     *
     * @param geneList ArrayList of Seed Genes.
     * @throws DaoException Database Error.
     */
    public NetworkOfInterest(
        ArrayList<CanonicalGene> geneList,
        Collection<String> dataSources
    )
        throws DaoException {
        DaoInteraction daoInteraction = DaoInteraction.getInstance();
        ArrayList<Interaction> interactionList = new ArrayList<Interaction>();
        HashSet<String> seedSet = new HashSet<String>();

        for (CanonicalGene gene : geneList) {
            //  Get all interactions involving current gene.
            ArrayList<Interaction> currentInteractionList = daoInteraction.getInteractions(
                gene,
                dataSources
            );
            interactionList.addAll(currentInteractionList);
            seedSet.add(gene.getHugoGeneSymbolAllCaps());
        }

        //  Convert to JUNG to Graph
        graph = InteractionToJung.createGraph(interactionList);

        //  For now, mark all nodes that have degree = 1, and mark for removal
        Collection<String> vertexCollection = graph.getVertices();
        ArrayList<String> vertexList = new ArrayList<String>(vertexCollection);
        ArrayList<String> deleteList = new ArrayList<String>();
        for (String gene : vertexList) {
            int seedDegree = 0;
            for (String neighbor : graph.getNeighbors(gene)) {
                if (seedSet.contains(neighbor)) {
                    seedDegree++;
                }
            }
            if (seedDegree == 1 && !seedSet.contains(gene)) {
                deleteList.add(gene);
            }
        }

        //  Remove marked genes
        for (String gene : deleteList) {
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
