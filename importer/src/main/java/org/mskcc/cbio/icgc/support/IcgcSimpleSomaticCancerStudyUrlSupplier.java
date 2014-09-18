package org.mskcc.cbio.icgc.support;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.io.Files;
import com.google.gdata.util.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.Config;

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
public class IcgcSimpleSomaticCancerStudyUrlSupplier implements Supplier<List<String>> {

    private  final String studyFileName ;

    private static final Splitter blankSplitter = Splitter.on(' ');
    private static final Logger logger = Logger.getLogger(IcgcSimpleSomaticCancerStudyUrlSupplier.class);
    private static final String US = "US";
    private static final String urlTemplate
            = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/XXXX/simple_somatic_mutation.open.XXXX.tsv.gz";
    

   
    
    public IcgcSimpleSomaticCancerStudyUrlSupplier(String dataSource) {
        
      Preconditions.checkArgument(!Strings.isNullOrEmpty(dataSource), "A source for ICSC cancer studies is required");
      this.studyFileName = dataSource;
    }

    Predicate usStudyFilter = new Predicate<String>() {
        public boolean apply(String t) {
            return (!(t.endsWith(US)) && !Strings.isNullOrEmpty(t));
        }

    };

    @Override
    public List<String> get() {

        try {
            List<String> allStudies = Files.readLines(new File(studyFileName), Charset.defaultCharset());
            return FluentIterable.from(allStudies)
                    .filter(usStudyFilter)
                    .transform(new Function<String, String>() {

                        public String apply(String f) {
                            return urlTemplate.replaceAll("XXXX", blankSplitter.splitToList(f).get(0));
                        }

                    }).toList();

        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return new ArrayList<String>();  // return empty string
    }
   
}
