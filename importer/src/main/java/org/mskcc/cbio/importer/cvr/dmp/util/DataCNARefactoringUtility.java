package org.mskcc.cbio.importer.cvr.dmp.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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
 * Created by Fred Criscuolo on 2/25/15.
 * criscuof@mskcc.org
 */
public class DataCNARefactoringUtility {
    /*
    Utility class to replace DMP legacy sample ids in the data_CNA.txt staging file with new
    sample ids.
     */
    private static final Logger logger = Logger.getLogger(DataCNARefactoringUtility.class);
    private static final String STAGING_FILE_NAME = "data_CNA.txt";
    private static final String SAMPLE_ID_COLUMN_NAME = "Tumor_Sample_Barcode";
    private final Path legacyFilePath;
    private final Path refactoredFilePath;

    public DataCNARefactoringUtility(Path inDirPath, Path outDirPath) {
        Preconditions.checkArgument(null != inDirPath, "A Path to the source directory is required");
        Preconditions.checkArgument(null != outDirPath, "A Path to the output directory is required");
        this.legacyFilePath = inDirPath.resolve(STAGING_FILE_NAME);
        this.refactoredFilePath = outDirPath.resolve(STAGING_FILE_NAME);
    }

    void refactorRegisteredStagingFile() {
        try {
            // read in all the lines
            final List<String> lineList = Files.readAllLines(this.legacyFilePath, Charset.defaultCharset());
            final List<String> sampleList = Lists.newArrayList();
            Observable<String> sampleObservable = Observable.
                    from(StagingCommonNames.tabSplitter.splitToList(lineList.get(0)));
            sampleObservable.subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    // write out the refactored sample ids and the remaining lines
                    List<String> outputList = Lists.newArrayList(StagingCommonNames.tabJoiner.join(sampleList));
                    outputList.addAll(lineList); // add the original file's lines
                    outputList.remove(1); // remove the original sample list
                    try {
                        Files.write(refactoredFilePath, outputList, Charset.defaultCharset());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.error(throwable.getMessage());
                    throwable.printStackTrace();
                }

                @Override
                public void onNext(String legacySampleId) {
                    // process the legacy sample id
                    Optional<String> idOpt = DmpLegacyIdResolver.INSTANCE.
                            resolveNewSampleIdFromLegacySampleId(legacySampleId);
                    if (idOpt.isPresent()) {
                        sampleList.add(idOpt.get());
                    } else {
                        sampleList.add(legacySampleId);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main (String...args) {
        DataCNARefactoringUtility utility =
                new DataCNARefactoringUtility(Paths.get("/tmp/msk-impact"),Paths.get("/tmp/msk-impact/new"));
        utility.refactorRegisteredStagingFile();
    }
}
