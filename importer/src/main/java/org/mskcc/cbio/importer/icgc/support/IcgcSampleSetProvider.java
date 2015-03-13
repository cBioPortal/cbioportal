package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;
import edu.stanford.nlp.io.IOUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
 * Created by criscuof on 2/10/15.
 */
public enum IcgcSampleSetProvider {

    INSTANCE;

    private static final String ICGC_WORKSHEET_NAME ="icgc";
    private static final String CLINICAL_SAMPLE_URL_COLUMN = "clinicalsampleurl";

    private static Logger logger = Logger.getLogger(IcgcSampleSetProvider.class);
    private LoadingCache<String,Set<String>> sampleSetCache = CacheBuilder.newBuilder()
            .maximumSize(100L)
            .build(new CacheLoader<String, Set<String>>() {
                @Override
                public Set<String> load(String key) throws Exception {
                    return resolveSampleSetByIcgcStudyName(key);
                }
            });

    public String getReferencedSamplesLine(String study) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(study),"An ICGC study name is required");
        StringBuilder sb = new StringBuilder(StagingCommonNames.REFERENCED_SAMPLES_COMMENT);
        return sb.append(StagingCommonNames.blankJoiner.join(this.getIcgcSamplesByStudy(study))).toString();
    }

    /*
    Represents a Single service that will provide a complete set if sample ids for a specified ICGC
    study. Returns an empty Set if the specified ICGC study name is invalid. The sample id data is retained in
    a cache for subsequent requests
     */

    public Set<String> getIcgcSamplesByStudy(String study){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(study),
                "An ICGC study name is required");
        try {
            return sampleSetCache.get(study);
        } catch (ExecutionException e) {
            logger.info(e.getMessage());
            e.printStackTrace();
        }
        return Sets.newHashSet();
    }
    /*
    private method to create a Set of sample ids for a specified ICGC study.
    An inclusive set of sample ids is obtained from extracting the first column of the study's
    clinical sample file
    This method is only invoked when the sample set is not found in the cache
     */

    private void resolveIcgcSampleSet(final String studyName){
        String url = determineClinicalSampleUrl(studyName);

    }


    private String determineClinicalSampleUrl(final String studyName) {
        Observable<String> sampleFileObservable = Observable.from(
                ImporterSpreadsheetService.INSTANCE
                        .getWorksheetValuesByColumnName(ICGC_WORKSHEET_NAME, CLINICAL_SAMPLE_URL_COLUMN)
        ).filter(new Func1<String, Boolean>() {
            @Override
            public Boolean call(String s) {
                return s.contains(studyName.toUpperCase());
            }
        }).single();

        final String[] clinicalSampleUrl = {""};
        sampleFileObservable.subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {
                logger.info("Completed");
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
            }

            @Override
            public void onNext(String s) {
                logger.info("clinical sample url: " +s);
                clinicalSampleUrl[0] = s;

            }
        });
        return clinicalSampleUrl[0];
    }

    private Set<String> resolveSampleSetByIcgcStudyName(String studyName){
        logger.info("Looking for samples in ICGC study "+studyName);
        Set<String> sampleSet = Sets.newHashSet();
        List<String> urlList =  ImporterSpreadsheetService.INSTANCE
                .getWorksheetValuesByColumnName(ICGC_WORKSHEET_NAME, CLINICAL_SAMPLE_URL_COLUMN);
        // find the studyName in the URL list
        for (String url : urlList){
            if( url.contains(studyName.toUpperCase())){
                String line ="";
                int lineCount = 0;
                try (BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(url)))){
                    while ((line = rdr.readLine()) != null) {
                        if( lineCount++ > 0) {
                            sampleSet.add(StagingCommonNames.tabSplitter.splitToList(line).get(0));
                        }
                    }
                    return sampleSet;
                } catch (IOException e){
                    logger.info(e.getMessage());
                    e.printStackTrace();
                }

            }
        }
        logger.info("Failed to find clinical sample URL for ICGC study " +studyName);
        return sampleSet;

    }

    public static void main (String...args){

        for(String sample : IcgcSampleSetProvider.INSTANCE.getIcgcSamplesByStudy("LICA-FR")) {
            logger.info(sample);
        }
        for(String sample : IcgcSampleSetProvider.INSTANCE.getIcgcSamplesByStudy("PACA-CA")) {
            logger.info(sample);
        }
        logger.info("Second reference to LICA-FR  sample size = " +IcgcSampleSetProvider.INSTANCE.getIcgcSamplesByStudy("LICA-FR").size());
        logger.info("URL = " + IcgcSampleSetProvider.INSTANCE.determineClinicalSampleUrl("LICA-FR"));
        logger.info(IcgcSampleSetProvider.INSTANCE.getReferencedSamplesLine("BRCA-UK"));
    }

}
