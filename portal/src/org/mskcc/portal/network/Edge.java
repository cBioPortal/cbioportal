
package org.mskcc.portal.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for Edge.
 * @author jj
 */
public class Edge {
    private Node source;
    private Node target;
    private String interactionType;
    private List<Attribute> attrs;
    
    /**
     * 
     * @param source source node
     * @param target target node
     * @param interactionType interaction type
     */
    Edge(Node source, Node target, String interactionType) {
        this.source = source;
        this.target = target;
        this.interactionType = interactionType;
        attrs = new ArrayList<Attribute>();
    }

    /**
     * 
     * @return interaction type
     */
    public String getInteractionType() {
        return interactionType;
    }

    /**
     * 
     * @param interactionType interaction type
     */
    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    /**
     * 
     * @return source node
     */
    public Node getSourceNode() {
        return source;
    }

    /**
     * 
     * @param source source node
     */
    public void setSourceNode(Node source) {
        this.source = source;
    }

    /**
     * 
     * @return target node
     */
    public Node getTargetNode() {
        return target;
    }

    /**
     * 
     * @param target target node
     */
    public void setTargetNode(Node target) {
        this.target = target;
    }

    /**
     * 
     * @return node attributes
     */
    public List<Attribute> getAttributes() {
        return attrs;
    }
    
    /**
     * 
     * @param attr attribute name
     * @param value attribute value
     */
    public void addAttribute(String attr, Object value) {
        attrs.add(new Attribute(attr, value));
    }    
}
