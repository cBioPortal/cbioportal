
package org.mskcc.portal.network;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for Node
 * @author jj
 */
public class Node {    
    private String id;
    private NodeType type = NodeType.UNKNOWN;
    private Map<String,Object> attrs; // map of attr type to attr value
    
    /**
     * 
     * @param id cannot be null
     */
    Node(String id) {
        if (id==null) {
            throw new IllegalArgumentException("Node ID cannot be null");
        }
        this.id = id;
        attrs = new LinkedHashMap<String,Object>();
    }

    /**
     * 
     * @return node ID
     */
    public String getId() {
        return id;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }
    
    /**
     * 
     * @return node attributes
     */
    public Map<String,Object> getAttributes() {
        return Collections.unmodifiableMap(attrs);
    }
    
    public Object getAttribute(String attrName) {
        return attrs.get(attrName);
    }
    
    /**
     * 
     * @param attr attribute name
     * @param value attribute value
     */
    public void addAttribute(String attr, Object value) {
        attrs.put(attr, value);
    }
    
    @Override
    public String toString() {
        return id;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Node)) {
            return false;
        }
        return id.equals(((Node)obj).id);
    }
}
