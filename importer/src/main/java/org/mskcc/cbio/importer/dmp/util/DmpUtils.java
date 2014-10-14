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
package org.mskcc.cbio.importer.dmp.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.gdata.util.common.base.Joiner;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mskcc.cbio.importer.dmp.support.DMPCommonNames;

/*
 represents a collection of static utility methods used thoughout the application
 */
public class DmpUtils {
    
    public static final Joiner tabJoiner = Joiner.on("\t");

    public static final Map<String, Map<String, String>> reportTypeAttributeMaps = Maps.newHashMap();
    
     public static final Map<String, String> cnvIntragenicAttributeMap = Maps.newTreeMap();

    static {
        cnvIntragenicAttributeMap.put("100Gene ID", "getGeneId");
        cnvIntragenicAttributeMap.put("101Chromosome", "getChromosome");
        cnvIntragenicAttributeMap.put("102CNV_Class", "getCnvClassName");
        cnvIntragenicAttributeMap.put("103Confidence Class", "getConfidenceClass");
        cnvIntragenicAttributeMap.put("104Cytoband", "getCytoband");
        cnvIntragenicAttributeMap.put("105Gene Fold Change", "getGeneFoldChange");
        cnvIntragenicAttributeMap.put("106Gene p-value", "getGenePValue");
        cnvIntragenicAttributeMap.put("107Significant", "getIsSignificant");
        cnvIntragenicAttributeMap.put("108Variant Status", "getVariantStatusName");
        // associate these attributes with the CNV Intragenic report
        reportTypeAttributeMaps.put(DMPCommonNames.REPORT_TYPE_CNV_INTRAGENIC, cnvIntragenicAttributeMap);
    }
    
      public static final Map<String, String> cnvAttributeMap = Maps.newTreeMap();

    static {
        cnvAttributeMap.put("100Gene ID", "getGeneId");
        cnvAttributeMap.put("101Chromosome", "getChromosome");
        cnvAttributeMap.put("102CNV_Class", "getCnvClassName");
        cnvAttributeMap.put("103Confidence Class", "getConfidenceClass");
        cnvAttributeMap.put("104Cytoband", "getCytoband");
        cnvAttributeMap.put("105Gene Fold Change", "getGeneFoldChange");
        cnvAttributeMap.put("106Gene p-value", "getGenePValue");
        cnvAttributeMap.put("107Significant", "getIsSignificant");
        cnvAttributeMap.put("108Variant Status", "getVariantStatusName");
        // associate these attributes with the CNV report
        reportTypeAttributeMaps.put(DMPCommonNames.REPORT_TYPE_CNV, cnvAttributeMap);
    }

    public static final Map<String, String> intragenicAttributeMap = Maps.newTreeMap();

    static {
        intragenicAttributeMap.put("100Gene ID", "getGeneId");
        
        // associate these attributes with the intragenic report
        reportTypeAttributeMaps.put(DMPCommonNames.REPORT_TYPE_CNV_INTRAGENIC, intragenicAttributeMap);
    }

    public static final Map<String, String> snpExonicAttributeMap = Maps.newTreeMap();

    static {
        snpExonicAttributeMap.put("100Gene ID", "getGeneId");
        snpExonicAttributeMap.put("101AA Change", "getAaChange");
        snpExonicAttributeMap.put("102 Alt Allele", "getAltAllele");
        snpExonicAttributeMap.put("103cDNA Change", "getCDNAChange");
        snpExonicAttributeMap.put("104Chromosome", "getChromosome");
        snpExonicAttributeMap.put("105Comments", "getComments");
        snpExonicAttributeMap.put("106Confidence Class", "getConfidenceClass");
        snpExonicAttributeMap.put("107COSMIC ID", "getCosmicId");
        snpExonicAttributeMap.put("108dbSNP ID", "getDbSNPId");
        snpExonicAttributeMap.put("109DMP Sample MRev ID", "getDmpSampleMrevId");
        snpExonicAttributeMap.put("110DMP Sample So ID", "getDmpSampleSoId");
        snpExonicAttributeMap.put("111DMP Variant ID", "getDmpVariantId");
        snpExonicAttributeMap.put("112Exon Number", "getExonNum");
        snpExonicAttributeMap.put("113Hotspot", "getIsHotspot");
        snpExonicAttributeMap.put("114MA Freq", "getMafreq1000g");
        snpExonicAttributeMap.put("115MRev Comments", "getMrevComments");
        snpExonicAttributeMap.put("116MRev Status ID", "getMrevStatusCvId");
        snpExonicAttributeMap.put("117MRev Status", "getMrevStatusName");
        snpExonicAttributeMap.put("118Normal Ad", "getNormalAd");
        snpExonicAttributeMap.put("119Normal DP", "getNormalDp");
        snpExonicAttributeMap.put("120Normal V freq", "getNormalVfreq");
        snpExonicAttributeMap.put("121Occurance In Normal", "getOccuranceInNormal");
        snpExonicAttributeMap.put("122Occurance In Population", "getOccuranceInPop");
        snpExonicAttributeMap.put("123Ref Allele", "getRefAllele");
        snpExonicAttributeMap.put("124SNP indel Variant ID", "getSnpIndelVariantId");
        snpExonicAttributeMap.put("125Signout Comments", "getSoComments");
        snpExonicAttributeMap.put("126Signout Status", "getSoStatusName");
        snpExonicAttributeMap.put("127Start Position", "getStartPosition");
        snpExonicAttributeMap.put("128Transcript ID", "getTranscriptId");
        snpExonicAttributeMap.put("129Tumor AD", "getTumorAd");
        snpExonicAttributeMap.put("130Tumor Dp", "getTumorDp");
        snpExonicAttributeMap.put("131Tumor V freq", "getTumorVfreq");
        snpExonicAttributeMap.put("132Variant Class", "getVariantClass");
        snpExonicAttributeMap.put("133Variant Status", "getVariantStatusName");
        // associate these attributes with the SNP Exonic report type
        reportTypeAttributeMaps.put(DMPCommonNames.REPORT_TYPE_SNP_EXONIC, snpExonicAttributeMap);
    }
    
    
     public static final Map<String, String> snpSilentAttributeMap = Maps.newTreeMap();

