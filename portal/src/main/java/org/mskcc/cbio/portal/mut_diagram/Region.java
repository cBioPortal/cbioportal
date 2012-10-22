/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.portal.mut_diagram;

import java.util.Map;

/**
 * Region, derived from Pfam graphics response in JSON format.
 */
public final class Region {
    private int start;
    private int end;
    private int aliStart;
    private int aliEnd;
    private int modelStart;
    private int modelEnd;
    private int modelLength;
    private String text;
    private String href;
    private String colour;
    private String display;
    private String startStyle;
    private String endStyle;
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

    public void setAliStart(final int aliStart) {
        this.aliStart = aliStart;
    }

    public int getAliStart() {
        return aliStart;
    }

    public void setAliEnd(final int aliEnd) {
        this.aliEnd = aliEnd;
    }

    public int getAliEnd() {
        return aliEnd;
    }

    public void setModelStart(final int modelStart) {
        this.modelStart = modelStart;
    }

    public int getModelStart() {
        return modelStart;
    }

    public void setModelEnd(final int modelEnd) {
        this.modelEnd = modelEnd;
    }

    public int getModelEnd() {
        return modelEnd;
    }

    public void setModelLength(final int modelLength) {
        this.modelLength = modelLength;
    }

    public int getModelLength() {
        return modelLength;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setHref(final String href) {
        this.href = href;
    }
    public String getHref() {
        return href;
    }

    public void setColour(final String colour) {
        this.colour = colour;
    }

    public String getColour() {
        return colour;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public void setStartStyle(final String startStyle) {
        this.startStyle = startStyle;
    }

    public String getStartStyle() {
        return startStyle;
    }

    public void setEndStyle(final String endStyle) {
        this.endStyle = endStyle;
    }

    public String getEndStyle() {
        return endStyle;
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
