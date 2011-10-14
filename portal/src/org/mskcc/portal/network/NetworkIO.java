
package org.mskcc.portal.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author jj
 */
public final class NetworkIO {
    
    /**
     * private constructor for utility class.
     */
    private NetworkIO(){}
    
    /**
     * Interface for get label from a node
     */
    public static interface NodeLabelHandler {
        /**
         * 
         * @param node a node
         * @return label for the node
         */
        String getLabel(Node node);
    }
    
    /**
     * Read a network from extended SIF of cPath2
     * @param isSif input stream of SIF
     * @return a network
     * @throws IOException if connection failed
     */
    public static Network readNetworkFromCPath2(InputStream isSif, boolean removeSelfEdge) throws IOException {
            Network network = new Network();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(isSif));
            
            // read edges
            String line = bufReader.readLine();
            if (!line.startsWith("PARTICIPANT_A\tINTERACTION_TYPE\tPARTICIPANT_B")) {// if empty
                return network;
            }
            
            String[] edgeHeaders = line.split("\t");
            ArrayList<String> edgeLines = new ArrayList<String>();
            for (line = bufReader.readLine(); !line.isEmpty(); line = bufReader.readLine()) {
                edgeLines.add(line);
            }
            
            // read nodes
            line = bufReader.readLine();
            if (!line.startsWith("PARTICIPANT\tPARTICIPANT_TYPE\tPARTICIPANT_NAME\t"
                    + "UNIFICATION_XREF\tRELATIONSHIP_XREF")) {
                System.err.print("cPath2 format changed.");
                //return network;
            }
            
            String[] nodeHeaders = line.split("\t");
            for (line = bufReader.readLine(); line!=null && !line.isEmpty(); line = bufReader.readLine()) {
                String[] strs = line.split("\t");
                Node node = new Node(strs[0]);
                for (int i=1; i<strs.length && i<nodeHeaders.length; i++) {
                    if (nodeHeaders[i].equals("PARTICIPANT_TYPE")) {
                        NodeType type;
                        if (strs[i].equals("ProteinReference")) {
                            type = NodeType.PROTEIN;
                        } else if (strs[i].equals("SmallMoleculeReference")) {
                            type = NodeType.SMALL_MOLECULE;
                        } else {
                            type = NodeType.UNKNOWN;
                        }
                        node.setType(type);
                    } else {
                        node.addAttribute(nodeHeaders[i], strs[i]);
//                        if (nodeHeaders[i].equals("PARTICIPANT_NAME")) {
//                            for (String name : strs[i].split(";")) {
//                                node.addAttribute("name", name);
//                            }
//                        } else 
                        if (nodeHeaders[i].endsWith("_XREF")) {
                            for (String xref : strs[i].split(";")) {
                                String[] typeId = xref.split(":",2);
                                if (typeId[0].equals("HGNC")) {
                                    node.addAttribute("HGNC", typeId[1].toUpperCase());
                                } 
                            }
                        }
                    }
                }
                
                network.addNode(node);
            }
            
            // add edges
            for (String edgeLine : edgeLines) {
                String[] strs = edgeLine.split("\t");
                
                if (strs.length<3) {// sth. is wrong
                    continue;
                }
                
                if (removeSelfEdge && strs[0].equals(strs[2])) {
                    continue;
                }
                
                String interaction = strs[1];
                Edge edge = new Edge(interaction);
                
                boolean isDirect = false; //TODO: determine directness
                
                for (int i=3; i<strs.length&&i<edgeHeaders.length; i++) {
                    if (edgeHeaders[i].equals("INTERACTION_PUBMED_ID")
                            && !strs[i].startsWith("PubMed:")) {
                        //TODO: REMOVE THIS CHECK AFTER THE CPATH2 PUBMED ISSUE IS FIXED
                        continue;
                    }
                        
                    edge.addAttribute(edgeHeaders[i], strs[i]);
                }
                network.addEdge(edge, strs[0], strs[2], isDirect);
            }
            
