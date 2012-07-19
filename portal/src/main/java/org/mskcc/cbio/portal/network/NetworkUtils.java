
package org.mskcc.portal.network;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
        String strXrefs = (String)node.getAttribute("RELATIONSHIP_XREF");
        if (strXrefs==null) {
            return null;
        }

        Pattern pattern = Pattern.compile("HGNC:([^;]+)");
        Matcher matcher = pattern.matcher(strXrefs);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
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
        Map<String,Node> mapSymbolNode = new HashMap<String,Node>();
        Set<Node> deleteNodes = new HashSet();
        for (Node node : net.getNodes()) {
            String symbol = getSymbol(node);
            if (symbol!=null) {
                Node mergeTo = mapSymbolNode.get(symbol);
                if (mergeTo==null) {
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
            if (ends[0] == mergeFrom) {
                ends[0] = mergeTo;
            } else {
                ends[1] = mergeTo;
            }
            
            net.removeEdge(edge);
            net.addEdge(edge, ends[0].getId(), ends[1].getId());
        }
    }
    
    /**
     * 
     * @param mergeTo
     * @param mergeFrom
     * @param attr 
     */
    private static void mergeStringAttributes(Node mergeTo, Node mergeFrom, String attr) {
        Set<String> attrs = new LinkedHashSet<String>();
        
        String attr2 = (String)mergeFrom.getAttribute(attr);
        if (attr2==null) {
            return;
        }
        
        String attr1 = (String)mergeTo.getAttribute(attr);
        if (attr1!=null) {
            attrs.addAll(Arrays.asList(attr1.split(";")));
        }
        
        if (attrs.addAll(Arrays.asList(attr2.split(";")))) {
            mergeTo.setAttribute(attr, StringUtils.join(attrs,";"));
        }
    }
}
