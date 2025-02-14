package org.cbioportal.domain.sample;

import org.cbioportal.domain.patient.Patient;

import java.io.Serializable;
import java.util.Objects;

public record Sample (
    Integer internalId,
    String stableId,
    SampleType sampleType,
    Integer patientId, 
    String patientStableId,
    Patient patient,
    String cancerStudyIdentifier,
    Boolean sequenced,
    Boolean copyNumberSegmentPresent, 
    String uniqueSampleKey,
    String uniquePatientKey
) implements Serializable {

    public Sample(Integer internalId, String stableId, String patientStableId, String cancerStudyIdentifier) {
        this(
            internalId,
            stableId,
            (SampleType) null,
            null,
            patientStableId,
            null,
            cancerStudyIdentifier,
            null,
            null,
            null,
            null
        );
    }

    public Sample(
        Integer internalId,
        String stableId,
        String sampleType,
        Integer patientId,
        String patientStableId,
        String cancerStudyIdentifier,
        String uniqueSampleKey,
        String uniquePatientKey
    ) {
        this(
            internalId,
            stableId,
            SampleType.fromString(sampleType),
            patientId,
            patientStableId,
            null,
            cancerStudyIdentifier,
            null,
            null,
            uniqueSampleKey,
            uniquePatientKey
        );
    }

    public Sample(
        Integer internalId,
        String stableId,
        String sampleType,
        Integer patientId,
        String patientStableId,
        Patient patient,
        String cancerStudyIdentifier,
        Boolean sequenced,
        Boolean copyNumberSegmentPresent,
        String uniqueSampleKey,
        String uniquePatientKey
    ) {
        this(
            internalId,
            stableId,
            SampleType.fromString(sampleType),
            patientId,
            patientStableId,
            patient,
            cancerStudyIdentifier,
            sequenced,
            copyNumberSegmentPresent,
            uniqueSampleKey,
            uniquePatientKey
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sample)) return false;
        Sample sample = (Sample) o;
        return stableId.equals(sample.stableId) && cancerStudyIdentifier.equals(sample.cancerStudyIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stableId, cancerStudyIdentifier);
    }
}
