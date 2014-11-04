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
package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;
import java.util.List;

public class FilterMutationsReport {

    /*
     Java application to filter out short-variant entries with status=unknown
     all other data are copied to a new XML file
     */
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(FilterMutationsReport.class);
    private static OpenOption[] options = new OpenOption[]{CREATE, DSYNC};

    public static void main(String... args) {
       
        Path filteredPath = Paths.get("/data/foundation/amc_rsp/mskcc/foundation/filtered");
        
        List<String> fileList = Lists.newArrayList("data_mutations_extended.txt");
        List<String> newList = Lists.newArrayList();
        Predicate<String> filterBlanks = new Predicate<String>() {

            @Override
            public boolean apply(String line) {
                return (!line.startsWith("\t\t\t\t"));
            }
        };

        for (String xmlFile : fileList) {
          
            Path inPath = filteredPath.resolve(xmlFile);
            Path outPath = filteredPath.resolve("data.txt");
            try {
               newList = FluentIterable.from(Files.readLines(inPath.toFile(), Charsets.UTF_8))
                       .filter(filterBlanks)
                       .toList();
               java.nio.file.Files.write(outPath, newList, Charset.defaultCharset(),
                    options);
               
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        

    }

}
