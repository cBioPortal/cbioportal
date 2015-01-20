package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import org.mskcc.cbio.importer.IDMapper;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;
import org.mskcc.cbio.importer.util.GeneSymbolIDMapper;
import scala.Tuple2;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

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
 * Created by criscuof on 12/20/14.
 */
public class IcgcFunctionLibrary {
    /*
    represents a collection of common Function and Predicates used to support
    ICGC data imports
     */

    public static String[] resolveFieldNames(Class modelClass){
        Field[] modelFields = modelClass.getDeclaredFields();
        String[] fieldnames = new String[modelFields.length];
        for (int i = 0; i < modelFields.length; i++) {
            fieldnames[i] = modelFields[i].getName();
        }
        return fieldnames;
    }
     /*
    transformation functions
     */

    protected static IDMapper geneMapper = new GeneSymbolIDMapper();
    /*
     function to calculate the reference allele count
     parameter 1 = total allele count
     parameter 2 mutant allele count
     */
    public static Function<Tuple2<String, String>, String> resolveReferenceCount
            = new Function<Tuple2<String,String>, String>() {

        @Override
        public String apply(Tuple2<String, String> f) {
            if (!Strings.isNullOrEmpty(f._1()) && !Strings.isNullOrEmpty(f._2())) {
                return new Integer(Integer.valueOf(f._1()) - Integer.valueOf(f._2()))
                        .toString();
            }
            return "";
        }
    };

    /*
     function to supply HUGO gene symbol via a table lookup
     public Tuple2<String,String> ensemblToHugoSymbolAndEntrezID(String ensemblID) {
     */

    public static Function <String,String> resolveGeneSymbol =
            new Function<String,String>() {
                @Nullable
                @Override
                public String apply(final String input) {
                    if (!Strings.isNullOrEmpty(input)) {
                        Tuple2<String,String> geneTuple = geneMapper.ensemblToHugoSymbolAndEntrezID(input);
                        return geneTuple._1();
                    }
                    return "";

                }
            };

    public static Function<String,String> resolveEntrezIdFromGeneName =
            new Function<String, String>() {
                @Nullable
                @Override
                public String apply(@Nullable String name) {
                    if(!Strings.isNullOrEmpty(name)) {
                        try {
                            return geneMapper.symbolToEntrezID(name);
                        } catch (Exception e) {
                          return "";
                        }
                    }
                    return "";
                }

            };

    public static  Function <String,String> resolveEntrezId =
            new Function<String,String>() {
                @Nullable
                @Override
                public String apply(final String input) {
                    if (!Strings.isNullOrEmpty(input)) {
                        Tuple2<String,String> geneTuple = geneMapper.ensemblToHugoSymbolAndEntrezID(input);
                        return geneTuple._2();
                    }
                    return  "";
                }
            };

    /*
     function to remove GRCh prefix from ICGC build value (GRCh37 -> 37)
     */
    public static  Function<String, String> resolveSimpleBuildNumber
            = new Function<String, String>() {
        @Override
        public String apply(final String f) {
            if (!Strings.isNullOrEmpty(f)) {
                return CharMatcher.DIGIT.retainFrom(f);
            }
            return "";
        }

    };

    public static  Function<String,String> resolveStrand = new Function<String,String>() {
        @Nullable
        @Override
        public String apply(@Nullable String strand) {
            if (strand.equals("1")){
                return "+";
            }
            return "-";
        }
    };

    public static  Function<Tuple2<String, String>, String> resolveVariantType
            = new Function<Tuple2<String, String>, String>() {
        /*
         determine variant type
         parm 1 is reference_genome_allele
         parm2 is mutated_to_allele
         both are required
         */
        @Override
        public String apply(Tuple2<String, String> f) {
            if (!Strings.isNullOrEmpty(f._1) && !Strings.isNullOrEmpty(f._2())) {
                String refAllele = f._1;
                String altAllele = f._2;
                if (refAllele.equals("-")) {
                    return StagingCommonNames.variationList.get(0);
                }
                if (altAllele.equals("-") || altAllele.length() < refAllele.length()) {
                    return "DEL";
                }
                if (refAllele.equals("-") || refAllele.length() < altAllele.length()) {
                    return "INS";
                }
                if (refAllele.length() < StagingCommonNames.variationList.size()) {
                    return StagingCommonNames.variationList.get(refAllele.length());
                }
            }
            return "UNK";
        }

    };


    /*
    function to generate a List of column names based on the convention of
    using key values prefixed by a 3 digit numeric value to control ordering
    (e.g. 001FirstColumn)
     */
    public static List<String> resolveColumnNames(Map<String,String> transformationMap) {
        return FluentIterable.from(transformationMap.keySet())
                .transform(new Function<String, String>() {
                    @Override
                    public String apply(String s) {
                        return (s.substring(3)); // strip off the three digit numeric prefix
                    }
                }).toList();
    }

}
