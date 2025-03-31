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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service to retrieve clinical data attributes and values for a study
 */
public class ClinicalAttributeDataService {

    public static final Set<String> DERIVED_SAMPLE_ATTRIBUTES = Set.of("MUTATION_COUNT", "FRACTION_GENOME_ALTERED");

    private final ClinicalAttributeDataMapper clinicalAttributeDataMapper;

    public ClinicalAttributeDataService(ClinicalAttributeDataMapper clinicalAttributeDataMapper) {
        this.clinicalAttributeDataMapper = clinicalAttributeDataMapper;
    }

    public CloseableIterator<SequencedMap<ClinicalAttribute, String>> getClinicalSampleAttributeData(String studyId) {
        final LinkedHashMap<String, ClinicalAttribute> clinicalSampleAttributes = Stream.concat(
                Stream.of(ClinicalAttribute.PATIENT_ID, ClinicalAttribute.SAMPLE_ID),
                clinicalAttributeDataMapper.getClinicalSampleAttributes(studyId).stream()
                    .filter(clinicalAttribute -> !DERIVED_SAMPLE_ATTRIBUTES.contains(clinicalAttribute.getAttributeId())))
            .collect(Collectors.toMap(
                ClinicalAttribute::getAttributeId,
                Function.identity(),
                (existing, duplicate) -> {
                    throw new IllegalStateException("Duplicate (the same attribute id) clinical sample attributes detected: " + existing + " and " + duplicate);
                },
                LinkedHashMap::new));

        final Cursor<ClinicalSampleAttributeValue> clinicalSampleAttributeValues = clinicalAttributeDataMapper.getClinicalSampleAttributeValues(studyId);

        return new CloseableIterator<>() {

            private final PeekingIterator<ClinicalSampleAttributeValue> clinicalSampleAttributeValueIterator = Iterators.peekingIterator(clinicalSampleAttributeValues.iterator());

            @Override
            public boolean hasNext() {
                return clinicalSampleAttributeValueIterator.hasNext();
            }

            @Override
            public SequencedMap<ClinicalAttribute, String> next() {
                if (clinicalSampleAttributeValueIterator.hasNext()) {
                    ClinicalSampleAttributeValue clinicalSampleAttributeValue = clinicalSampleAttributeValueIterator.next();
                    var attributeValueMap = new HashMap<String, String>();
                    if (!DERIVED_SAMPLE_ATTRIBUTES.contains(clinicalSampleAttributeValue.getAttributeId())) {
                        attributeValueMap.put(clinicalSampleAttributeValue.getAttributeId(), clinicalSampleAttributeValue.getAttributeValue());
                    }
                    while (clinicalSampleAttributeValueIterator.hasNext()
                        && clinicalSampleAttributeValueIterator.peek().getPatientId().equals(clinicalSampleAttributeValue.getPatientId())
                        && clinicalSampleAttributeValueIterator.peek().getSampleId().equals(clinicalSampleAttributeValue.getSampleId())) {
                        clinicalSampleAttributeValue = clinicalSampleAttributeValueIterator.next();
                        if (clinicalSampleAttributes.containsKey(clinicalSampleAttributeValue.getAttributeId())) {
                            if (!DERIVED_SAMPLE_ATTRIBUTES.contains(clinicalSampleAttributeValue.getAttributeId())) {
                                attributeValueMap.put(clinicalSampleAttributeValue.getAttributeId(), clinicalSampleAttributeValue.getAttributeValue());
                            }
                        }
                    }
                    attributeValueMap.put(ClinicalAttribute.PATIENT_ID.getAttributeId(), clinicalSampleAttributeValue.getPatientId());
                    attributeValueMap.put(ClinicalAttribute.SAMPLE_ID.getAttributeId(), clinicalSampleAttributeValue.getSampleId());
                    var result = new LinkedHashMap<ClinicalAttribute, String>();
                    clinicalSampleAttributes.forEach((attributeId, attribute) -> result.put(attribute, attributeValueMap.remove(attributeId)));
                    if (!attributeValueMap.isEmpty()) {
                        throw new IllegalStateException("The following sample attributes do not have metadata declared for the study (" + studyId + "): " + attributeValueMap.keySet());
                    }
                    return result;
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
        final LinkedHashMap<String, ClinicalAttribute> clinicalPatientAttributes = Stream.concat(
            Stream.of(ClinicalAttribute.PATIENT_ID),
            clinicalAttributeDataMapper.getClinicalPatientAttributes(studyId).stream()
        ).collect(Collectors.toMap(
            ClinicalAttribute::getAttributeId,
            Function.identity(),
            (existing, duplicate) -> {
                throw new IllegalStateException("Duplicate (the same attribute id) clinical patient attributes detected: " + existing + " and " + duplicate);
            },
            LinkedHashMap::new));

        if (clinicalPatientAttributes.containsKey(ClinicalAttribute.SAMPLE_ID.getAttributeId())) {
            throw new IllegalStateException("SAMPLE_ID is not allowed as patient attribute. studyId=" + studyId);
        }

        final Cursor<ClinicalPatientAttributeValue> clinicalPatientAttributeValues = clinicalAttributeDataMapper.getClinicalPatientAttributeValues(studyId);

        return new CloseableIterator<>() {
            private final PeekingIterator<ClinicalPatientAttributeValue> clinicalPatientAttributeValueIterator = Iterators.peekingIterator(clinicalPatientAttributeValues.iterator());

            @Override
            public boolean hasNext() {
                return clinicalPatientAttributeValueIterator.hasNext();
            }

            @Override
            public SequencedMap<ClinicalAttribute, String> next() {
                if (clinicalPatientAttributeValueIterator.hasNext()) {
                    ClinicalPatientAttributeValue clinicalPatientAttributeValue = clinicalPatientAttributeValueIterator.next();
                    var attributeValueMap = new HashMap<String, String>();
                    attributeValueMap.put(clinicalPatientAttributeValue.getAttributeId(), clinicalPatientAttributeValue.getAttributeValue());
                    while (clinicalPatientAttributeValueIterator.hasNext()
                        && clinicalPatientAttributeValueIterator.peek().getPatientId().equals(clinicalPatientAttributeValue.getPatientId())) {
                        clinicalPatientAttributeValue = clinicalPatientAttributeValueIterator.next();
                        attributeValueMap.put(clinicalPatientAttributeValue.getAttributeId(), clinicalPatientAttributeValue.getAttributeValue());
                    }
                    attributeValueMap.put(ClinicalAttribute.PATIENT_ID.getAttributeId(), clinicalPatientAttributeValue.getPatientId());
                    var result = new LinkedHashMap<ClinicalAttribute, String>();
                    clinicalPatientAttributes.forEach((attributeId, attribute) -> result.put(attribute, attributeValueMap.remove(attributeId)));
                    if (!attributeValueMap.isEmpty()) {
                        throw new IllegalStateException("The following patient attributes do not have metadata declared for the study (" + studyId + "): " + attributeValueMap.keySet());
                    }
                    return result;
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
