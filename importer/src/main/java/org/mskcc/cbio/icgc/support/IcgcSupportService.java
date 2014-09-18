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
package org.mskcc.cbio.icgc.support;

import com.google.common.base.Optional;
import com.google.gdata.util.common.base.Preconditions;
import joptsimple.internal.Strings;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.FileTransformer;
import org.mskcc.cbio.importer.transformer.ICGCSimpleSomaticFileTransformer;

/*
Singleton responsible for providing utility methods to ICGC import 
components
*/
/**
 *
 * @author criscuof
 */
public enum IcgcSupportService {
    INSTANCE;
    private static final Logger logger = Logger.getLogger(IcgcSupportService.class);
    
    public final String SIMPLE_SOMATIC_MUTATION_TYPE = "simple_somatic_mutation.open";
    public final String COPY_NUMBER_SOMATIC_MUTATION_TYPE = "copy_number_somatic_mutation";
    public final String STRUCTURAL_SOMATIC_MUTATION_TYPE = "structural_somatic_mutation";
    public final String CLINICALSAMPLE_TYPE = "clinicalsample";
    private final String MUTATION_TYPE = "MUTATION_TYPE";
     public final String icgcBaseUrlTemplate
            = "https://dcc.icgc.org/api/v1/download?fn=/current/Projects/PROJECT/MUTATION_TYPE.PROJECT.tsv.gz";
     
    
    public Optional<FileTransformer> getFileTransformerByMutationType(String icgcMutationType){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(icgcMutationType), 
                "A valid ICGC mutation Type is required");
        if (icgcMutationType.equals(this.SIMPLE_SOMATIC_MUTATION_TYPE)) {
            FileTransformer ft =  new ICGCSimpleSomaticFileTransformer();
            return Optional.of(ft);
        }
        logger.error("A FileTransformer for mutation type " + icgcMutationType +" is not supported");
        return Optional.absent();
    }
    
    public String getClinicalSampleBaseUrl() {
        return this.icgcBaseUrlTemplate.replaceAll(this.MUTATION_TYPE, this.CLINICALSAMPLE_TYPE);
    }
    public String getSimpleSomaticBaseUrl() {
        return this.icgcBaseUrlTemplate.replaceAll(this.MUTATION_TYPE, this.SIMPLE_SOMATIC_MUTATION_TYPE);
    }
    
}
