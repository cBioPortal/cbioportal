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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.foundation.extractor.FileDataSource;
import org.mskcc.cbio.importer.foundation.transformer.*;
import org.mskcc.cbio.importer.model.*;
import org.mskcc.cbio.importer.persistence.staging.*;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandler;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestXMLFileTransformer {

    private final static Logger logger = Logger.getLogger(TestXMLFileTransformer.class);

    public TestXMLFileTransformer() {}

    private void performTests() {
        try {

            Predicate xmlFileExtensionFilter = new Predicate<Path>() {
                @Override
                public boolean apply(Path input) {
                    return (input.toString().endsWith("xml"));
                }
            };
            FileDataSource fds = new FileDataSource("/data/foundation", xmlFileExtensionFilter);

            TsvStagingFileHandler svtFileHandler = new MutationFileHandlerImpl();
            CnvFileHandler cnVFileHandler = new CnvFileHandlerImpl();
            ClinicalDataFileHandler clinicalDataFileHandler = new ClinicalDataFileHandlerImpl();
            TsvStagingFileHandler fusionFileHandler = new FusionFileHandlerImpl();

            FoundationXMLTransformer transformer = new FoundationXMLTransformer(
                    new FoundationShortVariantTransformer(svtFileHandler),
                    new FoundationCnvTransformer(cnVFileHandler),
                    new FoundationClinicalDataTransformer(clinicalDataFileHandler),
                    new FoundationFusionTransformer(fusionFileHandler)


            );
            transformer.transform(fds);
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    public static void main(String... args) {
        TestXMLFileTransformer test = new TestXMLFileTransformer();
        test.performTests();
    }


}