    static {
        snpSilentAttributeMap.put("100Gene ID", "getGeneId");
        snpSilentAttributeMap.put("101AA Change", "getAaChange");
        snpSilentAttributeMap.put("102 Alt Allele", "getAltAllele");
        snpSilentAttributeMap.put("103cDNA Change", "getCDNAChange");
        snpSilentAttributeMap.put("104Chromosome", "getChromosome");
        snpSilentAttributeMap.put("105Comments", "getComments");
        snpSilentAttributeMap.put("106Confidence Class", "getConfidenceClass");
        snpSilentAttributeMap.put("107COSMIC ID", "getCosmicId");
        snpSilentAttributeMap.put("108dbSNP ID", "getDbSNPId");
        snpSilentAttributeMap.put("109DMP Sample MRev ID", "getDmpSampleMrevId");
        snpSilentAttributeMap.put("110DMP Sample So ID", "getDmpSampleSoId");
        snpSilentAttributeMap.put("111DMP Variant ID", "getDmpVariantId");
        snpSilentAttributeMap.put("112Exon Number", "getExonNum");
        snpSilentAttributeMap.put("113Hotspot", "getIsHotspot");
        snpSilentAttributeMap.put("114MA Freq", "getMafreq1000g");
        snpSilentAttributeMap.put("115MRev Comments", "getMrevComments");
        snpSilentAttributeMap.put("116MRev Status ID", "getMrevStatusCvId");
        snpSilentAttributeMap.put("117MRev Status", "getMrevStatusName");
        snpSilentAttributeMap.put("118Normal Ad", "getNormalAd");
        snpSilentAttributeMap.put("119Normal DP", "getNormalDp");
        snpSilentAttributeMap.put("120Normal V freq", "getNormalVfreq");
        snpSilentAttributeMap.put("121Occurance In Normal", "getOccuranceInNormal");
        snpSilentAttributeMap.put("122Occurance In Population", "getOccuranceInPop");
        snpSilentAttributeMap.put("123Ref Allele", "getRefAllele");
        snpSilentAttributeMap.put("124SNP indel Variant ID", "getSnpIndelVariantId");
        snpSilentAttributeMap.put("125Signout Comments", "getSoComments");
        snpSilentAttributeMap.put("126Signout Status", "getSoStatusName");
        snpSilentAttributeMap.put("127Start Position", "getStartPosition");
        snpSilentAttributeMap.put("128Transcript ID", "getTranscriptId");
        snpSilentAttributeMap.put("129Tumor AD", "getTumorAd");
        snpSilentAttributeMap.put("130Tumor Dp", "getTumorDp");
        snpSilentAttributeMap.put("131Tumor V freq", "getTumorVfreq");
        snpSilentAttributeMap.put("132Variant Class", "getVariantClass");
        snpSilentAttributeMap.put("133Variant Status", "getVariantStatusName");
        // associate these attributes with the SNP Silent report type
        reportTypeAttributeMaps.put(DMPCommonNames.REPORT_TYPE_SNP_SILENT, snpSilentAttributeMap);
    }
    public static String getColumnNamesByReportType(String reportType){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(reportType), "A report type is required");
        Preconditions.checkArgument(reportTypeAttributeMaps.containsKey(reportType),
                "Report type " +reportType +" is not supported");
        return tabJoiner.join(getPreDataColumnHeadings(),
                getDataColumnHeadingsByReportType(reportType),
                getPostDataColumnHeadings());
    }
        
    
    private static String getDataColumnHeadingsByReportType(String reportType){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(reportType), "A report type is required");
        Preconditions.checkArgument(reportTypeAttributeMaps.containsKey(reportType),
                "Report type " +reportType +" is not supported");
        List<String> headings = FluentIterable.from(reportTypeAttributeMaps.get(reportType).keySet())
                .transform(new Function<String,String>() {
            @Override
            public String apply(String s) {
                return(s.substring(3)); // strip off the three digit numeric prefix
            }
        }).toList();
           return tabJoiner.join(headings);
    }

    /*
     static method to provide a generic getter for DMP model attributes
     encapsulates handling for null attribute values
     converts non-String results to their String representation
     String geneName = DmpUtils.pojoStringGetter("getGeneName",cnaVariant);
     */
    public static String pojoStringGetter(String getterName, Object obj) {
        try {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(getterName), "A getter method name is required");
            Preconditions.checkArgument(null != obj, "An object instance is required");
            Method getterMethod = obj.getClass().getMethod(getterName);
            if (getterMethod.getReturnType() == java.lang.String.class) {
                String value = (String) getterMethod.invoke(obj);
                return (!Strings.isNullOrEmpty(value)) ? value : "";
            }
            Object value = getterMethod.invoke(obj);
            return (value != null) ? value.toString() : "";

        } catch (Exception ex) {
            Logger.getLogger(DmpUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    private static String getPreDataColumnHeadings() {
        return tabJoiner.join("DMP Sample ID", "Tumor Type","DMP Patient ID",
                "Gender");
    }
    
    private static String getPostDataColumnHeadings() {
        return tabJoiner.join("Metastasis","Metastasis Site", " Sample Coverage",
                "Signout Comments", "Signout Status", "Tumor Purity");
    }

}
