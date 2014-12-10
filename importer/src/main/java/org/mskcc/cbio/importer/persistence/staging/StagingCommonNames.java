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
import com.google.common.collect.Lists;

import java.util.List;

/**
 *
 * @author criscuof
 */
public interface StagingCommonNames {

    public static final String HUGO_COLUMNNAME = "Hugo_symbol";
    public static final String CNV_FILENAME = "data_CNA.txt";
    public static final String MUTATIONS_FILENAME = "data_mutations_extended.txt";
    public static final String CASE_STUDY_FILENAME = "data_case_study.txt";
    public static final String segmentFileBaseName = "_data_cna_hg.seg";

    public static final List<String> variationList = Lists.newArrayList("INS", "SNP", "DNP", "TNP", "ONP");

    public static final Splitter tabSplitter = Splitter.on("\t");
    public static final Splitter lineSplitter = Splitter.on("\n").trimResults();
    public static final Splitter blankSplitter = Splitter.on(" ");
    public static final Splitter scSplitter = Splitter.on(";");
    public static final Joiner scJoiner = Joiner.on(";");
    public static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    public static final Joiner commaJoiner = Joiner.on(',').useForNull(" ");
    public static final Joiner blankJoiner = Joiner.on(" ");
    public static final Splitter posSplitter = Splitter.on(':');
    public final Joiner pathJoiner =
            Joiner.on(System.getProperty("file.separator"));
    public final Splitter pathSplitter =
            Splitter.on(System.getProperty("file.separator"));
}
