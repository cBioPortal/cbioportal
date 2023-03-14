/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.maf;

import java.util.Map;

/**
 * Encapsulates Details Regarding a Single MAF Record.
 */
public class MafRecord {
    // standard MAF cols
    private String chr;
    private String ncbiBuild;
    private long startPosition;
    private long endPosition;
    private String hugoGeneSymbol;
    // store the literal value of the gene ID column for later parsing
    private String givenEntrezGeneId;
    private String referenceAllele;
    private String variantClassification; // mutation type
    private String variantType;
    private String center; // sequencing center
    private String strand;
    private String tumorSeqAllele1;
    private String tumorSeqAllele2;
    private String dbSNP_RS;
    private String tumorSampleID;
    private String mutationStatus;
    private String validationStatus;
    private String sequencer;
    private String dbSnpValStatus;
    private String matchedNormSampleBarcode;
    private String matchNormSeqAllele1;
    private String matchNormSeqAllele2;
    private String tumorValidationAllele1;
    private String tumorValidationAllele2;
    private String matchNormValidationAllele1;
    private String matchNormValidationAllele2;
    private String verificationStatus;
    private String sequencingPhase;
    private String sequenceSource;
    private String validationMethod;
    private String score;
    private String bamFile;

    private String aminoAcidChange;

    // allele frequency cols
    private int tumorAltCount;
    private int tumorRefCount;
    private int normalAltCount;
    private int normalRefCount;
    private int tTotCov;
    private int tVarCov;
    private int nTotCov;
    private int nVarCov;
    private int tumorDepth;
    private float tumorVaf;
    private int normalDepth;
    private float normalVaf;

    // custom annotator columns
    private String proteinChange;
    private String codons;
    private String refSeq;
    private String swissprot;
    private String proteinPosition;

    // Mutation Assessor cols
    private String maFuncImpact;
    private float maFIS;
    private String maLinkVar;
    private String maLinkMsa;
    private String maLinkPdb;

    // Oncotator columns are renamed to maf columns
    private String mafDbSnpValStatus;
    private String mafVariantClassification;
    private String mafRefseqMrnaId;
    private String mafUniprotAccession;
    private String mafCodonChange;
    private int mafProteinPosStart;
    private int mafProteinPosEnd;

    // custom filtering of passenger and driver mutations cols
    private String driverFilter;
    private String driverFilterAnn;
    private String driverTiersFilter;
    private String driverTiersFilterAnn;
    private Map<String, Map<String, Object>> namespacesMap;

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public String getNcbiBuild() {
        return ncbiBuild;
    }

