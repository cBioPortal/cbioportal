package org.cbioportal.web.mixin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cbioportal.model.Gene;

public class MutationEventMixin {

    @JsonIgnore
    private Integer mutationEventId;
    private Integer entrezGeneId;
    private String chr;
    private Long startPosition;
    private Long endPosition;
    private String referenceAllele;
    private String variantAllele;
    private String aminoAcidChange;
    private String mutationType;
    private String functionalImpactScore;
    private Float fisValue;
    private String xvarLink;
    private String xvarLinkPdb;
    private String xvarLinkMsa;
    private String ncbiBuild;
    private String strand;
    private String variantType;
    private String dbSnpRs;
    private String dbSnpValStatus;
    private String oncotatorDbsnpRs;
    private String oncotatorRefseqMrnaId;
    private String oncotatorCodonChange;
    private String oncotatorUniprotEntryName;
    private String oncotatorUniprotAccession;
    private Integer proteinStartPosition;
    private Integer proteinEndPosition;
    private Boolean canonicalTranscript;
    private String keyword;
    private Gene gene;
}
