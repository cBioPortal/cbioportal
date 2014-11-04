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

import org.mskcc.cbio.importer.foundation.extractor.FoundationStudyExtractor;
import org.mskcc.cbio.importer.foundation.extractor.FileDataSource;
import com.google.common.base.Predicate;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import org.apache.log4j.Logger;

import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.foundation.transformer.FoundationXMLTransformer;


public class TestFoundationStudyExtractorOld {
  
    private final static Logger logger = Logger.getLogger(TestFoundationStudyExtractorOld.class);

    public static void main (String...args) {
        Config config = new MockConfig();  
         Predicate xmlFileExtensionFilter = new Predicate<Path>() {
                @Override
                public boolean apply(Path input) {
                    return (input.toString().endsWith("xml"));
                }
            };
        try {
            FoundationStudyExtractor extractor = new FoundationStudyExtractor(config);
            FileTransformer ft = new FoundationXMLTransformer(config);
            Set<Path> pathSet = extractor.extractData();
            for (Path p : pathSet ){
                logger.info("processing files in "+  p.toString());
                FileDataSource fds = new FileDataSource(p.toString(),xmlFileExtensionFilter);  
                ft.transform(fds);
            }
        } catch (IOException ex) {
           logger.error(ex.getMessage());
        }
        

    }
}
