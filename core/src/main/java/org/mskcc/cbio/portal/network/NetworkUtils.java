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

package org.mskcc.cbio.portal.network;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jgao
 */
public final class NetworkUtils {

    private NetworkUtils() {}

    public static String getSymbol(Node node) {
        String strXrefs = (String) node.getAttribute("RELATIONSHIP_XREF");
        if (strXrefs == null) {
            return null;
        }

        Pattern pattern = Pattern.compile(
            "HGNC Symbol:([^;]+)|HGNC SYMBOL:([^;]+)|HGNC:([^;]+)"
        );
        Matcher matcher = pattern.matcher(strXrefs);
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                return matcher.group(1).toUpperCase();
            } else if (matcher.group(2) != null) {
                return matcher.group(2).toUpperCase();
            } else {
                return matcher.group(3).toUpperCase();
            }
        } else {
            return null;
        }
    }

    /**
     *
     */
    public static interface NodeSelector {
        /**
         *
         * @return
         */
        boolean select(Node node);
    }

    /**
     *
     * @param net
     * @param nodeSelector
     */
    public static void pruneNetwork(Network net, NodeSelector nodeSelector) {
        Set<Node> deleteNodes = new HashSet<Node>();
        for (Node node : net.getNodes()) {
            if (nodeSelector.select(node)) {
                deleteNodes.add(node);
            }
        }

        for (Node node : deleteNodes) {
            net.removeNode(node);
        }
    }

    /**
     *
     * @param net
     */
    public static void mergeNodesWithSameSymbol(Network net) {
        Map<String, Node> mapSymbolNode = new HashMap<String, Node>();
        Set<Node> deleteNodes = new HashSet();
        for (Node node : net.getNodes()) {
            String symbol = getSymbol(node);
            if (symbol != null) {
                Node mergeTo = mapSymbolNode.get(symbol);
                if (mergeTo == null) {
                    mapSymbolNode.put(symbol, node);
                } else {
                    mergeNodes(net, mergeTo, node);
                    deleteNodes.add(node);
                }
            }
        }

        for (Node node : deleteNodes) {
            net.removeNode(node);
        }
    }

    /**
     *
     * @param net
     * @param mergeTo
     * @param mergeFrom
     */
    private static void mergeNodes(Network net, Node mergeTo, Node mergeFrom) {
        // merge attributes
        mergeStringAttributes(mergeTo, mergeFrom, "PARTICIPANT_NAME");
        mergeStringAttributes(mergeTo, mergeFrom, "UNIFICATION_XREF");
        mergeStringAttributes(mergeTo, mergeFrom, "RELATIONSHIP_XREF");

        // merge edges
        for (Edge edge : net.getIncidentEdges(mergeFrom)) {
            Node[] ends = net.getNodes(edge);
            if (ends[0].equals(mergeFrom)) {
                ends[0] = mergeTo;
            } else {
                ends[1] = mergeTo;
            }

            net.removeEdge(edge);
            //TODO check if this edge already occurs between this nodes
            edge.setSourceID(ends[0].getId());
            edge.setTargetID(ends[1].getId());

            if (net.findEdgeSet(mergeTo, mergeFrom).size() > 0) {
                for (
                    Iterator iterator = net
                        .findEdgeSet(mergeTo, mergeFrom)
                        .iterator();
                    iterator.hasNext();
                ) {
                    Edge tempEdge = (Edge) iterator.next();

                    if (!tempEdge.hasSameSourceTargetAndType(edge)) net.addEdge(
                        edge
                    );
                }
            }
        }
    }

    /**
     *
     * @param mergeTo
     * @param mergeFrom
     * @param attr
     */
    private static void mergeStringAttributes(
        Node mergeTo,
        Node mergeFrom,
        String attr
    ) {
        Set<String> attrs = new LinkedHashSet<String>();

        String attr2 = (String) mergeFrom.getAttribute(attr);
        if (attr2 == null) {
            return;
        }

        String attr1 = (String) mergeTo.getAttribute(attr);
        if (attr1 != null) {
            attrs.addAll(Arrays.asList(attr1.split(";")));
        }

        if (attrs.addAll(Arrays.asList(attr2.split(";")))) {
            mergeTo.setAttribute(attr, StringUtils.join(attrs, ";"));
        }
    }
}
