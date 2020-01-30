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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class for Edge.
 * @author jj
 */
public class Edge {
    private boolean directed;
    private String interactionType;
    private Map<String, Object> attrs;
    private String sourceID;
    private String targetID;

    /**
     *
     * @param interactionType
     */
    public Edge(boolean directed, String interactionType) {
        this.directed = directed;
        this.interactionType = interactionType;
        attrs = new LinkedHashMap<String, Object>();
    }

    public Edge(
        boolean directed,
        String interactionType,
        String sourceID,
        String targetID
    ) {
        this.directed = directed;
        this.interactionType = interactionType;
        this.sourceID = sourceID;
        this.targetID = targetID;
        attrs = new LinkedHashMap<String, Object>();
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
    public Map<String, Object> getAttributes() {
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

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public boolean hasSameSourceTargetAndType(Edge other) {
        return (
            other.getSourceID().equals(this.getSourceID()) &&
            other.getTargetID().equals(this.getTargetID()) &&
            other
                .getAttributes()
                .get("INTERACTION_TYPE")
                .equals(this.getAttributes().get("INTERACTION_TYPE"))
        );
    }
}
