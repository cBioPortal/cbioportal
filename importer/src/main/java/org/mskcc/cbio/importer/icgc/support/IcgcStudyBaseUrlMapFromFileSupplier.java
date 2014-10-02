/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * 
 *  This library is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 *  documentation provided hereunder is on an "as is" basis, and
 *  Memorial Sloan-Kettering Cancer Center 
 *  has no obligations to provide maintenance, support,
 *  updates, enhancements or modifications.  In no event shall
 *  Memorial Sloan-Kettering Cancer Center
 *  be liable to any party for direct, indirect, special,
 *  incidental or consequential damages, including lost profits, arising
 *  out of the use of this software and its documentation, even if
 *  Memorial Sloan-Kettering Cancer Center 
 *  has been advised of the possibility of such damage.
 */
package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
/*
 reads in list of ICGC studies from a classpath accessible file
 filters out US studies and generates a Map of study names and base
 URLs to that study
 Clients are required to edit the supplies URL with the appropriate
 mutation type
Example:
Map<String, String> urlMap = Suppliers.memoize(new IcgcStudyBaseUrlMapFromFileSupplier()).get();
 */

public class IcgcStudyBaseUrlMapFromFileSupplier implements Supplier<Map<String, String>> {

    public final String icgcBaseUrlTemplate
            = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/STUDY/MUTATION_TYPE.STUDY.tsv.gz";
    private static final String US = "US";
    private static final String STUDY_LIST_FILENAME = "/ICGC_Studies.txt";
    private final String STUDY_FLAG = "STUDY";

    public IcgcStudyBaseUrlMapFromFileSupplier() {

    }

    @Override
    public Map<String, String> get() {
        
        URL resource = this.getClass().getResource(STUDY_LIST_FILENAME);
        try {
            Path p = Paths.get(resource.toURI());
            System.out.println("Building Map");
            return FluentIterable.from(Files.readAllLines(p, Charset.defaultCharset()))
                    .filter(IcgcImportService.INSTANCE.usStudyFilter)
                    .transform(new Function<String, String>() {

                        @Override
                        public String apply(String f) {
                            return IcgcImportService.blankSplitter.splitToList(f).get(0);
                        }
                    }).toMap(new Function<String, String>() {

                        @Override
                        public String apply(String f) {
                            return icgcBaseUrlTemplate.replaceAll(STUDY_FLAG, f);
                        }
                    });

           
        } catch (IOException | URISyntaxException ex) {
            Logger.getLogger(IcgcStudyBaseUrlMapFromFileSupplier.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Maps.newConcurrentMap(); // return empty map if exception is thrown
    }

    /*
     main method to test supplier 
     */
    public static void main(String... args) {
        Supplier<Map<String, String>> supplier = Suppliers.memoize(new IcgcStudyBaseUrlMapFromFileSupplier());
        Map<String, String> urlMap = supplier.get();
        System.out.println("Map size " + urlMap.size());
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
        // test memoization by getting the map again
        Map<String, String> urlMap2 = supplier.get();

    }

}
