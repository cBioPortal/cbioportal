package org.cbioportal.domain.mutation;

import java.io.Serializable;

// This are the data that the frontend service for mutation needs to display 
public record Mutation(
    String molecularProfileId,
    String sampleId,
    String patientId,
    Integer entrezGeneId,
    Gene gene,
    String studyId,
    String center,
    String mutationStatus,
    String validationStatus,
    Integer tumorAltCount,
    Integer tumorRefCount,
    Long startPosition,
    Long endPosition,
    String referenceAllele,
    String proteinChange,
    String mutationType,
    String ncbiBuild,
    String variantType,
    String refseqMrnaId,
    Integer proteinPosStart,
    Integer proteinPosEnd,
    String keyword,
    String chr,
    String variantAllele, 
    String uniqueSampleKey, 
    String uniquePatientKey
    ) implements Serializable {
    
    
}
