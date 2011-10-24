package org.mskcc.portal.mutation.diagram;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import com.google.common.collect.ImmutableList;

/**
 * Mutation diagram.
 */
final class MutationDiagram implements JSONAware {
    private final String label;
    private final int length;
    private final List<Domain> domains;
    private final List<Mutation> mutations;
    private final String json;

    MutationDiagram(final String label, final int length, final List<Domain> domains, final List<Mutation> mutations) {
        checkNotNull(label, "label must not be null");
        checkNotNull(domains, "domains must not be null");
        checkNotNull(mutations, "mutations must not be null");
        this.label = label;
        this.length = length;
        this.domains = ImmutableList.copyOf(domains);
        this.mutations = ImmutableList.copyOf(mutations);
        this.json = asJson();
    }

    /**
     * Return the label for this mutation diagram.  The label will not be null.
     *
     * @return the label for this mutation diagram
     */
    public String getLabel() {
        return label;
    }

    /**
     * Return the length of this mutation diagram.
     *
     * @return the length of this mutation diagram
     */
    public int getLength() {
        return length;
    }

    /**
     * Return an immutable list of domains for this mutation diagram.
     *
     * @return an immutable list of domains for this mutation diagram
     */
    public List<Domain> getDomains() {
        return domains;
    }

    /**
     * Return an immutable list of mutations for this mutation diagram.
     *
     * @return an immutable list of mutations for this mutation diagram
     */
    public List<Mutation> getMutations() {
        return mutations;
    }

    /** {@inheritDoc} */
    public String toJSONString() {
        return json;
    }

    @SuppressWarnings("unchecked")
    private String asJson() {
        JSONObject mutationDiagram = new JSONObject();
        mutationDiagram.put("label", label);
        mutationDiagram.put("length", length);
        if (!domains.isEmpty()) {
            mutationDiagram.put("domains", domains);
        }
        if (!mutations.isEmpty()) {
            mutationDiagram.put("mutations", mutations);
        }
        return mutationDiagram.toString();
    }
}
