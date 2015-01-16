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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/*
 represents a collection of static utility methods used though out the application
 */
public class StagingUtils {

    private static final Logger logger = Logger.getLogger(StagingUtils.class);

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
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
        return "";
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
       // com.google.common.base.Preconditions.checkArgument
         //       (Files.isDirectory(aPath, LinkOption.NOFOLLOW_LINKS),
        //                "The specified Path: " + aPath + " is not a directory");
      //  com.google.common.base.Preconditions.checkArgument
       //         (Files.isWritable(aPath),
       //                 "The specified Path: " + aPath + " is not writable");
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
}

