package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.CaseType;
import org.mskcc.cbio.foundation.jaxb.CasesType;
import org.mskcc.cbio.foundation.jaxb.ClientCaseInfoType;
import org.mskcc.cbio.foundation.jaxb.RearrangementType;
import org.mskcc.cbio.importer.persistence.staging.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;

import javax.annotation.Nullable;
import javax.xml.bind.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
 * Created by fcriscuo on 11/10/14.
 */
public class FoundationFusionTransformer {
    private static final Logger logger = Logger.getLogger(FoundationFusionTransformer.class);
    private final TsvStagingFileHandler fileHandler;

    public FoundationFusionTransformer(TsvStagingFileHandler aHandler ) {
        Preconditions.checkArgument(aHandler != null,
                "A TsvStagingFileHandler implemntation is required");

        this.fileHandler = aHandler;
    }

    /*
 package method to register the staging file directory with the file handler
 this requires a distinct method in order to support multiple source files being
 transformed to a single staging file
  */
    void registerStagingFileDirectory(Path stagingDirectoryPath){
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");

        Path mafPath = stagingDirectoryPath.resolve("data_fusions.txt");
        this.fileHandler.registerTsvStagingFile(mafPath, FusionModel.resolveColumnNames(), true);
    }

    /*
    transform the xml data to the staging file
     */
    Integer transform(CasesType casesType){
        Preconditions.checkState(this.fileHandler.isRegistered(),"The file handler has not been associated with a staging file");
        for (CaseType caseType : casesType.getCase()){
            final String sampleId = caseType.getCase();  // get sample id from case
            List<FusionModel> fusionModelList = FluentIterable.from(caseType.getVariantReport().getRearrangements().getContent())
                    .filter(new Predicate<Serializable>(){
                        @Override
                        public boolean apply(@Nullable Serializable input) {
                            return  input instanceof JAXBElement;

                        }
                    })
                    .transform(new Function<Serializable, JAXBElement>() {
                        @Override
                        public JAXBElement apply(@Nullable Serializable input) {
                            return (JAXBElement) input;
                        }
                    })
                    .transform(new Function<JAXBElement, RearrangementType>() {
                        @Nullable
                        @Override
                        public RearrangementType apply(JAXBElement input) {
                            return  (RearrangementType) input.getValue();
                        }
                    })
                    .filter(new Predicate<RearrangementType>(){
                        @Override
                        public boolean apply(@Nullable RearrangementType input) {
                            return input.getDescription().endsWith("fusion");
                        }
                    })
                    .transform(new Function<RearrangementType, FusionModel>() {
                        @Nullable
                        @Override
                        public FusionModel apply(@Nullable RearrangementType input) {
                            return new FoundationFusionModel(input, sampleId);
                        }
                    }).toList();
           // pass this list to the file handler for output
            if (!fusionModelList.isEmpty()) {
                this.fileHandler.transformImportDataToTsvStagingFile(fusionModelList, FusionModel.getTransformationModel());
            } else {
                logger.info("Sample id : " +sampleId +" does not contain any fusion rearrangements");
            }
        }

        return casesType.getCase().size();
    }
     /*
    main method for stand alone testing
     */

    public static void main (String...args){
        TsvStagingFileHandler fileHandler = new MutationFileHandlerImpl();
        Path testFile = Paths.get("/tmp/foundation_fusion.txt");
        fileHandler.registerTsvStagingFile(testFile, FusionModel.resolveColumnNames(),true);
        FoundationFusionTransformer test = new FoundationFusionTransformer(fileHandler);
        try {
            JAXBContext context = JAXBContext.newInstance(ClientCaseInfoType.class);
            Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
            JAXBElement obj = (JAXBElement) jaxbUnmarshaller.unmarshal(new FileInputStream("/tmp/13-081.xml"));
            ClientCaseInfoType ccit = (ClientCaseInfoType) obj.getValue();
            Integer sampleCount = test.transform(ccit.getCases());
            logger.info("Sample count = " +sampleCount);
        } catch (JAXBException | FileNotFoundException ex) {
            logger.error(ex.getMessage());
        }


    }



}
