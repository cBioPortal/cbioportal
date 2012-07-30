
package org.mskcc.cbio.portal.network;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for Edge.
 * @author jj
 */
public class Edge {
    private boolean directed;
    private String interactionType;
    private Map<String,Object> attrs;
    
    /**
     * 
     * @param interactionType 
     */
    public Edge(boolean directed, String interactionType) {
        this.directed = directed;
        this.interactionType = interactionType;
        attrs = new LinkedHashMap<String,Object>();
    }

    public String getInteractionType() {
        return interactionType;
    }

    public void setInteractionType(String interactionType) {
        this.interactionType = interactionType;
    }

    /**
     * 
     * @return edge attributes
     */
    public Map<String,Object> getAttributes() {
        return attrs;
    }
    
    /**
     * 
     * @param attr attribute name
     * @param value attribute value
     */
    public void addAttribute(String attr, Object value) {
        attrs.put(attr, value);
    }

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }
}