            return network;
    }
    
    /**
     * Write network to SIF format
     * @param network network to write
     * @param nlh 
     * @return a string in SIF format
     */    
    public static String writeNetwork2Sif(Network network, NodeLabelHandler nlh) {
        StringBuilder sb = new StringBuilder();
        
        for (Edge edge : network.getEdges()) {
            Node[] nodes = network.getNodes(edge);
            sb.append(nlh.getLabel(nodes[0]));
            sb.append("\t");
            sb.append(edge.getInteractionType());
            sb.append("\t");
            sb.append(nlh.getLabel(nodes[1]));
            sb.append("\n");
        }
        
        return sb.toString();   
    }
    
    /**
     * Write network to GraphML format
     * @param network network to write
     * @param nlh 
     * @return a tring in GraphML format
     */    
    public static String writeNetwork2GraphML(Network network, NodeLabelHandler nlh) {
        Map<String,String> mapNodeAttrNameType = new HashMap<String,String>();
        Map<String,String> mapEdgeAttrNameType = new HashMap<String,String>();
        
        StringBuilder sbNodeEdge = new StringBuilder();
        
        Set<String> nodeTypes = new HashSet<String>();
        for (Node node : network.getNodes()) {
            sbNodeEdge.append("  <node id=\"");
            sbNodeEdge.append(node.getId());
            sbNodeEdge.append("\">\n");
            sbNodeEdge.append("   <data key=\"label\">");
            sbNodeEdge.append(nlh.getLabel(node));
            sbNodeEdge.append("</data>\n");
            
            nodeTypes.add(node.getType().toString());
            sbNodeEdge.append("   <data key=\"type\">");
            sbNodeEdge.append(node.getType().toString());
            sbNodeEdge.append("</data>\n");
            
            exportAttributes(node.getAttributes(),sbNodeEdge,mapNodeAttrNameType);
            sbNodeEdge.append("  </node>\n");
        }
        
        Set<String> edgeTypes = new HashSet<String>();
        for (Edge edge : network.getEdges()) {
            Node[] nodes = network.getNodes(edge);
            sbNodeEdge.append("  <edge source=\"");
            sbNodeEdge.append(nodes[0].getId());
            sbNodeEdge.append("\" target=\"");
            sbNodeEdge.append(nodes[1].getId());
            sbNodeEdge.append("\" directed=\"");
            sbNodeEdge.append(Boolean.toString(network.isEdgeDirected(edge)));
            sbNodeEdge.append("\">\n");
            
            edgeTypes.add(edge.getInteractionType());
            sbNodeEdge.append("   <data key=\"type\">");
            sbNodeEdge.append(edge.getInteractionType());
            sbNodeEdge.append("</data>\n");
            
            exportAttributes(edge.getAttributes(),sbNodeEdge,mapEdgeAttrNameType);
            sbNodeEdge.append("  </edge>\n");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("<graphml>\n");
        sb.append(" <key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n");
        sb.append(" <key id=\"type\" for=\"all\" attr.name=\"type\" attr.type=\"string\"/>\n");
        
        for (Map.Entry<String,String> entry : mapNodeAttrNameType.entrySet()) {
            sb.append(" <key id=\"")
              .append(entry.getKey())
              .append("\" for=\"node\" attr.name=\"")
              .append(entry.getKey())
              .append("\" attr.type=\"")
              .append(entry.getValue())
              .append("\"/>\n");
        }
        
        for (Map.Entry<String,String> entry : mapEdgeAttrNameType.entrySet()) {
            sb.append(" <key id=\"")
              .append(entry.getKey())
              .append("\" for=\"edge\" attr.name=\"")
              .append(entry.getKey())
              .append("\" attr.type=\"")
              .append(entry.getValue())
              .append("\"/>\n");
        }
        
        sb.append(" <graph edgedefault=\"undirected\"\n   parse.edgetypes=\"");
        sb.append(StringUtils.join(edgeTypes,";"));
        sb.append("\"\n   parse.nodetypes=\"");
        sb.append(StringUtils.join(nodeTypes,";"));
        sb.append("\">\n");        
        sb.append(sbNodeEdge);
        sb.append(" </graph>\n");
        
        sb.append("</graphml>\n");
        
        return sb.toString();
    }
    
    private static void exportAttributes(Map<String,Object> attrs, 
            StringBuilder to, Map<String,String> mapAttrNameType) {
        for (Map.Entry<String,Object> entry : attrs.entrySet()) {
            String attr = entry.getKey();
            Object value = entry.getValue();
            
            to.append("   <data key=\"");
            to.append(attr);
            to.append("\">");
            to.append(value);
            to.append("</data>\n");

            String type = getAttrType(value);

            String pre = mapAttrNameType.get(attr);
            if (pre!=null) {
                if (!pre.equals(type)) {
                    mapAttrNameType.put(attr, "string");
                }
            } else {
                mapAttrNameType.put(attr, type);
            }
        }
    }
    
    private static String getAttrType(Object obj) {
        if (obj instanceof Integer) {
            return "integer";
        }
        
        if (obj instanceof Float || obj instanceof Double) {
            return "double";
        }
        
        if (obj instanceof Boolean) {
            return "boolean";
        }
        
        return "string";
    }
}
