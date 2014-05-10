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

/**
 * Interface used to validate cancer study import.
 */
public interface Validator {

    /**
     * Validates all cancers studies fonud within the given directory.
     *
     * Validates:
     *
     * - meta_study.txt exists and is 'valid'
     * -- type of cancer is set
     * -- cancer study identifier is set
     * -- name is set
     * -- description is set
     *
     * - cancer_type.txt exists and is 'valid'
     * -- validates types of cancer id found in meta_study.txt
     *
     * - validates cancer study data:
     * -- for each metadata file found:
     * --- validates all properties are set
     * --- validates proper cancer study id
     * --- validates proper genetic alteration type
     * --- validates proper stable id (prefix matches cancer study id)
     * --- validates no duplicate stable ids
     * --- existence of staging file
     *
     * - validates case list directory exists and contains case lists
     * -- for each case list:
     * --- validates all properties are set
     * --- validates proper cancer study id
     * --- validates proper stable id (prefix matches cancer study id)
     * --- validates no duplicate stable ids
     *
     * @param cancerStudyDirectoryName
     */
    boolean validateCancerStudy(String cancerStudyDirectoryName) throws Exception;
}
