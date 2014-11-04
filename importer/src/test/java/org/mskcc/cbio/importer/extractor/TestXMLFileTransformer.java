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

import org.mskcc.cbio.importer.foundation.extractor.FileDataSource;
import com.google.common.base.Predicate;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.log4j.Logger;

import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.foundation.transformer.FoundationXMLTransformer;

public class TestXMLFileTransformer {

    private final static Logger logger = Logger.getLogger(TestXMLFileTransformer.class);

    public static void main(String... args) {
        try {
            Config config = new MockConfig();
            Predicate xmlFileExtensionFilter = new Predicate<Path>() {
                @Override
                public boolean apply(Path input) {
                    return (input.toString().endsWith("xml"));
                }
            };
            FileDataSource fds = new FileDataSource("/data/foundation/amc_rsp/mskcc/foundation/filtered", xmlFileExtensionFilter);
            FoundationXMLTransformer transformer = new FoundationXMLTransformer(config);
            transformer.transform(fds);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
}
