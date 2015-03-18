package org.mskcc.cbio.importer.dmp.transformer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

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
 * Created by criscuof on 2/3/15.
 */
public class TestCSVRead {
    /*
    a application to test using the Apache Commoms CSV reader with a comment as the first line of the file
    this is to allow a list of DMP sample ids to precede the column headers
     */
    private static final Logger logger = Logger.getLogger(TestCSVRead.class);
    public static void main(String...args){
        File input = new File("/tmp/data_expression_miRNA.txt");
        final CSVParser parser;
        try {
            BufferedReader br = new BufferedReader(new FileReader(input) );

            CSVFormat f = CSVFormat.TDF.withHeader();
            parser = new CSVParser(new FileReader(input),
                    f);
            Observable<CSVRecord> recordObservable = Observable.from(parser.getRecords());
            recordObservable.subscribe(new Subscriber<CSVRecord>(){

                                           @Override
                                           public void onCompleted() {
                                               logger.info("FINIS");
                                           }

                                           @Override
                                           public void onError(Throwable throwable) {
                                                logger.error(throwable.getMessage());
                                           }

                                           @Override
                                           public void onNext(CSVRecord record) {
                                                for (String s : Lists.newArrayList(record.iterator())){
                                                    logger.info(s);
                                                }
                                           }
                                       });

            String headings = StagingCommonNames.tabJoiner.join(parser.getHeaderMap().keySet());


            logger.info("headings: " +headings);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
