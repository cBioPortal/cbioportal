/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.ShortVariantType;


/**
 * 
 * 
 *
 * @author criscuof
 */
public enum FoundationUtils {
    INSTANCE;
    
    private  static Map<String,String>complimentMap = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(FoundationUtils.class);
     private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");

    final static Map<Character, Character> revComplimentMap = Maps.newHashMap();
    final Joiner nullJoiner = Joiner.on("");
    static {

        revComplimentMap.put('A', 'T');
        revComplimentMap.put('T', 'A');
        revComplimentMap.put('C', 'G');
        revComplimentMap.put('G', 'C');


         complimentMap.put("A", "T");
         complimentMap.put("T", "A");
         complimentMap.put("C", "G");
         complimentMap.put("G", "C");
         complimentMap.put("a", "T");
         complimentMap.put("t", "A");
         complimentMap.put("c", "G");
         complimentMap.put("g", "C");

    }
    /**
     * common method to calculate a displayable tumor allele count value
     * @param svt
     * @return 
     */
    public  String displayTumorAltCount(ShortVariantType svt){
        Preconditions.checkArgument(null !=svt, "A ShortVariantType is required");
        Preconditions.checkArgument(svt.getDepth() > 0, "Depth value is invalid");
        Preconditions.checkArgument(svt.getPercentReads() > 0.0, "Percent reads is invalid");
         return Long.toString(this.calculateTumorAltCount(svt));
    }
    
    public String displayTumorRefCount(ShortVariantType svt){
        Preconditions.checkArgument(null !=svt, "A ShortVariantType is required");
        Preconditions.checkArgument(svt.getDepth() > 0, "Depth value is invalid");
        Preconditions.checkArgument(svt.getPercentReads() > 0.0, "Percent reads is invalid");
         return Long.toString(this.calculateTumorRefCount(svt));
    }
    
    private long calculateTumorRefCount(ShortVariantType svt) {
        return (long) svt.getDepth() - this.calculateTumorAltCount(svt);
    }
    
    private long calculateTumorAltCount(ShortVariantType svt){
        return Math.round( (float)svt.getDepth() * (svt.getPercentReads() / 100.0) );
    }
    
    public String getCompliment(String bases){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(bases), "One or more nucleotides is required");
        if(bases.length() ==1) {
            return getBaseCompliment(bases);  // most cases
        }
        // use StringBuffer for concurrency support
        StringBuffer sb = new StringBuffer();
       for(int i =0; i < bases.length(); i++) {
           sb.append(getBaseCompliment(bases.substring(i, i+1)));
       }
       return sb.toString();    
    }
    
   
    private String getBaseCompliment(String base){
        

        if( !complimentMap.containsKey(base)){
            return "";
        }
       
         return complimentMap.get(base);
    }
    /*
    public method to return the reverse compliment of a DNA sequence
    the result is standardized to upper case
     */
    public String getReverseCompliment(String sequence){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sequence),"The sequence is null or empty");
        if(sequence.length() == 1) {
           return  this.getBaseCompliment(sequence);
        }
        List<Character> revList =  FluentIterable.from(Lists.reverse(Lists.charactersOf(sequence.toUpperCase())))
                .transform(new Function<Character,Character>() {
                    @Override
                    public Character apply(Character nuc) {
                        if(revComplimentMap.containsKey(nuc)){
                            return(revComplimentMap.get(nuc));
                        }
                        return null;

                    }
                }).toList();
       return nullJoiner.join(revList);
    }
    

    
    /**
     * Copy the xml source file to the destination directory if it hasn't 
     * been copied there already
     * @param outDirectoryName
     * @param xmlFileName 
     */
    private void copyXmlSourceFile(String outDirectoryName, String xmlFileName) throws IOException{
        Path sourcePath = Paths.get(xmlFileName);
        
        String destXmlFilename = outDirectoryName + System.getProperty("file.separator")
                +Files.getNameWithoutExtension(xmlFileName) +".xml";
        Path outPath = Paths.get(destXmlFilename);
       if(!java.nio.file.Files.exists(outPath)) {
           Files.copy(sourcePath.toFile(), outPath.toFile());
           logger.info("XML source file: " +xmlFileName +" copied to " +destXmlFilename);
       } else {
           logger.info("XML source file: " +xmlFileName +" has already been copied to "  +destXmlFilename);
       }
                
    }
    
    
    
}
