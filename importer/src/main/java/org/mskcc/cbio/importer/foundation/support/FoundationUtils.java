/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    
    Map<String,String>complimentMap = new ConcurrentHashMap<>();
    private static final Logger logger = Logger.getLogger(FoundationUtils.class);
     private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
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
        
        if(complimentMap.isEmpty()){
           complimentMap.put("A", "T");
           complimentMap.put("T", "A");
           complimentMap.put("C", "G");
           complimentMap.put("G", "C");
           complimentMap.put("a", "T");
           complimentMap.put("t", "A");
           complimentMap.put("c", "G");
           complimentMap.put("g", "C");  
        }
        if( !complimentMap.containsKey(base)){
            return "";
        }
       
         return complimentMap.get(base);
    }
    
    public File configureOutputFile(String xmlFileName, String reportName, String baseDir){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xmlFileName), "An XML file name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(reportName), "A report name is required");
        Preconditions.checkState( (new File(xmlFileName)).exists(), "The specified XML input file does not exist");
        Preconditions.checkState( (new File(xmlFileName)).canRead(), "The specified XML input file cannot be read");
        String studyName = Files.getNameWithoutExtension(xmlFileName);
        logger.info("Preparing output file for study: " +studyName);
        String pathSep = System.getProperty("file.separator");
        String outDirectoryName = null;
        if (!Strings.isNullOrEmpty(baseDir) && new File(baseDir).isDirectory()) {
            outDirectoryName = baseDir + pathSep + studyName;
        } else {
            outDirectoryName = CommonNames.DEFAULT_FOUNDATION_OUTPUT_BASE + pathSep + studyName;
         }
       
        String reportFileName = outDirectoryName + pathSep +reportName;
         logger.info("Report: " + reportName+" Output file: " +reportFileName);
         File outFile = new File(reportFileName);
        try {
            // create this directory and any parents as necessary
            Files.createParentDirs(outFile);
            // copy xml file to destination directory if necessary
            this.copyXmlSourceFile(outDirectoryName, xmlFileName);
            return outFile;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return null;     
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
