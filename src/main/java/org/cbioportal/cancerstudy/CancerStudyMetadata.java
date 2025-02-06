package org.cbioportal.cancerstudy;

import java.util.Date;

public record CancerStudyMetadata(Integer cancerStudyId, String cancerStudyIdentifier, String typeOfCancerId,
                                  String name, String description, Boolean publicStudy, String pmid, String citation,
                                  String groups, Integer status, Date importDate, Integer allSampleCount,
                                  Integer sequencedSampleCount, Integer cnaSampleCount, Integer mrnaRnaSeqSampleCount,
                                  Integer mrnaRnaSeqV2SampleCount, Integer mrnaMicroarraySampleCount,
                                  Integer miRnaSampleCount, Integer methylationHm27SampleCount, Integer rppaSampleCount,
                                  Integer massSpectrometrySampleCount, Integer completeSampleCount,
                                  String referenceGenome, Integer treatmentCount, Integer structuralVariantCount,
                                  TypeOfCancer typeOfCancer) {

    public CancerStudyMetadata(Integer cancerStudyId, String cancerStudyIdentifier, String typeOfCancerId,
                               String name, String description, Boolean publicStudy, String pmid, String citation,
                               String groups, Integer status, Date importDate, String referenceGenome, TypeOfCancer typeOfCancer){
        this(cancerStudyId, cancerStudyIdentifier, typeOfCancerId, name, description, publicStudy, pmid,citation, groups,
               status, importDate, null, null, null, null, null,
                null, null, null, null, null,
                null, referenceGenome, null, null, typeOfCancer);
    }

}
