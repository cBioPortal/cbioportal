package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.CaseType;
import org.mskcc.cbio.foundation.jaxb.CasesType;
import org.mskcc.cbio.foundation.jaxb.ClientCaseInfoType;
import org.mskcc.cbio.foundation.jaxb.ShortVariantType;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;
import org.mskcc.cbio.importer.persistence.staging.TsvStagingFileHandler;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationTransformer;

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
 * Created by fcriscuo on 11/8/14.
 */
public class FoundationShortVariantTransformer extends MutationTransformer {
    /*
    responsible for the transformation of Foundation Short Variants to standard MAF formatted Strings
    and output to a staging file
     */
    private static final Logger logger = Logger.getLogger(FoundationShortVariantTransformer.class);

    public FoundationShortVariantTransformer(TsvStagingFileHandler aHandler) {
        super(aHandler);

    }


    /*
    package level method to transform all the short variants in a Foundation XML file
     */
    Integer transform( CasesType casesType){
        // get of list of ShortVariantType
        List<MutationModel> modelList = Lists.newArrayList();
        // we need to add the sample id to each set of short variants
        for (CaseType caseType: casesType.getCase()) {
            final String sampleId = caseType.getCase();

            modelList.addAll(FluentIterable.from(caseType.getVariantReport().getShortVariants().getShortVariant())
                    .transform(new Function<ShortVariantType,ShortVariantType>(){
                        @Nullable
                        @Override
                        public ShortVariantType apply(ShortVariantType svt) {
                            svt.setValue(sampleId);
                            return svt;
                        }
                    })
                    .transform( new Function<ShortVariantType, FoundationShortVariantModel>() {

                        @Nullable
                        @Override
                        public FoundationShortVariantModel apply(@Nullable ShortVariantType input) {
                            return new FoundationShortVariantModel(input);
                        }
                    }).toList()

            );
        }
        this.fileHandler.transformImportDataToTsvStagingFile(modelList,MutationModel.getTransformationFunction());
        return casesType.getCase().size();
    }

    /*
    main method for stand alone testing
     */

    public static void main (String...args){
        TsvStagingFileHandler fileHandler = new MutationFileHandlerImpl();
        Path testFile = Paths.get("/tmp");

        FoundationShortVariantTransformer test = new FoundationShortVariantTransformer(fileHandler);
        try {

            JAXBContext context = JAXBContext.newInstance(ClientCaseInfoType.class);
            Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
            JAXBElement obj = (JAXBElement) jaxbUnmarshaller.unmarshal(new FileInputStream("/tmp/13-081.xml"));
            ClientCaseInfoType ccit = (ClientCaseInfoType) obj.getValue();
            CancerStudyMetadata metadata = new CancerStudyMetadata(
                    new String[]{"dlbc/13-081/mskcc/foundation",	"dlbc",	"dlbc_13-081_mskcc_foundation",	"DLBCL MYC/BCL2 13-081 & 13-158 (MSKCC / Foundation Medicine)",
                            "DLBCL samples, profiled by Foundation Medicine","","","LEVINER",	"DLBCL (MSKCC/Foundation)",	"FALSE",	"TRUE",	"FALSE",	"FALSE",
                            "x"			}
            );
            test.registerStagingFileDirectory(metadata,testFile);
            Integer sampleCount = test.transform(ccit.getCases());
           logger.info("Sample count = " +sampleCount);
        } catch (JAXBException | FileNotFoundException ex) {
            logger.error(ex.getMessage());
        }


    }


}
