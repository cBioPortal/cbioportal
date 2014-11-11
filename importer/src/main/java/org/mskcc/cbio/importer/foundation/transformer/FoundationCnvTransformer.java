package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.*;
import org.mskcc.cbio.importer.foundation.support.CommonNames;

import org.mskcc.cbio.importer.persistence.staging.ClinicalDataModel;
import org.mskcc.cbio.importer.persistence.staging.CnvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.CnvTransformer;
import scala.Tuple2;

import javax.xml.bind.JAXBElement;
import java.nio.file.Path;

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
 * Created by fcriscuo on 11/9/14.
 */
public class FoundationCnvTransformer extends CnvTransformer {

    private final static Logger logger = Logger.getLogger(CnvTransformer.class);

    public FoundationCnvTransformer(CnvFileHandler fileHandler){
        super(fileHandler);
    }

    /*
    package level method to map CNV data from Foundation data to the CNV table
     */
     void generateCNATable(CasesType casesType) {
        for (CaseType ct : casesType.getCase()) {
            VariantReportType vrt = ct.getVariantReport();
            CopyNumberAlterationsType cnat = vrt.getCopyNumberAlterations();
            if (null != cnat) {
                for (Tuple2<String, Double> cnaTuple : FluentIterable
                        .from(cnat.getContent())
                        .filter(JAXBElement.class)
                        .transform(cnaFumction)
                        .toList()) {
                    this.registerCnv(cnaTuple._1(), ct.getCase(), cnaTuple._2());
                }
            }
        }
    }

    void registerStagingFileDirectory(Path stagingDirectoryPath){
        Preconditions.checkArgument(null != stagingDirectoryPath,
                "A Path to the staging file directory is required");
        Path cnvPath = stagingDirectoryPath.resolve("data_CNA.txt");
        this.fileHandler.initializeFilePath(cnvPath);
    }


    private final Function<JAXBElement, Tuple2<String, Double>> cnaFumction = new Function<JAXBElement, Tuple2<String, Double>>() {
        @Override
        public Tuple2<String, Double> apply(JAXBElement je) {
            CopyNumberAlterationType cna = (CopyNumberAlterationType) je.getValue();
            switch (cna.getType()) {
                case CommonNames.CNA_AMPLIFICATION:
                    return new Tuple2(cna.getGene(), CommonNames.CNA_AMPLIFICATION_VALUE);
                case CommonNames.CNA_LOSS:
                    return new Tuple2(cna.getGene(), CommonNames.CNA_LOSS_VALUE);
                default:
                    return new Tuple2(cna.getGene(), CommonNames.CNA_DEFAULT_VALUE);
            }
        }
    };


    void persistFoundationCnvs() { super.persistCnvData();}

}
