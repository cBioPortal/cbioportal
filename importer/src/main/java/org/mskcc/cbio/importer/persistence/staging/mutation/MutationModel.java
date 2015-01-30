package org.mskcc.cbio.importer.persistence.staging.mutation;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.util.GeneSymbolIDMapper;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center
 *  has been advised of the possibility of such damage.
*
 * Created by fcriscuo on 11/8/14.
  */
public abstract class MutationModel {
    /*
    abstract class to support a consistent view of mutation data
     */


    protected static IDMapper geneMapper = new GeneSymbolIDMapper();

    public static List<String> resolveColumnNames() {
        final Map<String, String> transformationMap = MutationTransformation.INSTANCE.getTransformationMap();
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    /*
    abstract getters that a subclass must implement
     */

    public abstract String getGene();

    public abstract String getEntrezGeneId();

    public abstract String getCenter();

    public abstract String getBuild();

    public abstract String getChromosome();

    public abstract String getStartPosition();

    public abstract String getEndPosition();

    public abstract String getStrand();

    public abstract String getVariantClassification();

    public abstract String getVariantType();

    public abstract String getRefAllele();

    public abstract String getTumorAllele1();

    public abstract String getTumorAllele2();

    public abstract String getDbSNPRS();

    public abstract String getDbSNPValStatus();

    public abstract String getTumorSampleBarcode();

    public abstract String getMatchedNormSampleBarcode();

    public abstract String getMatchNormSeqAllele1();

    public abstract String getMatchNormSeqAllele2();

    public abstract String getTumorValidationAllele1();

    public abstract String getTumorValidationAllele2();

    public abstract String getMatchNormValidationAllele1();

    public abstract String getMatchNormValidationAllele2();

    public abstract String getVerificationStatus();

    public abstract String getValidationStatus();

    public abstract String getMutationStatus();

    public abstract String getSequencingPhase();

    public abstract String getSequenceSource();

    public abstract String getValidationMethod();

    public abstract String getScore();

    public abstract String getBAMFile();

    public abstract String getSequencer();

    public abstract String getTumorSampleUUID();

    public abstract String getMatchedNormSampleUUID();

    public abstract String getTAltCount();

    public abstract String getTRefCount();

    public abstract String getNAltCount();

    public abstract String getNRefCount();

    public abstract String getAAChange();

    public abstract String getTranscript();


    /*
     provide access to the transformation function
      */
    public static Function<MutationModel, String> getTransformationFunction() {
        return MutationTransformation.INSTANCE.transformationFunction;
    }


}

