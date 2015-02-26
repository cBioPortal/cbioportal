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

public class UnknownVariationFilter {

    /*
     Java application to filter out short-variant entries with status=unknown
     all other data are copied to a new XML file
     */
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(UnknownVariationFilter.class);
    private static OpenOption[] options = new OpenOption[]{CREATE, DSYNC};

    public static void main(String... args) {
        Path basePath = Paths.get("/data/foundation/amc_rsp/mskcc/foundation");
        Path filteredPath = Paths.get("/data/foundation/amc_rsp/mskcc/foundation/filtered");
        
        List<String> xmlFileList = Lists.newArrayList("13-081.xml",
                "AMC-RSP-13-158_MSKCC.xml");
        List<String> newList = Lists.newArrayList();
        Predicate<String> filterUnknown = new Predicate<String>() {

            @Override
            public boolean apply(String line) {
                return (!line.contains("status=\"unknown\""));
            }
        };

        for (String xmlFile : xmlFileList) {
            Path inPath = basePath.resolve(xmlFile);
            Path outPath = filteredPath.resolve(xmlFile);
            try {
                List<String> lines = Files.readLines(inPath.toFile(), Charsets.UTF_8);
                for (String line : lines) {
                    if (!line.contains("<short-variant cds")) {
                        newList.add(line);
                    } else if (filterUnknown.apply(line)) {
                        newList.add(line);
                    }
                }
               java.nio.file.Files.write(outPath, newList, Charset.defaultCharset(),
                    options);
               newList.clear();
            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }
        }
        

    }

}
