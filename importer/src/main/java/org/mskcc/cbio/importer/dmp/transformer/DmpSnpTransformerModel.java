package org.mskcc.cbio.importer.dmp.transformer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.dmp.model.DmpSnp;
import org.mskcc.cbio.importer.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import scala.Tuple2;

import java.util.List;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/11/14.
 */
public class DmpSnpTransformerModel extends MutationModel {

    /*
    a data value object that represents a MutationModel subclass that maps
    DMP SNP attributes to a standard format for mutation data
     */

    private static final Logger logger = Logger.getLogger(DmpSnpTransformerModel.class);
    private final DmpSnp snp;

    DmpSnpTransformerModel(DmpSnp aSnp){
        Preconditions.checkArgument(null != aSnp," A DmpSnp object is required");
        this.snp = aSnp;
    }

    @Override
    public String getGene() {
        return this.snp.getGeneId();
    }

    @Override
    public String getEntrezGeneId() {
        try {
            return (Strings.isNullOrEmpty(geneMapper.symbolToEntrezID(snp.getGeneId()))) ? ""
                    : geneMapper.symbolToEntrezID(snp.getGeneId());
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getCenter() {
        return  DMPCommonNames.CENTER_MSKCC;
    }

    @Override
    public String getBuild() {
        return DMPCommonNames.DEFAULT_BUILD_NUMBER;
    }

    @Override
    public String getChromosome() {
        return this.snp.getChromosome();
    }

    @Override
    public String getStartPosition() {
        return this.snp.getStartPosition().toString();
    }

    @Override
    public String getEndPosition() {
        return Integer.valueOf(snp.getStartPosition() + snp.getRefAllele().length() -1).toString();
    }

    @Override
    public String getStrand() {
        return DMPCommonNames.DEFAULT_STRAND;
    }

    @Override
    public String getVariantClassification() {
        return snp.getVariantClass();
    }

    @Override
    public String getVariantType() {
        return resolveVariantType.apply(new Tuple2<String, String>(snp.getRefAllele(), snp.getAltAllele()));
    }

    @Override
    public String getRefAllele() {
        return snp.getRefAllele();
    }

    @Override
    public String getTumorAllele1() {
        return snp.getAltAllele();
    }

    @Override
    public String getTumorAllele2() {
        return snp.getAltAllele();
    }

    @Override
    public String getDbSNPRS() {
        return snp.getDbSNPId();
    }

    @Override
    public String getDbSNPValStatus() {
        return "";
    }

    @Override
    public String getTumorSampleBarcode() {
        return snp.getDmpSampleId();
    }

    @Override
    public String getMatchedNormSampleBarcode() {
        return "";
    }

    @Override
    public String getMatchNormSeqAllele1() {
        return "";
    }

    @Override
    public String getMatchNormSeqAllele2() {
        return "";
    }

    @Override
    public String getTumorValidationAllele1() {
        return "";
    }

    @Override
    public String getTumorValidationAllele2() {
        return "";
    }

    @Override
    public String getMatchNormValidationAllele1() {
        return "";
    }

    @Override
    public String getMatchNormValidationAllele2() {
        return "";
    }

    @Override
    public String getVerificationStatus() {
        return "";
    }

    @Override
    public String getValidationStatus() {
        return "";
    }

    @Override
    public String getMutationStatus() {
        return "";
    }

    @Override
    public String getSequencingPhase() {
        return "";
    }

    @Override
    public String getSequenceSource() {
        return "";
    }

    @Override
    public String getValidationMethod() {
        return "";
    }

    @Override
    public String getScore() {
        return "";
    }

    @Override
    public String getBAMFile() {
        return "";
    }

    @Override
    public String getSequencer() {
        return "";
    }

    @Override
    public String getTumorSampleUUID() {
        return "";
    }

    @Override
    public String getMatchedNormSampleUUID() {
        return "";
    }

    @Override
    public String getTAltCount() {
        return "";
    }

    @Override
    public String getTRefCount() {
        return "";
    }

    @Override
    public String getNAltCount() {
        return "";
    }

    @Override
    public String getNRefCount() {
        return "";
    }

    @Override
    public String getAAChange() {
        return this.snp.getAaChange();
    }

    @Override
    public String getTranscript() {
        return this.snp.getTranscriptId();
    }

    private final List<String> variationList = Lists.newArrayList("INS", "SNP", "DNP", "TNP", "ONP");
    Function<Tuple2<String, String>, String> resolveVariantType
            = new Function<Tuple2<String, String>, String>() {

        @Override
        public String apply(Tuple2<String, String> f) {
            if (!Strings.isNullOrEmpty(f._1()) && !Strings.isNullOrEmpty(f._2())) {
                String refAllele = f._1();
                String altAllele = f._2();
                if (refAllele.equals("-")) {
                    return variationList.get(0);
                }
                if (altAllele.equals("-") || altAllele.length() < refAllele.length()) {
                    return "DEL";
                }
                if ( refAllele.length() < altAllele.length()) {
                    return "INS";
                }
                if (refAllele.length() < variationList.size()) {
                    return variationList.get(refAllele.length());
                }
            }
            return "UNK";
        }

    };
}
