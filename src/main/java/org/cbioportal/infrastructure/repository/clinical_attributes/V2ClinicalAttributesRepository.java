package org.cbioportal.infrastructure.repository.clinical_attributes;

import org.cbioportal.legacy.model.ClinicalAttribute;
import org.cbioportal.legacy.persistence.enums.DataSource;
import org.cbioportal.legacy.web.parameter.ClinicalDataType;
import org.springframework.stereotype.Repository;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class V2ClinicalAttributesRepository implements org.cbioportal.domain.clinical_attributes.repository.ClinicalAttributesRepository {

    private Map<DataSource, List<ClinicalAttribute>> clinicalAttributesMap = new EnumMap<>(DataSource.class);

    private final V2ClinicalAttributesMapper mapper;

    public V2ClinicalAttributesRepository(V2ClinicalAttributesMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ClinicalAttribute> getClinicalAttributesForStudies(List<String> studyIds) {
        return mapper.getClinicalAttributesForStudies(studyIds);
    }

    @Override
    public Map<String, ClinicalDataType> getClinicalAttributeDatatypeMap() {
        if (clinicalAttributesMap.isEmpty()) {
            buildClinicalAttributeNameMap();
        }

        Map<String, ClinicalDataType> attributeDatatypeMap = new HashMap<>();

        clinicalAttributesMap
                .get(DataSource.SAMPLE)
                .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.SAMPLE));

        clinicalAttributesMap
                .get(DataSource.PATIENT)
                .forEach(attribute -> attributeDatatypeMap.put(attribute.getAttrId(), ClinicalDataType.PATIENT));

        return attributeDatatypeMap;
    }

    private void buildClinicalAttributeNameMap() {
        clinicalAttributesMap = mapper.getClinicalAttributes()
                .stream()
                .collect(Collectors.groupingBy(ca -> ca.getPatientAttribute().booleanValue() ? DataSource.PATIENT : DataSource.SAMPLE));
    }
}
