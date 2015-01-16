package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.*;
import org.mskcc.cbio.importer.persistence.staging.*;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionModel;
import org.mskcc.cbio.importer.persistence.staging.fusion.FusionTransformer;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationFileHandlerImpl;

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
public class FoundationFusionTransformer extends FusionTransformer {
    private static final Logger logger = Logger.getLogger(FoundationFusionTransformer.class);

    public FoundationFusionTransformer(TsvStagingFileHandler aHandler ) {
        super(aHandler);
        Preconditions.checkArgument(aHandler != null,
                "A TsvStagingFileHandler implementation is required");

    }


    /*
    transform the xml data to the staging file
     */
    Integer transform(CasesType casesType){
        Preconditions.checkState(this.fileHandler.isRegistered(),"The file handler has not been associated with a staging file");
        for (CaseType caseType : casesType.getCase()){
            final String sampleId = caseType.getCase();  // get sample id from case
            List<FusionModel> fusionModelList = FluentIterable.from(this.replicateFusionEvents(caseType))
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
    private method to replicate Fusion rearrangement event to support the original other-gene as
    a targeted-gene

    mod 19Nov2014 FJC added support for two fusion entries for each foundation rearrangement
    create a new Rearrangement type
     */
    private List<RearrangementType> replicateFusionEvents(CaseType caseType){
        List<RearrangementType> rearrangementTypes = Lists.newArrayList();
        final String sampleId = caseType.getCase();  // get sample id from case
        /*
        extract the current rearrangements from the XML
         */
       List<RearrangementType> originalList =  FluentIterable.from(caseType.getVariantReport().getRearrangements().getContent())
               .filter(new Predicate<Serializable>() {
                   @Override
                   public boolean apply(@Nullable Serializable input) {
                       return input instanceof JAXBElement;
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
                       return (RearrangementType) input.getValue();
                   }
               }).toList();
        rearrangementTypes.addAll(originalList);

        logger.info("Sample " +sampleId +" original rearrangement count " +rearrangementTypes.size());
        for (RearrangementType origFusion : originalList) {
            if ( !origFusion.getOtherGene().equals(origFusion.getTargetedGene()) && !origFusion.getOtherGene().contains("Region") ) {
                RearrangementType newFusion = new ObjectFactory().createRearrangementType();
                newFusion.setDescription(origFusion.getDescription());
                newFusion.setInFrame(origFusion.getInFrame());
                newFusion.setOtherGene(origFusion.getTargetedGene());
                newFusion.setPos1(origFusion.getPos2());
                newFusion.setPos2(origFusion.getPos1());
                newFusion.setStatus(origFusion.getStatus());
                newFusion.setTargetedGene(origFusion.getOtherGene());
                newFusion.setSupportingReadPairs(origFusion.getSupportingReadPairs());
                rearrangementTypes.add(newFusion);
            }
        }
        logger.info("Sample " +sampleId +" replicated rearrangement count " +rearrangementTypes.size());
        return rearrangementTypes;
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
            ex.printStackTrace();
        }
    }



}
