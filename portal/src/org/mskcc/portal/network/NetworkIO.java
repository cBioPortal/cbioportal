
package org.mskcc.portal.network;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

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
    public static Network readNetworkFromCPath2(InputStream isSif, boolean removeSelfEdge) throws IOException {
            Network network = new Network();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(isSif));
            
            // read edges
            String line = bufReader.readLine();
            if (!line.startsWith("PARTICIPANT_A\tINTERACTION_TYPE\tPARTICIPANT_B")) // if empty
                return network;
            
            String[] edgeHeaders = line.split("\t");            
            while (!(line = bufReader.readLine()).isEmpty()) {
                String[] strs = line.split("\t");
                
                if (strs.length<3) // sth. is wrong
                    continue;
                
                Node source = network.getNodeById(strs[0]);
                if (source==null)
                    source = new Node(strs[0]);
                network.addNode(source);
                
                Node target = network.getNodeById(strs[2]);
                if (target==null)
                    target = new Node(strs[2]);
                
                if (removeSelfEdge && target==source)
                    continue;
                
                String interaction = strs[1];
                Edge edge = new Edge(source, target, interaction);
                for (int i=3; i<strs.length&&i<edgeHeaders.length; i++) {
                    if (edgeHeaders[i].equals("INTERACTION_PUBMED_ID")) {
                        for (String pubmed : strs[i].split(";")) {
                            if (pubmed.startsWith("PubMed:")) // fix wrong pubmed problem
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
                        if (typeId[0].equals("HGNC"))
                            node.addXref(typeId[0], typeId[1].toUpperCase());
                        else
                            node.addXref(typeId[0], typeId[1]);
                    }
                    
                }
            }
            
            // set node types
            // TODO: remove this after node types are exported
            for (Node node : network.getNodes()) {
                String id = node.getId();
                String type;
                if (id.startsWith("urn:miriam:uniprot:"))
                    type = "Protein";
                else if (id.startsWith("urn:miriam:chebi:"))
                    type = "SmallMolecule";
                else
                    type = "Unknown";
                node.setType(type);
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
        
        StringBuilder sbNodeEdge = new StringBuilder();
        
        Set<String> nodeTypes = new HashSet<String>();
        for (Node node : network.getNodes()) {
            sbNodeEdge.append("  <node id=\"");
            sbNodeEdge.append(node.getId());
            sbNodeEdge.append("\">\n");
            sbNodeEdge.append("   <data key=\"label\">");
            sbNodeEdge.append(nlh.getLabel(node));
            sbNodeEdge.append("</data>\n");
            
            nodeTypes.add(node.getType());
            sbNodeEdge.append("   <data key=\"type\">");
            sbNodeEdge.append(node.getType());
            sbNodeEdge.append("</data>\n");
            
            for (Attribute av : node.getAttributes()) {
                String attr = av.getName();
                Object value = av.getValue();
                
                sbNodeEdge.append("   <data key=\"");
                sbNodeEdge.append(attr);
                sbNodeEdge.append("\">");
                sbNodeEdge.append(value);
                sbNodeEdge.append("</data>\n");
                
                String type = getAttrType(value);
                
                String pre = mapNodeAttrNameType.get(attr);
                if (pre!=null) {
                    if (!pre.equals(type))
                        mapNodeAttrNameType.put(attr, "string");
                } else {
                    mapNodeAttrNameType.put(attr, type);
                }
            }
            sbNodeEdge.append("  </node>\n");
        }
        
        Set<String> edgeTypes = new HashSet<String>();
        for (Edge edge : network.getEdges()) {
            sbNodeEdge.append("  <edge source=\"");
            sbNodeEdge.append(edge.getSourceNode().getId());
            sbNodeEdge.append("\" target=\"");
            sbNodeEdge.append(edge.getTargetNode().getId());
            sbNodeEdge.append("\">\n");
            
            edgeTypes.add(edge.getInteractionType());
            sbNodeEdge.append("   <data key=\"type\">");
            sbNodeEdge.append(edge.getInteractionType());
            sbNodeEdge.append("</data>\n");
            
            for (Attribute av : edge.getAttributes()) {
                String attr = av.getName();
                Object value = av.getValue();
                
                sbNodeEdge.append("   <data key=\"");
                sbNodeEdge.append(attr);
                sbNodeEdge.append("\">");
                sbNodeEdge.append(value);
                sbNodeEdge.append("</data>\n");
                
                String type = getAttrType(value);
                
                String pre = mapEdgeAttrNameType.get(attr);
                if (pre!=null) {
                    if (!pre.equals(type))
                        mapEdgeAttrNameType.put(attr, "string");
                } else {
                    mapEdgeAttrNameType.put(attr, type);
                }
            }
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
