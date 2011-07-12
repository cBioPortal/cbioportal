
package org.mskcc.portal.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jj
 */
public class NetworkIO {
    
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
    public static Network readNetworkFromCPath2(InputStream isSif) throws IOException {
            Network network = new Network();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(isSif));
            
            // read edges
            String line = bufReader.readLine();
            String[] edgeHeaders = line.split("\t");            
            while (!(line = bufReader.readLine()).isEmpty()) {
                String[] strs = line.split("\t");
                Node source = network.getNodeById(strs[0]);
                if (source==null)
                    source = new Node(strs[0]);
                Node target = network.getNodeById(strs[2]);
                if (target==null)
                    target = new Node(strs[2]);
                String interaction = strs[1];
                Edge edge = new Edge(source, target, interaction);
                for (int i=3; i<strs.length&&i<edgeHeaders.length; i++) {
                    if (edgeHeaders[i].equals("INTERACTION_PUBMED_ID")) {
                        for (String pubmed : strs[i].split(";")) {
                            edge.addAttribute(edgeHeaders[i], pubmed);
                        }
                    } else {
                        edge.addAttribute(edgeHeaders[i], strs[i]);
                    }
                }
                network.addEdge(edge);
            }
            
            // read nodes xrefs
            line = bufReader.readLine();
            if (!line.startsWith("PARTICIPANT\tUNIFICATION_XREF\tRELATIONSHIP_XREF")) {
                System.err.print("cPath2 format changed.");
                return network;
            }
            while ((line = bufReader.readLine())!=null && !line.isEmpty()) {
                String[] strs = line.split("\t");
                Node node = network.getNodeById(strs[0]);
                for (int i=1; i<strs.length && i<3; i++) {
                    for (String xref : strs[i].split(";")) {
                        String[] typeId = xref.split(":",2);
                        node.addXref(typeId[0], typeId[1]);
                    }
                    
                }
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
        
        List<Edge> edges = network.getEdges();
        for (Edge edge : edges) {
            sb.append(nlh.getLabel(edge.getSourceNode()));
            sb.append("\t");
            sb.append(edge.getInteractionType());
            sb.append("\t");
            sb.append(nlh.getLabel(edge.getTargetNode()));
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
        StringBuilder sb = new StringBuilder();
        sb.append("<graphml>\n");
        sb.append(" <graph edgedefault=\"undirected\">\n");
        for (Node node : network.getNodes()) {
            sb.append("  <node id=\"");
            sb.append(node.getId());
            sb.append("\">\n");
            sb.append("   <data key=\"label\">");
            sb.append(nlh.getLabel(node));
            sb.append("</data>\n");
            for (Attribute av : node.getAttributes()) {
                String attr = av.getName();
                Object value = av.getValue();
                
                sb.append("   <data key=\"");
                sb.append(attr);
                sb.append("\">");
                sb.append(value);
                sb.append("</data>\n");
                
                String type = getAttrType(value);
                
                String pre = mapNodeAttrNameType.get(attr);
                if (pre!=null) {
                    if (!pre.equals(type))
                        mapNodeAttrNameType.put(attr, "string");
                } else {
                    mapNodeAttrNameType.put(attr, type);
                }
            }
            sb.append("  </node>\n");
        }
        
        for (Edge edge : network.getEdges()) {
            sb.append("  <edge source=\"");
            sb.append(edge.getSourceNode().getId());
            sb.append("\" target=\"");
            sb.append(edge.getTargetNode().getId());
            sb.append("\">\n");
            
            sb.append("   <data key=\"type\">");
            sb.append(edge.getInteractionType());
            sb.append("</data>\n");
            mapEdgeAttrNameType.put("type", "string");
            
            for (Attribute av : edge.getAttributes()) {
                String attr = av.getName();
                Object value = av.getValue();
                
                sb.append("   <data key=\"");
                sb.append(attr);
                sb.append("\">");
                sb.append(value);
                sb.append("</data>\n");
                
                String type = getAttrType(value);
                
                String pre = mapEdgeAttrNameType.get(attr);
                if (pre!=null) {
                    if (!pre.equals(type))
                        mapEdgeAttrNameType.put(attr, "string");
                } else {
                    mapEdgeAttrNameType.put(attr, type);
                }
            }
            sb.append("  </edge>\n");
        }
        sb.append(" </graph>\n");
        sb.append(" <key id=\"label\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>\n");
        
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
        
        sb.append("</graphml>\n");
        
        return sb.toString();
    }
    
    private static String getAttrType(Object obj) {
        if (obj instanceof Integer)
            return "integer";
        if (obj instanceof Float || obj instanceof Double)
            return "double";
        if (obj instanceof Boolean)
            return "boolean";
        
        return "string";
    }
}
