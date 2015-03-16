package org.mskcc.cbio.importer.icgc;

import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.support.IcgcFunctionLibrary;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.persistence.staging.mutation.MutationModel;

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
 * Created by criscuof on 12/24/14.
 */
public class TestIcgcModelClass {
    private final static Logger logger = Logger.getLogger(TestIcgcModelClass.class);
    public TestIcgcModelClass() {}

    private void perfromTests(Class modelClass) throws Exception{

        String line= "MU13680743\tDO46744\tPAEN-AU\tSP102485\tSA506648\tSA506652\t8051707\t8051438\t1\t3092543\t3092543\t1\tGRCh37\tsingle base substitution\tC\tC\tG\t\t0.17437845433639174\t70\t6\tnot tested\t\tnot tested\t\tintron_variant\t\t\tENSG00000142611\tENST00000378391\t75\tIllumina HiSeq\tPaired End http://www.illumina.com/technology/paired_end_sequencing_assay.ilmn\tWGS\tRTA http://support.illumina.com/sequencing/sequencing_software/real-time_analysis_rta.ilmn\tbwa http://bio-bwa.sourceforge.net/\tqSNP http://sourceforge.net/p/adamajava/wiki/qSnp/\tGATK http://www.broadinstitute.org/gatk/\t\tEGA\tEGAS00001000154\t\n";
        MutationModel model = (MutationModel) StringUtils.columnStringToObject(modelClass,
                line, StagingCommonNames.tabPattern, IcgcFunctionLibrary.resolveFieldNames(modelClass));
        logger.info(MutationModel.getTransformationFunction().apply(model) );
    }

    public static void main(String...args){
        TestIcgcModelClass test = new TestIcgcModelClass();
        try {
            Class modelClass = Class.forName("org.mskcc.cbio.importer.icgc.model.IcgcSimpleSomaticMutationModel");
            test.perfromTests(modelClass);
        } catch(Exception e){
            logger.error(e.getMessage());
        }
    }

}
