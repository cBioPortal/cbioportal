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