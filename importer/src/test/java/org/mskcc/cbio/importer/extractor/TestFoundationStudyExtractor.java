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

package org.mskcc.cbio.importer.extractor;


import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;

import org.mskcc.cbio.importer.foundation.extractor.FoundationStudyExtractor;

import org.mskcc.cbio.importer.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TestFoundationStudyExtractor {
  
    private final static Logger logger = Logger.getLogger(TestFoundationStudyExtractor.class);

    public TestFoundationStudyExtractor(){

    }

    private void performTests() {

        String testDataSource= "foundation-dev";
        try {
            FoundationStudyExtractor extractor = new FoundationStudyExtractor(testDataSource);

            Set<Path> pathSet = extractor.extractData();
            for (Path p : pathSet ){
               logger.info("Extracted file " + p.toString());
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }


    public static void main (String...args) {

        TestFoundationStudyExtractor test = new TestFoundationStudyExtractor();
        test.performTests();
    }


}
