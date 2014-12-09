package org.mskcc.cbio.importer.icgc.transformer;

import com.google.common.base.*;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import scala.Tuple2;
import javax.annotation.Nullable;
import java.util.Map;

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
 * Created by criscuof on 12/7/14.
 */
public class SimpleSomaticModel extends MutationModel {

    private final Map<String,String> recordMap;
    private static final Logger logger = Logger.getLogger(SimpleSomaticModel.class);

    public SimpleSomaticModel  (Map<String,String> aMap){
        Preconditions.checkArgument(null != aMap ,
                "A Map of ICGC Simple Somatic key value attributes is required");
        Preconditions.checkArgument(!aMap.isEmpty(), "The supplied Map object is empty");
        this.recordMap = aMap;
    }
    @Override
    public String getGene() {
        return this.resolveGeneSymbol.apply(this.recordMap.get("gene_affected"));
    }

    @Override
    public String getEntrezGeneId() {
        return this.resolveEntrezId.apply(this.recordMap.get("gene_affected"));
    }

    @Override
    public String getCenter() {
        return this.recordMap.get("project_code");
    }

    @Override
    public String getBuild() {
        return this.resolveSimpleBuildNumber.apply(this.recordMap.get("assembly_version"));
    }

    @Override
    public String getChromosome() {
        return this.recordMap.get("chromosome");
    }

    @Override
    public String getStartPosition() {
        return this.recordMap.get("chromosome_start");
    }

    @Override
    public String getEndPosition() {
        return this.recordMap.get("chromosome_end");
    }

    @Override
    public String getStrand() {
        return this.recordMap.get("chromosome_strand");
    }

    @Override
    public String getVariantClassification() {
        return this.recordMap.get("consequence_type");
    }

    @Override
    public String getVariantType() {
        return this.resolveVariantType.apply(new Tuple2<String,String>(this.recordMap.get("reference_genome_allele"),
                this.recordMap.get("mutated_to_allele")));
    }

    @Override
    public String getRefAllele() {
        return this.recordMap.get("reference_genome_allele");
    }

    @Override
    public String getTumorAllele1() {
        return this.recordMap.get("mutated_to_allele");
    }

    @Override
    public String getTumorAllele2() {
        return this.recordMap.get("mutated_to_allele");
    }

    @Override
    public String getDbSNPRS() {
        return "";
    }

    @Override
    public String getDbSNPValStatus() {
        return "";
    }

    @Override
    public String getTumorSampleBarcode() {
        return this.recordMap.get("icgc_sample_id");
    }

    @Override
    public String getMatchedNormSampleBarcode() {
        return this.recordMap.get("submitted_matched_sample_id");
    }

    @Override
    public String getMatchNormSeqAllele1() {
        return this.recordMap.get("reference_genome_allele");
    }

    @Override
    public String getMatchNormSeqAllele2() {
        return this.recordMap.get("reference_genome_allele");
    }

    @Override
    public String getTumorValidationAllele1() {
        return this.recordMap.get("mutated_to_allele");
    }

    @Override
    public String getTumorValidationAllele2() {
        return this.recordMap.get("mutated_to_allele");
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
        return this.recordMap.get("verification_status");
    }

    @Override
    public String getValidationStatus() {
        return this.recordMap.get("biological_validation_status");
    }

    @Override
    public String getMutationStatus() {
        return this.recordMap.get("biological_validation_status");
    }

    @Override
    public String getSequencingPhase() {
        return "";
    }

    @Override
    public String getSequenceSource() {
        return this.recordMap.get("sequencing_strategy");
    }

    @Override
    public String getValidationMethod() {
        return "";
    }

    @Override
    public String getScore() {
        return this.recordMap.get("quality_score");
    }

    @Override
    public String getBAMFile() {
        return "";
    }

    @Override
    public String getSequencer() {
        return this.recordMap.get("platform");
    }

    @Override
    public String getTumorSampleUUID() {
        return this.recordMap.get("submitted_sample_id");
    }

    @Override
    public String getMatchedNormSampleUUID() {
        return this.recordMap.get("matched_igc_sample_id");
    }

    @Override
    public String getTAltCount() {
        return this.recordMap.get("mutant_allele_read_count");
    }

    @Override
    public String getTRefCount() {
        return this.resolveVariantType.apply(new Tuple2<String,String>(this.recordMap.get("total_read_count"),
                this.recordMap.get("mutant_allele_read_count")));
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
        return this.recordMap.get("aa_mutation");
    }

    @Override
    public String getTranscript() {
        return this.recordMap.get("cds_mutation");
    }

    /*
    transformation functions
     */

    /*
     function to calculate the reference allele count
     parameter 1 = total allele count
     parameter 2 mutant allele count
     */
    Function<Tuple2<String, String>, String> resolveReferenceCount
            = new Function<Tuple2<String,String>, String>() {

        @Override
        public String apply(Tuple2<String, String> f) {
            if (!Strings.isNullOrEmpty(f._1()) && !Strings.isNullOrEmpty(f._2())) {
                return new Integer(Integer.valueOf(f._1()) - Integer.valueOf(f._2()))
                        .toString();
            }
            return "";
        }
    };
    /*
     function to calculate the reference allele count
     parameter 1 = total allele count
     parameter 2 mutant allele count
     */


    /*
     function to supply HUGO gene symbol via a table lookup
     public Tuple2<String,String> ensemblToHugoSymbolAndEntrezID(String ensemblID) {
     */

    Function <String,String> resolveGeneSymbol =
            new Function<String,String>() {
                @Nullable
                @Override
                public String apply(final String input) {
                    Tuple2<String,String> geneTuple = geneMapper.ensemblToHugoSymbolAndEntrezID(input);
                    return geneTuple._1();
                }
            };

    Function <String,String> resolveEntrezId =
            new Function<String,String>() {
                @Nullable
                @Override
                public String apply(final String input) {
                    Tuple2<String,String> geneTuple = geneMapper.ensemblToHugoSymbolAndEntrezID(input);
                    return geneTuple._2();
                }
            };

    /*
     function to remove GRCh prefix from ICGC build value (GRCh37 -> 37)
     */
    Function<String, String> resolveSimpleBuildNumber
            = new Function<String, String>() {
        @Override
        public String apply(final String f) {
            if (!Strings.isNullOrEmpty(f)) {
                return CharMatcher.DIGIT.retainFrom(f);
            }
            return "";

        }

    };

    Function<Tuple2<String, String>, String> resolveVariantType
            = new Function<Tuple2<String, String>, String>() {
        /*
         determine variant type
         parm 1 is reference_genome_allele
         parm2 is mutated_to_allele
         both are required
         */
        @Override
        public String apply(Tuple2<String, String> f) {
            if (!Strings.isNullOrEmpty(f._1) && !Strings.isNullOrEmpty(f._2())) {
                String refAllele = f._1;
                String altAllele = f._2;
                if (refAllele.equals("-")) {
                    return StagingCommonNames.variationList.get(0);
                }
                if (altAllele.equals("-") || altAllele.length() < refAllele.length()) {
                    return "DEL";
                }
                if (refAllele.equals("-") || refAllele.length() < altAllele.length()) {
                    return "INS";
                }
                if (refAllele.length() < StagingCommonNames.variationList.size()) {
                    return StagingCommonNames.variationList.get(refAllele.length());
                }
            }
            return "UNK";
        }

    };

}
