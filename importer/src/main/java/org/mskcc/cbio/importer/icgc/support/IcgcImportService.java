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
package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.gdata.util.common.base.Preconditions;
import joptsimple.internal.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.icgc.transformer.IcgcFileTransformer;
import org.mskcc.cbio.importer.persistence.staging.StagingCommonNames;

import java.util.Map;

/*
 Singleton responsible for providing utility methods to ICGC import 
 components
 */
/**
 *
 * @author criscuof
 */
public enum IcgcImportService {

    INSTANCE;
    private static final Logger logger = Logger.getLogger(IcgcImportService.class);

    public final String SIMPLE_SOMATIC_MUTATION_TYPE = "simple_somatic_mutation.open";
    public final String COPY_NUMBER_SOMATIC_MUTATION_TYPE = "copy_number_somatic_mutation";
    public final String STRUCTURAL_SOMATIC_MUTATION_TYPE = "structural_somatic_mutation";
    public final String CLINICALSAMPLE_TYPE = "clinicalsample";
    public final String CLINICAL_TYPE = "clinical";
    public final String MUTATION_TYPE = "MUTATION_TYPE";
    public final String icgcProjectUrlTemplate
            = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/PROJECT/MUTATION_TYPE.PROJECT.tsv.gz";
    private static final String US = "US";


    private Map<String, String> mutationMap = Suppliers.memoize(new IcgcMutationURLSupplier()).get();
    private Map<String,String> clinicalMap = Suppliers.memoize(new IcgcClinicalURLSupplier()).get();

    public Map<String, String> getIcgcMutationUrlMap() {
        return this.mutationMap;
    }

    public Map<String,String> getIcgcClinicalUrlMap() {return this.clinicalMap; }

    /*
    return the appropriate type of transformer based on the ICGC file type
    return object is encapsulated within an Optional so caller is aware
    that not all calls will return a transformer instance
    */
    public Optional<IcgcFileTransformer> getFileTransformerByMutationType(String icgcMutationType) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(icgcMutationType),
                "A valid ICGC mutation Type is required");
        switch (icgcMutationType) {
            case SIMPLE_SOMATIC_MUTATION_TYPE:
                //return Optional.of((IcgcFileTransformer) new SimpleSomaticFileTransformer());
            //case CLINICALSAMPLE_TYPE:
             //   return Optional.of((IcgcFileTransformer) new ClinicalDataFileTransformer());
            default:
                logger.error("A FileTransformer for mutation type " + icgcMutationType + " is not supported");
                return Optional.absent();
                
        }
            
    }

    public Predicate usStudyFilter = new Predicate<String>() {
        public boolean apply(String t) {
            return (!(t.endsWith(US)) && !com.google.common.base.Strings.isNullOrEmpty(t));
        }

    };

    public String getClinicalSampleBaseUrl() {
        return this.icgcProjectUrlTemplate.replaceAll(this.MUTATION_TYPE, this.CLINICALSAMPLE_TYPE);
    }

    public String getSimpleSomaticBaseUrl() {
        return this.icgcProjectUrlTemplate.replaceAll(this.MUTATION_TYPE, this.SIMPLE_SOMATIC_MUTATION_TYPE);
    }

    public String getStructuralSomaticBaseUrl() {
        return this.icgcProjectUrlTemplate.replaceAll(this.MUTATION_TYPE, this.STRUCTURAL_SOMATIC_MUTATION_TYPE);
    }

    public String getCopyNumberSomaticBaseUrl() {
        return this.icgcProjectUrlTemplate.replaceAll(this.MUTATION_TYPE, this.COPY_NUMBER_SOMATIC_MUTATION_TYPE);
    }

    private class IcgcMutationURLSupplier implements Supplier<Map<String, String>> {

        public final String icgcMutationUrlTemplate
                = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/STUDY/MUTATION_TYPE.STUDY.tsv.gz";

        private final String STUDY_FLAG = "STUDY";

        public IcgcMutationURLSupplier() {

        }

        @Override
        public Map<String, String> get() {
            return FluentIterable.from(IcgcMetadataService.INSTANCE.getRegisteredIcgcStudyList())
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String f) {
                            return StagingCommonNames.blankSplitter.splitToList(f).get(0);
                        }
                    }).toMap(new Function<String, String>() {

                        @Override
                        public String apply(String f) {
                            String template = icgcMutationUrlTemplate.replaceAll(MUTATION_TYPE, SIMPLE_SOMATIC_MUTATION_TYPE);
                            return template.replaceAll(STUDY_FLAG, f);
                        }
                    });
        }
    }

    private class IcgcClinicalURLSupplier implements Supplier<Map<String, String>> {

        public final String icgcMutationUrlTemplate
                = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/STUDY/MUTATION_TYPE.STUDY.tsv.gz";

        private final String STUDY_FLAG = "STUDY";

        public IcgcClinicalURLSupplier() {

        }

        @Override
        public Map<String, String> get() {
            return FluentIterable.from(IcgcMetadataService.INSTANCE.getRegisteredIcgcStudyList())
                    .transform(new Function<String, String>() {
                        @Override
                        public String apply(String f) {
                            return StagingCommonNames.blankSplitter.splitToList(f).get(0);
                        }
                    }).toMap(new Function<String, String>() {

                        @Override
                        public String apply(String f) {
                            String template = icgcMutationUrlTemplate.replaceAll(MUTATION_TYPE, CLINICAL_TYPE);
                            return template.replaceAll(STUDY_FLAG, f);
                        }
                    });
        }
    }

     /*
    main method for testing
     */

    public static void main(String... args) {
        // mutation urls
        Map<String, String> mutationMap = IcgcImportService.INSTANCE.getIcgcMutationUrlMap();
        for (Map.Entry<String, String> entry : mutationMap.entrySet()) {
            System.out.println("study: " + entry.getKey() + "  url " + entry.getValue());
        }
        // clinical urls
        Map<String, String> clinicalMap = IcgcImportService.INSTANCE.getIcgcClinicalUrlMap();
        for (Map.Entry<String, String> entry : clinicalMap.entrySet()) {
            System.out.println("study: " + entry.getKey() + "  url " + entry.getValue());
        }
    }

}
