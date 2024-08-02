package org.cbioportal.service.studyview;

import org.cbioportal.model.SampleTreatment;
import org.cbioportal.model.SampleTreatmentReport;
import org.cbioportal.persistence.StudyViewRepository;
import org.cbioportal.persistence.model.SampleAcquisitionEventRecord;
import org.cbioportal.persistence.model.TreatmentRecord;
import org.cbioportal.web.parameter.StudyViewFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class TreatmentStudyViewServiceImpl implements TreatmentStudyViewService {
    
    private final StudyViewRepository studyViewRepository;
    
    @Autowired
    public TreatmentStudyViewServiceImpl(StudyViewRepository studyViewRepository) {
        this.studyViewRepository = studyViewRepository;
    }
    
    @Override
    public SampleTreatmentReport getSampleTreatmentReport(StudyViewFilter studyViewFilter) {
        var sampleAcquisitionEventsPerPatient = studyViewRepository.getSampleAcquisitionEventsPerPatient(studyViewFilter);
        var treatmentsPerPatient = studyViewRepository.getTreatmentsPerPatient(studyViewFilter);
        
        return generateSampleTreatmentReport(sampleAcquisitionEventsPerPatient, treatmentsPerPatient);
    }
    
    private SampleTreatmentReport generateSampleTreatmentReport(Map<String, List<SampleAcquisitionEventRecord>> sampleAcquisitionEventsPerPatient,
                                                                Map<String, List<TreatmentRecord>> treatmentsPerPatient) {
        
       Map<String,SampleTreatment> sampleTreatments = new HashMap<>();
       for (var entry : treatmentsPerPatient.entrySet()){
           var sampleAcquisitionEvents = sampleAcquisitionEventsPerPatient.getOrDefault(entry.getKey(), List.of());
           for (var treatmentRecord : entry.getValue()){
               var sampleTreatment = calculateSampleTreatmentCount(sampleAcquisitionEvents, treatmentRecord);
               var sampleTreatmentToUpdate = sampleTreatments.getOrDefault(treatmentRecord.treatment(), new SampleTreatment(treatmentRecord.treatment(), 0, 0));
               var sampleTreatmentUpdate = mergeSampleTreatments(sampleTreatment, sampleTreatmentToUpdate);
               sampleTreatments.put(treatmentRecord.treatment(), sampleTreatmentUpdate);
           }
        }

        int totalSampleCount = sampleAcquisitionEventsPerPatient
            .values()
            .stream().flatMap(List::stream)
            .collect(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SampleAcquisitionEventRecord::sampleId))))
            .size();
            
        return new SampleTreatmentReport(treatmentsPerPatient.size(), totalSampleCount, sampleTreatments.values()); 
    }
    
    private SampleTreatment mergeSampleTreatments(SampleTreatment s1, SampleTreatment s2) {
        return new SampleTreatment(s1.treatment(),
            s1.preSampleCount() + s2.preSampleCount(),
            s1.postSampleCount() + s2.postSampleCount());
    }
    
    private SampleTreatment calculateSampleTreatmentCount(List<SampleAcquisitionEventRecord> sampleAcquisitionEventRecords, TreatmentRecord treatment) {
        int postTreatmentCount = 0;
        for (var event : sampleAcquisitionEventRecords) {
            postTreatmentCount = (event.timeTaken() > treatment.startTime()) ? postTreatmentCount + 1 : postTreatmentCount;
        }
        int preTreatmentCount = sampleAcquisitionEventRecords.size() - postTreatmentCount;
        return new SampleTreatment(treatment.treatment(), preTreatmentCount, postTreatmentCount);
    }
}
