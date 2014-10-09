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
 */

package org.mskcc.cbio.importer.dmp.transformer;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.gdata.util.common.base.Joiner;
import com.google.inject.internal.Preconditions;
import java.util.List;
import java.util.Map;
import org.mskcc.cbio.importer.dmp.model.CnvVariant;
import scala.Tuple2;


public class CnvVariantDataTransformer implements DMPTransformable {
    
    private final List<CnvVariant> cnvVariantList;
    private static final String DATA_TYPE = "CNV_Variant";
    public static final Joiner tabJoiner = Joiner.on("\t");
    private Map<String,String> baseMap;
   
    
     public CnvVariantDataTransformer(final Map<String,String> aMap, final List<CnvVariant> aList){
        Preconditions.checkArgument(null != aMap, "A Map of metadata values is required");
        Preconditions.checkArgument(!aList.isEmpty(),
                "A List of CnvVariant objects is required");
        this.cnvVariantList = aList;
        this.baseMap = aMap;
    }
     
       @Override
    public Tuple2<String, Function> getTransformationFunction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
       
    /*
    function to add the CnvVariant attributes to the metadata attributes
    */
    private Function<CnvVariant,Map<String,String>> cnvMapFunction
            = new Function<CnvVariant,Map<String,String>>() {

        @Override
        public Map<String, String> apply(CnvVariant cnv) {
            Map<String,String> cnvMap = Maps.newTreeMap(); 
            cnvMap.putAll(baseMap);
          cnvMap.put("100Chromosome", cnv.getChromosome());
            cnvMap.put("101CNV_Class",cnv.getCnvClassName());
            return cnvMap;
        }
    };
     
    
    private Function<Map<String,String>,String> cnvTransformationFunction = 
            new Function<Map<String,String>,String>() {
        @Override
        public String apply(Map<String, String> cnvMap) {
            return tabJoiner.join(cnvMap.values());
        }
    };
    /*
    method to transform a list of CnvVariant objects to a List of Strings
    prepends the Cnv data with metadata attributes
    */
  public  List<String> transform() {
        return FluentIterable.from(this.cnvVariantList)
                .transform(cnvMapFunction)
                .transform(cnvTransformationFunction)
                .toList();
    }

}
/*

    @JsonProperty("confidence_class")
    private String confidenceClass;
    @JsonProperty("confidence_cv_id")
    private Integer confidenceCvId;
    @JsonProperty("cytoband")
    private String cytoband;
    @JsonProperty("gene_fold_change")
    private Double geneFoldChange;
    @JsonProperty("gene_id")
    private String geneId;
    @JsonProperty("gene_p_value")
    private Double genePValue;
    @JsonProperty("is_significant")
    private Integer isSignificant;
    @JsonProperty("variant_status_cv_id")
    private Integer variantStatusCvId;
    @JsonProperty("variant_status_name")
    private String variantStatusName;
*/