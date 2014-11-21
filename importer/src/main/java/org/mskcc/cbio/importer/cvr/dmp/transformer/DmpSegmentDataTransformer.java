package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.model.SegmentData;
import org.mskcc.cbio.importer.cvr.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.segment.SegmentModel;
import org.mskcc.cbio.importer.persistence.staging.segment.SegmentTransformer;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * *
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
 *
 * Created by criscuof on 10/28/14.
 */
class DmpSegmentDataTransformer extends SegmentTransformer implements DMPDataTransformable {
    /*
    responsible for transforming DMP segment data from model objects to
    a staging file format and invoking their output.
     */

    private final static Logger logger = Logger.getLogger(DmpSegmentDataTransformer.class);

    public DmpSegmentDataTransformer(TsvStagingFileHandler aHandler, Path stagingDirectoryPath) {
        super(aHandler);
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        Preconditions.checkArgument(Files.isDirectory(stagingDirectoryPath, LinkOption.NOFOLLOW_LINKS),
                "The specified Path: " + stagingDirectoryPath + " is not a directory");
        Preconditions.checkArgument(Files.isWritable(stagingDirectoryPath),
                "The specified Path: " + stagingDirectoryPath + " is not writable");
        this.registerStagingFileDirectory(stagingDirectoryPath);
    }

    @Override
    public void transform(DmpData data) {
        // the deprecated samples in the legacy data must be removed before appending
        // the new samples
        Preconditions.checkArgument(null != data, "A DmpData object is required");

        Set<String> deprecatedSamples = FluentIterable.from(data.getResults())
                .filter(new Predicate<Result>() {
                    @Override
                    public boolean apply(Result result) {
                        return (result.getMetaData().getRetrieveStatus() == DMPCommonNames.DMP_DATA_STATUS_RETRIEVAL) ;
                    }
                })
                .transform(new Function<Result, String>() {

                    @Override
                    public String apply(Result result) {
                        return result.getMetaData().getDmpSampleId();
                    }
                })
                .toSet();


        logger.info(deprecatedSamples.size() + " samples have been deprecated by new DMP data");


        // remove any deprecated Samples
        if (!deprecatedSamples.isEmpty()) {
            this.fileHandler.removeDeprecatedSamplesFomTsvStagingFiles(DMPCommonNames.SEGMENT_ID_COLUMN_NAME, deprecatedSamples);

        }
        this.processSegments(data);
    }

    private void processSegments(DmpData data){
        List<SegmentModel> segmentModelList = FluentIterable.from(data.getResults())
                .transformAndConcat(new Function<Result, List<SegmentData>>() {
            @Nullable
            @Override
            public List<SegmentData> apply(@Nullable Result result) {
                return result.getSegmentData();
            }
        })
                .transform(new Function<SegmentData,SegmentModel>(){
                    @Nullable
                    @Override
                    public SegmentModel apply(@Nullable SegmentData segmentData) {
                        return new DmpSegmentModel(segmentData);
                    }
                }).toList();

        // output the list of SegmentData objects to the staging file
        this.fileHandler.transformImportDataToTsvStagingFile(segmentModelList, SegmentModel.getTransformationModel());
    }

}
