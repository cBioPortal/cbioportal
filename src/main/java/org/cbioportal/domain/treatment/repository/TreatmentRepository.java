package org.cbioportal.domain.treatment.repository;

import org.cbioportal.legacy.model.PatientTreatment;
import org.cbioportal.legacy.model.SampleTreatment;
import org.cbioportal.domain.studyview.StudyViewFilterContext;

import java.util.List;

/**
 * Repository interface for performing operations related to patient and sample treatments.
 * This interface defines methods for retrieving patient treatments, sample treatments,
 * and their respective total counts based on the filter criteria provided in the study view filter context.
 */
public interface TreatmentRepository {

    /**
     * Retrieves the treatments associated with patients based on the provided study view filter context.
     *
     * @param studyViewFilterContext the context containing the filter criteria for the study view
     * @return a list of {@link PatientTreatment} representing the treatments for patients
     */
    List<PatientTreatment> getPatientTreatments(StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves the total count of patient treatments based on the provided study view filter context.
     *
     * @param studyViewFilterContext the context containing the filter criteria for the study view
     * @return the total count of patient treatments
     */
    int getTotalPatientTreatmentCount(StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves the treatments associated with samples based on the provided study view filter context.
     *
     * @param studyViewFilterContext the context containing the filter criteria for the study view
     * @return a list of {@link SampleTreatment} representing the treatments for samples
     */
    List<SampleTreatment> getSampleTreatments(StudyViewFilterContext studyViewFilterContext);

    /**
     * Retrieves the total count of sample treatments based on the provided study view filter context.
     *
     * @param studyViewFilterContext the context containing the filter criteria for the study view
     * @return the total count of sample treatments
     */
    int getTotalSampleTreatmentCount(StudyViewFilterContext studyViewFilterContext);
}
