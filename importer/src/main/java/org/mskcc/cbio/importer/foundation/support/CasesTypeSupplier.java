package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.*;
import org.mskcc.cbio.importer.model.FoundationMetadata;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
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
/**
 * The intent of this class is to provide global access to the JAXB
 * representation of a Foundation Medicine XML file. This class should be
 * invoked using the Google Guava Suppliers.memoize method to avoid repeated
 * unmarshalling of the XML data
 *
 * @author criscuof
 */
public class CasesTypeSupplier implements Supplier<CasesType> {

    private final String xmlFileName;
    private static final Logger logger = Logger.getLogger(CasesTypeSupplier.class);
    private FoundationMetadata metadata;

    public CasesTypeSupplier(String xmlFileName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(xmlFileName), "A Foundation xml file name is required");
        this.xmlFileName = xmlFileName;
    }
    /*
    constructor to support studies that have a list of case ids to exclude
    */
    public CasesTypeSupplier(String xmlFileName, FoundationMetadata meta) {
        this(xmlFileName);
        Preconditions.checkArgument(null != meta,"A FoundationMetadata instance is required");
        this.metadata = meta;

    }

    /*
    As long as this method is referenced by the Suppliers.memoize method, it will only actually be invoked once
    In addition to unmarshalling the XML data, this method filters out any cases specified in the foundation worksheet
    as well as variants with a specified excluded status
     */

    @Override
    public CasesType get() {
        try {
            logger.info("CaseTypeSupplier get  invoked...");
            JAXBContext context = JAXBContext.newInstance(ClientCaseInfoType.class);
            Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
            JAXBElement obj = (JAXBElement) jaxbUnmarshaller.unmarshal(new FileInputStream(this.xmlFileName));
            ClientCaseInfoType ccit = (ClientCaseInfoType) obj.getValue();
            logger.info("++++File " +this.xmlFileName +" has " +ccit.getCases().getCase().size() +" samples before filtering");
            // mod 04Oct2014 - add support for filtering out excluded cases
            if (null!=metadata && !metadata.getExcludedCases().isEmpty()){
                List<CaseType> removalList = FluentIterable.from(ccit.getCases().getCase())
                        .filter(removeCaseFilter).toList();
                ccit.getCases().getCase().removeAll(removalList);
                // output removed cases during development
                for (CaseType caseType : removalList){
                    logger.info("Foundation case removed: " +caseType.getCase());
                }
            }
            // filter out short variants based on specified status
            if(null!= metadata && !metadata.getShortVariantExcludedStatuses().isEmpty()) {
                for ( CaseType caseType : ccit.getCases().getCase()){
                   List<ShortVariantType>  removalList = FluentIterable.from(caseType.getVariantReport().getShortVariants().getShortVariant())
                           .filter(new Predicate<ShortVariantType>() {
                               @Override
                               public boolean apply(@Nullable ShortVariantType svt) {
                                   return metadata.getShortVariantExcludedStatuses().contains(svt.getStatus());
                               }
                           }).toList();
                    // replace the original List with the filtered List
                    caseType.getVariantReport().getShortVariants().getShortVariant().removeAll(removalList);
                    // output removed svt during development
                    for (ShortVariantType svt : removalList){
                        logger.debug ("Foundation svt removed from case " + caseType.getCase() + " gene " + svt.getGene() + " status " + svt.getStatus());
                    }
                    logger.info(removalList.size() +" SVTs were removed from case " +caseType.getCase() +" because of excluded status");
                }

                // filter out CNVs that match an excluded status value
                if (null != metadata && !metadata.getCnvExcludedStatuses().isEmpty()){
                    for (CaseType caseType : ccit.getCases().getCase()){
                        List<Serializable> removalList  = FluentIterable.from(caseType.getVariantReport().getCopyNumberAlterations().getContent())

                                .filter(new Predicate<Serializable>() {
                                            @Override
                                            public boolean apply(@Nullable Serializable s) {
                                                if (!(s instanceof JAXBElement)) {
                                                    return false;
                                                }
                                                CopyNumberAlterationType cnv = (CopyNumberAlterationType) ((JAXBElement) s).getValue();
                                                if (metadata.getCnvExcludedStatuses().contains(cnv.getStatus())) {
                                                    return true;
                                                }
                                                return false;
                                            }
                                        }).toList();

                        // remove these items from the original cnv list
                        caseType.getVariantReport().getCopyNumberAlterations().getContent().removeAll(removalList);
                        for (Serializable s : removalList){
                            CopyNumberAlterationType ct = (CopyNumberAlterationType) ((JAXBElement) s).getValue();
                            logger.info("CNV filter removed " +ct.getGene() +" status = " +ct.getStatus());
                        }
                    }
                }

            }
            logger.info("++++File " +this.xmlFileName +" has " +ccit.getCases().getCase().size() +" samples after filtering");

            return ccit.getCases();
        } catch (JAXBException | FileNotFoundException ex) {
            logger.error(ex.getMessage());
        }

        return null; // TODO: change to Optional
    }
    /*
    Predicate to identify case ids that match elements in the excluded case list
    */
    final private Predicate<CaseType> removeCaseFilter = new Predicate<CaseType>() {
        @Override
        public boolean apply(final CaseType caseType) {
            return metadata.getExcludedCases().contains(caseType.getCase());
        }
    };

}
