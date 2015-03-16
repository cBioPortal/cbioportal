package org.mskcc.cbio.importer.dmp.transformer;

import com.google.common.collect.Sets;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
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
public class TestCSVReadWithComment {
    /*
    a application to test using the Apache Commoms CSV reader with a comment as the first line of the file
    this is to allow a list of DMP sample ids to precede the column headers
     */
    private static final Logger logger = Logger.getLogger(TestCSVReadWithComment.class);
    public static void main(String...args){
        File input = new File("/tmp/data_mutations_extended.txt");
        final CSVParser parser;
        try {
            BufferedReader br = new BufferedReader(new FileReader(input) );
            String comment = br.readLine();
            Set<String> sampleSet = getDmpSamples(comment);
            logger.info("There are " +sampleSet.size() + " samples");
            logger.info("The first sample is " +sampleSet.toArray()[0]);
            logger.info("The last sample is " + sampleSet.toArray()[sampleSet.size()-1]);
            logger.info(comment);
            CSVFormat f = CSVFormat.TDF.withHeader().withCommentMarker('#');
            parser = new CSVParser(new FileReader(input),
                    f);

            String headings = StagingCommonNames.tabJoiner.join(parser.getHeaderMap().keySet());


            logger.info("headings: " +headings);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static Set<String> getDmpSamples(String sampleCommentLine){
        String line = (sampleCommentLine.indexOf(':')>0 ) ?
                sampleCommentLine.substring(sampleCommentLine.indexOf(':')+2)
                :sampleCommentLine;

        return Sets.newTreeSet(StagingCommonNames.blankSplitter.splitToList(line));
    }
}
