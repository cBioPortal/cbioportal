package org.cbioportal.service.impl.clickhouse;

import java.util.List;


import java.util.ArrayList;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.PatientNotFoundException;
import org.cbioportal.service.exception.SampleNotFoundException;
import org.cbioportal.service.exception.StudyNotFoundException;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("clickhouse")
public class SampleServiceClickHouseImpl implements SampleService {
	
	@Autowired
    private SampleRepository sampleRepository;

	@Override
	public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) throws StudyNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseMeta getMetaSamplesInStudy(String studyId) throws StudyNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		return sampleRepository.getAllSamplesInStudies(studyIds, projection, pageSize, pageNumber, sortBy, direction);
	}

	@Override
	public Sample getSampleInStudy(String studyId, String sampleId)
			throws SampleNotFoundException, StudyNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection,
			Integer pageSize, Integer pageNumber, String sortBy, String direction)
			throws StudyNotFoundException, PatientNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId)
			throws StudyNotFoundException, PatientNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds,
			String projection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> fetchSamples(List<String> sampleListIds, String projection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> getAllSamples(String keyword, List<String> studyIds, String projection, Integer pageSize,
			Integer pageNumber, String sort, String direction) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Sample> fetchSamples(StudyViewFilter interceptedStudyViewFilter, Boolean negateFilters,
			String projection) {
		return sampleRepository.fetchSamplesByStudyViewFilter(interceptedStudyViewFilter, negateFilters, projection);
	}

}
