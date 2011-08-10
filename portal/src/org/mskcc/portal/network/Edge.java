
package org.mskcc.portal.network;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class for Edge.
 * @author jj
 */
public class Edge {
    private Node source;
    private Node target;
    private String interactionType;
    private Map<String,Set<Object>> attrs; // map of attr type to attr value
    
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
        attrs = new LinkedHashMap<String,Set<Object>>();
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
     * @return edge attributes
     */
    public Map<String,Set<Object>> getAttributes() {
        return attrs;
    }
    
    /**
     * 
     * @param attr attribute name
     * @param value attribute value
     */
    public void addAttribute(String attr, Object value) {
        Set<Object> values = attrs.get(attr);
        if (values==null) {
            values = new HashSet<Object>();
            attrs.put(attr, values);
        }
        values.add(value);
    }   
}
