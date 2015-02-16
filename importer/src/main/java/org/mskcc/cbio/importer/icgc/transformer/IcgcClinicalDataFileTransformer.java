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
package org.mskcc.cbio.importer.icgc.transformer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gdata.util.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.model.IcgcClinicalModel;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandler;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataFileHandlerImpl;
import org.mskcc.cbio.importer.persistence.staging.clinical.ClinicalDataTransformer;
import org.mskcc.cbio.importer.persistence.staging.util.StagingUtils;

public class IcgcClinicalDataFileTransformer extends ClinicalDataTransformer implements IcgcFileTransformer {

    private Path icgcFilePath;
    private final String txtExtension = ".txt";
    private Path txtFilePath;
    private String icgcClinicalFileUrl;
    private static final Pattern pat = Pattern.compile("\t");
    private static final String NORMAL = "Normal";
    private static final Logger logger = Logger.getLogger(IcgcClinicalDataFileTransformer.class);

    /*
    constructor that associates a file handler with the transformer and
    determines the column headings for the output file
     */
    public IcgcClinicalDataFileTransformer(ClinicalDataFileHandler fileHandler, Path stagingFileDirectory) {
        super(fileHandler);
        Preconditions.checkArgument(null != stagingFileDirectory,
                "A Path to a staging file directory is required");
        try {
            Files.createDirectories(stagingFileDirectory);
            fileHandler.registerClinicalDataStagingFile(stagingFileDirectory.resolve("data_clinical.txt"),
                    StagingUtils.resolveColumnNames(IcgcClinicalModel.transformationMap));
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
    /*
    public method to set the URL for an ICGC clinical file
     */
    public void setIcgcUrl(String url){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(url),
                "A URL to an ICGC clinical data file is required");
        this.icgcClinicalFileUrl = url;
        logger.info("The URL for an ICGC clinical file set to " +this.icgcClinicalFileUrl);
    }

    @Override
    public Path call() throws Exception {
        Preconditions.checkState(null != this.icgcFilePath || !Strings.isNullOrEmpty(this.icgcClinicalFileUrl),
                "An ICGC clinical data file or URL  must be provided before initiating transformation");
        this.processIcgcClinicalData();
        return this.txtFilePath;
    }

    @Override
    public void setIcgcFilePath(final Path aPath) {
        if (StagingUtils.isValidStagingDirectoryPath(aPath)) {
            this.icgcFilePath = aPath;
            this.txtFilePath = this.generateTxtFile();
        }
    }

    /* use the URL to obtain data directly from ICGC and transform each row individually

     */
    private void processIcgcClinicalData()  {
          logger.info(("Processing URL"));
        try {
            int recordCount = 0; // skip the header record
            BufferedReader rdr = new BufferedReader(new InputStreamReader(IOUtils.getInputStreamFromURLOrClasspathOrFileSystem(this.icgcClinicalFileUrl)));
            String line = "";
            while ( (line = rdr.readLine()) != null){
                if (recordCount++ > 0){
                    IcgcClinicalModel model = StringUtils.columnStringToObject(IcgcClinicalModel.class,
                            line, pat, IcgcClinicalModel.getFieldNames());
                    if (!model.getSpecimen_type().startsWith(NORMAL)) {
                        this.fileHandler.transformImportDataToTsvStagingFile(Lists.newArrayList(model), IcgcClinicalModel.transformationFunction);
                    }
                }
            }
            logger.info("Processed " +recordCount +" from URL " +this.icgcClinicalFileUrl );
        } catch (IOException  | InvocationTargetException | NoSuchMethodException |
                NoSuchFieldException | InstantiationException |  IllegalAccessException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * private method to generate a new file for the clinical data whose name is
     * based on the name of the ICGC file
     *
     * @return
     */
    private Path generateTxtFile() {
        String icscExtension = com.google.common.io.Files.getFileExtension(this.icgcFilePath.toString());
        String txtFilename = this.icgcFilePath.toString().replace(icscExtension, txtExtension);
        System.out.println(this.icgcFilePath.toString() + " will be mapped to " + txtFilename);
        return Paths.get(txtFilename);
    }

    /*
     main method for standalone testing
     */
    public static void main(String... args) {
       ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));
       IcgcClinicalDataFileTransformer transformer =new IcgcClinicalDataFileTransformer(new ClinicalDataFileHandlerImpl(),
               Paths.get("/tmp/icgc"));
        String url = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/PBCA-DE/clinical.PBCA-DE.tsv.gz";
       transformer.setIcgcUrl(url);
       service.submit(transformer);
        try {
            Thread.sleep(60000); // shutdown after 1 minute
              service.shutdown();
              logger.info("Test completed");
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
     
    }

}
