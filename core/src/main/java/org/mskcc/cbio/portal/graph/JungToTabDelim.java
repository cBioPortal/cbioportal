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
    private JungToTabDelim() {}

    /**
     * Converts the Specified JUNG Graph to a Cytoscape SIF Format.
     *
     * @param g JUNG Graph.
     * @return Simple Tab Delimited Format.
     */
    public static String convertToSif(Graph<String, String> g) {
        StringBuffer buf = new StringBuffer();
        for (String edge : g.getEdges()) {
            Pair pair = g.getEndpoints(edge);
            String geneA = (String) pair.getFirst();
            String geneB = (String) pair.getSecond();
            buf
                .append(geneA)
                .append(TAB)
                .append(edge)
                .append(TAB)
                .append(geneB)
                .append("\n");
        }
        return buf.toString();
    }
}
