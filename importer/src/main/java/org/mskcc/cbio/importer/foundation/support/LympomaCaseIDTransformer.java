/*
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
 */
package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;

public class LympomaCaseIDTransformer {

    /*
     Java application to remap the case identifier for Batlevi lymphoma study
     replace the case id provided by FMI with the value of the test-request attribute for the same case

     */
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LympomaCaseIDTransformer.class);
    private static final String CASE_ATTRIBUTE = "case=";
    private static final String CASE_LINE = "<Case case";
    private static final String VARIANT_REPORT_LINE = "<variant-report";
    private static final String TEST_REQUEST_ATTRIBUTE = "test-request=";


    private static OpenOption[] options = new OpenOption[]{CREATE, DSYNC};

    public static void main(String... args) {
        Path inputPath = Paths.get("/tmp/foundation/CLINICAL-HEME-COMPLETE.xml");
        Path outputPath = Paths.get("/tmp/foundation/CLINICAL-HEME-COMPLETE-NEW.xml");
        Map<String,String> caseIdMap = Maps.newHashMap();
        String currentCaseId = "XXXXXX";
        int caseCount=0;
        try {
            List<String> lines = Files.readLines(inputPath.toFile(), Charset.defaultCharset());
            for (String line1 : lines){
                String line = line1.trim();
                if (line.startsWith(CASE_LINE)) {
                    caseCount++;
                    int startPos = line.indexOf(CASE_ATTRIBUTE)+CASE_ATTRIBUTE.length()+1;
                    logger.info(line);
                    logger.info("start " +startPos);
                    if (startPos > 0) {
                        int stopPos = line.substring(startPos).indexOf('"') +startPos;
                        logger.info("stop " +stopPos);
                        currentCaseId = line.substring(startPos,stopPos);
                        logger.info("start " +startPos +" stop " +stopPos + " case " +currentCaseId);
                        logger.info("Case count = " +caseCount);
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }



             //  java.nio.file.Files.write(outPath, newList, Charset.defaultCharset(),
             //       options);


        

    }

}
