package org.mskcc.cbio.importer.persistence.staging.mutation;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * Created by criscuof on 12/22/14.
 * public class IcgcCancerStudyCliinicalSampleUrlSupplier implements Supplier<List<String>>
 */

/*
 provides global access to the standardized transformation map for mutation maf files
 */
public enum MutationTransformation {
    INSTANCE;
    public  Map<String,String>  getTransformationMap() {
        return Suppliers.memoize(new MutationTransformationMapSupplier()).get();
    }

   public  final  Function<MutationModel, String> transformationFunction = new Function<MutationModel, String>() {
        @Override
        public String apply(final MutationModel mm) {
            final  Map<String,String> transformationMap = MutationTransformation.INSTANCE.getTransformationMap();
            Set<String> attributeList = transformationMap.keySet();
            List<String> mafAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return StagingUtils.pojoStringGetter(getterName, mm);

                        }
                    }).toList();
            String retRecord = StagingCommonNames.tabJoiner.join(mafAttributes);

            return retRecord;
        }

    };


    private class MutationTransformationMapSupplier implements Supplier<Map<String, String>> {

    @Override
    public Map<String, String> get() {
        Map<String,String> transformationMap = Maps.newTreeMap();

            transformationMap.put("001Hugo_Symbol",  "getGene"); //1
            transformationMap.put("002Entrez_Gene_Id", "getEntrezGeneId"); //2
            transformationMap.put("003Center", "getCenter"); //3
            transformationMap.put("004NCBI_Build","getBuild"); //4
            transformationMap.put("005Chromosome", "getChromosome"); //4
            transformationMap.put("006Start_Position", "getStartPosition");
            transformationMap.put("007End_Position", "getEndPosition"); //7
            transformationMap.put("008Strand","getStrand"); //
            transformationMap.put("009Variant_Classification", "getVariantClassification");
            transformationMap.put("010Variant_Type","getVariantType");

            transformationMap.put("011Reference_Allele", "getRefAllele");
            transformationMap.put("012Tumor_Seq_Allele1", "getTumorAllele1");
            transformationMap.put("013Tumor_Seq_Allele2", "getTumorAllele2");
            transformationMap.put("014dbSNP_RS", "getDbSNPRS");
            transformationMap.put("015dbSNP_Val_Status", "getDbSNPValStatus");
            transformationMap.put("016Tumor_Sample_Barcode", "getTumorSampleBarcode");
            transformationMap.put("017Matched_Norm_Sample_Barcode", "getMatchedNormSampleBarcode"); //17
            transformationMap.put("018Match_Norm_Seq_Allele1", "getMatchNormSeqAllele1"); //18
            transformationMap.put("019Match_Norm_Seq_Allele2", "getMatchNormSeqAllele2"); //19
            transformationMap.put("020Tumor_Validation_Allele1", "getTumorValidationAllele1"); //20

            transformationMap.put("021Tumor_Validation_Allele2", "getTumorValidationAllele2"); //21
            transformationMap.put("022Match_Norm_Validation_Allele1", "getMatchNormValidationAllele1");  //22
            transformationMap.put("023Match_Norm_Validation_Allele2", "getMatchNormValidationAllele2"); //23
            transformationMap.put("024Verification_Status", "getVerificationStatus"); //24
            transformationMap.put("025Validation_Status", "getValidationStatus"); //25
            transformationMap.put("026Mutation_Status","getMutationStatus"); //26
            transformationMap.put("027Sequencing_Phase","getSequencingPhase");  //27
            transformationMap.put("028Sequence_Source", "getSequenceSource"); //28
            transformationMap.put("029Validation_Method", "getValidationMethod");// 29
            transformationMap.put("030Score", "getScore");  //30

            transformationMap.put("031BAM_File","getBAMFile"); //31
            transformationMap.put("032Sequencer","getSequencer");//32
            transformationMap.put("033Tumor_Sample_UUID", "getTumorSampleUUID");//33
            transformationMap.put("034Matched_Norm_Sample_UUID", "getMatchedNormSampleUUID"); //34
            transformationMap.put("035t_ref_count", "getTRefCount"); // new
            transformationMap.put("036t_alt_count", "getTAltCount");  // ne
            transformationMap.put("037n_ref_count", "getNRefCount");  //new
            transformationMap.put("038n_alt_count", "getNAltCount");  // new
            transformationMap.put("039cDNA_Change","getCDNA_change");
            transformationMap.put("040Amino_Acid_Change","getAAChange");
            transformationMap.put("041Transcript","getTranscript");
        return transformationMap;

    }
    }
}
