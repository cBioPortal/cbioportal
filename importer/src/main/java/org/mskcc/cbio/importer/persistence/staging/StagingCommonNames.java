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
package org.mskcc.cbio.importer.persistence.staging;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * represents a collection of common values used throughout the importer
 * module
 * @author criscuof
 */
public interface StagingCommonNames {

    public static final String HUGO_COLUMNNAME = "Hugo_symbol";
    public static final String INTERGENIC = "intergenic";

    public static final String xmlExtension = "xml";

    public static final List<String> variationList = Lists.newArrayList("INS", "SNP", "DNP", "TNP", "ONP");

    public static final Splitter tabSplitter = Splitter.on("\t");
    public static final Splitter lineSplitter = Splitter.on("\n").trimResults();
    public static final Splitter blankSplitter = Splitter.on(" ");
    public static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    public static final Joiner commaJoiner = Joiner.on(',').useForNull(" ");
    public static final Joiner blankJoiner = Joiner.on(" ");
    public static final Joiner lineJoiner = Joiner.on("\n");
    public static final Splitter posSplitter = Splitter.on(':');
    public static final Splitter semicolonSplitter = Splitter.on(';');
    public final Joiner pathJoiner =
            Joiner.on(System.getProperty("file.separator"));
    public final Splitter pathSplitter =
            Splitter.on(System.getProperty("file.separator"));
    public final Pattern tabPattern = Pattern.compile("\t");

    // transformation types
    public static final String MUTATION_TYPE = "mutation";
    public static final String CLINICAL_TYPE = "clinical";

    public static final String STRUCTURAL_MUTATION_TYPE = "structural_mutation";
    // staging file names
    // mutations
    public static final String MUTATIONS_STAGING_FILENAME = "data_mutations_extended.txt";
    public static final String  MUTATIONS_METADATA_FILENAME = "meta_mutations_extended.txt";
    //copy number
    public static final String CNA_STAGING_FILENAME = "data_CNA.txt";
    public static final String CNA_METADATA_FILENAME = "meta_CNA.txt";
    // fusion data
    public static final String FUSION_STAGING_FILENAME ="data_fusions.txt";
    public static final String FUSION_METADATA_FILENAME = "meta_fusions.txt";
    //clinical
    public static final String CLINICAL_STAGING_FILENAmE = "data_clinical.txt";

    // Datasource names
    public static final String DATA_SOURCE_ICGC = "icgc";
    public static final String DATA_SOURCE_FOUNDATION = "foundation";
    public static final String DATA_SOURCE_DMP = "dmp-darwin-mskcc";

    // default base directory
    public static final String DEFAULT_BASE_DIRECTORY = "/tmp/cbio-portal-data";

    //suffix for filtered foundation studies
    public static final String FOUNDATION_FILTERED_NOTATION = "-filtered";

    // validation status values
    public static final String VALIDATION_STATUS_UNKNOWN = "Unknown";
    public static final String VALIDATION_STATUS_VALID = "Valid";

    // IMPACT study
    public static final String IMPACT_STUDY_IDENTIFIER = "mskimpact";
    public static final String DMP_COMMENT_MARKER = "#";
    public static final String DMP_STAGING_FILE_COMMENT = "#sequenced_samples:";



    // length of human chromosomes
    // http://www.ncbi.nlm.nih.gov/projects/genome/assembly/grc/human/data/
    public static final ImmutableMap<String, Long> chromosomeLengthMap = new ImmutableMap.Builder<String, Long>()
            .put("1", Long.valueOf(248_956_422))
                    .put("2", Long.valueOf(242_193_529))
                    .put("3", Long.valueOf(198_295_559))
                    .put("4", Long.valueOf(190_214_555))
                    .put("5", Long.valueOf(181_538_259))
                    .put("6", Long.valueOf(170_805_979))
                    .put("7", Long.valueOf(159_345_973))
                    .put("8", Long.valueOf(145_138_636))
                    .put("9", Long.valueOf(138_394_717))
                    .put("10", Long.valueOf(133_797_42))
                    .put("11", Long.valueOf(135_086_622))  // this is correct
                    .put("12", Long.valueOf(133_75_309))
                    .put("13", Long.valueOf(114_364_328))
                    .put("14", Long.valueOf(107_043_718))
                    .put("15", Long.valueOf(101_991_189))
                    .put("16", Long.valueOf(90_338_345))
                    .put("17", Long.valueOf(83_257_441))
                    .put("18", Long.valueOf(80_373_285))
                    .put("19", Long.valueOf(58_617_616))
                    .put("20", Long.valueOf(64_444_167))
                    .put("21", Long.valueOf(46_709_983))
                    .put("22", Long.valueOf(50_818_468))
                    .put("X", Long.valueOf(156_040_895))
                    .put("Y", Long.valueOf(57_227_415)).build();
    // valid chromosome values
    public static final Set<String> validChromosomeSet = Sets.newHashSet("1","2","3","4","5","6",
           "7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","X","Y",
           "x","y" );
}