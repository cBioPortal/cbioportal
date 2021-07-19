package org.cbioportal.model;

import java.util.Set;

public class TreatmentSankeyGraph {
    private final Set<TreatmentSequenceNode> nodes;
    private final Set<TreatmentSequenceEdge> edges;

    public TreatmentSankeyGraph(Set<TreatmentSequenceNode> nodes, Set<TreatmentSequenceEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Set<TreatmentSequenceNode> getNodes() {
        return nodes;
    }

    public Set<TreatmentSequenceEdge> getEdges() {
        return edges;
    }
}
