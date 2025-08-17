package org.cbioportal.domain.mutation;

import java.io.Serializable;

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

    // Constructor for ID projection
    public Mutation(String molecularProfileId, String sampleId, String patientId, Integer entrezGeneId,String uniqueSampleKey,
                    String uniquePatientKey ) {
        this(
            molecularProfileId,
            sampleId,
            patientId,
            entrezGeneId,
            null, null, null, null, null, null, null, 
            null, null, null, null, null,
            null, null, null, null, null, 
            null, null, null, uniqueSampleKey, uniquePatientKey
        );
    }
}
