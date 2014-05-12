/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

// package
package org.mskcc.cbio.importer;

// imports
import org.mskcc.cbio.importer.model.ReferenceMetadata;

/**
 * Interface used to import portal data.
 */
public interface Importer {

	/**
	 * Imports data for use in the given portal.
	 *
     * @param portal String
	 * @param initPortalDatabase Boolean
	 * @param initTumorTypes Boolean
	 * @param importReferenceData Boolean
	 * @throws Exception
	 */
	void importData(String portal, Boolean initPortalDatabase, Boolean initTumorTypes, Boolean importReferenceData) throws Exception;

	/**
	 * Imports the given reference data.
	 *
     * @param referenceMetadata ReferenceMetadata
	 * @throws Exception
	 */
	void importReferenceData(ReferenceMetadata referenceMetadata) throws Exception;

        /**
	 * Imports tumor type metadata.
	 */
        void importTypesOfCancer() throws Exception;
        
        void importCaseLists(String portal) throws Exception;
        
    /**
     * Imports all cancer studies found within the given directory.
     * If force is set, user will not be prompted to override existing cancer study.
     * If cancer study exists and skip is set, new study will not be imported.
     *
     * @param cancerStudyDirectoryName
     * @param skip
     * @param force

     */
    void importCancerStudy(String cancerStudyDirectoryName, boolean skip, boolean force) throws Exception;
}
