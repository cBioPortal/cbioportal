package org.mskcc.cbio.importer.cvr.darwin.transformer;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DSYNC;

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
 * Created by criscuof on 11/20/14.
 */
public abstract class DarwinDataTransformer {

    public static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    public abstract Integer transform();

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
        return tabJoiner.join(headerList);
    }

    protected String generateDarwinDataRecord(final Object obj) {
        List<String> valueList = FluentIterable.from(Lists.newArrayList(obj.getClass().getDeclaredMethods()))
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
                                return (String) method.invoke(obj);
                            } else {
                                Object value = method.invoke(obj);
                                return (null != value) ? value.toString() : "";
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                }).toList();

        return tabJoiner.join(valueList);
    }

    protected void generateStagingFile(Path stagingFilePath, List<String> dataList){
        OpenOption[] options = new OpenOption[]{ CREATE, APPEND, DSYNC};

        try {
            Files.deleteIfExists(stagingFilePath);
            Files.write(stagingFilePath, dataList, Charset.defaultCharset(),options);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
