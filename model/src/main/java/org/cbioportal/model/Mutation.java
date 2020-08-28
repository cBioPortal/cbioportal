package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Mutation extends Alteration implements Serializable {
    
    private String center;
    private String mutationStatus;
    private String validationStatus;
    private Integer tumorAltCount;
    private Integer tumorRefCount;
    private Integer normalAltCount;
    private Integer normalRefCount;
    private String aminoAcidChange;
    private String chr;
    private Long startPosition;
    private Long endPosition;
    private String referenceAllele;
    private String tumorSeqAllele;
    private String proteinChange;
    private String mutationType;
    private String functionalImpactScore;
    private BigDecimal fisValue;
    private String linkXvar;
    private String linkPdb;
    private String linkMsa;
    private String ncbiBuild;
    private String variantType;
    private String oncotatorRefseqMrnaId;
    private Integer oncotatorProteinPosStart;
    private Integer oncotatorProteinPosEnd;
    private String keyword;
    private AlleleSpecificCopyNumber alleleSpecificCopyNumber;
    
    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getMutationStatus() {
        return mutationStatus;
    }

    public void setMutationStatus(String mutationStatus) {
        this.mutationStatus = mutationStatus;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public Integer getTumorAltCount() {
        return tumorAltCount;
    }

    public void setTumorAltCount(Integer tumorAltCount) {
        this.tumorAltCount = tumorAltCount;
    }

    public Integer getTumorRefCount() {
        return tumorRefCount;
    }

    public void setTumorRefCount(Integer tumorRefCount) {
        this.tumorRefCount = tumorRefCount;
    }

    public Integer getNormalAltCount() {
        return normalAltCount;
    }

    public void setNormalAltCount(Integer normalAltCount) {
        this.normalAltCount = normalAltCount;
    }

    public Integer getNormalRefCount() {
        return normalRefCount;
    }

    public void setNormalRefCount(Integer normalRefCount) {
        this.normalRefCount = normalRefCount;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }
    
    public String getChr() { return chr; }
    
    public void setChr(String chr) { this.chr = chr; }

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

    public BigDecimal getFisValue() {
        return fisValue;
    }

    public void setFisValue(BigDecimal fisValue) {
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

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getOncotatorRefseqMrnaId() {
        return oncotatorRefseqMrnaId;
    }

    public void setOncotatorRefseqMrnaId(String oncotatorRefseqMrnaId) {
        this.oncotatorRefseqMrnaId = oncotatorRefseqMrnaId;
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

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public AlleleSpecificCopyNumber getAlleleSpecificCopyNumber() {
        return alleleSpecificCopyNumber;
    }

    public void setAlleleSpecificCopyNumber(AlleleSpecificCopyNumber alleleSpecificCopyNumber) {
        this.alleleSpecificCopyNumber = alleleSpecificCopyNumber;
    }
}
