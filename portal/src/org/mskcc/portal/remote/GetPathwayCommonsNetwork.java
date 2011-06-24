
package org.mskcc.portal.remote;

import org.mskcc.portal.util.XDebug;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.URL;

import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gets network from Pathway Commons.
 * @author jj
 */
public class GetPathwayCommonsNetwork {

    /**
     * Gets network of interest of seed genes.
     *
     * @param geneList Gene List.
     * @return Tab-delimited content.
     */
    public String getNetwork (List<String> geneList,
          XDebug xdebug) throws RemoteException {
        try {
            StringBuilder sbUrl = new StringBuilder("http://www.pathwaycommons.org/pc2/graph?"
                    + "format=EXTENDED_BINARY_SIF&kind=NEIGHBORHOOD");
            for (String gene : geneList) {
                sbUrl.append("&source=urn:pathwaycommons:RelationshipXref:NGNC_");
                sbUrl.append(gene);
            }
            
            //xdebug.logMsg(this, "URL: "+sbUrl.toString());

            Network network = readNetworkFromExtendedSif(new URL(sbUrl.toString()).openStream(), xdebug);
            
            return network2GraphML(network);
        } catch (IOException e) {
            throw new RemoteException("Remote Access Error", e);
        }
        
    }
    
    private Network readNetworkFromExtendedSif(InputStream isSif, XDebug xdebug) throws IOException {
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
                xdebug.logMsg(this, "cPath2 format changed.");
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
    
    private String network2Sif(Network network) {
        StringBuilder sb = new StringBuilder();
        
        List<Edge> edges = network.getEdges();
        for (Edge edge : edges) {
            sb.append(getNodeNGNCIdIfAvailable(edge.getSourceNode()));
            sb.append("\t");
            sb.append(edge.getInteractionType());
            sb.append("\t");
            sb.append(getNodeNGNCIdIfAvailable(edge.getTargetNode()));
            sb.append("\n");
        }
        
        return sb.toString();   
    }
    
    private String network2GraphML(Network network) {
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
            sb.append(getNodeNGNCIdIfAvailable(node));
            sb.append("</data>\n");
            for (AttributeValuePair av : node.getAvPairs()) {
                String attr = av.getAttr();
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
            for (AttributeValuePair av : edge.getAvPairs()) {
                String attr = av.getAttr();
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
    
    private String getAttrType(Object obj) {
        if (obj instanceof Integer)
            return "integer";
        if (obj instanceof Float || obj instanceof Double)
            return "double";
        if (obj instanceof Boolean)
            return "boolean";
        
        return "string";
    }
    
    private static final String NGNC = "NGNC";
    
    private String getNodeNGNCIdIfAvailable(Node node) {
        Set<String> ngnc = node.getXref(NGNC);
        if (ngnc.isEmpty())
            return node.getId();
        return ngnc.iterator().next();
    }
}

class AttributeValuePair {
    private String attr;
    private Object value;
    AttributeValuePair(String attr, Object value) {
        this.attr = attr;
        this.value = value;
    }

    public String getAttr() {
        return attr;
    }

    public Object getValue() {
        return value;
    }
    
}

class Node {
    private String id;
    private List<AttributeValuePair> avPairs;
    private Map<String,Set<String>> xrefs; // map of id type to ids
    
    Node(String id) {
        if (id==null) throw new IllegalArgumentException("Node ID cannot be null");
        this.id = id;
        avPairs = new ArrayList<AttributeValuePair>();
        xrefs = new HashMap<String,Set<String>>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AttributeValuePair> getAvPairs() {
        return avPairs;
    }
    
    public Map<String,Set<String>> getXrefs() {
        return xrefs;
    }
    
    public Set<String> getXref(String type) {
        Set<String> ids = xrefs.get(type);
        if (ids==null) 
            return Collections.emptySet();
        
        return ids;
    }
    
    public void addAttribute(String attr, Object value) {
        avPairs.add(new AttributeValuePair(attr, value));
    }
    
    public void addXref(String type, String id) {
        Set<String> ids = xrefs.get(type);
        if (ids==null) {
            ids = new HashSet<String>();
            xrefs.put(type, ids);
        }
        
        // also add attribute
        addAttribute("xref", type+":"+id);
        
        ids.add(id);
    }
    
    public String toString() {
        return id;
    }
    
    public int hashCode() {
        return id.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) return false;
        return id.equals(((Node)obj).id);
    }
}

class Edge {
    private Node source;
    private Node target;
    private String interactionType;
    private List<AttributeValuePair> avPairs;
    
    Edge(Node source, Node target, String interactionType) {
        this.source = source;
        this.target = target;
        this.interactionType = interactionType;
        avPairs = new ArrayList<AttributeValuePair>();
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    public Node getSourceNode() {
        return source;
    }

    public void setSourceNode(Node source) {
        this.source = source;
    }

    public Node getTargetNode() {
        return target;
    }

    public void setTargetNode(Node target) {
        this.target = target;
    }

    public List<AttributeValuePair> getAvPairs() {
        return avPairs;
    }
    
    public void addAttribute(String attr, Object value) {
        avPairs.add(new AttributeValuePair(attr, value));
    }    
    
}

class Network {
    private Map<String,Node> nodes; // map of id to node
    private List<Edge> edges;
    
    public Network() {
        nodes = new HashMap<String,Node>();
        edges = new ArrayList<Edge>();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Set<Node> getNodes() {
        return new HashSet<Node>(nodes.values());
    }
    
    public Node getNodeById(String id) {
        return nodes.get(id);
    }
    
    public void addNode(Node node) {
        if (nodes.get(node.getId())==null)
            nodes.put(node.getId(),node);
    }
    
    public void addEdge(Edge edge) {
        edges.add(edge);
        addNode(edge.getSourceNode());
        addNode(edge.getTargetNode());
    }
}