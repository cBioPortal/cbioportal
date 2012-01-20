package org.mskcc.portal.mutation.diagram;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 * Mutation.
 */
public final class Mutation implements JSONAware {
    private final String label;
    private final int location;
    private final int count;
    private final String json;

    Mutation(final int location, final int count) {
        this(null, location, count);
    }

    Mutation(final String label, final int location, final int count) {
        this.label = label;
        this.location = location;
        this.count = count;
        this.json = asJson();
    }

    /**
     * Return the label for this mutation.
     *
     * @return the label for this mutation
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return the location of this mutation.
     *
     * @return the location of this mutation
     */
    public int getLocation() {
        return location;
    }

    /**
     * Return the count of mutations at this location.
     *
     * @return the count of mutations as this location
     */
    public int getCount() {
        return count;
    }

    /** {@inheritDoc} */
    public String toJSONString() {
        return json;
    }

    @SuppressWarnings("unchecked")
    private String asJson() {
        JSONObject mutation = new JSONObject();
        if (label != null) {
            mutation.put("label", label);
        }
        mutation.put("location", location);
        mutation.put("count", count);
        return mutation.toString();
    }
}
