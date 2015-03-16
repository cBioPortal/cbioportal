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
package org.mskcc.cbio.importer.persistence.staging.util;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import edu.stanford.nlp.io.FileSequentialCollection;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.model.FoundationMetadata;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/*
 represents a collection of static utility methods used though out the application
 */
public class StagingUtils {



    private static final Logger logger = Logger.getLogger(StagingUtils.class);


    /*
    Public method to determine the absolute file name for a file that
    starts with an environmental variable
    (e.g. $PORTAL_DATA_HOME/study/xyz.txt
    If the input parameter does not start with a $, the input is returned as is
     */
    public static String resolveFileFromEnvironmentVariable(String input){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(input),"A file name is required");
        if(input.startsWith("$")) {
            String[] parts = StringUtils.split(input, File.separator);
            parts[0] = System.getenv(parts[0].replace("$", ""));
            return StagingCommonNames.pathJoiner.join(parts);
        } else {
            return input;
        }
    }

    /*
     static method to provide a generic getter for model attributes
     encapsulates handling for null attribute values
     converts non-String results to their String representation
     String geneName = DmpUtils.pojoStringGetter("getGeneName",cnaVariant);
     */
    public static String pojoStringGetter(String getterName, Object obj) {
        try {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(getterName), "A getter method name is required");
            Preconditions.checkArgument(null != obj, "An object instance is required");
            Method getterMethod = obj.getClass().getMethod(getterName);
            if (getterMethod.getReturnType() == java.lang.String.class) {
                String value = (String) getterMethod.invoke(obj);
                return (!Strings.isNullOrEmpty(value)) ? value : "";
            }
            Object value = getterMethod.invoke(obj);
            return (value != null) ? value.toString() : "";

        } catch (Exception ex) {
            logger.error("Failed to execute getter " +getterName +" for " +obj.getClass().getName());
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
        return "";
    }

    /*
    public static method to find Foundation XML files that should be copied for filtered import
    1. find the XML files in a specified download directory
    2. filter out prexisting filtered files
    3. find the FoundationMetadata object associated with each XML file
    4, determine if the file belongs to a filtered study
    5. if so, copy the file to the same directory, appending "filtered" to the file name
     */
    public static void copyFilteredXMLFiles(Path xmlDirPath) {
        FileSequentialCollection fsc = new FileSequentialCollection(xmlDirPath.toFile(),
                StagingCommonNames.xmlExtension,false);
        Observable<File> fileObservable = Observable.from(fsc)
                // filter out -filtered files from a previous run
                .filter(new Func1<File,Boolean>() {
                            @Override
                            public Boolean call(File file) {
                                return !file.getName().contains(StagingCommonNames.FOUNDATION_FILTERED_NOTATION +".xml");
                            }
                        }
                        // filter for files belonging to a filtered study
                ).filter(new Func1<File, Boolean>() {
                    @Override
                    public Boolean call(File file) {
                        Optional<FoundationMetadata>optSrcMeta =
                                FoundationMetadata.findFoundationMetadataByXmlFileName(file.getName());
                        return optSrcMeta.isPresent() && !Strings.isNullOrEmpty(optSrcMeta.get().getFilteredStudy());
                    }
                });
        fileObservable.subscribe(new Subscriber<File>() {
            @Override
            public void onCompleted() {
                logger.info("Filtered XML file copy operation(s) completed");
            }
            @Override
            public void onError(Throwable throwable) {
                logger.error(throwable.getMessage());
            }
            @Override
            public void onNext(File file) {
                try {
                    // filtered files use lower case names to avoid name collisions with original file
                        String destFileName = file.getName().toLowerCase().replace(".xml",StagingCommonNames.FOUNDATION_FILTERED_NOTATION +".xml");
                        File destFile = new File(file.getAbsolutePath().replace(file.getName(), destFileName));
                        new FileOutputStream(destFile).getChannel().transferFrom(
                                new FileInputStream(file).getChannel(), 0, Long.MAX_VALUE);
                        logger.info("XML file " +file.getName() +" copied to " +destFile.getName()
                        +" for filtered analysis");


                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    utility method to convert the keys of a supplied map to a List of Strings
    to be used as column headings in a tsv file
     */
    public static List<String> resolveColumnNames(Map<String,String> transformationMap) {
        Preconditions.checkArgument(null != transformationMap && transformationMap.size()>0,
                "A valid transformation map is required");
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

    public static String[]  resolveFieldNames(Class modelClass){
        Field[] modelFields = modelClass.getDeclaredFields();
        String[] fieldnames = new String[modelFields.length];
        for (int i = 0; i < modelFields.length; i++) {
            fieldnames[i] = modelFields[i].getName();
        }
        return fieldnames;
    }

    /*
    common set of criteria for a Path to a directory to be used for
    writing staging files
     */
    public static boolean isValidStagingDirectoryPath(Path aPath) {
        com.google.common.base.Preconditions.checkArgument
                (null != aPath,
                        "A Path to the staging file directory is required");
        com.google.common.base.Preconditions.checkArgument
                (Files.isDirectory(aPath, LinkOption.NOFOLLOW_LINKS),
                        "The specified Path: " + aPath + " is not a directory");
        com.google.common.base.Preconditions.checkArgument
                (Files.isWritable(aPath),
                        "The specified Path: " + aPath + " is not writable");
        return true;

    }
/*
common criteria for validating a specified Path to an input file
 */
    public static boolean isValidInputFilePath(Path aPath) {
        com.google.gdata.util.common.base.Preconditions.checkArgument(null != aPath,
                "A Path to an input  file is required");
        com.google.gdata.util.common.base.Preconditions.checkArgument(Files.exists(aPath, LinkOption.NOFOLLOW_LINKS),
                aPath + " is not a file");
        com.google.gdata.util.common.base.Preconditions.checkArgument(Files.isReadable(aPath),
                aPath + " is not readable");
        return true;
    }

    // main method for stand alone testing
    public static void main (String...args){
        Path testPath = Paths.get("/tmp/foundation-test");
        StagingUtils.copyFilteredXMLFiles(testPath);
       // logger.info("Valid staging directory " + StagingUtils.isValidStagingDirectoryPath(testPath));
        // invalid directory - should throw Exception
        try{
            logger.info("Valid staging directory " + StagingUtils.isValidStagingDirectoryPath(Paths.get("/tmp/xxxxxxxxxx")));
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        // valid input file
        Path inputPath = testPath.resolve("lymphoma.xml"); // good file
        //logger.info("Valid input file " + StagingUtils.isValidInputFilePath(inputPath));
        // invalid input file
        Path badPath = testPath.resolve("xxxxxx.xml");
        try{
            logger.info("Input file " + StagingUtils.isValidInputFilePath(badPath));
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        logger.info("Test path resolutiom");
        logger.info(" Path " +resolveFileFromEnvironmentVariable("$PORTAL_DATA_HOME/msk-impact/msk-impact"));
    }

    /*
   create a staging file path if one does not exist already
   */
    public static boolean validateStagingPath(final Path aPath) {
        Preconditions.checkArgument
                (null != aPath,
                        "A Path to the staging file directory is required");
        try {
            if(Files.notExists(aPath)) {
                Files.createDirectories(aPath);
                logger.info("Staging file path " +aPath +" created");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        }
        return true;

    }
}

