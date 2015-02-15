package org.mskcc.cbio.importer.persistence.staging.segment;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.mskcc.cbio.importer.icgc.model.IcgcSegmentModel;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DatatypeMetadata;
import org.mskcc.cbio.importer.persistence.staging.MetadataFileHandler;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.filehandler.FileHandlerService;
import org.mskcc.cbio.importer.persistence.staging.filehandler.TsvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;

import java.nio.file.Path;
import java.util.*;

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

    /*
    method that completes a chromosome structural variation map for a sample
     */
    protected List<SegmentModel> resolveChromosomeMap(String sampleId, String chr, Collection<SegmentModel> chrModels) {
        // logger.info("Processing sample " +sampleId +" chromosome " +chr);
        // sort the models by their start position
        SortedSet<SegmentModel> sortedModelSet = FluentIterable.from(chrModels)
                .toSortedSet(new CopyNumberModelStartPositionComparator());
        Integer currentStop = 1;
        Integer currentStart = 1;
        Long maxStop = StagingCommonNames.chromosomeLengthMap.get(chr);
        int mapSize = chrModels.size();
        List<SegmentModel> modelList = Lists.newArrayList();
        //int entryCount = 1;
        for (SegmentModel model : sortedModelSet) {
            Integer start = Integer.valueOf(model.getLocStart());
            if (start > (currentStop + 1)) {
                // generate default copy number segment to fill in the gap
                modelList.add(this.createDefaultCopyNumberModel(sampleId, chr, currentStop.toString(), start.toString()));
                currentStart = currentStop + 1;
                currentStop = start - 1;
            }
            // process the current model object
            modelList.add(model);
            currentStart = Integer.valueOf(model.getLocStart());
            currentStop = Integer.valueOf(model.getLocStart());
        }
        // fill in the gap to the end of the chromosome
        if (currentStop < maxStop) {
            modelList.add(this.createDefaultCopyNumberModel(sampleId, chr, currentStop.toString(), maxStop.toString()));
        }
        return modelList;
    }

    public  SegmentModel createDefaultCopyNumberModel(String sampleId, String chr, String start, String stop) {
        return new DefaultSegmentModel(sampleId, chr, start, stop);
    }


    /*
    used to support gaps in chromosomes where there are no structural variations
     */
    public class DefaultSegmentModel extends SegmentModel {
        final String id;
        final String chromosome;
        final String locStart;
        final String locEnd;
        final String numMark;
        final String segMean;

        DefaultSegmentModel(String sampleId, String chr, String start, String stop){
            this.id = sampleId;
            this.chromosome = chr;
            this.locStart = start;
            this.locEnd = stop;
            this.numMark = "0";
            this.segMean = "0.0";
        }

        @Override
        public String getID() {
            return this.id;
        }

        @Override
        public String getChromosome() {
            return this.chromosome;
        }

        @Override
        public String getLocStart() {
            return this.locStart;
        }

        @Override
        public String getLocEnd() {
            return this.locEnd;
        }

        @Override
        public String getNumMark() {
            return this.numMark;
        }

        @Override
        public String getSegMean() {
            return this.segMean;
        }
    }


    /*
   Comparator implementation to support sorting data by start position
    */
    private class CopyNumberModelStartPositionComparator implements Comparator<SegmentModel> {
        @Override
        public int compare(SegmentModel o1, SegmentModel o2) {
            Integer start1 = Integer.valueOf(o1.getLocStart());
            Integer start2 = Integer.valueOf(o2.getLocStart());
            return start1.compareTo(start2);
        }
    }


}
