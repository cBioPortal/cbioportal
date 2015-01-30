package org.mskcc.cbio.importer.persistence.staging.segment;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.persistence.staging.MetadataFileHandler;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;

import java.nio.file.Path;
import java.util.Map;

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
 * Created by fcriscuo on 11/15/14.
 */
public class SegmentTransformer  {

    /*
     protected TsvFileHandler tsvFileHandler;

    public MutationTransformer(Path aPath, Boolean deleteFlag){
        Preconditions.checkArgument(null != aPath,"A Path to a staging file is required");
        if (deleteFlag) {
            this.tsvFileHandler = FileHandlerService.INSTANCE.obtainFileHandlerForNewStagingFile(aPath,
                    MutationModel.resolveColumnNames());
        } else {
            this.tsvFileHandler = FileHandlerService.INSTANCE
                    .obtainFileHandlerForAppendingToStagingFile(aPath, MutationModel.resolveColumnNames());
        }
    }
     */

    protected  TsvStagingFileHandler fileHandler;
    protected TsvFileHandler tsvFileHandler;
    //TODO: get from data sources metadata
    private static final String SEGMENT_DATA_TYPE = "cna-hg19-seg";
    private static final String CANCER_STUDY_TEMPLATE = "<CANCER_STUDY>";

    private static final String segmentFileBaseName = "_data_cna_hg19.seg";
    private static final String segmentMetaFileBaseName = "_meta_cna_hg19_seg.txt";

    private static final DatatypeMetadata dtMeta = DatatypeMetadata.findDatatypeMetadatByDataType(SEGMENT_DATA_TYPE).get();

    /*
    new constructor using FileHandlerService
     */

    protected SegmentTransformer (Path aPath, Boolean deleteFlag, CancerStudyMetadata csMeta){
        Path segmentDataFilePath = resolveSegmentFilePath(csMeta, aPath);
        if (deleteFlag) {
            this.tsvFileHandler = FileHandlerService.INSTANCE.obtainFileHandlerForNewStagingFile(segmentDataFilePath,
                    SegmentModel.resolveColumnNames());
        } else {
            this.tsvFileHandler = FileHandlerService.INSTANCE
                    .obtainFileHandlerForAppendingToStagingFile(segmentDataFilePath, SegmentModel.resolveColumnNames());
        }
    }

    protected SegmentTransformer(TsvStagingFileHandler aHandler) {
        Preconditions.checkArgument(aHandler != null,
                "A TsvStagingFileHandler implementation is required");
        this.fileHandler = aHandler;
    }

    protected void registerStagingFileDirectory( CancerStudyMetadata csMetadata, Path stagingDirectoryPath){
        Preconditions.checkArgument(null != csMetadata, "A CancerStudyMetadata object is required");
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        this.fileHandler.registerTsvStagingFile(this.resolveSegmentFilePath(csMetadata,stagingDirectoryPath),
                SegmentModel.resolveColumnNames(), true);
        this.generateMetadataFile(csMetadata,stagingDirectoryPath);

    }

   // protected void registerStagingFileDirectory( Path stagingDirectoryPath){
     //   Preconditions.checkArgument(null != stagingDirectoryPath,
     //           "A Path to the staging file directory is required");
     //   this.fileHandler.registerTsvStagingFile(this.resolveSegmentFilePath(csMetadata,stagingDirectoryPath), SegmentModel.resolveColumnNames(), true);
//
   // }

    protected Path resolveSegmentFilePath(CancerStudyMetadata csMetadata, Path basePath){
        String filename = dtMeta.getStagingFilename().replace(CANCER_STUDY_TEMPLATE,csMetadata.getStableId());
        if (basePath.toString().contains("mixed")) {
            int start = basePath.toString().indexOf("mixed");
            String rootname = basePath.toString().substring(start);
            filename = rootname.replaceAll("/", "_") + filename;
        }
        return basePath.resolve(filename);
    }


    protected void registerStagingFileDirectory( Path stagingDirectoryPath,String studyName){
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        this.fileHandler.registerTsvStagingFile(this.resolveSegmentFilePath(stagingDirectoryPath,studyName),
                SegmentModel.resolveColumnNames(), true);
    }

    private void generateMetadataFile(CancerStudyMetadata csMetadata, Path stagingDirectoryPath){
        String filename = csMetadata.getStableId() +segmentFileBaseName;
        Path metadataPath = stagingDirectoryPath.resolve(filename);
        MetadataFileHandler.INSTANCE.generateMetadataFile(this.generateMetadataMap(csMetadata),
                metadataPath);
    }


    protected Map<String,String> generateMetadataMap(CancerStudyMetadata meta){
        Map<String,String> metaMap = Maps.newTreeMap();
        metaMap.put("001cancer_study_identifier:", meta.getStableId());
        metaMap.put("002reference_genome_id:","hg19");
        metaMap.put("003description:",meta.getDescription());
        metaMap.put("004data_filename:","mskimpact_data_cna_hg19.seg");
        return metaMap;
    }

    protected Path resolveSegmentFilePath(Path basePath,String studyName){
        String filename;
        if (studyName.contains("mixed")) {
            int start = basePath.toString().indexOf("mixed");
            filename = basePath.toString().substring(start).replaceAll("/", "_") + segmentFileBaseName;;
        } else {
            filename = studyName + segmentFileBaseName;
        }
        return basePath.resolve(filename);
    }

}
