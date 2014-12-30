package org.mskcc.cbio.importer.persistence.staging.cnv;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.persistence.staging.MetadataFileHandler;

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
    protected final CnvFileHandler fileHandler;

    protected CnvTransformer(CnvFileHandler aHandler){
        Preconditions.checkArgument(null!=aHandler,"A CnvFileHandler Implementation is required");
        this.fileHandler = aHandler;
    }

    public void registerStagingFileDirectory(Path stagingDirectoryPath, boolean reuse) {
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        Path cnvPath = stagingDirectoryPath.resolve("data_CNA.txt");
        this.fileHandler.initializeFilePath(cnvPath);
        // if we wish to reuse any  existing CNV data (e.g. DMP studies)
        if (reuse) {
            this.cnaTable = this.fileHandler.initializeCnvTable();
        } else {
            this.cnaTable = HashBasedTable.create();
        }
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


    protected void processDeprecatedSamples(Set<String> deprecatedSamples){
        logger.info(deprecatedSamples.size() +" DMP samples have been deprecated");
        Set<String> geneNameSet = this.cnaTable.rowKeySet();
        for (String sampleId : deprecatedSamples){
            for (String geneName : geneNameSet) {
                this.cnaTable.put(geneName, sampleId, "0");
            }

        }
    }
     protected void persistCnvData() {
        this.fileHandler.persistCnvTable(this.cnaTable);
    }
}
