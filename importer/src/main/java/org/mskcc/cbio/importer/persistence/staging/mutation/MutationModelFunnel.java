package org.mskcc.cbio.importer.persistence.staging.mutation;

import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import java.nio.charset.Charset;

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
public class MutationModelFunnel implements Funnel<MutationModel> {

    public MutationModelFunnel() {
    }

    /*
      private final String id;
    private final String projectCode;
    private final String sampleId;
    private final String chromosome;
    private final String start;
    private final String end;
    private final String refAllele;
    private final String mutAllele;
    private final String totalReads;
    private final String mutReads;
     */


    // mod 17Feb2015 - simplify Bloom Filter funnel
    @Override
    public void funnel(MutationModel t, PrimitiveSink into) {
        //into.putString(t.getGene(), Charset.defaultCharset())
               into.putString(t.getChromosome(), Charset.defaultCharset())
                .putString(t.getStartPosition(),Charset.defaultCharset())
                .putString(t.getEndPosition(), Charset.defaultCharset())
                .putString(t.getStrand(),Charset.defaultCharset())
               // .putString(t.getVariantClassification(),Charset.defaultCharset())
              //  .putString(t.getVariantType(),Charset.defaultCharset())
                .putString(t.getRefAllele(),Charset.defaultCharset())
                .putString(t.getTumorAllele1(),Charset.defaultCharset())
                .putString(t.getTumorSampleBarcode(),Charset.defaultCharset());
              //  .putString(t.getAAChange(),Charset.defaultCharset())
              //  .putString(t.getEntrezGeneId(), Charset.defaultCharset());

    }

}
