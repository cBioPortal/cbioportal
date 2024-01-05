package org.cbioportal.persistence.clickhouse;

import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cbioportal.model.ClinicalEventKeyCode;
import org.cbioportal.model.ClinicalEventSample;
import org.cbioportal.model.Treatment;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.persistence.clickhouse.mapper.TreatmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class TreatmentClickHouseRepository implements TreatmentRepository {
	
	@Autowired
	TreatmentMapper treatmentMapper;

	@Override
	public Map<String, List<Treatment>> getTreatmentsByPatientId(List<String> sampleIds, List<String> studyIds,
			ClinicalEventKeyCode key) {
        return getTreatments(sampleIds, studyIds, key)
                .stream()
                .collect(groupingBy(Treatment::getPatientId));

	}

	@Override
	public List<Treatment> getTreatments(List<String> sampleIds, List<String> studyIds, ClinicalEventKeyCode key) {
		return treatmentMapper.getAllTreatments(sampleIds, studyIds, key.getKey())
	            .stream()
	            .flatMap(treatment -> splitIfDelimited(treatment, key))
	            .collect(Collectors.toList());
	}
	


	@Override
	public Map<String, List<ClinicalEventSample>> getSamplesByPatientId(List<String> sampleIds, List<String> studyIds) {
        return treatmentMapper.getAllSamples(sampleIds, studyIds)
                .stream()
                .sorted(Comparator.comparing(ClinicalEventSample::getTimeTaken)) // put earliest events first
                .distinct() // uniqueness determined by sample id, patient id, and study id
                // combined, the sort and distinct produce the earliest clinical event row for each unique sample
                .collect(groupingBy(ClinicalEventSample::getPatientId));

	}

	@Override
	public Map<String, List<ClinicalEventSample>> getShallowSamplesByPatientId(List<String> sampleIds,
			List<String> studyIds) {
        return treatmentMapper.getAllShallowSamples(sampleIds, studyIds)
                .stream()
                .distinct()
                .collect(groupingBy(ClinicalEventSample::getPatientId));
	}

	@Override
	public Boolean hasTreatmentData(List<String> studies, ClinicalEventKeyCode key) {
		return treatmentMapper.hasTreatmentData(null, studies, key.getKey());
	}

	@Override
	public Boolean hasSampleTimelineData(List<String> studies) {
		return treatmentMapper.hasSampleTimelineData(null, studies);
	}
	
	private Stream<Treatment> splitIfDelimited(Treatment unsplitTreatment, ClinicalEventKeyCode key) {
        if (key.isDelimited()) {
            return Arrays.stream(unsplitTreatment.getTreatment().split(key.getDelimiter()))
                .map(treatmentName -> {
                    Treatment treatment = new Treatment();
                    treatment.setTreatment(treatmentName);
                    treatment.setStudyId(unsplitTreatment.getStudyId());
                    treatment.setPatientId(unsplitTreatment.getPatientId());
                    treatment.setStart(unsplitTreatment.getStart());
                    treatment.setStop(unsplitTreatment.getStop());
                    treatment.setPatientId(unsplitTreatment.getPatientId());
                    return treatment;
                });
        }
        return Stream.of(unsplitTreatment);
    }

}
