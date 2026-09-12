package org.cbioportal.domain.mutation;

import java.util.List;

/**
 * The genomic similarity between a reference patient and another patient in the same study,
 * computed as the Jaccard index over a user-specified set of genes.
 *
 * <p>Jaccard similarity = |intersection| / |union| where intersection and union are taken over the
 * sets of queried genes that are mutated in each patient.
 *
 * <p>A score of 1.0 means both patients share exactly the same set of mutations across all queried
 * genes; a score of 0.0 means they share no mutations in common.
 */
public record PatientSimilarityScore(
    /** Patient ID of the candidate patient being compared to the reference. */
    String patientId,

    /**
     * Jaccard similarity score in [0, 1]. Higher means more similar to the reference patient across
     * the queried gene set.
     */
    double similarityScore,

    /**
     * Hugo gene symbols that are mutated in BOTH the reference patient and this candidate. This is
     * the intersection used in the Jaccard numerator.
     */
    List<String> commonMutatedGenes) {}
