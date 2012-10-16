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

package org.mskcc.cbio.cgds.graph;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

import java.util.Iterator;

/**
 * Utility Class for Converting a JUNG Graph to a Tab-Delimited Tab Format.
 *
 * @author Ethan Cerami.
 */
public final class JungToTabDelim {
    private static final String TAB = "\t";

    /**
     * Private Constructor to prevent instantiation.
     */
    private JungToTabDelim() {
    }

    /**
     * Converts the Specified JUNG Graph to a Cytoscape SIF Format.
     *
     * @param g JUNG Graph.
     * @return Simple Tab Delimited Format.
     */
    public static String convertToSif(Graph<String, String> g) {
        StringBuffer buf = new StringBuffer();
        Iterator edgeIterator = g.getEdges().iterator();
        while (edgeIterator.hasNext()) {
            String edge = (String) edgeIterator.next();
            Pair pair = g.getEndpoints(edge);
            String geneA = (String) pair.getFirst();
            String geneB = (String) pair.getSecond();
            buf.append(geneA + TAB + edge + TAB + geneB + "\n");
        }
        return buf.toString();
    }
}