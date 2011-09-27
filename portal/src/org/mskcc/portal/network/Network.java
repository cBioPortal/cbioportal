/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.portal.network;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jj
 */
public class Network {
    private Map<String,Node> nodes; // map of id to node
    private Set<Edge> edges;
    private Map<String,Map<String,Set<Node>>> nodesByXrefs;

    public Network() {
        nodes = new LinkedHashMap<String,Node>();
        edges = new LinkedHashSet<Edge>();
        nodesByXrefs = null;
    }

    /**
     * 
     * @return all edges
     */
    public Set<Edge> getEdges() {
        return edges;
    }

    /**
     * 
     * @return all nodes
     */
    public Set<Node> getNodes() {
        return new LinkedHashSet<Node>(nodes.values());
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
        if (nodes.get(node.getId())==null) {
            nodes.put(node.getId(),node);
        }
    }
    
    /**
     * remove a node
     * @param node a node
     * @return true if node exists and removed
     */
    public boolean removeNode(Node node) {
        boolean ret = nodes.remove(node.getId())!=null;
        if (ret) {
            nodesByXrefs = null;
        }
        return ret;
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
     * 
     * @param edge an edge
     * @return true if exists and removed
     */
    public boolean removeEdge(Edge edge) {
        return edges.remove(edge);
    }
    
    /**
     * Get nodes with a particular cross-reference
     * @param type id type
     * @param id id
     * @return a set of nodes
     */
    public Set<Node> getNodesByXref(String type, String identifier) {
        if (nodesByXrefs==null) {// lazy init
            nodesByXrefs = new LinkedHashMap<String,Map<String,Set<Node>>>();
        }
        
        Map<String,Set<Node>> mapIdNodes = nodesByXrefs.get(type);
        if (mapIdNodes==null) { // cache for the query type
            mapIdNodes = new LinkedHashMap<String,Set<Node>>();
            nodesByXrefs.put(type, mapIdNodes);
            for (Node node : getNodes()) {
                for (String id : node.getXref(type)) {
                    Set<Node> ns = mapIdNodes.get(id);
                    if (ns==null) {
                        ns = new LinkedHashSet<Node>();
                        mapIdNodes.put(id, ns);
                    }
                    ns.add(node);
                }
            }
        }
        
        Set<Node> ret = mapIdNodes.get(identifier);
        if (ret==null) {
            return Collections.emptySet();
        }
        
        return ret;
    }
    
    
    
    /**
     * 
     */
    public static interface Filter {
        /**
         * 
         * @param node
         * @return true if filter the node
         */
        boolean filterNode(Node node);
        
        /**
         * 
         * @param node
         * @return true if filter the edge
         */
        boolean filterEdge(Edge edge);
    }
    
    /**
     * Filter network
     * @param net a network
     * @param filter filter to apply
     */
    public void filter(Filter filter) {
        // filter edges
        for (Iterator<Edge> it = edges.iterator(); it.hasNext();) {
            if (filter.filterEdge(it.next())) {
                it.remove();
            }
        }
        
        // filter nodes
        for (Iterator<Map.Entry<String,Node>> entry = nodes.entrySet().iterator(); entry.hasNext();) {
            if (filter.filterNode(entry.next().getValue())) {
                entry.remove();
            }
        }
    }
}
