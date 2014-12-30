package org.mskcc.cbio.importer.persistence.staging.segment;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.internal.Preconditions;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileProcessor;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Created by criscuof on 10/28/14.
 */
public class SegmentFileHandlerImpl extends TsvStagingFileProcessor implements SegmentFileHandler{
    private final static Logger logger = Logger.getLogger(SegmentFileHandlerImpl.class);
    private static final List<String> columnHeadings = Lists.newArrayList("ID", "chrom","loc.start", "loc.end", "num.mark", "seg.means");


    public SegmentFileHandlerImpl() {}

    /*
   Implementation of interface method to associate a staging file with segment data
   and to initialize the file if it is new
     */

    @Override
    public void registerSegmentStagingFile(Path segmentFilePath) {
        Preconditions.checkArgument(null != segmentFilePath,
                "A Path object is required to write out the segment data staging file");
        super.registerStagingFile(segmentFilePath, columnHeadings,true);
    }



    @Override
    public void removeDeprecatedSamplesFromSegmentStagingFiles(String sampleIdColumnName, Set<String> deprecatedSampleSet) {
        super.removeDeprecatedSamplesFomTsvStagingFiles(sampleIdColumnName, deprecatedSampleSet);
    }

    @Override
    public void transformImportDataToStagingFile(List aList, Function transformationFunction) {
        super.transformImportDataToStagingFile(aList, transformationFunction);

    }

    @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings) {

    }

    @Override
    public void registerTsvStagingFile(Path stagingFilePath, List<String> columnHeadings, boolean deleteFile) {

    }

    @Override
    public void appendDataToTsvStagingFile(List<String> mafData) {

    }

    @Override
    public void transformImportDataToTsvStagingFile(List aList, Function transformationFunction) {

    }
}
