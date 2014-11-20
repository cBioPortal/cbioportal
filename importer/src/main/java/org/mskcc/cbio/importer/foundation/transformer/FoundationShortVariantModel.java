package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.ShortVariantType;

import org.mskcc.cbio.importer.cvr.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.foundation.support.FoundationCommonNames;
import org.mskcc.cbio.importer.foundation.support.FoundationUtils;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import scala.Tuple2;

import java.util.List;

/**
 * <p>
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
 * Created by fcriscuo on 11/8/14.
 */
public class FoundationShortVariantModel extends MutationModel{

    /*
    responsible for supporting the standard MutationModel contract using attributes from
    the Foundation Short Variant XML element
     */
    private static final Logger logger = Logger.getLogger(FoundationShortVariantModel.class);
    private final ShortVariantType svt;

    public FoundationShortVariantModel(ShortVariantType anSvt){
        super();
        Preconditions.checkArgument(null != anSvt, "A ShortVariantType instance is required");
        this.svt = anSvt;
    }

    @Override
    public String getGene() {
        return this.svt.getGene();
    }

    @Override
    public String getEntrezGeneId() {
        try {
            return (Strings.isNullOrEmpty(geneMapper.symbolToEntrezID(svt.getGene()))) ? "" : geneMapper.symbolToEntrezID(svt.getGene());
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public String getCenter() {
        return DMPCommonNames.CENTER_FOUNDATION;
    }

    @Override
    public String getBuild() {
        return DMPCommonNames.DEFAULT_BUILD_NUMBER;
    }

    @Override
    public String getChromosome() {
        return Lists.newArrayList(StagingCommonNames.posSplitter.split(svt.getPosition()).iterator()).get(0)
                .replace(FoundationCommonNames.CHR_PREFIX, "");
    }

    @Override
    public String getStartPosition() {
        return Lists.newArrayList(StagingCommonNames.posSplitter.split(svt.getPosition()).iterator()).get(1);
    }

    @Override
    public String getEndPosition() {
        return this.calculateEndPosition.apply(new Tuple2(svt.getCdsEffect(), svt.getPosition()));
    }

    private final Function<Tuple2<String, String>, String> calculateEndPosition
            = new Function<Tuple2<String, String>, String>() {

        @Override
        public String apply(Tuple2<String, String> f) {
            String cdsEffect = f._1();
            Integer startPos = Integer.valueOf(Lists.newArrayList(StagingCommonNames.posSplitter.split(f._2()).iterator()).get(1));
            String[] changes = cdsEffect.replaceAll("[^atcgATCG]", " ").trim().split(" "); // normally only 1
            if (cdsEffect.contains("del")  ) {

                if (changes.length > 1) {
                    return (Integer.valueOf(startPos + changes[1].length() - changes[0].length() - 1)).toString();
                }
                return (Integer.valueOf(startPos + changes[0].length()  - 1)).toString();
            }
            if (cdsEffect.contains("ins") ) {
                return (Integer.valueOf(startPos + 1)).toString();
            }
            if (cdsEffect.contains(">")) {

                if (changes.length > 0) {
                    return (Integer.valueOf(startPos + changes[0].length() - 1)).toString();
                }
            }

            logger.info("++++Unable to determine stop position for CDS effect " + cdsEffect);

            return startPos.toString();

        }
    };

    @Override
    public String getStrand() {
        return svt.getStrand();
    }

    @Override
    public String getVariantClassification() {
        return svt.getFunctionalEffect();
    }

    @Override
    public String getVariantType() {
        return this.resolveVariantType.apply(new Tuple2<String,String>(this.getRefAllele(), this.getTumorAllele1()));
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

    @Override
    public String getRefAllele() {
        return this.resolveRefAllele.apply(new Tuple2(svt.getCdsEffect(), svt.getStrand()));
    }

    Function<Tuple2<String,String>, String> resolveRefAllele =
            new Function<Tuple2<String, String>, String>() {
                public String apply(Tuple2<String, String> f) {
                    final String cdsEffect = f._1();
                    final String strand = f._2();
                    String bases = cdsEffect.replaceAll("[^tcgaTCGA]", " ").trim();
                    // check for cdsEffects without nucleotides
                    if (cdsEffect.contains("ins")) {
                        return "-";
                    }
                    if (Strings.isNullOrEmpty(bases)) {
                        logger.info("No nucleotides in CDS: " + cdsEffect);
                        return "";
                    }
                    List<String> alleleList = FluentIterable
                            .from(StagingCommonNames.blankSplitter.split(bases))
                            .transform(new Function<String, String>() {
                                @Override
                                public String apply(String input) {
                                    if ((strand.equals(FoundationCommonNames.MINUS_STRAND))) {
                                        return FoundationUtils.INSTANCE.getReverseCompliment(input);
                                    }
                                    return input.toUpperCase();
                                }
                            })
                            .toList();


                    if (alleleList.size() > 0) {

                        return alleleList.get(0);
                    }
                    return "";

                }
            };

    @Override
    public String getTumorAllele1() {
        return this.resolvetTumorAlleles.apply(new Tuple2(svt.getCdsEffect(),svt.getStrand()));
    }

    @Override
    public String getTumorAllele2() {
        return this.resolvetTumorAlleles.apply(new Tuple2(svt.getCdsEffect(),svt.getStrand()));
    }

    private Function<Tuple2<String, String>, String> resolvetTumorAlleles =
            new Function<Tuple2<String, String>, String>() {
                public String apply(Tuple2<String, String> f) {
                    String cdsEffect = f._1();
                    final String strand = f._2();

                    String bases = cdsEffect.replaceAll("[^tcgaTCGA]", " ").trim();

                    List<String> alleleList = FluentIterable
                            .from(StagingCommonNames.blankSplitter.split(bases))
                            .transform(new Function<String, String>() {
                                @Override
                                public String apply(String input) {
                                    if ((strand.equals(FoundationCommonNames.MINUS_STRAND))) {
                                        return FoundationUtils.INSTANCE.getReverseCompliment(input);
                                    }
                                    return input.toUpperCase();
                                }
                            })
                            .toList();

                    if (alleleList.size() > 1) {
                        return alleleList.get(1);
                    } else if (cdsEffect.contains("ins")) {
                        return alleleList.get(0);
                    } else if (cdsEffect.contains("del")) {
                        return "-";
                    }
                    return "";

                }
            };


    @Override
    public String getDbSNPRS() {
        return "";
    }

    @Override
    public String getDbSNPValStatus() {
        return "";
    }

    /*
    the short variant object must be transformed to include the sample id in the value attrubute
     */
    @Override
    public String getTumorSampleBarcode() {
        return svt.getValue();
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
        return FoundationCommonNames.DEFAULT_VALIDATION_STATUS;
    }

    @Override
    public String getValidationStatus() {
        return FoundationCommonNames.DEFAULT_VALIDATION_STATUS;
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
        return this.calculateTumorAltCount.apply(new Tuple2(svt.getDepth().toString(),svt.getPercentReads().toString()));
    }

    @Override
    public String getTRefCount() {
        return this.calculateTumorRefCount.apply(new Tuple2(svt.getDepth().toString(),svt.getPercentReads().toString()));
    }


    private final Function<Tuple2<String, String>, String> calculateTumorRefCount =
            new Function<Tuple2<String, String>, String>() {
                public String apply(Tuple2<String, String> f) {
                    final Long depth = Long.valueOf(f._1());
                    final Float percentReads = Float.valueOf(f._2());
                    final Long  tumorAltCount=  Math.round( depth * (percentReads/100.0));
                    return Long.toString( (long) depth - tumorAltCount);

                }
            };

    private final Function<Tuple2<String, String>, String> calculateTumorAltCount =
            new Function<Tuple2<String, String>, String>() {
                public String apply(Tuple2<String,String> f) {
                    final Float depth = Float.valueOf(f._1());
                    final Float percentReads = Float.valueOf(f._2());
                    return Long.toString(Math.round( depth * (percentReads/100.0)));

                }
            };

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
        return svt.getProteinEffect();
    }

    @Override
    public String getTranscript() {
        return svt.getTranscript();
    }


}
