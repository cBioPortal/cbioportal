package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Domain.
 */
final class Domain implements JSONAware {
    private final String label;
    private final int start;
    private final int end;
    private final String json;

    Domain(final String label, final int start, final int end) {
        checkNotNull(label, "label must not be null");
        this.label = label;
        this.start = start;
        this.end = end;
        this.json = asJson();
    }

    /**
     * Return the label for this domain.  The label will not be null.
     *
     * @return the label for this domain
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return the start of this domain.
     *
     * @return the start of this domain
     */
    public int getStart() {
        return start;
    }

    /**
     * Return the end of this domain.
     *
     * @return the end of this domain
     */
    public int getEnd() {
        return end;
    }

    /** {@inheritDoc} */
    public String toJSONString() {
        return json;
    }

    @SuppressWarnings("unchecked")
    private String asJson() {
        JSONObject domain = new JSONObject();
        domain.put("label", label);
        domain.put("start", start);
        domain.put("end", end);
        return domain.toString();
    }
}
