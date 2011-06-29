/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.portal.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jj
 */
public class Network {
    private Map<String,Node> nodes; // map of id to node
    private List<Edge> edges;
    private Map<String,Map<String,Set<Node>>> nodesByXrefs;

    public Network() {
        nodes = new HashMap<String,Node>();
        edges = new ArrayList<Edge>();
        nodesByXrefs = null;
    }

    /**
     * 
     * @return all edges
     */
    public List<Edge> getEdges() {
        return edges;
    }

    /**
     * 
     * @return all nodes
     */
    public Set<Node> getNodes() {
        return new HashSet<Node>(nodes.values());
    }
    
    /**
     * 
     * @param id node id
     * @return the node with a particular id or null if not exist
     */
    public Node getNodeById(String id) {
        return nodes.get(id);
    }
    
    /**
     * add a node
     * @param node a node
     */
    public void addNode(Node node) {
        if (nodes.get(node.getId())==null)
            nodes.put(node.getId(),node);
    }
    
    /**
     * add an edge
     * @param edge an edge 
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
        addNode(edge.getSourceNode());
        addNode(edge.getTargetNode());
    }
    
    /**
     * Get nodes with a particular cross-reference
     * @param type id type
     * @param id id
     * @return a set of nodes
     */
    public Set<Node> getNodesByXref(String type, String id) {
        if (nodesByXrefs==null) // lazy init
            nodesByXrefs = new HashMap<String,Map<String,Set<Node>>>();
        
        Map<String,Set<Node>> mapIdNodes = nodesByXrefs.get(type);
        if (mapIdNodes==null) { // cache for the query type
            mapIdNodes = new HashMap<String,Set<Node>>();
            nodesByXrefs.put(type, mapIdNodes);
            for (Node node : getNodes()) {
                for (String i_d : node.getXref(type)) {
                    Set<Node> ns = mapIdNodes.get(i_d);
                    if (ns==null) {
                        ns = new HashSet<Node>();
                        mapIdNodes.put(i_d, ns);
                    }
                    ns.add(node);
                }
            }
        }
        
        Set<Node> ret = mapIdNodes.get(id);
        if (ret==null)
            return Collections.emptySet();
        return ret;
    }
}
