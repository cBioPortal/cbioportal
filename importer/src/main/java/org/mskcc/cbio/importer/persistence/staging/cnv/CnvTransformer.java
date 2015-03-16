package org.mskcc.cbio.importer.persistence.staging.cnv;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.persistence.staging.MetadataFileHandler;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;
import scala.Tuple3;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
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
 * Created by fcriscuo on 11/9/14.
 */
public abstract class CnvTransformer {

    protected Table<String, String, String> cnaTable;
    private final static Logger logger = Logger.getLogger(CnvTransformer.class);
    protected  CnvFileHandler fileHandler;
    private static final String cnaFileName = "data_CNA.txt";

    private TsvFileHandler tsvHandler;

    protected static final String COPY_NEUTRAL = "0";
    protected static final String HEMIZY_LOSS = "-1";
    protected static final String HOMOZY_LOSS = "-2";
    protected static final String AMP_LOH = "+1";
    protected static final String AMP = "+2";

    public static  String resolveCopyNumberVariation(String copyNumber) {
        if(Strings.isNullOrEmpty(copyNumber)) { return COPY_NEUTRAL;}
        switch (copyNumber ) {
            case "0":
            case "0.0":
                return HOMOZY_LOSS;
            case "1":
            case "1.0":
                return HEMIZY_LOSS;
            case "2":
            case "2.0":
                return COPY_NEUTRAL;
            case "3":
            case "3.0":
                return AMP_LOH;
        }
        return AMP;
    }

    protected CnvTransformer( Path stagingFileDirectory, Boolean deleteFile){
        Preconditions.checkArgument(null != stagingFileDirectory,
                "A Path to a staging file directory is required");
        this.tsvHandler = FileHandlerService.INSTANCE.obtainFileHandlerForCnvFile(stagingFileDirectory,deleteFile);
        this.cnaTable = this.tsvHandler.initializeCnvTable();
    }
    protected CnvTransformer( TsvFileHandler aHandler){
        Preconditions.checkArgument(null != aHandler,
                "A TsvFileHandler implementation is required");
        this.tsvHandler = aHandler;
        this.cnaTable = this.tsvHandler.initializeCnvTable();
    }

    protected CnvTransformer(CnvFileHandler aHandler){
        Preconditions.checkArgument(null!=aHandler,"A CnvFileHandler Implementation is required");
        this.fileHandler = aHandler;
    }

    public void registerStagingFileDirectory(Path stagingDirectoryPath, boolean reuse) {
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        Path cnvPath = stagingDirectoryPath.resolve(cnaFileName);
        this.fileHandler.initializeFilePath(cnvPath);
        // if we wish to reuse any  existing CNV data (e.g. DMP studies)
        if (reuse) {
            this.cnaTable = this.fileHandler.initializeCnvTable();
        } else {
            this.cnaTable = HashBasedTable.create();
        }
    }

    public void registerStagingFileDirectory(CancerStudyMetadata csMetadata, Path stagingDirectoryPath, boolean reuse){
        Preconditions.checkArgument(null != csMetadata," A CancerStudyMetadata object is required");
        this.registerStagingFileDirectory(stagingDirectoryPath,reuse);
        this.generateMetadataFile(csMetadata,stagingDirectoryPath);
    }

    public void registerStagingFileDirectory(CancerStudyMetadata csMetadata, Path stagingDirectoryPath){
        Preconditions.checkArgument(null != csMetadata," A CancerStudyMetadata object is required");
        this.registerStagingFileDirectory(stagingDirectoryPath,false);
        this.generateMetadataFile(csMetadata,stagingDirectoryPath);
    }

    protected void generateMetadataFile(CancerStudyMetadata csMetadata, Path stagingDirectoryPath){
        Path metadataPath = stagingDirectoryPath.resolve("meta_CNA.txt");
        MetadataFileHandler.INSTANCE.generateMetadataFile(this.generateMetadataMap(csMetadata),
                metadataPath);
    }

    protected Map<String,String> generateMetadataMap(CancerStudyMetadata meta){
        String values =" Values: -2 = homozygous deletion; 2 = high level amplification.";
        Map<String,String> metaMap = Maps.newTreeMap();
        metaMap.put("001cancer_study_identifier:", meta.getStableId());
        metaMap.put("002genetic_alteration_type:","COPY_NUMBER_ALTERATION");
        metaMap.put("003stable_id:",meta.getStableId()+"_cna");
        metaMap.put("004show_profile_in_analysis_tab:","true");
        metaMap.put("005profile_description:",meta.getDescription()+values);
        metaMap.put("006profile_name:",meta.getName());
        return metaMap;
    }

    protected void registerCnv(String geneName, String sampleId, String cnv){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(geneName),"A gene name is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(sampleId),"A sample id is required");
       String cnvValue = (!Strings.isNullOrEmpty(cnv) )?cnv:"0";
        this.cnaTable.put(geneName, sampleId,cnvValue);
    }

    protected void registerCnv(Tuple3<String,String,String> name_sample_cnv){
        Preconditions.checkArgument(null !=name_sample_cnv);
        this.registerCnv(name_sample_cnv._1(),
                name_sample_cnv._2(), name_sample_cnv._3());
    }

    protected void processDeprecatedSamples(Set<String> deprecatedSamples){
        logger.info(deprecatedSamples.size() +" samples have been deprecated");
        Set<String> geneNameSet = this.cnaTable.rowKeySet();
        for (String sampleId : deprecatedSamples){
            for (String geneName : geneNameSet) {
                this.cnaTable.put(geneName, sampleId, "0");
            }

        }
    }

    /*
    method to ensure that all know samples in a study are included in the cnv table
    this is to ensure that samples without copy number variants are represented.
    this method should be invoked prior to persisting the cnv table to disk
     */
    protected void completeTableSampleSet(Set <String>currentSampleSet){

        Preconditions.checkArgument(!currentSampleSet.isEmpty(),
                "A current sample set is required ");
        Set<String> tableSampleSet = this.cnaTable.columnKeySet();
        Sets.SetView<String> missingSampleSet = Sets.difference(currentSampleSet,tableSampleSet);
        if(missingSampleSet.size() > 0 ) {
            logger.info("There are " + missingSampleSet.size() + " samples without CNVs that must be added to the table");
            Set<String> geneSet = this.cnaTable.rowKeySet();
            for (String gene : geneSet) {
                for (String sample : missingSampleSet) {
                    this.cnaTable.put(gene, sample, "0");
                }
            }
        } else {
            logger.info("There are no samples without CNVs.");
        }

    }

     protected void outputCnvData () {
         this.tsvHandler.persistCnvTable(this.cnaTable);
     }
     protected void persistCnvData() {
        this.fileHandler.persistCnvTable(this.cnaTable);
    }
}
