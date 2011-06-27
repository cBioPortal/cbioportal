/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.portal.network;

import java.util.ArrayList;
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

    public Network() {
        nodes = new HashMap<String,Node>();
        edges = new ArrayList<Edge>();
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
}
