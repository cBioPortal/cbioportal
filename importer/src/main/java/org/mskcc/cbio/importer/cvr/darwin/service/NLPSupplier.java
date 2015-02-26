package org.mskcc.cbio.importer.cvr.darwin.service;

import com.google.common.base.Supplier;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.Properties;

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
 * Created by criscuof on 12/3/14.
 */
public class NLPSupplier  implements Supplier<StanfordCoreNLP> {

        public NLPSupplier() {}

        @Override
        public StanfordCoreNLP get() {
            // specifiy the minimum set of annotators
            Properties props = new Properties();
            props.put("annotators", "tokenize, ssplit");

            return new StanfordCoreNLP(props);
        }

}
