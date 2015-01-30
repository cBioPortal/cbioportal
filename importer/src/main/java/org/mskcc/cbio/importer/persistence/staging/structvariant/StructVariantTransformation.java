package org.mskcc.cbio.importer.persistence.staging.structvariant;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

import javax.annotation.Nullable;
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
 * Created by criscuof on 1/29/15.
 */
public enum StructVariantTransformation {
    INSTANCE;

    public  Map<String,String>  getTransformationMap() {
        return Suppliers.memoize(new StructVariationTransformationMapSupplier()).get();
    }




    public final Function<StructVariantModel, String> transformationFunction = new Function<StructVariantModel, String>() {
        @Nullable
        @Override
        public String apply(final StructVariantModel svm) {
            final Map<String, String> transformationMap = StructVariantTransformation.INSTANCE.getTransformationMap();

            List<String> varAttributes = FluentIterable.from(transformationMap.keySet())
                    .transform(new Function<String, String>() {
                        @Nullable
                        @Override
                        public String apply(@Nullable String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return StagingUtils.pojoStringGetter(getterName,svm);
                        }
                    }).toList();
            return StagingCommonNames.tabJoiner.join(varAttributes);
        }
    };

    private class StructVariationTransformationMapSupplier implements Supplier<Map<String,String>> {

        @Override
        public Map<String, String> get() {
            Map<String,String> transformationMap = Maps.newTreeMap();

            transformationMap.put("001TumorId",  "getTumorId");
            transformationMap.put("002Chr1", "getChromosome1");
            transformationMap.put("003Pos1","getPosition1");
            transformationMap.put("004Chr2","getChromosome2");
            transformationMap.put("005Pos2", "getPosition2");
            transformationMap.put("006SV_Type","getsvType");
            transformationMap.put("007Gene1","getGene1");
            transformationMap.put("008Gene2","getGene2");
            transformationMap.put("009Site1Description","getSite1Description");
            transformationMap.put("010Site2Description","getSite2Description");
            transformationMap.put("011Fusion","getFusion");
            transformationMap.put("012Connection_Type","getConnectionType");
            transformationMap.put("013SV_LENGTH", "getSvLength");
            transformationMap.put("014MAPQ","getMapq");
            transformationMap.put("015PairEndReadSupport","getPairEndReadSupport");
            transformationMap.put("016SplitReadSupport","getSplitReadSupport");
            transformationMap.put("017BrkptType","getBrkptType");
            transformationMap.put("018ConsensusSequence","getConsensusSequence");
            transformationMap.put("019TumorVariantCount","getTumorVariantCount");
            transformationMap.put("020TumorReadCount","getTumorReadCount");
            transformationMap.put("021TumorGenotypeQScore","getTumorGenotypeQScore");
            transformationMap.put("022NormalVariantCount","getNormalVariantCount");
            transformationMap.put("023NormalReadCount","getNormalReadCount");
            transformationMap.put("024NormalGenotypeQScore","getNormalGenotypeQScore"  );
            return transformationMap;
        }

    }
}
