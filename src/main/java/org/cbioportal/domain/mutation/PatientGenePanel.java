package org.cbioportal.domain.mutation;

/**
 * A gene panel that at least one of a patient's samples was profiled with.
 *
 * @param patientId patient ID, unique within the study
 * @param genePanelId stable ID of the gene panel; {@code WES} for samples profiled without a panel
 */
public record PatientGenePanel(String patientId, String genePanelId) {}
