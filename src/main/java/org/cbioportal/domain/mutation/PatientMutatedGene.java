package org.cbioportal.domain.mutation;

/**
 * A gene that has at least one mutation in a patient.
 *
 * @param patientId patient ID, unique within the study
 * @param hugoGeneSymbol Hugo symbol of the mutated gene
 */
public record PatientMutatedGene(String patientId, String hugoGeneSymbol) {}
