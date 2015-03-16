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

import com.google.common.base.*;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import edu.stanford.nlp.util.StringUtils;
import org.mskcc.cbio.importer.config.internal.ImporterSpreadsheetService;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Class which contains metadata relating to Foundation Medicine cancer studies
 */
public class FoundationMetadata {

    public static final String worksheetName = MetadataCommonNames.Worksheet_Foundation;
    public static final String idColumn = "cancerstudy";
    public static final String dependeciesColumnName = "dependencies";
    public static final String cancerStudyColumnName = "cancerstudy";
    // instantiate a map of the foundation worksheet
    public static final Table<Integer,String,String> foundationWorksheetTable =
            ImporterSpreadsheetService.INSTANCE.getWorksheetTableByName(worksheetName);

    // bean properties
    final private String cancerStudy;
    final private List<String> dependencies;
    final private String comments;
    final private List<String> excludedCases;
    final private List<String> shortVariantExcludedStatuses;
    final private List<String> cnvExcludedStatuses;
    final private String filteredStudy;

    /**
     * Create a FoundationMetadata instance with properties in given array. Its
     * assumed order of properties is that from google worksheet.
     * A minimum of four (4) properties are required:
     *  1. cancer study name
     *  2. >= 1 cancer study dependencies
     *  3. study description
     *  4. MSKCC contact
     *
     * @param properties String[]
     */
    public FoundationMetadata(String[] properties) {
        Preconditions.checkArgument(null != properties, "No properties have been provided");
        Preconditions.checkArgument(properties.length >= 3, "Insufficient number of properties provided");
        this.cancerStudy = properties[0].trim();
        this.dependencies = Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(properties[1]));
        this.comments = properties[2];

