package org.mskcc.cbio.importer.extractor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Sets;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;

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
 *
 * @author fcriscuo
 * 
*/
/**
 * Represents a specialized file extractor responsible for copying FMI XML
 * files from a base directory to subdirectories based on the MSKCC cancer
 * study name. After a successful copy the original XML file is renamed to
 * reflect the copy date. This class is also responsible for providing a list
 * of MSKCC cancer studies that have received one or more new/updated XML
 * files. The primary input to this class is a FileDataSource object that 
 * contains the base directory and the XML files to be processed
 *
 * @author fcriscuo
 */
public class FoundationStudyExtractor {

    private final FileDataSource inputDataSource;
    private Set<Path> cancerStudyPathSet;

    
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
    private final Logger logger = Logger.getLogger(FoundationStudyExtractor.class);

    public FoundationStudyExtractor(final FileDataSource xmlSource) {
        Preconditions.checkArgument(null != xmlSource, 
                "An input FileDataSource of XML files is required");
       
        this.inputDataSource = xmlSource;
        this.cancerStudyPathSet = Sets.newHashSet();
        
    }

    
    public void extractData() throws IOException {
        this.processFoundationFiles();
    }
    
    private Path resolveDestinationPath(Path sourcePath) {
        //TODO: complete implementation
        return Paths.get("/tmp/foundation");
    }

    /**
     * Process each XML file in the input source. Determine the copy destination from the
     * cancer study the file belongs to, copy the file and rename the original. Add the
     * affected cancer study to the Set
     * 
     */
    private void processFoundationFiles() throws IOException {
       List<Path> destinationPathList =
                FluentIterable
                .from(Files.newDirectoryStream((Paths.get(inputDataSource.getDirectoryName()))))
               
                .transform(new Function<Path, Path>() {
                    @Override
                    public Path apply(Path inPath) {                 
                        Path outPath = resolveDestinationPath(inPath);
                        try {
                            Files.copy(inPath, outPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            logger.error(ex.getMessage());
                        }
                        
                        return outPath;
                    }
                }).toList();

    }

}