    public void setNcbiBuild(String ncbiBuild) {
        this.ncbiBuild = ncbiBuild;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public String getHugoGeneSymbol() {
        return hugoGeneSymbol;
    }

    public void setHugoGeneSymbol(String hugoGeneSymbol) {
        this.hugoGeneSymbol = hugoGeneSymbol;
    }

    public String getGivenEntrezGeneId() {
        return this.givenEntrezGeneId;
    }

    public void setGivenEntrezGeneId(String entrezGeneIdAsString) {
        this.givenEntrezGeneId = entrezGeneIdAsString;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public String getVariantClassification() {
        return variantClassification;
    }

    public void setVariantClassification(String variantClassification) {
        this.variantClassification = variantClassification;
    }

    public String getVariantType() {
        return variantType;
    }

    public void setVariantType(String variantType) {
        this.variantType = variantType;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getTumorSeqAllele1() {
        return tumorSeqAllele1;
    }

    public void setTumorSeqAllele1(String tumorSeqAllele1) {
        this.tumorSeqAllele1 = tumorSeqAllele1;
    }

    public String getTumorSeqAllele2() {
        return tumorSeqAllele2;
    }

    public void setTumorSeqAllele2(String tumorSeqAllele2) {
        this.tumorSeqAllele2 = tumorSeqAllele2;
    }

    public String getDbSNP_RS() {
        return dbSNP_RS;
    }

    public void setDbSNP_RS(String dbSNP_RS) {
        this.dbSNP_RS = dbSNP_RS;
    }

    public String getTumorSampleID() {
        return tumorSampleID;
    }

    public void setTumorSampleID(String tumorSampleID) {
        this.tumorSampleID = tumorSampleID;
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

    public String getSequencer() {
        return sequencer;
    }

    public void setSequencer(String sequencer) {
        this.sequencer = sequencer;
    }

    public String getDbSnpValStatus() {
        return dbSnpValStatus;
    }

    public void setDbSnpValStatus(String dbSnpValStatus) {
        this.dbSnpValStatus = dbSnpValStatus;
    }

    public String getMatchedNormSampleBarcode() {
        return matchedNormSampleBarcode;
    }

    public void setMatchedNormSampleBarcode(String matchedNormSampleBarcode) {
        this.matchedNormSampleBarcode = matchedNormSampleBarcode;
    }

    public String getMatchNormSeqAllele1() {
        return matchNormSeqAllele1;
    }

    public void setMatchNormSeqAllele1(String matchNormSeqAllele1) {
        this.matchNormSeqAllele1 = matchNormSeqAllele1;
    }

    public String getMatchNormSeqAllele2() {
        return matchNormSeqAllele2;
    }

    public void setMatchNormSeqAllele2(String matchNormSeqAllele2) {
        this.matchNormSeqAllele2 = matchNormSeqAllele2;
    }

    public String getTumorValidationAllele1() {
        return tumorValidationAllele1;
    }

    public void setTumorValidationAllele1(String tumorValidationAllele1) {
        this.tumorValidationAllele1 = tumorValidationAllele1;
    }

    public String getTumorValidationAllele2() {
        return tumorValidationAllele2;
    }

    public void setTumorValidationAllele2(String tumorValidationAllele2) {
        this.tumorValidationAllele2 = tumorValidationAllele2;
    }

    public String getMatchNormValidationAllele1() {
        return matchNormValidationAllele1;
    }

    public void setMatchNormValidationAllele1(String matchNormValidationAllele1) {
        this.matchNormValidationAllele1 = matchNormValidationAllele1;
    }

    public String getMatchNormValidationAllele2() {
        return matchNormValidationAllele2;
    }

    public void setMatchNormValidationAllele2(String matchNormValidationAllele2) {
        this.matchNormValidationAllele2 = matchNormValidationAllele2;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getSequencingPhase() {
        return sequencingPhase;
    }

    public void setSequencingPhase(String sequencingPhase) {
        this.sequencingPhase = sequencingPhase;
    }

    public String getSequenceSource() {
        return sequenceSource;
    }

    public void setSequenceSource(String sequenceSource) {
        this.sequenceSource = sequenceSource;
    }

    public String getValidationMethod() {
        return validationMethod;
    }

    public void setValidationMethod(String validationMethod) {
        this.validationMethod = validationMethod;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getAminoAcidChange() {
        return aminoAcidChange;
    }

    public void setAminoAcidChange(String aminoAcidChange) {
        this.aminoAcidChange = aminoAcidChange;
    }

    public String getBamFile() {
        return bamFile;
    }

    public void setBamFile(String bamFile) {
        this.bamFile = bamFile;
    }

    public int getTumorAltCount() {
        return tumorAltCount;
    }

    public void setTumorAltCount(int tumorAltCount) {
        this.tumorAltCount = tumorAltCount;
    }

    public int getTumorRefCount() {
        return tumorRefCount;
    }

    public void setTumorRefCount(int tumorRefCount) {
        this.tumorRefCount = tumorRefCount;
    }

    public int getNormalAltCount() {
        return normalAltCount;
    }

    public void setNormalAltCount(int normalAltCount) {
        this.normalAltCount = normalAltCount;
    }

    public int getNormalRefCount() {
        return normalRefCount;
    }

    public void setNormalRefCount(int normalRefCount) {
        this.normalRefCount = normalRefCount;
    }

    public int getTTotCov() {
        return tTotCov;
    }

    public void setTTotCov(int tTotCov) {
        this.tTotCov = tTotCov;
    }

    public int getTVarCov() {
        return tVarCov;
    }

    public void setTVarCov(int tVarCov) {
        this.tVarCov = tVarCov;
    }

    public int getNTotCov() {
        return nTotCov;
    }

    public void setNTotCov(int nTotCov) {
        this.nTotCov = nTotCov;
    }

    public int getNVarCov() {
        return nVarCov;
    }

    public void setNVarCov(int nVarCov) {
        this.nVarCov = nVarCov;
    }

    public int getTumorDepth() {
        return tumorDepth;
    }

    public void setTumorDepth(int tumorDepth) {
        this.tumorDepth = tumorDepth;
    }

    public float getTumorVaf() {
        return tumorVaf;
    }

    public void setTumorVaf(float tumorVaf) {
        this.tumorVaf = tumorVaf;
    }

    public int getNormalDepth() {
        return normalDepth;
    }

    public void setNormalDepth(int normalDepth) {
        this.normalDepth = normalDepth;
    }

    public float getNormalVaf() {
        return normalVaf;
    }

    public void setNormalVaf(float normalVaf) {
        this.normalVaf = normalVaf;
    }

    public String getProteinChange() {
        return proteinChange;
    }

    public void setProteinChange(String proteinChange) {
        this.proteinChange = proteinChange;
    }

    public String getCodons() {
        return codons;
    }

    public void setCodons(String codons) {
        this.codons = codons;
    }

    public String getRefSeq() {
        return refSeq;
    }

    public void setRefSeq(String refSeq) {
        this.refSeq = refSeq;
    }

    public String getSwissprot() {
        return swissprot;
    }

    public void setSwissprot(String swissprot) {
        this.swissprot = swissprot;
    }

    public String getProteinPosition() {
        return proteinPosition;
    }

    public void setProteinPosition(String proteinPosition) {
        this.proteinPosition = proteinPosition;
    }

    public String getMafVariantClassification() {
        return mafVariantClassification;
    }

    public void setMafVariantClassification(String mafVariantClassification) {
        this.mafVariantClassification = mafVariantClassification;
    }
    
    public String getMafDbSnpValStatus() {
        return mafDbSnpValStatus;
    }

    public void setMafDbSnpValStatus(String mafDbSnpValStatus) {
        this.mafDbSnpValStatus = mafDbSnpValStatus;
    }

    public String getMafRefseqMrnaId() {
        return mafRefseqMrnaId;
    }

    public void setMafRefseqMrnaId(String mafRefseqMrnaId) {
        this.mafRefseqMrnaId = mafRefseqMrnaId;
    }

    public String getMafUniprotAccession() {
        return mafUniprotAccession;
    }

    public void setMafUniprotAccession(String mafUniprotAccession) {
        this.mafUniprotAccession = mafUniprotAccession;
    }

    public String getMafCodonChange() {
        return mafCodonChange;
    }

    public void setMafCodonChange(String mafCodonChange) {
        this.mafCodonChange = mafCodonChange;
    }

    public int getMafProteinPosStart() {
        return mafProteinPosStart;
    }

    public void setMafProteinPosStart(int mafProteinPosStart) {
        this.mafProteinPosStart = mafProteinPosStart;
    }

    public int getMafProteinPosEnd() {
        return mafProteinPosEnd;
    }

    public void setMafProteinPosEnd(int mafProteinPosEnd) {
        this.mafProteinPosEnd = mafProteinPosEnd;
    }

    public String getMaFuncImpact() {
        return maFuncImpact;
    }

    public void setMaFuncImpact(String maFuncImpact) {
        this.maFuncImpact = maFuncImpact;
    }

    public float getMaFIS() {
        return maFIS;
    }

    public void setMaFIS(float maFIS) {
        this.maFIS = maFIS;
    }

    public String getMaLinkVar() {
        return maLinkVar;
    }

    public void setMaLinkVar(String maLinkVar) {
        this.maLinkVar = maLinkVar;
    }

    public String getMaLinkMsa() {
        return maLinkMsa;
    }

    public void setMaLinkMsa(String maLinkMsa) {
        this.maLinkMsa = maLinkMsa;
    }

    public String getMaLinkPdb() {
        return maLinkPdb;
    }

    public void setMaLinkPdb(String maLinkPdb) {
        this.maLinkPdb = maLinkPdb;
    }

    public String getDriverFilter() {
        return driverFilter;
    }

    public void setDriverFilter(String driverFilter) {
        this.driverFilter = driverFilter;
    }

    public String getDriverFilterAnn() {
        return driverFilterAnn;
    }

    public void setDriverFilterAnn(String driverFilterAnn) {
        this.driverFilterAnn = driverFilterAnn;
    }

    public String getDriverTiersFilter() {
        return driverTiersFilter;
    }

    public void setDriverTiersFilter(String driverTiersFilter) {
        this.driverTiersFilter = driverTiersFilter;
    }

    public String getDriverTiersFilterAnn() {
        return driverTiersFilterAnn;
    }

    public void setDriverTiersFilterAnn(String driverTiersFilterAnn) {
        this.driverTiersFilterAnn = driverTiersFilterAnn;
    }

    public Map<String, Map<String, Object>> getNamespacesMap() {
        return namespacesMap;
    }

    public void setNamespacesMap(Map<String, Map<String, Object>> namespacesMap) {
        this.namespacesMap = namespacesMap;
    }
}
