package org.mskcc.cbio.portal.model;

import java.io.Serializable;

public class MutationEvent implements Serializable {
    private Integer mutationEventId;
    private Integer entrezGeneId;
    private String chr;
    private Long startPosition;
    private Long endPosition;
    private String referenceAllele;
    private String tumorSeqAllele;
    private String proteinChange;
    private String mutationType;
    private String functionalImpactScore;
    private Float fisValue;
    private String linkXvar;
    private String linkPdb;
    private String linkMsa;
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
    private Integer oncotatorProteinPosStart;
    private Integer oncotatorProteinPosEnd;
    private Boolean canonicalTranscript;
    private String keyword;

    public Integer getMutationEventId() {
        return mutationEventId;
    }

    public void setMutationEventId(Integer mutationEventId) {
        this.mutationEventId = mutationEventId;
    }

    public Integer getEntrezGeneId() {
        return entrezGeneId;
    }

    public void setEntrezGeneId(Integer entrezGeneId) {
        this.entrezGeneId = entrezGeneId;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public Long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition = startPosition;
    }

    public Long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Long endPosition) {
        this.endPosition = endPosition;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public String getTumorSeqAllele() {
        return tumorSeqAllele;
    }

    public void setTumorSeqAllele(String tumorSeqAllele) {
        this.tumorSeqAllele = tumorSeqAllele;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public String getMutationType() {
        return mutationType;
    }

    public void setMutationType(String mutationType) {
        this.mutationType = mutationType;
    }

    public String getFunctionalImpactScore() {
        return functionalImpactScore;
    }

    public void setFunctionalImpactScore(String functionalImpactScore) {
        this.functionalImpactScore = functionalImpactScore;
    }

    public Float getFisValue() {
        return fisValue;
    }

    public void setFisValue(Float fisValue) {
        this.fisValue = fisValue;
    }

    public String getLinkXvar() {
        return linkXvar;
    }

    public void setLinkXvar(String linkXvar) {
        this.linkXvar = linkXvar;
    }

    public String getLinkPdb() {
        return linkPdb;
    }

    public void setLinkPdb(String linkPdb) {
        this.linkPdb = linkPdb;
    }

    public String getLinkMsa() {
        return linkMsa;
    }

    public void setLinkMsa(String linkMsa) {
        this.linkMsa = linkMsa;
    }

    public String getNcbiBuild() {
        return ncbiBuild;
    }

    public void setNcbiBuild(String ncbiBuild) {
        this.ncbiBuild = ncbiBuild;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getDbSnpRs() {
        return dbSnpRs;
    }

    public void setDbSnpRs(String dbSnpRs) {
        this.dbSnpRs = dbSnpRs;
    }

    public String getDbSnpValStatus() {
        return dbSnpValStatus;
    }

    public void setDbSnpValStatus(String dbSnpValStatus) {
        this.dbSnpValStatus = dbSnpValStatus;
    }

    public String getOncotatorDbsnpRs() {
        return oncotatorDbsnpRs;
    }

    public void setOncotatorDbsnpRs(String oncotatorDbsnpRs) {
        this.oncotatorDbsnpRs = oncotatorDbsnpRs;
    }

    public String getOncotatorRefseqMrnaId() {
        return oncotatorRefseqMrnaId;
    }

    public void setOncotatorRefseqMrnaId(String oncotatorRefseqMrnaId) {
        this.oncotatorRefseqMrnaId = oncotatorRefseqMrnaId;
    }

    public String getOncotatorCodonChange() {
        return oncotatorCodonChange;
    }

    public void setOncotatorCodonChange(String oncotatorCodonChange) {
        this.oncotatorCodonChange = oncotatorCodonChange;
    }

    public String getOncotatorUniprotEntryName() {
        return oncotatorUniprotEntryName;
    }

    public void setOncotatorUniprotEntryName(String oncotatorUniprotEntryName) {
        this.oncotatorUniprotEntryName = oncotatorUniprotEntryName;
    }

    public String getOncotatorUniprotAccession() {
        return oncotatorUniprotAccession;
    }

    public void setOncotatorUniprotAccession(String oncotatorUniprotAccession) {
        this.oncotatorUniprotAccession = oncotatorUniprotAccession;
    }

    public Integer getOncotatorProteinPosStart() {
        return oncotatorProteinPosStart;
    }

    public void setOncotatorProteinPosStart(Integer oncotatorProteinPosStart) {
        this.oncotatorProteinPosStart = oncotatorProteinPosStart;
    }

    public Integer getOncotatorProteinPosEnd() {
        return oncotatorProteinPosEnd;
    }

    public void setOncotatorProteinPosEnd(Integer oncotatorProteinPosEnd) {
        this.oncotatorProteinPosEnd = oncotatorProteinPosEnd;
    }

    public Boolean getCanonicalTranscript() {
        return canonicalTranscript;
    }

    public void setCanonicalTranscript(Boolean canonicalTranscript) {
        this.canonicalTranscript = canonicalTranscript;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
