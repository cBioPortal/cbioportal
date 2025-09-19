package org.cbioportal.domain.mutation;
import java.io.Serializable;

public record Mutation(
    String uniqueSampleKey,
    String uniquePatientKey,
    String molecularProfileId,
    String sampleId,
    String patientId,
    Integer entrezGeneId,
    Gene gene,
    String studyId,
    String driverFilter,
    String driverFilterAnnotation,
    String driverTiersFilter,
    String driverTiersFilterAnnotation,
    String center,
    String mutationStatus,
    String validationStatus,
    Integer tumorAltCount,
    Integer tumorRefCount,
    Integer normalAltCount,
    Integer normalRefCount,
    String aminoAcidChange,
    String chr,
    Long startPosition,
    Long endPosition,
    String referenceAllele,
    String tumorSeqAllele,
    String proteinChange,
    String mutationType,
    String ncbiBuild,
    String variantType,
    String refseqMrnaId,
    Integer proteinPosStart,
    Integer proteinPosEnd,
    String keyword,
    AlleleSpecificCopyNumber alleleSpecificCopyNumber
) implements Serializable {
    
    // Constructor for ID projection
    public Mutation(String uniqueSampleKey, String uniquePatientKey, String molecularProfileId,
                    String sampleId, String patientId, Integer entrezGeneId, String studyId) {
        this(
            uniqueSampleKey,
            uniquePatientKey,
            molecularProfileId,
            sampleId,
            patientId,
            entrezGeneId,
            null,  // Gene
            studyId,
            null,  // driverFilter
            null,  // driverFilterAnnotation
            null,  // driverTiersFilter
            null,  // driverTiersFilterAnnotation
            null,  // center
            null,  // mutationStatus
            null,  // validationStatus
            null,  // tumorAltCount
            null,  // tumorRefCount
            null,  // normalAltCount
            null,  // normalRefCount
            null,  // aminoAcidChange
            null,  // chr
            null,  // startPosition
            null,  // endPosition
            null,  // referenceAllele
            null,  // tumorSeqAllele
            null,  // proteinChange
            null,  // mutationType
            null,  // ncbiBuild
            null,  // variantType
            null,  // refseqMrnaId
            null,  // proteinPosStart
            null,  // proteinPosEnd
            null,  // keyword
            null   // alleleSpecificCopyNumber
        );
    }
    
    
    //This constructor is meant for Detailed projection something is wrong here 
//    public Mutation(
//        String uniqueSampleKey,
//        String uniquePatientKey,
//        String molecularProfileId,
//        String sampleId,
//        String patientId,
//        Integer entrezGeneId,
//        Gene gene,
//        String studyId,
//        String driverFilter,
//        String driverFilterAnnotation,
//        String driverTiersFilter,
//        String driverTiersFilterAnnotation,
//        String center,
//        String mutationStatus,
//        String validationStatus,
//        Integer tumorAltCount,
//        Integer tumorRefCount,
//        Integer normalAltCount,
//        Integer normalRefCount,
//        String aminoAcidChange,
//        String chr,
//        Long startPosition,
//        Long endPosition,
//        String referenceAllele,
//        String tumorSeqAllele,
//        String proteinChange,
//        String mutationType,
//        String ncbiBuild,
//        String variantType,
//        String refseqMrnaId,
//        Integer proteinPosStart,
//        Integer proteinPosEnd,
//        String keyword,
//        AlleleSpecificCopyNumber alleleSpecificCopyNumber
//    ) {
//        this(
//        );
//    }
    

}
