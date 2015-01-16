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

package org.mskcc.cbio.importer.model;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gdata.util.common.base.Preconditions;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/*
Represents metadata associated with ICGC Cancer studies
Mapped from ICGC panel on Google spreadsheet
*/

public class IcgcMetadata {

    public static final String worksheetName = MetadataCommonNames.Worksheet_ICGC;
    public static final String  keyColumnName = "icgcid";
    public static final List<String> urlColumnNames = Lists.newArrayList("clinicalurl", "copynumberurl",
            "exparrayurl", "expsequrl" , "metharrayurl","methsequrl","mirnasequrl", "mirnasequrl", "somaticmutationurl",
           "splicevarianturl", "structuralmutationurl" );



    private static final Logger logger = Logger.getLogger(IcgcMetadata.class);

    private  String icgcid;
    private  String downloaddirectory;
    private  String tumortype;
    private  String description;
    private  String shortname;
    private String studyname;
    private String clinicalurl;
    private String copynumberurl;
    private String exparrayurl;
    private String expsequrl;
    private String metharrayurl;
    private String methsequrl;
    private String mirnasequrl;
    private String somaticmutationurl;
    private String splicevarianturl;
    private String structuralmutationurl;


    
    public IcgcMetadata(String[] properties){
        Preconditions.checkArgument(null != properties && properties.length >2,
                "The properties array is null or invalid");
        this.icgcid = properties[0];
        this.downloaddirectory = properties[1];
        this.tumortype = properties[2];
        this.description = (properties.length >3)?properties[3]:"";
        this.shortname = (properties.length >4)? properties[4]:"";
        this.studyname = (properties.length >5)? properties[5]:"";
    }

    /*
    constructor based on row from google worksheet
     */
    public IcgcMetadata(Map<String,String> worksheetRowMap){
        try {
            BeanUtils.populate(this, worksheetRowMap);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,String> resolveIcgcUrlsByType(String studyType) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(studyType),"An ICGC Study type is required");
        Preconditions.checkArgument(urlColumnNames.contains(studyType),"Study type " +studyType +" is invalid");
        Map<String,String> urlMap = Maps.newHashMap();
        List<String> urlList =  ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName(worksheetName,studyType);
        for (String url : urlList){
            if(!Strings.isNullOrEmpty(url)){
                IcgcMetadata meta =  new IcgcMetadata(ImporterSpreadsheetService.INSTANCE.getWorksheetRowByColumnValue(worksheetName, studyType, url).get());
                urlMap.put(meta.getIcgcid(), url);
            }
        }
        return urlMap;


    }

  // collection of static service methods
    public static Optional<IcgcMetadata> getIcgcMetadataById(String icgcId) {
        if (Strings.isNullOrEmpty(icgcId)){ return Optional.absent(); }
            return Optional.of(new IcgcMetadata(ImporterSpreadsheetService.INSTANCE.
                    getWorksheetRowByColumnValue(worksheetName, keyColumnName, icgcId).get()));
    }

    public static Optional<String> getCancerStudyPathByStudyId(String studyId ){
        if (Strings.isNullOrEmpty(studyId) ) { return Optional.absent(); }
        IcgcMetadata meta = new IcgcMetadata(ImporterSpreadsheetService.INSTANCE.
                getWorksheetRowByColumnValue(worksheetName, keyColumnName, studyId).get());

        if ( null != meta){
            Optional<CancerStudyMetadata> opt = CancerStudyMetadata.findCancerStudyMetaDataByStableId(meta.getStudyname());
            if (opt.isPresent()){
                return Optional.of(opt.get().getStudyPath());
            }
        }
        logger.info("Unable to find a study path for ICGC study " + studyId);
        return Optional.absent();
    }

    public static List<String> getRegisteredIcgcStudyList() {
        return ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName(worksheetName,keyColumnName);
    }

