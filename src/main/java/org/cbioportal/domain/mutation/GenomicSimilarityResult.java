package org.cbioportal.domain.mutation;

import java.util.List;

/**
 * The result of a patient genomic similarity query: the reference patient's mutated genes, which
 * give the context for interpreting each score, and the patients most similar to it.
 *
 * @param referencePatientMutatedGenes Hugo symbols of the requested genes that are mutated in the
 *     reference patient, sorted alphabetically
 * @param similarPatients at most {@code topN} other patients, ordered from most to least similar to
 *     the reference patient
 */
public record GenomicSimilarityResult(
    List<String> referencePatientMutatedGenes, List<PatientSimilarityScore> similarPatients) {}
