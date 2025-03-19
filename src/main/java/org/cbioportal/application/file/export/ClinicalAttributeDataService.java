package org.cbioportal.application.file.export;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.LongTable;

import java.util.*;
import java.util.function.Function;

/**
 * Service to retrieve clinical data attributes and values for a study as a long table
 */
public class ClinicalAttributeDataService {

    public static final Set<String> NOT_EXPORTABLE_SAMPLE_ATTRIBUTES = Set.of("MUTATION_COUNT", "FRACTION_GENOME_ALTERED");
    
    private final ClinicalAttributeDataMapper clinicalAttributeDataMapper;

    public ClinicalAttributeDataService(ClinicalAttributeDataMapper clinicalAttributeDataMapper) {
        this.clinicalAttributeDataMapper = clinicalAttributeDataMapper;
    }

    public LongTable<ClinicalAttribute, String> getClinicalSampleAttributeData(String studyId) {
        List<ClinicalAttribute> clinicalSampleAttributes = clinicalAttributeDataMapper.getClinicalSampleAttributes(studyId);
        Iterable<ClinicalSampleAttributeValue> clinicalSampleAttributeValues = clinicalAttributeDataMapper.getClinicalSampleAttributeValues(studyId);

        Iterable<ClinicalAttribute> completeSampleAttributes = Iterables.concat(
            List.of(ClinicalAttribute.PATIENT_ID, ClinicalAttribute.SAMPLE_ID),
            clinicalSampleAttributes);
        return new LongTable<>(completeSampleAttributes, new Iterator<>() {
            private final PeekingIterator<ClinicalSampleAttributeValue> clinicalSampleAttributeValueIterator = Iterators.peekingIterator(clinicalSampleAttributeValues.iterator());

            @Override
            public boolean hasNext() {
                return clinicalSampleAttributeValueIterator.hasNext();
            }

            @Override
            public Function<ClinicalAttribute, Optional<String>> next() {
                while (clinicalSampleAttributeValueIterator.hasNext()) {
                    ClinicalSampleAttributeValue clinicalSampleAttributeValue = clinicalSampleAttributeValueIterator.next();
                    HashMap<String, String> attributeValueMap = new HashMap<>();
                    attributeValueMap.put(ClinicalAttribute.PATIENT_ID.getAttributeId(), clinicalSampleAttributeValue.getPatientId());
                    attributeValueMap.put(ClinicalAttribute.SAMPLE_ID.getAttributeId(), clinicalSampleAttributeValue.getSampleId());
                    if (!NOT_EXPORTABLE_SAMPLE_ATTRIBUTES.contains(clinicalSampleAttributeValue.getAttributeId())) {
                        attributeValueMap.put(clinicalSampleAttributeValue.getAttributeId(), clinicalSampleAttributeValue.getAttributeValue());
                    }
                    while (clinicalSampleAttributeValueIterator.hasNext()
                        && clinicalSampleAttributeValueIterator.peek().getPatientId().equals(clinicalSampleAttributeValue.getPatientId())
                        && clinicalSampleAttributeValueIterator.peek().getSampleId().equals(clinicalSampleAttributeValue.getSampleId())) {
                        clinicalSampleAttributeValue = clinicalSampleAttributeValueIterator.next();
                        if (!NOT_EXPORTABLE_SAMPLE_ATTRIBUTES.contains(clinicalSampleAttributeValue.getAttributeId())) {
                            attributeValueMap.put(clinicalSampleAttributeValue.getAttributeId(), clinicalSampleAttributeValue.getAttributeValue());
                        }
                    }
                    return (clinicalAttribute -> Optional.ofNullable(attributeValueMap.get(clinicalAttribute.getAttributeId())));
                }
                throw new IllegalStateException("No more elements");
            }
        });
    }

    public LongTable<ClinicalAttribute, String> getClinicalPatientAttributeData(String studyId) {
        List<ClinicalAttribute> clinicalPatientAttributes = clinicalAttributeDataMapper.getClinicalPatientAttributes(studyId);
        Iterable<ClinicalPatientAttributeValue> clinicalPatientAttributeValues = clinicalAttributeDataMapper.getClinicalPatientAttributeValues(studyId);

        Iterable<ClinicalAttribute> completePatientAttributes = Iterables.concat(
            List.of(ClinicalAttribute.PATIENT_ID),
            clinicalPatientAttributes);
        return new LongTable<>(completePatientAttributes, new Iterator<>() {
            private final PeekingIterator<ClinicalPatientAttributeValue> clinicalPatientAttributeValueIterator = Iterators.peekingIterator(clinicalPatientAttributeValues.iterator());

            @Override
            public boolean hasNext() {
                return clinicalPatientAttributeValueIterator.hasNext();
            }

            @Override
            public Function<ClinicalAttribute, Optional<String>> next() {
                while (clinicalPatientAttributeValueIterator.hasNext()) {
                    ClinicalPatientAttributeValue clinicalPatientAttributeValue = clinicalPatientAttributeValueIterator.next();
                    HashMap<String, String> attributeValueMap = new HashMap<>();
                    attributeValueMap.put(ClinicalAttribute.PATIENT_ID.getAttributeId(), clinicalPatientAttributeValue.getPatientId());
                    attributeValueMap.put(clinicalPatientAttributeValue.getAttributeId(), clinicalPatientAttributeValue.getAttributeValue());
                    while (clinicalPatientAttributeValueIterator.hasNext()
                        && clinicalPatientAttributeValueIterator.peek().getPatientId().equals(clinicalPatientAttributeValue.getPatientId())) {
                        clinicalPatientAttributeValue = clinicalPatientAttributeValueIterator.next();
                        attributeValueMap.put(clinicalPatientAttributeValue.getAttributeId(), clinicalPatientAttributeValue.getAttributeValue());
                    }
                    return (clinicalAttribute -> Optional.ofNullable(attributeValueMap.get(clinicalAttribute.getAttributeId())));
                }
                throw new IllegalStateException("No more elements");
            }
        });
    }
}
