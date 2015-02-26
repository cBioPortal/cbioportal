package org.mskcc.cbio.importer.foundation.transformer;

import com.google.common.base.Function;

import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;
import org.mskcc.cbio.foundation.jaxb.*;
import org.mskcc.cbio.importer.foundation.support.FoundationCommonNames;

import org.mskcc.cbio.importer.persistence.staging.cnv.CnvFileHandler;
import org.mskcc.cbio.importer.persistence.staging.cnv.CnvTransformer;
import scala.Tuple2;

import javax.xml.bind.JAXBElement;

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
                for (Tuple2<String, String> cnaTuple : FluentIterable
                        .from(cnat.getContent())
                        .filter(JAXBElement.class)
                        .transform(cnaFumction)
                        .toList()) {
                    this.registerCnv(cnaTuple._1(), ct.getCase(), cnaTuple._2());
                }
            }
        }
    }

    private final Function<JAXBElement, Tuple2<String, String>> cnaFumction = new Function<JAXBElement, Tuple2<String, String>>() {
        @Override
        public Tuple2<String, String> apply(JAXBElement je) {
            CopyNumberAlterationType cna = (CopyNumberAlterationType) je.getValue();
            switch (cna.getType()) {
                case FoundationCommonNames.CNA_AMPLIFICATION:
                    return new Tuple2(cna.getGene(), AMP);
                case FoundationCommonNames.CNA_LOSS:
                    return new Tuple2(cna.getGene(), HOMOZY_LOSS);
                default:
                    return new Tuple2(cna.getGene(), COPY_NEUTRAL);
            }
        }
    };


    void persistFoundationCnvs() { super.persistCnvData();}

}
