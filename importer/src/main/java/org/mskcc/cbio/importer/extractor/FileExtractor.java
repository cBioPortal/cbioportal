package org.mskcc.cbio.importer.extractor;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
 * represents a class that can extract data from an input DataSource and copy
 * them to an output DataSource
 *
 * @author fcriscuo
 */
public class FileExtractor {

    private final FileDataSource inputDataSource;
    private final FileDataSource extractedDataSource;

    private final Predicate fileFilter;
    private final Joiner pathJoiner = Joiner.on(System.getProperty("file.separator"));
    private final Logger logger = Logger.getLogger(FileExtractor.class);

    public FileExtractor(final FileDataSource inSource, FileDataSource outSource,
            Predicate filter) {
        Preconditions.checkArgument(null != inSource, "An input DataSource is required");
        Preconditions.checkArgument(null != outSource, "An extraction DataSource is required");
        this.inputDataSource = inSource;
        this.extractedDataSource = outSource;
        this.fileFilter = filter;
    }

    public FileDataSource getExtractedDataSource() {
        return this.extractedDataSource;
    }

    public void extractData() throws IOException {
        this.filterAndExtractFiles();
    }

    /**
     * Copy the files listed in the input source to the specified target
     * directory and list the new files in the extractedDataSource Use the
     * supplied Predicate to filter which files are copied
     */
    private void filterAndExtractFiles() throws IOException {
        this.extractedDataSource.setFilenameList(
                FluentIterable
                .from(Files.newDirectoryStream((Paths.get(inputDataSource.getDirectoryName()))))
                .filter(fileFilter)
                .transform(new Function<Path, Path>() {
                    @Override
                    public Path apply(Path f) {
                        Path outPath = Paths.get(pathJoiner.join(extractedDataSource.getDirectoryName(), f.toFile().getName()));

                        try {
                            Files.copy(f, outPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ex) {
                            logger.error(ex.getMessage());
                        }
                        
                        return outPath;
                    }
                }).toList());

    }

}
