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
package org.mskcc.cbio.importer.cvr.dmp.transformer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.inject.internal.Iterables;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.model.CnvVariant;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.util.DMPCommonNames;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvTransformer;
import scala.Tuple3;

/*
 responsible for maintaining the CNV table for DMP data:
 invoked when new DMP CNV data are available
 creates a in-memory table of CNV values by gene (rows) and samples (columns)
 reads in any persisted CNV data into table
 updates table with new CNV data
 persists updated table to designated file
 */
public class DmpCnvTransformer extends CnvTransformer implements DMPDataTransformable {

    private final static Logger logger = Logger.getLogger(DmpCnvTransformer.class);

    public DmpCnvTransformer(CnvFileHandler aHandler,Path stagingDirectoryPath) {
        super(aHandler);
        this.registerStagingFileDirectory(stagingDirectoryPath, true);
    }

    /*
    Function to transform attributes from the Result object to a list of tuples containing the cnv gene name, the result sample id,
    and the cnv fold change
    */
    Function<Result, List<Tuple3<String, String, String>>> cnvFunction = new Function<Result, List<Tuple3<String, String, String>>>() {
        @Override
        public List<Tuple3<String, String, String>> apply(final Result result) {
            List<Tuple3<String, String,String>> list1 = Lists.newArrayList();
            list1.addAll(FluentIterable.from(result.getCnvVariants())
                    .transform(new Function<CnvVariant, Tuple3<String, String, String>>() {
                        @Override
                        public Tuple3<String, String, String> apply(CnvVariant cnv) {
                            return new Tuple3(cnv.getGeneId(), result.getMetaData().getDmpSampleId(), cnv.getGeneFoldChange().toString());
                        }
                    }).toList());

            return list1;
        }
    };

    @Override
    public void transform(DmpData data) {
        // reset deprecated samples  to 0 before inserting new data
        this.resetDeprecatedSamples(data);
        Iterable<Tuple3<String, String, String>> cnvList = Iterables.concat(Lists.transform(data.getResults(), cnvFunction));
        for (Tuple3<String, String, String> cnv : cnvList) {
            this.registerCnv(cnv._1(), cnv._2(), cnv._3().toString());
        }
        // persist results to staging file
        super.persistCnvData();
    }
    /*
    private method to reset CNA data for deprecated samples from table

    */
    private void resetDeprecatedSamples(DmpData data) {

         Set<String> deprecatedSamples = FluentIterable.from(data.getResults())
                .filter(new Predicate<Result>() {                
                    @Override
                    public boolean apply(Result result) {
                       return (result.getMetaData().getRetrieveStatus() == DMPCommonNames.DMP_DATA_STATUS_RETRIEVAL) ;
                    }
                })
                .transform(new Function<Result, String>() {
                    
                    @Override
                    public String apply(Result result) {
                        return result.getMetaData().getDmpSampleId();
                    }
                })
                .toSet();
        if (deprecatedSamples.size() >0) {
            this.processDeprecatedSamples(deprecatedSamples);
        }
    }

    // main method for stand alone testing
    public static void main(String...args){
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();
        String tempDir = "/tmp/cvr/dmp";
        File tmpDir = new File(tempDir);
        tmpDir.mkdirs();
        Path stagingFileDirectory = Paths.get(tempDir);
        DmpCnvTransformer transformer = new DmpCnvTransformer( new CnvFileHandlerImpl(),
                stagingFileDirectory);
        try {
            DmpData data = OBJECT_MAPPER.readValue(new File("/tmp/cvr/dmp/result-sv.json"), DmpData.class);
            transformer.transform(data);

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }
    
}
