package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.CaseType;
import org.mskcc.cbio.foundation.jaxb.CasesType;
import org.mskcc.cbio.foundation.jaxb.ClientCaseInfoType;
import org.mskcc.cbio.foundation.jaxb.ShortVariantType;
import org.mskcc.cbio.importer.persistence.staging.*;

import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
public class FoundationClinicalDataTransformer {
    /*
    responsible for the transformation of Foundation XML elements into FoundationClinicalDataModel objects and
    their subsequent output to staging files using a ClinicalDataFile Handler implementation
     */

    private final ClinicalDataFileHandler fileHandler;
    private final static Logger logger = Logger.getLogger(FoundationClinicalDataTransformer.class);

    public FoundationClinicalDataTransformer (ClinicalDataFileHandler aHandler) {
        Preconditions.checkArgument(null != aHandler, " A ClinicalDataFileHandlerImplementation is required");

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

        Path clinicalPath = stagingDirectoryPath.resolve("data_clinical.txt");
        this.fileHandler.registerClinicalDataStagingFile(clinicalPath, ClinicalDataModel.resolveColumnNames(), true);
    }

    Integer transform(CasesType casesType){
        Preconditions.checkState(this.fileHandler.isRegistered(),
                "The Path to the clinical data file has not been specified");
        List<ClinicalDataModel> modelList =FluentIterable.from(casesType.getCase())
                .transform(new Function<CaseType,ClinicalDataModel>(){
                    @Nullable
                    @Override
                    public ClinicalDataModel apply(@Nullable CaseType input) {
                        return new FoundationClinicalDataModel(input);
                    }
                })
                .toList();
        this.fileHandler.transformImportDataToStagingFile(modelList,ClinicalDataModel.getTransformationFunction());
        return casesType.getCase().size();
    }

      /*
    main method for stand alone testing
     */

    public static void main (String...args){
        ClinicalDataFileHandler fileHandler = new ClinicalDataFileHandlerImpl();
        Path testFile = Paths.get("/tmp/clinicaldata_model.txt");
        fileHandler.registerClinicalDataStagingFile(testFile,ClinicalDataModel.resolveColumnNames(),true);
        FoundationClinicalDataTransformer test = new FoundationClinicalDataTransformer(fileHandler);
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
