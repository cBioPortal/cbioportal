package org.mskcc.cbio.importer.cvr.crdb.transformer;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.cvr.dmp.util.DmpLegacyIdResolver;
import org.mskcc.cbio.importer.model.ClinicalAttributesNamespace;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/21/14.
 */
public abstract class CrdbTransformer {

    private final Path stagingFilePath;
    private static final Logger logger = Logger.getLogger(CrdbTransformer.class);
    private String columnHeaderLine = "";

    protected CrdbTransformer(Path aPath){
        Preconditions.checkArgument(null != aPath, "A Path to a staging file is required");
        this.stagingFilePath = aPath;
    }

    protected void setColumnHeaderLine(List<String> chList){
        this.columnHeaderLine = StagingCommonNames.tabJoiner.join(chList);
    }


    public abstract void transform();

    public abstract List<String> generateReportByPatientId(String patientId);

    public abstract List<String> generateReportByPatientIdList(List<String> patientIdList);

    protected String generateColumnHeaders(final Class aClass){
        List<String> headerList = FluentIterable.from(Lists.newArrayList(aClass.getDeclaredMethods()))
                .filter(new Predicate<Method>() {
                    @Override
                    public boolean apply(Method method) {
                        return method.getName().startsWith("get");
                    }
                })
                .transform(new Function<Method, String>() {
                    @Override
                    public String apply(Method method) {
                        return method.getName().substring(3);
                    }
                })
                .toList();

        return StagingCommonNames.tabJoiner.join(this.normalizeHeaders(headerList));
    }

    private List<String> normalizeHeaders(List<String> externalHeaderList){
      return  FluentIterable.from(externalHeaderList)
               .transform(new Function<String,String>(){
                   @Nullable
                   @Override
                   public String apply(@Nullable String extHeader) {
                       Optional<ClinicalAttributesNamespace> metaOpt =
                               ClinicalAttributesNamespace.findClinicalAttributeNamespaceByExternalColumnHeader(extHeader);
                       if(metaOpt.isPresent()) {
                           return metaOpt.get().getNormalizedColumnHeader();
                   }
                       logger.info("External Column Header: " + extHeader +" could not be normalzed");
                       return extHeader;
                   }
               }).toList();


    }

    protected void writeStagingFile( List<String> dataList,Class c){
        OpenOption[] options = new OpenOption[]{ CREATE, APPEND, DSYNC};
        try {
            Files.deleteIfExists(this.stagingFilePath);

            Files.write(stagingFilePath,Lists.newArrayList(this.generateColumnHeaders(c)),Charset.defaultCharset(),options);
            Files.write(this.stagingFilePath, Lists.transform(dataList,transformationFunction), Charset.defaultCharset(),options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   protected void writeStagingFile( List<String> dataList){
        OpenOption[] options = new OpenOption[]{ CREATE, APPEND, DSYNC};
        try {
            Files.deleteIfExists(this.stagingFilePath);
            Files.write(this.stagingFilePath, dataList, Charset.defaultCharset(),options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    Function to replace legacy DMP patient id with new CVR id
     */
    final Function<String,String> transformationFunction = new Function<String,String>(){
        @Nullable
        @Override
        public String apply(@Nullable String input) {
            String id = StagingCommonNames.tabSplitter.splitToList(input).get(0);
            if ( DmpLegacyIdResolver.INSTANCE
                        .resolveNewPatientIdFromLegacyPatientId(id).isPresent()){
                return input.replace(id,DmpLegacyIdResolver.INSTANCE
                        .resolveNewPatientIdFromLegacyPatientId(id).get());

            }
            return input;
        }
    };

    protected List<String> generateStagingFileRecords(final List<CrdbPatientTransformer.CrdbPatient> patientList){
        return FluentIterable.from(patientList)
                .transform(new Function<CrdbPatientTransformer.CrdbPatient, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable final CrdbPatientTransformer.CrdbPatient o) {
                        List<String> valueList = FluentIterable.from(Lists.newArrayList(o.getClass().getDeclaredMethods()))
                                .filter(new Predicate<Method>() {
                                    @Override
                                    public boolean apply(@Nullable Method method) {

                                        return method != null && method.getName().startsWith("get");
                                    }
                                })
                                .transform(new Function<Method, String>() {
                                    @Override
                                    public String apply(Method method) {
                                        try {
                                            if (method.getReturnType() == String.class) {
                                                Object value = method.invoke(o);
                                                return (null != value) ? value.toString() : "";
                                            } else if (method.getReturnType() == Short.class) {
                                                Object value = method.invoke(o);
                                                return (null != value) ? Short.toString((Short) value) : "";
                                            } else {
                                                Object value = method.invoke(o);
                                                return (null != value) ? value.toString() : "";
                                            }
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                        return "";
                                    }
                                }).toList();
                        return StagingCommonNames.tabJoiner.join(valueList);
                    }
                }).toList();
    }

    /*
    protected method to transform a list of Objects to a List of tab delimited strings
    the declared getter methods in each object are invoked and the individual values are
    concatenated to form a tsv record
     */
    protected List<String> generateStagingFileRecords(final List<Object> objectList){
        return FluentIterable.from(objectList)
                .transform(new Function<Object, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nullable final Object o) {
                        List<String> valueList = FluentIterable.from(Lists.newArrayList(o.getClass().getDeclaredMethods()))
                                .filter(new Predicate<Method>() {
                                    @Override
                                    public boolean apply(@Nullable Method method) {

                                        return method != null && method.getName().startsWith("get");
                                    }
                                })
                                .transform(new Function<Method, String>() {
                                    @Override
                                    public String apply(Method method) {
                                        try {
                                            if (method.getReturnType() == String.class) {
                                                Object value = method.invoke(o);
                                                return (null != value) ? value.toString() : "";
                                            } else if (method.getReturnType() == Short.class) {
                                                Object value = method.invoke(o);
                                                return (null != value) ? Short.toString((Short) value): "";
                                            } else {
                                                Object value = method.invoke(o);
                                                return (null != value) ? value.toString() : "";
                                            }
                                        } catch (IllegalAccessException | InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                        return "";
                                    }
                                }).toList();
                        return StagingCommonNames.tabJoiner.join(valueList);
                    }
                }).toList();
    }

    protected String generateStagingFileRecord(final Object object) {
        List<String> valueList = FluentIterable.from(Lists.newArrayList(object.getClass().getDeclaredMethods()))
                .filter(new Predicate<Method>() {
                    @Override
                    public boolean apply(Method method) {
                        return method.getName().startsWith("get");
                    }
                })
                .transform(new Function<Method, String>() {
                    @Override
                    public String apply(Method method) {
                        try {
                            if (method.getReturnType() == String.class) {
                                return (String) method.invoke(object);
                            } else {
                                Object value = method.invoke(object);
                                return (null != value) ? value.toString() : "";
                            }
                        } catch (IllegalAccessException  | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                }).toList();

        return StagingCommonNames.tabJoiner.join(valueList);
    }
}
