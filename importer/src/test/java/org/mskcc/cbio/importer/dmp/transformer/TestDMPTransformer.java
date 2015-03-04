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
package org.mskcc.cbio.importer.dmp.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.log4j.Logger;

import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.transformer.DMPDataTransformer;

public class TestDMPTransformer {

    private final static Logger logger = Logger.getLogger(TestDMPTransformer.class); 

    private final DMPDataTransformer transformer;

    public TestDMPTransformer() {
      
        this.transformer = new DMPDataTransformer(Paths.get("/tmp/dmp"));
    }

    private void testTransformations(DmpData data) {
        this.transformer.transform(data);
    }

    public static void main(String... args) {
        ObjectMapper OBJECT_MAPPER = new ObjectMapper(); 

        try {
            DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/result.json"), DmpData.class);
            TestDMPTransformer test = new TestDMPTransformer();
            test.testTransformations(data);

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

}
