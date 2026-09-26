package org.cbioportal.domain.mutation;

import java.util.List;

/**
 * The genomic similarity between a reference patient and another patient in the same study.
 *
 * <p>The similarity is the Jaccard index of the two patients' mutated genes, taken over the
 * requested genes that were profiled in both patients: |mutated in both| / |mutated in either|. A
 * score of 1.0 means the patients have the same mutated genes among those genes; 0.0 means they
 * have none in common.
 *
 * @param patientId ID of the patient compared with the reference patient
 * @param similarityScore Jaccard index in [0, 1]; higher is more similar to the reference patient
 * @param commonMutatedGenes Hugo symbols of the genes mutated in both patients, sorted
 *     alphabetically
 */
public record PatientSimilarityScore(
    String patientId, double similarityScore, List<String> commonMutatedGenes) {}
