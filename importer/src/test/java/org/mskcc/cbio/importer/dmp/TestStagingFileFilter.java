package org.mskcc.cbio.importer.dmp;

import org.apache.log4j.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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
 * Created by Fred Criscuolo on 3/10/15.
 * criscuof@mskcc.org
 */
public class TestStagingFileFilter {

    private static final Logger logger = Logger.getLogger(TestStagingFileFilter.class);
    private static class RefactorFileFilter implements FilenameFilter
    { public boolean accept(File dir, String s) {
        if (s.endsWith(".txt") || s.endsWith(".seg") ) {
            return true; }

        return false; }
    }
    public static void main (String... args) {
        Path stagingFilePath = Paths.get("/Users/criscuof/cbio-portal-data/msk-impact/msk-impact");
        List<File> stagingFileList = Arrays.asList(stagingFilePath.toFile()
                .listFiles(new RefactorFileFilter()));
        Observable<File> fileObservable = Observable.from(stagingFileList)
                // filter out metadata files
                .filter(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        return !file.getName().contains("meta");
                    }
                });
        fileObservable.subscribe(new Subscriber<File>() {
            @Override
            public void onCompleted() {
                logger.info("All staging files processed");
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
                throwable.printStackTrace();
            }

            @Override
            public void onNext(File file) {
                logger.info("Processing staging file " + file.getName());

            }
        });

    }
}
