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
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import edu.stanford.nlp.util.CollectionUtils;
import org.apache.log4j.Logger;
import org.hibernate.type.descriptor.java.DateTypeDescriptor;
import org.mskcc.cbio.importer.cvr.darwin.util.IdMapService;
import org.mskcc.cbio.importer.cvr.dmp.importer.DMPclinicaldataimporter;
import org.mskcc.cbio.importer.cvr.dmp.model.DmpData;
import org.mskcc.cbio.importer.cvr.dmp.model.Result;
import org.mskcc.cbio.importer.cvr.dmp.persistence.file.DMPTumorTypeSampleMapManager;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.model.DataSourcesMetadata;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import scala.Tuple2;

/*
 Responsible for transforming the DMP data encapsulated in the DmpData object
 graph into a set of MAF files
 Inputs: 1. The DMP data as a Java object graph

 */
public class DMPDataTransformer {

    private final static Logger logger = Logger.getLogger(DMPDataTransformer.class);
    
    private  List<DMPDataTransformable> transformableList;
    private  DMPTumorTypeSampleMapManager tumorTypeMap;
    private  Path stagingDirectoryPath;
    private static final String DATA_SOURCE_NAME = "dmp-clinical-data-darwin";
    private static final String STABLE_ID = "mskimpact-new";
    private static final Path DEFAULT_BASE_PATH = Paths.get("/tmp/dmp-staging");

/*
temporarily retain this constructor to support legacy client code and testing
 */
 public DMPDataTransformer(Path aPath) {
            if(StagingUtils.isValidStagingDirectoryPath(aPath)) {
                this.stagingDirectoryPath = this.resolveStagingPath(aPath);
                logger.info("Staging Path = " + this.stagingDirectoryPath.toString());
                registerTransformables();
            }
    }
    /*
    preferred constructor
     */
    public DMPDataTransformer() {
        this.stagingDirectoryPath = this.resolveStagingPath(this.resolveBasePath());
        logger.info("Staging Path = " + this.stagingDirectoryPath.toString());
        registerTransformables();

    }

    /*
    resolves the base staging directory from the data sources worksheet
     */
    private Path resolveBasePath() {
        Optional<DataSourcesMetadata> optMeta = DataSourcesMetadata.findDataSourcesMetadataByDataSourceName(DATA_SOURCE_NAME);
        if(optMeta.isPresent()){
            return optMeta.get().resolveBaseStagingDirectory();
        }
        return DEFAULT_BASE_PATH;
    }

    /*
    resolves the specific staging directory based  on the established base directory and the
    cancer study name
     */
    private Path resolveStagingPath (Path basePath) {
        Optional<CancerStudyMetadata> optMeta = CancerStudyMetadata.findCancerStudyMetaDataByStableId(STABLE_ID);
        if (optMeta.isPresent()){
            return basePath.resolve(optMeta.get().getStudyPath());
        }
        return basePath; // default -files go into base path
    }

    private void registerTransformables() {
        // instantiate and register data transformers
        //SNPs
        //this.transformableList = Lists.newArrayList((DMPDataTransformable)
         //       new DmpSnpTransformer(new MutationFileHandlerImpl(),
         //               stagingDirectoryPath));
        this.transformableList = Lists.newArrayList((DMPDataTransformable)
                new DmpSnpTransformer(
                        stagingDirectoryPath));


        //CNVs
        this.transformableList.add((DMPDataTransformable)
                new DmpCnvTransformer( new CnvFileHandlerImpl(),
                        stagingDirectoryPath));
        //Metadata
        //this.transformableList.add((DMPDataTransformable) new DmpMetadataTransformer
         //   ( new ClinicalDataFileHandlerImpl(), stagingDirectoryPath));

        //IMPACT Clinical Data
        // replacement for Metadata transformer
        this.transformableList.add((DMPDataTransformable)
                new DmpImpactClinicalDataTransformer(stagingDirectoryPath));

        //Structural Variants
        this.transformableList.add((DMPDataTransformable) new DmpFusionTransformer
                (new MutationFileHandlerImpl(),stagingDirectoryPath) );
        // segment data
         this.transformableList.add( (DMPDataTransformable) new DmpSegmentDataTransformer
                 ( new MutationFileHandlerImpl() , stagingDirectoryPath));
        // this.tumorTypeMap = new DMPTumorTypeSampleMapManager(this.fileManager);
    }

    /*
     transform the DMP data into variant type-specific MAF files
     return a Set of processed SMP sample ids
     */

    public List<String> transform(DmpData data) {
        Preconditions.checkArgument(null != data, "DMP data is required for transformation");
        //++++++++DARWIN DATABASE FILTER +++++++++++++++++++++
        //25Jan2015 - filter turned off until access to production Darwin database is resolved
        //filter out DMP samples that have not been registered in Darwin
        //this.filterDmpSamples(data);
        //it's possible that none of the new DMP samples were found in Darwin
        // if so return an empty List to caller
        if (data.getResults().isEmpty() ) {
            logger.info("No suitable DMP samples were found in the the input data");
            return Lists.newArrayList();
        }
        // process the tumor types
        //this.tumorTypeMap.updateTumorTypeSampleMap(data.getResults());
        // invoke the type specific transformers on the DMP data
        for (DMPDataTransformable transformable : this.transformableList) {
            transformable.transform(data);
        }

        return FluentIterable.from(data.getResults())
        .transform(new com.google.common.base.Function<Result, String>() {
            @Override
            public String apply(Result result) {
                return result.getMetaData().getDmpSampleId();
            }
        }).toList();
    }

    /*
    private method to filter out DMP samples that have not yet been registered in Darwin
    these samples will not be marked as consumed and will continue to be presented until
    they have been registered in Darwin
     */
    private void filterDmpSamples (DmpData data){
        logger.info("Original sample count = " +data.getResults().size());
       List<Result> newResultList = FluentIterable.from(data.getResults())
               .filter(new Predicate<Result>() {
                   @Override
                   public boolean apply(Result input) {
                       Tuple2<String,String> idTuple = new Tuple2(input.getMetaData().getDmpSampleId(),
                              input.getMetaData().getLegacySampleId());

                       return IdMapService.INSTANCE.isSampleIdInDarwin(idTuple);
                   }
               }).toList();
        // during development - report how many samples were skipped

        logger.info("Number of samples that pass Darwin filter " +newResultList.size());
        // replace original list with new one if necessary
        if( newResultList.size() < data.getResults().size()) {
            logger.info("DMP data has samples that have not been registered in Darwin");
            Collection<Result> missingResultList = CollectionUtils.diff(data.getResults(), newResultList);
            for ( Result result : missingResultList){
                logger.info("DMP sample id :" +result.getMetaData().getDmpSampleId() +" was not found in the Darwin database");
            }
            data.setResults(newResultList);
        } else {
            logger.info("All DMP samples were found in the Darwin database");
        }

    }

    // main method for stand alone testing
    public static void main(String...args){
        ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        try {
            DMPDataTransformer transformer = new DMPDataTransformer((Paths.get("/tmp/msk-impact")));
            DMPclinicaldataimporter dmpImporterRetriever = new DMPclinicaldataimporter();
            DmpData data = OBJECT_MAPPER.readValue(dmpImporterRetriever.getResult(), DmpData.class);
              logger.info("Results size = " + data.getResults().size());


            DMPclinicaldataimporter dmpIporter_mark =
                    new  DMPclinicaldataimporter(transformer.transform(data));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
