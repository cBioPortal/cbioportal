

package org.mskcc.cbio.importer.foundation.support;

import com.google.common.base.Strings;
import com.google.inject.internal.Preconditions;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.mskcc.cbio.importer.CaseIDs;
import org.mskcc.cbio.importer.Config;
import org.mskcc.cbio.importer.FileUtils;
import org.mskcc.cbio.importer.caseids.internal.CaseIDsImpl;
import org.mskcc.cbio.importer.io.internal.FileUtilsImpl;
import org.mskcc.cbio.importer.model.CancerStudyMetadata;

/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 *
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
*/
/**
 * This class is responsible for creating the metadata files associated
 * with a FoundationMedicine cancer study
 * @author criscuof
 */
public class FoundationMetaDataGenerator {
    
    private final Config config;
    private final String outputDirectory;
    private final Logger logger = Logger.getLogger(FoundationMetaDataGenerator.class);
    
    private final FileUtils fileUtils;
    private final CaseIDs caseIDs;
    
    
    public FoundationMetaDataGenerator(Config aConfig, String aDirectory) {
        Preconditions.checkArgument(null != aConfig, "A Config implemntation is required");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(aDirectory), "An metadata file output directory is required");
        
        this.config = aConfig;
        this.outputDirectory = aDirectory;
        this.caseIDs = new CaseIDsImpl(this.config);
        this.fileUtils = new FileUtilsImpl(this.config,
                this.caseIDs);
    }
    
    public void generateCancerStudyMetaData(final String cancerStudyIdentifier, Integer numCases){
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cancerStudyIdentifier), 
                "A cancer study identifier is required");
        Preconditions.checkArgument(numCases > 1, "The number of cases must be > 0");
        List<String> cancerStudyList = config.findCancerStudiesBySubstring(cancerStudyIdentifier.toLowerCase());
        if(null == cancerStudyList || cancerStudyList.isEmpty()){
            logger.info("There are no cancer study names associated with: " +cancerStudyIdentifier);
            return;
        }
        if(cancerStudyList.size() > 1){
            logger.error("The cancer study name: " +cancerStudyIdentifier +" is not specific");
            for (String study : cancerStudyList){
                logger.info(study);
            }
        }
        try {
            // generate the metadata files
            this.generateStudyMetadataFile(cancerStudyList.get(0), numCases);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        
    }
    
    private void 
    generateStudyMetadataFile(String cancerStudy, Integer numCases) throws Exception{
        CancerStudyMetadata cancerMetadata = this.config.getCancerStudyMetadataByName(cancerStudy);
        if (null != cancerMetadata){
            this.fileUtils.writeCancerStudyMetadataFile(
			this.outputDirectory,
			cancerMetadata,
			numCases);
        }
    }
    
     

}
