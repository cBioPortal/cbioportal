
package org.mskcc.portal.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class for Node
 * @author jj
 */
public class Node {
    
    private String id;
    private String type;
    private List<Attribute> attrs;
    private Map<String,Set<String>> xrefs; // map of id type to ids
    
    /**
     * 
     * @param id cannot be null
     */
    Node(String id) {
        if (id==null) throw new IllegalArgumentException("Node ID cannot be null");
        this.id = id;
        attrs = new ArrayList<Attribute>();
        xrefs = new HashMap<String,Set<String>>();
    }

    /**
     * 
     * @return node ID
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * @param id node ID
     */
    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
     * @return cross references; map of ID type to set of IDs
     */
    public Map<String,Set<String>> getXrefs() {
        return xrefs;
    }
    
    /**
     * 
     * @param type ID type
     * @return a set of ID of the type
     */
    public Set<String> getXref(String type) {
        Set<String> ids = xrefs.get(type);
        if (ids==null) 
            return Collections.emptySet();
        
        return ids;
    }
    
    /**
     * 
     * @param attr attribute name
     * @param value attribute value
     */
    public void addAttribute(String attr, Object value) {
        attrs.add(new Attribute(attr, value));
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
        if (!(obj instanceof Node)) return false;
        return id.equals(((Node)obj).id);
    }
}
