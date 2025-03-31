package org.cbioportal.application.file.export.services;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.export.mappers.ClinicalAttributeDataMapper;
import org.cbioportal.application.file.model.ClinicalAttribute;
import org.cbioportal.application.file.model.ClinicalPatientAttributeValue;
import org.cbioportal.application.file.model.ClinicalSampleAttributeValue;
import org.cbioportal.application.file.utils.CloseableIterator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.SequencedMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service to retrieve clinical data attributes and values for a study
 */
public class ClinicalAttributeDataService {

    public static final Set<String> NOT_EXPORTABLE_SAMPLE_ATTRIBUTES = Set.of("MUTATION_COUNT", "FRACTION_GENOME_ALTERED");

    private final ClinicalAttributeDataMapper clinicalAttributeDataMapper;

    public ClinicalAttributeDataService(ClinicalAttributeDataMapper clinicalAttributeDataMapper) {
        this.clinicalAttributeDataMapper = clinicalAttributeDataMapper;
    }

    public CloseableIterator<SequencedMap<ClinicalAttribute, String>> getClinicalSampleAttributeData(String studyId) {
        final TreeMap<String, ClinicalAttribute> clinicalSampleAttributes = clinicalAttributeDataMapper.getClinicalSampleAttributes(studyId).stream()
            .filter(clinicalAttribute -> !NOT_EXPORTABLE_SAMPLE_ATTRIBUTES.contains(clinicalAttribute.getAttributeId()))
            .filter(clinicalAttribute -> !ClinicalAttribute.PATIENT_ID.getAttributeId().equals(clinicalAttribute.getAttributeId()))
            .filter(clinicalAttribute -> !ClinicalAttribute.SAMPLE_ID.getAttributeId().equals(clinicalAttribute.getAttributeId()))
            .collect(Collectors.toMap(
                ClinicalAttribute::getAttributeId,
                Function.identity(),
                (existing, duplicate) -> {
                    throw new IllegalStateException("Duplicate (the same attribute id) clinical sample attributes detected: " + existing + " and " + duplicate);
                },
                TreeMap::new));

        final Cursor<ClinicalSampleAttributeValue> clinicalSampleAttributeValues = clinicalAttributeDataMapper.getClinicalSampleAttributeValues(studyId);

        return new CloseableIterator<>() {

            private final PeekingIterator<ClinicalSampleAttributeValue> clinicalSampleAttributeValueIterator = Iterators.peekingIterator(clinicalSampleAttributeValues.iterator());

            @Override
            public boolean hasNext() {
                return clinicalSampleAttributeValueIterator.hasNext();
            }

            @Override
            public LinkedHashMap<ClinicalAttribute, String> next() {
                while (clinicalSampleAttributeValueIterator.hasNext()) {
                    ClinicalSampleAttributeValue clinicalSampleAttributeValue = clinicalSampleAttributeValueIterator.next();
                    LinkedHashMap<ClinicalAttribute, String> attributeValueMap = new LinkedHashMap<>();
                    attributeValueMap.put(ClinicalAttribute.PATIENT_ID, clinicalSampleAttributeValue.getPatientId());
                    attributeValueMap.put(ClinicalAttribute.SAMPLE_ID, clinicalSampleAttributeValue.getSampleId());
                    if (clinicalSampleAttributes.containsKey(clinicalSampleAttributeValue.getAttributeId())) {
                        attributeValueMap.put(clinicalSampleAttributes.get(clinicalSampleAttributeValue.getAttributeId()), clinicalSampleAttributeValue.getAttributeValue());
                    }
                    while (clinicalSampleAttributeValueIterator.hasNext()
                        && clinicalSampleAttributeValueIterator.peek().getPatientId().equals(clinicalSampleAttributeValue.getPatientId())
                        && clinicalSampleAttributeValueIterator.peek().getSampleId().equals(clinicalSampleAttributeValue.getSampleId())) {
                        clinicalSampleAttributeValue = clinicalSampleAttributeValueIterator.next();
                        if (clinicalSampleAttributes.containsKey(clinicalSampleAttributeValue.getAttributeId())) {
                            attributeValueMap.put(clinicalSampleAttributes.get(clinicalSampleAttributeValue.getAttributeId()), clinicalSampleAttributeValue.getAttributeValue());
                        }
                    }
                    return attributeValueMap;
                }
                throw new IllegalStateException("No more elements");
            }

            @Override
            public void close() throws IOException {
                clinicalSampleAttributeValues.close();
            }
        };
    }

    public CloseableIterator<SequencedMap<ClinicalAttribute, String>> getClinicalPatientAttributeData(String studyId) {
        final TreeMap<String, ClinicalAttribute> clinicalPatientAttributes = clinicalAttributeDataMapper.getClinicalSampleAttributes(studyId).stream()
            .filter(clinicalAttribute -> !ClinicalAttribute.PATIENT_ID.getAttributeId().equals(clinicalAttribute.getAttributeId()))
            .filter(clinicalAttribute -> !ClinicalAttribute.SAMPLE_ID.getAttributeId().equals(clinicalAttribute.getAttributeId()))
            .collect(Collectors.toMap(
                ClinicalAttribute::getAttributeId,
                Function.identity(),
                (existing, duplicate) -> {
                    throw new IllegalStateException("Duplicate (the same attribute id) clinical patient attributes detected: " + existing + " and " + duplicate);
                },
                TreeMap::new));

        final Cursor<ClinicalPatientAttributeValue> clinicalPatientAttributeValues = clinicalAttributeDataMapper.getClinicalPatientAttributeValues(studyId);

        return new CloseableIterator<>() {
            private final PeekingIterator<ClinicalPatientAttributeValue> clinicalPatientAttributeValueIterator = Iterators.peekingIterator(clinicalPatientAttributeValues.iterator());

            @Override
            public boolean hasNext() {
                return clinicalPatientAttributeValueIterator.hasNext();
            }

            @Override
            public LinkedHashMap<ClinicalAttribute, String> next() {
                while (clinicalPatientAttributeValueIterator.hasNext()) {
                    ClinicalPatientAttributeValue clinicalPatientAttributeValue = clinicalPatientAttributeValueIterator.next();
                    LinkedHashMap<ClinicalAttribute, String> attributeValueMap = new LinkedHashMap<>();
                    attributeValueMap.put(ClinicalAttribute.PATIENT_ID, clinicalPatientAttributeValue.getPatientId());
                    if (clinicalPatientAttributes.containsKey(clinicalPatientAttributeValue.getAttributeId())) {
                        attributeValueMap.put(clinicalPatientAttributes.get(clinicalPatientAttributeValue.getAttributeId()), clinicalPatientAttributeValue.getAttributeValue());
                    }
                    while (clinicalPatientAttributeValueIterator.hasNext()
                        && clinicalPatientAttributeValueIterator.peek().getPatientId().equals(clinicalPatientAttributeValue.getPatientId())) {
                        clinicalPatientAttributeValue = clinicalPatientAttributeValueIterator.next();
                        if (clinicalPatientAttributes.containsKey(clinicalPatientAttributeValue.getAttributeId())) {
                            attributeValueMap.put(clinicalPatientAttributes.get(clinicalPatientAttributeValue.getAttributeId()), clinicalPatientAttributeValue.getAttributeValue());
                        }
                    }
                    return attributeValueMap;
                }
                throw new IllegalStateException("No more elements");
            }

            @Override
            public void close() throws IOException {
                clinicalPatientAttributeValues.close();
            }
        };
    }
}
