/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.network;

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
    private Map<String, Object> attrs; // map of attr type to attr value

    /**
     *
     * @param id cannot be null
     */
    Node(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Node ID cannot be null");
        }
        this.id = id;
        attrs = new LinkedHashMap<String, Object>();
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
    public Map<String, Object> getAttributes() {
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
    public void setAttribute(String attr, Object value) {
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
        return obj instanceof Node && id.equals(((Node) obj).id);
    }
}
