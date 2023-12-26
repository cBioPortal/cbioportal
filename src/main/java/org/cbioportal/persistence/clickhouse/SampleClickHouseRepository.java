package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Sample;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SampleRepository;
import org.cbioportal.persistence.clickhouse.mapper.SampleMapper;
import org.cbioportal.persistence.clickhouse.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class SampleClickHouseRepository implements SampleRepository {

    @Autowired
    private SampleMapper sampleMapper;

	@Override
	public List<Sample> getAllSamples(String keyword, List<String> studyIds, String projection, Integer pageSize,
			Integer pageNumber, String sort, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public BaseMeta getMetaSamples(String keyword, List<String> studyIds) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<Sample> getAllSamplesInStudy(String studyId, String projection, Integer pageSize, Integer pageNumber,
			String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public BaseMeta getMetaSamplesInStudy(String studyId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<Sample> getAllSamplesInStudies(List<String> studyIds, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
        return sampleMapper.getSamples(studyIds, null, null, null, projection, pageSize, 
                OffsetCalculator.calculate(pageSize, pageNumber), sortBy, direction);

	}

	@Override
	public Sample getSampleInStudy(String studyId, String sampleId) {
		// TODO Auto-generated method stub
		return new Sample();
	}

	@Override
	public List<Sample> getAllSamplesOfPatientInStudy(String studyId, String patientId, String projection,
			Integer pageSize, Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public BaseMeta getMetaSamplesOfPatientInStudy(String studyId, String patientId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<Sample> getAllSamplesOfPatientsInStudy(String studyId, List<String> patientIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public List<Sample> getSamplesOfPatientsInMultipleStudies(List<String> studyIds, List<String> patientIds,
			String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public List<Sample> fetchSamples(List<String> studyIds, List<String> sampleIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public List<Sample> fetchSamplesBySampleListIds(List<String> sampleListIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public List<Sample> fetchSampleBySampleListId(String sampleListIds, String projection) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

	@Override
	public BaseMeta fetchMetaSamples(List<String> studyIds, List<String> sampleIds) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public BaseMeta fetchMetaSamples(List<String> sampleListIds) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<Sample> getSamplesByInternalIds(List<Integer> internalIds) {
		// TODO Auto-generated method stub
		return new ArrayList<Sample>();
	}

}
