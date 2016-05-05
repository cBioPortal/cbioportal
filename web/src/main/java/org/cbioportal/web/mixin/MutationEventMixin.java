package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.cbioportal.model.Gene;

import java.math.BigInteger;

public class MutationEventMixin {

    private Integer mutationEventId;

    private Boolean canonicalTranscript;

    private String chr;

    private String dbSnpRs;

    private String dbSnpValStatus;

    private Long endPosition;

    private Float fisValue;

    private String functionalImpactScore;

    private String keyword;

    private String linkMsa;

    private String linkPdb;

    private String linkXvar;

    private String mutationType;

    private String ncbiBuild;

    private String oncotatorCodonChange;

    private String oncotatorDbsnpRs;

    private Integer oncotatorProteinPosEnd;

    private Integer oncotatorProteinPosStart;

    private String oncotatorRefseqMrnaId;

    private String oncotatorUniprotAccession;

    private String oncotatorUniprotEntryName;

    private String proteinChange;

    private String referenceAllele;

    private Long startPosition;

    private String strand;

    private String tumorSeqAllele;

    private String variantType;

    @JsonUnwrapped
    private Gene gene;
}
