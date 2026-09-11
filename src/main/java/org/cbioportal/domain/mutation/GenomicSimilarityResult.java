package org.cbioportal.domain.mutation;

import java.util.List;

/**
 * The full result of a patient genomic similarity query.
 *
 * <p>Bundles two pieces of information together so callers do not need to issue a second API call:
 *
 * <ol>
 *   <li>The Hugo gene symbols that are mutated in the reference patient (over the queried gene
 *       set), which provides the context for interpreting each candidate's similarity score.
 *   <li>The ranked list of candidate patients, sorted by descending Jaccard similarity to the
 *       reference patient.
 * </ol>
 *
 * @param referencePatientMutatedGenes Hugo symbols mutated in the reference patient, restricted to
 *     the requested gene panel and sorted alphabetically.
 * @param similarPatients Ordered list (most → least similar) of candidate patients; at most {@code
 *     topN} entries, never including the reference patient itself.
 */
public record GenomicSimilarityResult(
    List<String> referencePatientMutatedGenes, List<PatientSimilarityScore> similarPatients) {}
