package org.mskcc.cbio.importer.persistence.staging.fusion;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.util.GeneSymbolIDMapper;

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
 * Created by fcriscuo on 11/10/14.
 */
public abstract class FusionModel {

    protected static IDMapper geneMapper = new GeneSymbolIDMapper();

    public static final Map<String,String> transformationMap = Maps.newTreeMap();
    static {
        transformationMap.put("001Hugo_Symbol",  "getGene"); //1
        transformationMap.put("002Entrez_Gene_Id", "getEntrezGeneId"); //2
        transformationMap.put("003Center", "getCenter"); //3
        transformationMap.put("004Tumor_Sample_Barcode","getTumorSampleBarcode");
        transformationMap.put("005Fusion","getFusion");
        transformationMap.put("006DNA_support","getDNASupport");
        transformationMap.put("007RNA_support","getRNASupport");
        transformationMap.put("008Method","getMethod");
        transformationMap.put("009Frame","getFrame");
    }

    public static List<String> resolveColumnNames() {
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    public abstract String getGene();
    public abstract String getEntrezGeneId();
    public abstract String getCenter();
    public abstract String getTumorSampleBarcode();
    public abstract String getFusion();
    public abstract String getDNASupport();
    public abstract String getRNASupport();
    public abstract String getMethod();
    public abstract String getFrame();

    /*
    transformation function to transform a concrete MutationModel sublcass instance into a tab-delimited
    String
     */
     /*
    Function to transform attributes from a  Foundation Short Variant object into MAF attributes collected in
    a tsv String for subsequent output
    */
    final static Function<FusionModel, String> transformationFunction = new Function<FusionModel, String>() {
        @Override
        public String apply(final FusionModel fm) {
            Set<String> attributeList = transformationMap.keySet();
            List<String> mafAttributes = FluentIterable.from(attributeList)
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String attribute) {
                            String getterName = transformationMap.get(attribute);
                            return StagingUtils.pojoStringGetter(getterName, fm);

                        }
                    }).toList();
            String retRecord = StagingCommonNames.tabJoiner.join(mafAttributes);

            return retRecord;
        }

    };

    /*
     provide access to the transformation function
      */
    public static Function<FusionModel,String> getTransformationModel () { return transformationFunction;}


}
