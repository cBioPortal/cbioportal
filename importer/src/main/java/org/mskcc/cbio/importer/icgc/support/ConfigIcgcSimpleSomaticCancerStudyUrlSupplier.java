package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.gdata.util.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

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
public class ConfigIcgcSimpleSomaticCancerStudyUrlSupplier implements Supplier<List<String>> {

  

    private static final Splitter blankSplitter = Splitter.on(' ');
    private static final Logger logger = Logger.getLogger(ConfigIcgcSimpleSomaticCancerStudyUrlSupplier.class);
    private static final String US = "US";
    private static final String urlTemplate
            = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/XXXX/simple_somatic_mutation.open.XXXX.tsv.gz";
    private final Config config;
    private final String portalName;

    public ConfigIcgcSimpleSomaticCancerStudyUrlSupplier(Config aConfig, String aName) {
        Preconditions.checkArgument(null != aConfig , "A org.mskcc.cbio.importer.Config implementation is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aName),"A potal name is required");
        this.config = aConfig;
        this.portalName = aName;
    }
    
    Predicate usStudyFilter = new Predicate<String>() {
        public boolean apply(String t) {
            return (!(t.endsWith(US)) && !Strings.isNullOrEmpty(t));
        }

    };

    @Override
    public List<String> get() {
        
            return FluentIterable.from(this.resolveIcgcStudyNames())
                    .filter(usStudyFilter)
                    .transform(new Function<String, String>() {

                        public String apply(String f) {
                            return urlTemplate.replaceAll("XXXX", blankSplitter.splitToList(f).get(0));
                        }

                    }).toList();

       
    }
    /*
    private method to determine ICGC study names from import Config object
    use Cancer
    */
    private List<String> resolveIcgcStudyNames(){
        List<String> studyList = Lists.newArrayList();
        Collection<CancerStudyMetadata> cancerStudies = this.config.getCancerStudyMetadata(this.portalName);
        for (CancerStudyMetadata study : cancerStudies) {
            studyList.add(study.getStableId());
        }
        return studyList; 
    }

}
