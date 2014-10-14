/**
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

package org.mskcc.cbio.importer.fetcher.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.mskcc.cbio.importer.dmp.importer.DMPclinicaldataimporter;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.mskcc.cbio.importer.dmp.model.DmpData;
import org.mskcc.cbio.importer.dmp.support.DMPStagingFileManager;
import org.mskcc.cbio.importer.dmp.transformer.CnvVariantDataTransformer;
import org.mskcc.cbio.importer.dmp.transformer.DMPDataTransformer;
import org.mskcc.cbio.importer.dmp.transformer.DMPTransformable;
import org.mskcc.cbio.importer.dmp.transformer.SnpExonicDataTransformer;
import org.mskcc.cbio.importer.dmp.transformer.SnpSilentDataTransformer;

public class DMPFetcherImpl {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final DMPStagingFileManager fileManager = new DMPStagingFileManager(Paths.get("/tmp/dmp/"));
            
    public static void main(String args[])
            throws IOException {
        
        //Register transformers
        List<DMPTransformable> transformableList = Lists.newArrayList();
        transformableList.add(new CnvVariantDataTransformer());
        transformableList.add(new SnpExonicDataTransformer());
        transformableList.add(new SnpSilentDataTransformer());
        DMPDataTransformer transformer = new DMPDataTransformer(fileManager, transformableList);
        
        //Retrieve process
        DMPclinicaldataimporter dmpImporter_retrieve = new DMPclinicaldataimporter();
        DmpData data = OBJECT_MAPPER.readValue(dmpImporter_retrieve.getResult(), DmpData.class);
        transformer.transform(data);
        
        //Marking/call back process
        ArrayList<String> _consumedSampleIds = new ArrayList<>();
        DMPclinicaldataimporter dmpImporter_mark = new DMPclinicaldataimporter(_consumedSampleIds);
    
    }

}
