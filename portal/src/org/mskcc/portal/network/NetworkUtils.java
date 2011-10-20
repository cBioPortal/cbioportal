
package org.mskcc.portal.network;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
}
