package org.cbioportal.model;

import java.io.Serializable;

public class MutationEvent implements Serializable {

    private Integer mutationEventId;
    private Integer entrezGeneId;
    private Gene gene;
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

    public Gene getGene() {
        return gene;
    }

    public void setGene(Gene gene) {
        this.gene = gene;
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

    public String getVariantAllele() {
        return variantAllele;
    }

    public void setVariantAllele(String variantAllele) {
        this.variantAllele = variantAllele;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
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

    public String getXvarLink() {
        return xvarLink;
    }

    public void setXvarLink(String xvarLink) {
        this.xvarLink = xvarLink;
    }

    public String getXvarLinkPdb() {
        return xvarLinkPdb;
    }

    public void setXvarLinkPdb(String xvarLinkPdb) {
        this.xvarLinkPdb = xvarLinkPdb;
    }

    public String getXvarLinkMsa() {
        return xvarLinkMsa;
    }

    public void setXvarLinkMsa(String xvarLinkMsa) {
        this.xvarLinkMsa = xvarLinkMsa;
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

    public Integer getProteinStartPosition() {
        return proteinStartPosition;
    }

    public void setProteinStartPosition(Integer proteinStartPosition) {
        this.proteinStartPosition = proteinStartPosition;
    }

    public Integer getProteinEndPosition() {
        return proteinEndPosition;
    }

    public void setProteinEndPosition(Integer proteinEndPosition) {
        this.proteinEndPosition = proteinEndPosition;
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