package org.mskcc.cbio.importer.persistence.staging.structvariant;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationTransformation;
import org.mskcc.cbio.importer.util.GeneSymbolIDMapper;
import scala.util.parsing.combinator.testing.Str;

import java.util.List;
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
 * Created by criscuof on 1/29/15.
 */
public  abstract class StructVariantModel {

    protected static IDMapper geneMapper = new GeneSymbolIDMapper();
    protected final static String SAMPLE_ID_COLUMN_NAME = "TumorId";

    public static String getSampleIdColumnName() { return SAMPLE_ID_COLUMN_NAME;}

    public static List<String> resolveColumnNames() {
        final Map<String, String> transformationMap = StructVariantTransformation.INSTANCE.getTransformationMap();
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    public abstract String getTumorId();
    public  abstract String getChromosome1();
    public abstract String getPosition1();
    public abstract String getChromosome2();
    public abstract String getPosition2();
    public abstract String getsvType();
    public abstract String getGene1();
    public abstract String getGene2();
    public abstract String getSite1Description();
    public abstract String getSite2Description();
    public abstract String getFusion();
    public abstract String getConnectionType();
    public abstract String getSvLength();
    public abstract String getMapq();
    public abstract String getPairEndReadSupport();
    public abstract String getSplitReadSupport();
    public abstract String getBrkptType();
    public abstract String getConsensusSequence();
    public abstract String getTumorVariantCount();
    public abstract String getTumorReadCount();
    public abstract String getTumorGenotypeQScore();
    public abstract String getNormalVariantCount();
    public abstract String getNormalReadCount();
    public abstract String getNormalGenotypeQScore();


 public static Function<StructVariantModel, String> getTransformationFunction() {
     return StructVariantTransformation.INSTANCE.transformationFunction;
 }


}
