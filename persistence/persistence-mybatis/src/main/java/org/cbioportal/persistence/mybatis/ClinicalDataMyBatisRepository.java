package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.ClinicalAttribute;
import org.cbioportal.model.ClinicalData;
import org.cbioportal.model.ClinicalDataCount;
import org.cbioportal.model.Patient;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.ClinicalDataRepository;
import org.cbioportal.persistence.ClinicalAttributeRepository;
import org.cbioportal.persistence.PatientRepository;
import org.cbioportal.persistence.PersistenceConstants;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ClinicalDataMyBatisRepository implements ClinicalDataRepository {

    @Autowired
    private ClinicalDataMapper clinicalDataMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private OffsetCalculator offsetCalculator;
    @Autowired
    private ClinicalAttributeRepository clinicalAttributeRepository;

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId,
                                                                String attributeId, String projection,
                                                                Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction) {

        return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
            attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaSampleClinicalData(String studyId, String sampleId, String attributeId) {
        return clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
            attributeId != null ? Arrays.asList(attributeId) : null);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataOfPatientInStudy(String studyId, String patientId,
                                                                 String attributeId, String projection,
                                                                 Integer pageSize, Integer pageNumber,
                                                                 String sortBy, String direction) {

        return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
            attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
            offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
    }

    @Override
    public BaseMeta getMetaPatientClinicalData(String studyId, String patientId, String attributeId) {
        return clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), Arrays.asList(patientId),
            attributeId != null ? Arrays.asList(attributeId) : null);
    }

    @Override
    public List<ClinicalData> getAllClinicalDataInStudy(String studyId, String attributeId,
                                                        String clinicalDataType, String projection,
                                                        Integer pageSize, Integer pageNumber,
                                                        String sortBy, String direction) {

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), null, 
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        } else {
            return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), null, 
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
                offsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);
        }
    }

    @Override
    public BaseMeta getMetaAllClinicalData(String studyId, String attributeId, String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), null,
                attributeId != null ? Arrays.asList(attributeId) : null).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), null,
                attributeId != null ? Arrays.asList(attributeId) : null).getTotalCount());
        }

        return baseMeta;
    }

    @Override
    public List<ClinicalData> fetchAllClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                          String clinicalDataType, String projection) {

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), ids, attributeIds,
                projection, 0, 0, null, null);
        } else {
            return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), ids, attributeIds,
                projection, 0, 0, null, null);
        }
    }

    @Override
    public BaseMeta fetchMetaClinicalDataInStudy(String studyId, List<String> ids, List<String> attributeIds,
                                                 String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(Arrays.asList(studyId), ids,
                attributeIds).getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(Arrays.asList(studyId), ids,
                attributeIds).getTotalCount());
        }

        return baseMeta;
    }

    @Override
    public List<ClinicalData> fetchClinicalData(List<String> studyIds, List<String> ids,
                                                List<String> attributeIds, String clinicalDataType,
                                                String projection) {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.getSampleClinicalData(studyIds, ids, attributeIds, projection, 0, 0, null, null);
        } else {
            return clinicalDataMapper.getPatientClinicalData(studyIds, ids, attributeIds, projection, 0, 0, null, null);
        }
    }

    public List<Integer> getVisibleSampleInternalIdsForClinicalTable(List<String> studyIds, List<String> sampleIds,
                                                                       Integer pageSize, Integer pageNumber, String searchTerm,
                                                                       String sortBy, String direction) {
        if (sampleIds.isEmpty()) {
            return new ArrayList<>();
        }
        int offset = offsetCalculator.calculate(pageSize, pageNumber);
        String sortAttrId = sortBy;
        Boolean sortAttrIsNumber = false;
        Boolean sortIsPatientAttr = false;
        if (sortBy != null && ! sortBy.isEmpty()) {
            Optional<ClinicalAttribute> clinicalAttributeMeta = studyIds.stream()
                .map(studyId -> clinicalAttributeRepository.getClinicalAttribute(studyId, sortBy))
                .findFirst();
            Assert.isTrue(clinicalAttributeMeta.isPresent(), "Attribute was not found");
            sortAttrIsNumber = clinicalAttributeMeta.get().getDatatype().equals("NUMBER");
            sortIsPatientAttr = clinicalAttributeMeta.get().getPatientAttribute();
        }
        return clinicalDataMapper.getVisibleSampleInternalIdsForClinicalTable(studyIds, sampleIds,"SUMMARY", pageSize, 
            offset, searchTerm, sortAttrId, sortAttrIsNumber, sortIsPatientAttr, direction);
    }

    @Override
    public BaseMeta fetchMetaClinicalData(List<String> studyIds, List<String> ids, List<String> attributeIds,
                                          String clinicalDataType) {

        BaseMeta baseMeta = new BaseMeta();

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaSampleClinicalData(studyIds, ids, attributeIds)
                .getTotalCount());
        } else {
            baseMeta.setTotalCount(clinicalDataMapper.getMetaPatientClinicalData(studyIds, ids, attributeIds)
                .getTotalCount());
        }

        return baseMeta;
    }

	@Override
	public List<ClinicalDataCount> fetchClinicalDataCounts(List<String> studyIds, List<String> sampleIds,
			List<String> attributeIds, String clinicalDataType, String projection) {

        if (clinicalDataType.equals(PersistenceConstants.SAMPLE_CLINICAL_DATA_TYPE)) {
            return clinicalDataMapper.fetchSampleClinicalDataCounts(studyIds, sampleIds, attributeIds);
        } else {
            List<Patient> patients = patientRepository.getPatientsOfSamples(studyIds, sampleIds);
            List<String> patientStudyIds = new ArrayList<>();
            patients.forEach(p -> patientStudyIds.add(p.getCancerStudyIdentifier()));
            return clinicalDataMapper.fetchPatientClinicalDataCounts(patientStudyIds, 
                patients.stream().map(Patient::getStableId).collect(Collectors.toList()), attributeIds, projection);
        }
	}
	
    @Override
    public List<ClinicalData> getPatientClinicalDataDetailedToSample(List<String> studyIds, List<String> patientIds,
            List<String> attributeIds) {

        return clinicalDataMapper.getPatientClinicalDataDetailedToSample(studyIds, patientIds, attributeIds, "SUMMARY",
                0, 0, null, null);
    }

    @Override
    public List<ClinicalData> getSampleClinicalDataBySampleInternalIds(List<Integer> sampleInternalIds) {
        return clinicalDataMapper.getSampleClinicalDataBySampleInternalIds(sampleInternalIds);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalDataBySampleInternalIds(List<Integer> sampleInternalIds) {
        return clinicalDataMapper.getPatientClinicalDataBySampleInternalIds(sampleInternalIds);
    }
}
