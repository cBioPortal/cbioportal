package org.mskcc.portal.mutation.diagram.pfam;

import java.util.List;
import java.util.Map;

/**
 * Markup, derived from Pfram graphics response in JSON format.
 */
final class Markup {
    private int start;
    private int end;
    private String href;
    private List<String> colour;
    private String lineColour;
    private String display;
    private String residue;
    private String type;
    private String headStyle;
    private String v_align;
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

    public void setLineColour(final String lineColour) {
        this.lineColour = lineColour;
    }

    public String getLineColour() {
        return lineColour;
    }

    public void setDisplay(final String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public void setResidue(final String residue) {
        this.residue = residue;
    }

    public String getResidue() {
        return residue;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setHeadStyle(final String headStyle) {
        this.headStyle = headStyle;
    }

    public String getHeadStyle() {
        return headStyle;
    }

    public void setV_align(final String v_align) {
        this.v_align = v_align;
    }

    public String getV_align() {
        return v_align;
    }

    public void setMetadata(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
