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
import org.cbioportal.persistence.mybatis.util.PaginationCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class ClinicalDataMyBatisRepository implements ClinicalDataRepository {

    @Autowired
    private ClinicalDataMapper clinicalDataMapper;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private ClinicalAttributeRepository clinicalAttributeRepository;

    @Override
    public List<ClinicalData> getAllClinicalDataOfSampleInStudy(String studyId, String sampleId,
                                                                String attributeId, String projection,
                                                                Integer pageSize, Integer pageNumber,
                                                                String sortBy, String direction) {

        return clinicalDataMapper.getSampleClinicalData(Arrays.asList(studyId), Arrays.asList(sampleId),
            attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize, 
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
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
            PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
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
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
        } else {
            return clinicalDataMapper.getPatientClinicalData(Arrays.asList(studyId), null, 
                attributeId != null ? Arrays.asList(attributeId) : null, projection, pageSize,
                PaginationCalculator.offset(pageSize, pageNumber), sortBy, direction);
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
                                                                       String sortAttrId, String direction) {
        if (sampleIds.isEmpty()) {
            return new ArrayList<>();
        }
        Integer offset = PaginationCalculator.offset(pageSize, pageNumber);
        Boolean sortAttrIsNumber = null;
        Boolean sortIsPatientAttr = null;
        
        if (sortAttrId != null) {
            if (sortAttrId.equals("patientId") || sortAttrId.equals("sampleId")) {
                //these are both false because patientId and sampleId are actually not 
                //clinical attributes and are never numbers
                sortAttrIsNumber = false;
                sortIsPatientAttr = false;
            } else {
                ClinicalAttribute clinicalAttributeMeta = getClinicalAttributeMeta(studyIds, sortAttrId);
                sortAttrIsNumber = clinicalAttributeMeta.getDatatype().equals("NUMBER");
                sortIsPatientAttr = clinicalAttributeMeta.getPatientAttribute();
            }
        }
        
        
        
        return clinicalDataMapper.getVisibleSampleInternalIdsForClinicalTable(studyIds, sampleIds,"SUMMARY", pageSize, 
            offset, searchTerm, sortAttrId, sortAttrIsNumber, sortIsPatientAttr, direction);
    }
    
    private ClinicalAttribute getClinicalAttributeMeta(List<String> studyIds, String attrId) {
        Assert.notNull(studyIds, "Arguments may not be null");
        Assert.notNull(attrId, "Arguments may not be null");
        Stream<String> uniqueStudyIds = studyIds.stream().distinct();
        Optional<ClinicalAttribute> clinicalAttributeMeta = uniqueStudyIds
            .map(studyId -> clinicalAttributeRepository.getClinicalAttribute(studyId, attrId))
            .filter(Objects::nonNull)
            .findFirst();
        Assert.isTrue(clinicalAttributeMeta.isPresent(), "Clinical Attribute " + attrId + 
            " was not found in studies " + studyIds.stream().collect(Collectors.joining(", ")) );
        return clinicalAttributeMeta.get();
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
        return sampleInternalIds == null || sampleInternalIds.isEmpty() ?
            new ArrayList<>() : clinicalDataMapper.getSampleClinicalDataBySampleInternalIds(sampleInternalIds);
    }
    
    @Override
    public List<ClinicalData> getPatientClinicalDataBySampleInternalIds(List<Integer> sampleInternalIds) {
        return sampleInternalIds == null || sampleInternalIds.isEmpty() ?
            new ArrayList<>() : clinicalDataMapper.getPatientClinicalDataBySampleInternalIds(sampleInternalIds);
    }
}
