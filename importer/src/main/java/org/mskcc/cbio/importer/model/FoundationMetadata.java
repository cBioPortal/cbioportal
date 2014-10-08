/**
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
 */
package org.mskcc.cbio.importer.model;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;


/**
 * Class which contains metadata relating to Foundation Medicine cancer studies
 */
public class FoundationMetadata {

    // delimiter between foundation studies entries and excluded cases
    public static final Splitter semicolonSplitter = Splitter.on(';');

    // bean properties
    final private String cancerStudy;
    final private List<String> dependencies;
    final private List<String> excludedCases;

    /**
     * Create a FoundationMetadata instance with properties in given array. Its
     * assumed order of properties is that from google worksheet.
     *
     * @param properties String[]
     */
    public FoundationMetadata(String[] properties) {
        Preconditions.checkArgument(null != properties, "No properties have been provided");
        Preconditions.checkArgument(properties.length >= 2, "Insufficient number of properties provided");
        this.cancerStudy = properties[0].trim();
        this.dependencies = Lists.newArrayList(semicolonSplitter.split(properties[1]));
        // only a limited number of studies have excluded cases; most wil not have that property
        if (properties.length >2) {
            this.excludedCases = Lists.newArrayList(semicolonSplitter.split(properties[2]));
        }else {
            this.excludedCases = Lists.newArrayList(); // an empty list
        }

    }

    public String getCancerStudy() {
        return this.cancerStudy;
    }

    List<String> getDependencies() {
        return this.dependencies;
    }
    
    List<String> getExcludedCases() {return this.excludedCases;}
    
    /*
    A NEGATIVE filter for excluded case ids
    returns TRUE for cases that are NOT in the list of excluded cases
    */
    private final Predicate<String> includedCaseFilter = new Predicate<String>() {
        @Override
        public boolean apply(String caseId) {
            return !excludedCases.contains(caseId);
        }
        
    };
    
    public Predicate<String> getIncludedCaseFilter() {
        return includedCaseFilter;
    }

    /*
     predicate to determine if a filename contains one of the dependency values
     this is identify the cancer study assocaited with non-standard filenames
     received from FMI
     */
    private final Predicate<String> relatedFileFilter = new Predicate<String>() {

        @Override
        public boolean apply(final String filename) {
            return FluentIterable.from(dependencies)
                    .anyMatch(new Predicate<String>() {
                        @Override
                        public boolean apply(final String dependency) {
                            return filename.contains(dependency);
                        }
                    });

        }
    ;

    };
    
    public Predicate<String> getRelatedFileFilter() {
        return this.relatedFileFilter;
    }
    /*
     main method for stand alone testing
     */

    public static void main(String... args) {
        String[] testProperties1 = {"BRCA1/BRCA2 Study", "15-089;20-123"};
        String[] testProperties2 = {"LBCL Study", "13-081;13-158"};
        FoundationMetadata meta1 = new FoundationMetadata(testProperties1);
        FoundationMetadata meta2 = new FoundationMetadata(testProperties2);
        final List<FoundationMetadata> metaList = Lists.newArrayList(meta1, meta2);
        String[] inputFiles = {"/test/XXX_15-089_brca.xml", "/test/xyz.xml",
            "/data/cbio/20-123.xml", "cvftrxnkol", "/tmp/fgghhh13-158.xml"};
        final List<String> brcaFileList = FluentIterable.from(Arrays.asList(inputFiles))
                .filter(meta1.getRelatedFileFilter())
                .toList();

        for (String f : brcaFileList) {
            System.out.println(f);
        }

        for (String f : inputFiles) {
            final List<String> fileList = Lists.newArrayList(f);
            List<String> affectedList = FluentIterable.from(metaList)
                    .filter(new Predicate<FoundationMetadata>() {

                        @Override
                        public boolean apply(final FoundationMetadata meta) {
                            List<String> fl = FluentIterable.from(fileList).filter(meta.getRelatedFileFilter()).toList();
                            return (!fl.isEmpty());

                        }
                    }).transform(new Function<FoundationMetadata, String>() {

                        @Override
                        public String apply(FoundationMetadata f) {
                            return f.getCancerStudy();
                        }
                    }).toList();
            for (String s : affectedList) {
                System.out.println("File " +f + " relates to study " +s);
            }

        }

        System.out.println("Finis");

    }
}
