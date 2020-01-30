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

package org.mskcc.cbio.portal.mut_diagram;

import java.util.List;
import java.util.Map;

/**
 * Motif, derived from Pfam graphics response in JSON format.
 */
public final class Motif {
    private int start;
    private int end;
    private String href;
    private List<String> colour;
    private String display;
    private String type;
    private Map<String, Object> metadata;

    public void setStart(final int start) {
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    public int getEnd() {
        return end;
    }

    public void setHref(final String href) {
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public void setColour(final List<String> colour) {
        this.colour = colour;
    }

    public List<String> getColour() {
        return colour;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setMetadata(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