        // only a limited number of studies have excluded cases; most will not have that property
        if (properties.length >3) {
            this.excludedCases = Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(properties[3]));

        }else {
            this.excludedCases = Lists.newArrayList(); // an empty list
        }
        if (properties.length > 4){
            this.shortVariantExcludedStatuses = Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(properties[4]));
        } else {
            this.shortVariantExcludedStatuses = Lists.newArrayList();
        }
        if (properties.length > 5){
            this.cnvExcludedStatuses = Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(properties[5]));
        } else {
            this.cnvExcludedStatuses = Lists.newArrayList();
        }
        if (properties.length > 6){
            this.filteredStudy = properties[6].trim();
        } else {
            this.filteredStudy = "";
        }
    }

    /*
    Constructor uses a row from the foundation worksheet on the importer Google spreadsheet
     */

    public FoundationMetadata(Map<String,String> worksheetRowMap){
        this.cancerStudy = worksheetRowMap.get("cancerstudy").trim();
        this.dependencies = Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(
                worksheetRowMap.get("dependencies")));
        this.comments = worksheetRowMap.get("comments").trim();
        this.excludedCases =  Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(
                worksheetRowMap.get("excludedcases")));
        this.shortVariantExcludedStatuses =  Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(
                worksheetRowMap.get("excludedsvstatuses")));
        this.cnvExcludedStatuses =  Lists.newArrayList(StagingCommonNames.semicolonSplitter.split(
                worksheetRowMap.get("excludedcvstatuses")));
        this.filteredStudy = worksheetRowMap.get("filteredstudy").trim();
    }

    public String getCancerStudy() {
        return this.cancerStudy;
    }

    public List<String> getCnvExcludedStatuses() { return this.cnvExcludedStatuses;}

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public String getComments() { return this.comments;}

    public List<String> getExcludedCases() {return this.excludedCases;}

    public List<String> getShortVariantExcludedStatuses() {
        return shortVariantExcludedStatuses;
    }

    public String getFilteredStudy() { return this.filteredStudy;}

    
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
    NEGATIVE filter for excluded short variant statuses
    returns short variant records whose status is NOT in the list
     */
    private final Predicate <String> includedShortVariantStatusFilter = new Predicate<String>() {
        @Override
        public boolean apply(@Nullable String svStatus) {
            return !shortVariantExcludedStatuses.contains(svStatus);
        }
    };

    public Predicate<String> getIncludedShortVariantStatusFilter () { return this.includedShortVariantStatusFilter; }

    /*
     predicate to determine if a filename contains one of the dependency values
     this is identify the cancer study associated with non-standard filenames
     received from FMI
     */
    private final Predicate<String> relatedFileFilter = new Predicate<String>() {

        @Override
        public boolean apply(final String filename) {
            return FluentIterable.from(dependencies)
                    .anyMatch(new Predicate<String>() {
                        @Override
                        public boolean apply(final String dependency) {
                            // mod to accommodate name collisions with  -filtered files
                            if (!dependency.contains(StagingCommonNames.FOUNDATION_FILTERED_NOTATION)
                                    && filename.contains(StagingCommonNames.FOUNDATION_FILTERED_NOTATION)){
                                return false;
                            }
                            return filename.contains(dependency);
                        }
                    });
        }
    };
    
    public Predicate<String> getRelatedFileFilter() {
        return this.relatedFileFilter;
    }

    /*
    public static method to find and instantiate a FoundationMetadata object based on a
    supplied link to a registered filtered study
    a filtered study represents a copy of another study with the exclusion of all variants of
     unknown status
     */
    public static Optional<FoundationMetadata> findFilteredStudyMetaData (FoundationMetadata baseStudy){
        if (null == baseStudy || Strings.isNullOrEmpty(baseStudy.getFilteredStudy())){
            return Optional.absent();
        }
        return findFoundtaionMetadataByStudyName(baseStudy.getFilteredStudy());
    }

    /*
    public static method to find and instantiate a FoundationMetadata object based on a
    registered Foundation Medicine study name
    the return object is encapsulated in an Optional to deal with cases where the specified
    study name was not found
     */
    public static Optional<FoundationMetadata> findFoundtaionMetadataByStudyName(String studyName){
        if(Strings.isNullOrEmpty(studyName)) { return Optional.absent();}

        return Optional.of(new FoundationMetadata(ImporterSpreadsheetService.INSTANCE.
                getWorksheetRowByColumnValue(worksheetName, cancerStudyColumnName, studyName).get()));

    }

    /*
    static method to return a FoundationMetadata instance based on a dependency value
     */
    public static Optional<FoundationMetadata> findFoundationMetadataByDependency(String dependency){
        if(Strings.isNullOrEmpty(dependency)){ return Optional.absent(); }
        Map<Integer,String> dependenciesColumnMap = foundationWorksheetTable.column(dependeciesColumnName);
        for (Map.Entry<Integer,String> entry : dependenciesColumnMap.entrySet()){
            if ( entry.getValue().contains(dependency)){
                return  Optional.of(new FoundationMetadata(foundationWorksheetTable.row(entry.getKey())));
            }
        }
        return Optional.absent();
    }

    /*
  static method to return a FoundationMetadata instance based on a file name value
   */
    public static Optional<FoundationMetadata> findFoundationMetadataByXmlFileName(String filename){
        if(Strings.isNullOrEmpty(filename)){ return Optional.absent(); }
        Map<Integer,String> dependenciesColumnMap = foundationWorksheetTable.column(dependeciesColumnName);
        for (Map.Entry<Integer,String> entry : dependenciesColumnMap.entrySet()){
          for (String dependency : StagingCommonNames.semicolonSplitter.splitToList(entry.getValue()) ) {
              if (StringUtils.getBaseName(filename, StagingCommonNames.xmlExtension).contains(dependency)) {
                  return Optional.of(new FoundationMetadata(foundationWorksheetTable.row(entry.getKey())));
              }
          }
        }
        return Optional.absent();
    }

    /*
     main method for stand alone testing
     */

    public static void main(String... args) {
        String[] testProperties1 = {"BRCA1/BRCA2 Study", "15-089;20-123","XYZ,ABC", "unknown"};
        String[] testProperties2 = {"BRCA1/BRCA2 Study", "15-089;20-123","unknown"};
        String[] testProperties3 = {"LBCL Study", "13-081;13-158","unknown"};
        FoundationMetadata meta1 = new FoundationMetadata(testProperties1);
        FoundationMetadata meta2 = new FoundationMetadata(testProperties2);
        FoundationMetadata meta3 = new FoundationMetadata(testProperties3);
        final List<FoundationMetadata> metaList = Lists.newArrayList(meta1, meta2);
        String[] inputFiles = {"/test/XXX_15-089_brca.xml", "/test/xyz.xml",
                "/data/cbio/20-123.xml", "cvftrxnkol", "/tmp/fgghhh13-158.xml"};

        String[] statuses = {"unknown", "likely", "known", "unknown"};
        final List<String> validStatusList = FluentIterable.from(Lists.newArrayList(statuses))
                .filter(meta1.getIncludedShortVariantStatusFilter())
                .toList();
        for (String s : validStatusList) {
            System.out.println("Valid status " + s);
        }
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
                    System.out.println("File " + f + " relates to study " + s);
                }

            }

        /*
        test using the google worksheet constructor
         */
        Optional<FoundationMetadata> opt = FoundationMetadata.findFoundationMetadataByDependency("13-084");
        if (opt.isPresent()){
            System.out.println("Found related study " +opt.get().getCancerStudy() + " comments: " +opt.get().getComments());
        }

        Optional<FoundationMetadata> opt2 = FoundationMetadata.findFoundationMetadataByDependency("HEME-COMPLETE");
        if (opt2.isPresent()){
            System.out.println("Found related study " +opt2.get().getCancerStudy()+ " comments: " +opt2.get().getComments());
        }



        Optional<FoundationMetadata> opt3 = FoundationMetadata.findFoundtaionMetadataByStudyName("lymphoma/mskcc/foundation");
        if (opt3.isPresent()) {
            System.out.println("Found metadata by study " +opt3.get().getCancerStudy());
            // find filtered study
            Optional<FoundationMetadata> opt4 = FoundationMetadata.findFilteredStudyMetaData(opt3.get());
            if (opt4.isPresent()){
                System.out.println("Found filtered study: " +opt4.get().getCancerStudy());
            }
        }

        Optional<FoundationMetadata> opt4 = FoundationMetadata.findFoundationMetadataByXmlFileName("/tmp/clinical-heme-complete-filtered.xml");
        if(opt4.isPresent()){
            System.out.println("Found metadata by file " +opt4.get().getCancerStudy() );
        }
            System.out.println("Finis");
    }


}
