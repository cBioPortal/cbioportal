
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
            
            xdebug.logMsg(this, "URL: "+sbUrl.toString());

            Network network = readNetworkFromExtendedSif(new URL(sbUrl.toString()).openStream(), xdebug);
            
            String sif = network2Sif(network);
            
            return sif;
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
                    edge.addAttribute(edgeHeaders[i], strs[i]);
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
            
            for (Node node : network.getNodes()) {
                System.out.print(node.getId()+"\t");
                System.out.println(node.getXrefs().toString());
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
    private String value;
    AttributeValuePair(String attr, String value) {
        this.attr = attr;
        this.value = value;
    }

    public String getAttr() {
        return attr;
    }

    public String getValue() {
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
    
    public void addAttribute(String attr, String value) {
        avPairs.add(new AttributeValuePair(attr, value));
    }
    
    public void addXref(String type, String id) {
        Set<String> ids = xrefs.get(type);
        if (ids==null) {
            ids = new HashSet<String>();
            xrefs.put(type, ids);
        }
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
    
    public void addAttribute(String attr, String value) {
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