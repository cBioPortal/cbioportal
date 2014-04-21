/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center 
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center 
 * has been advised of the possibility of such damage.
*/


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
