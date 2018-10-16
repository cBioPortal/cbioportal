package org.cbioportal.model;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

public class Mutation extends Alteration implements Serializable {
    
    private String center;
    private String mutationStatus;
    private String validationStatus;
    private Integer tumorAltCount;
    private Integer tumorRefCount;
    private Integer normalAltCount;
    private Integer normalRefCount;
    private String aminoAcidChange;
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
    private String driverFilter;
    private String driverFilterAnnotation;
    private String driverTiersFilter;
    private String driverTiersFilterAnnotation;
    private Float dipLogR;
    private Float cellularFraction;
    private Integer totalCopyNumber;
    private Integer minorCopyNumber;
    private Float cellularFractionEm;
    private Integer totalCopyNumberEm;
    private Integer minorCopyNumberEm;
    private Float purity;
    private Float ploidy;
    private Float ccfMCopies;
    private Float ccfMCopiesLower;
    private Float ccfMCopiesUpper;
    private Float ccfMCopiesProb95;
    private Float ccfMCopiesProb90;
    private Float ccfMCopiesEm;
    private Float ccfMCopiesLowerEm;
    private Float ccfMCopiesUpperEm;
    private Float ccfMCopiesProb95Em;
    private Float ccfMCopiesProb90Em;
    
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
    
    public String getDriverFilter() {
        return driverFilter;
    }
    
    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }
    
    public String getDriverFilterAnnotation() {
        return driverFilterAnnotation;
    }
    
    public void setDriverFilterAnnotation(String driverFilterAnnotation) {
        this.driverFilterAnnotation = driverFilterAnnotation;
    }
    
    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }
    
    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }
    
    public String getDriverTiersFilterAnnotation() {
        return driverTiersFilterAnnotation;
    }
    
    public void setDriverTiersFilterAnnotation(String driverTiersFilterAnnotation) {
        this.driverTiersFilterAnnotation = driverTiersFilterAnnotation;
    }

    public Float getDipLogR() {
        return dipLogR;
    }

    public void setDipLogR(Float dipLogR) {
        this.dipLogR = dipLogR;
    }

    public Float getCellularFraction() {
        return cellularFraction;
    }

    public void setCellularFraction(Float cellularFraction) {
        this.cellularFraction = cellularFraction;
    }

    public Integer getTotalCopyNumber() {
        return totalCopyNumber;
    }

    public void setTotalCopyNumber(Integer totalCopyNumber) {
        this.totalCopyNumber = totalCopyNumber;
    }

    public Integer getMinorCopyNumber() {
        return minorCopyNumber;
    }

    public void setMinorCopyNumber(Integer minorCopyNumber) {
        this.minorCopyNumber = minorCopyNumber;
    }

    public Float getCellularFractionEm() {
        return cellularFractionEm;
    }

    public void setCellularFractionEm(Float cellularFractionEm) {
        this.cellularFractionEm = cellularFractionEm;
    }

    public Integer getTotalCopyNumberEm() {
        return totalCopyNumberEm;
    }

    public void setTotalCopyNumberEm(Integer totalCopyNumberEm) {
        this.totalCopyNumberEm = totalCopyNumberEm;
    }

    public Integer getMinorCopyNumberEm() {
        return minorCopyNumberEm;
    }

    public void setMinorCopyNumberEm(Integer minorCopyNumberEm) {
        this.minorCopyNumberEm = minorCopyNumberEm;
    }

    public Float getPurity() {
        return purity;
    }

    public void setPurity(Float purity) {
        this.purity = purity;
    }

    public Float getPloidy() {
        return ploidy;
    }

    public void setPloidy(Float ploidy) {
        this.ploidy = ploidy;
    }

    public Float getCcfMCopies() {
        return ccfMCopies;
    }

    public void setCcfMCopies(Float ccfMCopies) {
        this.ccfMCopies = ccfMCopies;
    }

    public Float getCcfMCopiesLower() {
        return ccfMCopiesLower;
    }

    public void setCcfMCopiesLower(Float ccfMCopiesLower) {
        this.ccfMCopiesLower = ccfMCopiesLower;
    }

    public Float getCcfMCopiesUpper() {
        return ccfMCopiesUpper;
    }

    public void setCcfMCopiesUpper(Float ccfMCopiesUpper) {
        this.ccfMCopiesUpper = ccfMCopiesUpper;
    }

    public Float getCcfMCopiesProb95() {
        return ccfMCopiesProb95;
    }

    public void setCcfMCopiesProb95(Float ccfMCopiesProb95) {
        this.ccfMCopiesProb95 = ccfMCopiesProb95;
    }

    public Float getCcfMCopiesProb90() {
        return ccfMCopiesProb90;
    }

    public void setCcfMCopiesProb90(Float ccfMCopiesProb90) {
        this.ccfMCopiesProb90 = ccfMCopiesProb90;
    }

    public Float getCcfMCopiesEm() {
        return ccfMCopiesEm;
    }

    public void setCcfMCopiesEm(Float ccfMCopiesEm) {
        this.ccfMCopiesEm = ccfMCopiesEm;
    }

    public Float getCcfMCopiesLowerEm() {
        return ccfMCopiesLowerEm;
    }

    public void setCcfMCopiesLowerEm(Float ccfMCopiesLowerEm) {
        this.ccfMCopiesLowerEm = ccfMCopiesLowerEm;
    }

    public Float getCcfMCopiesUpperEm() {
        return ccfMCopiesUpperEm;
    }

    public void setCcfMCopiesUpperEm(Float ccfMCopiesUpperEm) {
        this.ccfMCopiesUpperEm = ccfMCopiesUpperEm;
    }

    public Float getCcfMCopiesProb95Em() {
        return ccfMCopiesProb95Em;
    }

    public void setCcfMCopiesProb95Em(Float ccfMCopiesProb95Em) {
        this.ccfMCopiesProb95Em = ccfMCopiesProb95Em;
    }

    public Float getCcfMCopiesProb90Em() {
        return ccfMCopiesProb90Em;
    }

    public void setCcfMCopiesProb90Em(Float ccfMCopiesProb90Em) {
        this.ccfMCopiesProb90Em = ccfMCopiesProb90Em;
    }
}
