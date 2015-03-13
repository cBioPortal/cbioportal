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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.inject.internal.Iterables;
import java.io.File;
import java.io.IOException;

import java.util.List;
import org.apache.log4j.Logger;

import org.mskcc.cbio.importer.cvr.dmp.model.CnvIntragenicVariant;
import org.mskcc.cbio.importer.cvr.dmp.model.CnvVariant;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import scala.Tuple3;




public class TestDmpFunctions {
    public static final List<String> metadataHeadingsList = Lists.newArrayList("DMP ID","Gene","Chromosome","Cytoband","Gender", "Metastasis" ,"Metastasis Site", "Sample Coverage");
    private static final Logger logger = Logger.getLogger(TestDmpFunctions.class);
    private DmpData data;
    private final List<Tuple3<String, String, Double>> cnvList = Lists.newArrayList();
    
    public TestDmpFunctions(DmpData d){
        this.data = d;
    }
      Function<Result, List<Tuple3<String, String, Double>>> cnvFunction = new Function<Result, List<Tuple3<String, String, Double>>>() {
          
        @Override
        public List<Tuple3<String, String, Double>> apply(final Result result) {
            List<Tuple3<String, String, Double>> list1 = Lists.newArrayList();
            list1.addAll(FluentIterable.from(result.getCnvVariants())
                    .transform(new Function<CnvVariant, Tuple3<String, String, Double>>() {
                        @Override
                        public Tuple3<String, String, Double> apply(CnvVariant cnv) {
                            return new Tuple3(cnv.getGeneId(), result.getMetaData().getDmpSampleId().toString(), cnv.getGeneFoldChange());
                        }
                    }).toList());
         //  list1.addAll(FluentIterable.from(result.getCnvIntragenicVariants())
            //        .transform(new Function<CnvIntragenicVariant, Tuple3<String, String, Double>>() {
           //             @Override
              //          public Tuple3<String, String, Double> apply(CnvIntragenicVariant intra) {
            //               return new Tuple3(intra.getGeneId(), result.getMetaData().getDmpSampleId().toString(), intra.getGeneFoldChange());
             //           }
             //       }).toList());
            
            return list1;
        }
    };
      
     
    
    private void testFunction(){
        logger.info("Test function");
        
      //List< List<Tuple3<String, String, Double>>> tupleList = Lists.transform(this.data.getResults(), cnvFunction);
      //Iterable<Tuple3<String, String, Double>>  cnvList = Iterables.concat(tupleList);
       Iterable<Tuple3<String, String, Double>>  cnvList = Iterables.concat(Lists.transform(this.data.getResults(), cnvFunction));
      for (Tuple3<String, String, Double> tuple: cnvList){
          logger.info(tuple._1() +" " +tuple._2() +" " +tuple._3().toString());
      }
     
    }
    
    
    public static void main (String...args)  {
          ObjectMapper OBJECT_MAPPER = new ObjectMapper();    
        try {
           
          DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/cvr/dmp/result-dec-11.json"), DmpData.class);
           TestDmpFunctions test = new TestDmpFunctions(data);
           test.testFunction();
            
            
        } catch (IOException ex) {
          System.out.println(ex.getMessage());
        }
        
    }

   
}