    /*
    public method to generate a Map of all ICGC metadata entries from the importer spreadsheet
     keyed by the ICGC ID attribute
     */
    public static List<IcgcMetadata> getIcgcMetadataList() {

        return FluentIterable.from(ImporterSpreadsheetService.INSTANCE.getWorksheetValuesByColumnName(worksheetName, keyColumnName))
                .filter(new Predicate<String>() {
                    @Override
                    public boolean apply(@Nullable String input) {
                        return ImporterSpreadsheetService.INSTANCE.getWorksheetRowByColumnValue(worksheetName, keyColumnName, input).isPresent();
                    }
                })
                    .transform(new Function<String, IcgcMetadata>() {
                                   @Nullable
                                   @Override
                                   public IcgcMetadata apply(String studyId) {

                                       return getIcgcMetadataById(studyId).get();
                                   }}

                    ).toList();
                }


    public String getIcgcid() {
        return icgcid;
    }

    public String getDownloaddirectory() {
        return downloaddirectory;
    }

    public String getTumortype() {
        return tumortype;
    }

    public String getDescription() {
        return description;
    }

    public String getShortname() {
        return shortname;
    }

    public void setIcgcid(String icgcid) {
        this.icgcid = icgcid;
    }

    public void setDownloaddirectory(String downloaddirectory) {
        this.downloaddirectory = downloaddirectory;
    }

    public void setTumortype(String tumortype) {
        this.tumortype = tumortype;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getStudyname() {
        return studyname;
    }

    public void setStudyname(String studyname) {
        this.studyname = studyname;
    }

    public String getClinicalurl() {
        return clinicalurl;
    }

    public void setClinicalurl(String clinicalurl) {
        this.clinicalurl = clinicalurl;
    }

    public String getCopynumberurl() {
        return copynumberurl;
    }

    public void setCopynumberurl(String copynumberurl) {
        this.copynumberurl = copynumberurl;
    }

    public String getExparrayurl() {
        return exparrayurl;
    }

    public void setExparrayurl(String exparrayurl) {
        this.exparrayurl = exparrayurl;
    }

    public String getExpsequrl() {
        return expsequrl;
    }

    public void setExpsequrl(String expsequrl) {
        this.expsequrl = expsequrl;
    }

    public String getMetharrayurl() {
        return metharrayurl;
    }

    public void setMetharrayurl(String metharrayurl) {
        this.metharrayurl = metharrayurl;
    }

    public String getMethsequrl() {
        return methsequrl;
    }

    public void setMethsequrl(String methsequrl) {
        this.methsequrl = methsequrl;
    }

    public String getSomaticmutationurl() {
        return somaticmutationurl;
    }

    public void setSomaticmutationurl(String somaticmutationurl) {
        this.somaticmutationurl = somaticmutationurl;
    }

    public String getSplicevarianturl() {
        return splicevarianturl;
    }

    public void setSplicevarianturl(String splicevarianturl) {
        this.splicevarianturl = splicevarianturl;
    }

    public String getStructuralmutationurl() {
        return structuralmutationurl;
    }

    public void setStructuralmutationurl(String structuralmutationurl) {
        this.structuralmutationurl = structuralmutationurl;
    }

    public String getMirnasequrl() {
        return mirnasequrl;
    }

    public void setMirnasequrl(String mirnasequrl) {
        this.mirnasequrl = mirnasequrl;
    }

    public static void main (String...args){
        // test getIcgcMetadataById method
        IcgcMetadata meta1 = IcgcMetadata.getIcgcMetadataById("BLCA-CN").get();
        logger.info("download directory = " +meta1.getDownloaddirectory());
        // test getCancerStudyPathByStudyId method
        String cs = IcgcMetadata.getCancerStudyPathByStudyId("BRCA-UK").get();
        logger.info("Cancer study = " +cs);
        // test List<String> getRegisteredIcgcStudyList() method
        for (String s : IcgcMetadata.getRegisteredIcgcStudyList()) {
            logger.info("registered study " +s);
        }
        // test getRegisteredIcgcStudyList method
        List<IcgcMetadata> metaList  = IcgcMetadata.getIcgcMetadataList();
        logger.info ("There are " +metaList.size() +" icgc metadata entries");
        // test urlMap
        Map<String,String> urlMap = IcgcMetadata.resolveIcgcUrlsByType("somaticmutationurl");
        for(Map.Entry<String,String> entry : urlMap.entrySet()){
            logger.info("URL map " +entry.getKey() +" " +entry.getValue());
        }

    }
}
