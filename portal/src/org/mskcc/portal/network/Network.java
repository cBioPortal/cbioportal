
package org.mskcc.portal.network;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jj
 */
public class Network {
    private Graph<Node,Edge> graph;
    private Map<String,Node> nodesByIds;

    public Network() {
        graph = new UndirectedSparseMultigraph<Node,Edge>();
        nodesByIds = new HashMap<String,Node>();
    }

    /**
     * 
     * @return all edges
     */
    public Collection<Edge> getEdges() {
        return graph.getEdges();
    }

    /**
     * 
     * @return all nodes
     */
    public Collection<Node> getNodes() {
        return graph.getVertices();
    }
    
    /**
     * 
     * @param id node id
     * @return the node with a particular id or null if not exist
     */
    public Node getNodeById(String id) {
        return nodesByIds.get(id);
    }
    
    /**
     * add a node
     * @param node a node
     */
    public void addNode(Node node) {
        if (nodesByIds.get(node.getId())!=null) {
            return;
        }
        
        nodesByIds.put(node.getId(),node);
        graph.addVertex(node);
    }
    
    /**
     * remove a node
     * @param node a node
     * @return true if node exists and removed
     */
    public boolean removeNode(Node node) {
        boolean ret = graph.removeVertex(node);
        if (ret) {
            nodesByIds.remove(node.getId());
        }
        return ret;
    }
    
    /**
     * add an edge
     * @param edge an edge 
     */
    public void addEdge(Edge edge, String idOfNode1, String idOfNode2) {
        Node node1 = nodesByIds.get(idOfNode1);
        Node node2 = nodesByIds.get(idOfNode2);
        if (node1==null || node2==null) {
            throw new java.lang.UnsupportedOperationException("Add nodes before adding an edge");
        }
        
        graph.addEdge(edge, node1, node2);
    }
    
    /**
     * 
     * @param edge an edge
     * @return true if exists and removed
     */
    public boolean removeEdge(Edge edge) {
        return graph.removeEdge(edge);
    }
    
    /**
     * 
     * @param edge
     * @return an array of 2 nodes
     */
    public Node[] getNodes(Edge edge) {
        Pair<Node> nodes = graph.getEndpoints(edge);
        return new Node[] {nodes.getFirst(), nodes.getSecond()};
    }
    
    /**
     * 
     */
    public Collection<Node> getNeighbors(Node node) {
        return graph.getNeighbors(node);
    }
    
    /**
     * 
     * @param node
     * @return 
     */
    public int getDegree(Node node) {
        return graph.degree(node);
    }
}
