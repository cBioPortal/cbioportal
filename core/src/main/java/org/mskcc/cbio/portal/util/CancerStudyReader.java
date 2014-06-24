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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;

/**
 * Reads and loads a cancer study file. (Before July 2011, was called a cancer type file.)
 * By default, the loaded cancers are public. 
 * 
 * @author Arthur Goldberg goldberg@cbio.mskcc.org
 */
public class CancerStudyReader {

    public static CancerStudy loadCancerStudy(File file) throws IOException, DaoException {
        return loadCancerStudy(file, true, true);
    }

    public static CancerStudy loadCancerStudy(File file, boolean strict, boolean addStudyToDb) throws IOException, DaoException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));

        CancerStudy cancerStudy = getCancerStudy(properties);

        if (strict && null==DaoTypeOfCancer.getTypeOfCancerById(cancerStudy.getTypeOfCancerId())) {
            throw new IllegalArgumentException(cancerStudy.getTypeOfCancerId()+" is not a supported cancer type.");
        }

        if (addStudyToDb) {
            DaoCancerStudy.addCancerStudy(cancerStudy, true); // overwrite if exist
        }

        return cancerStudy;
    }

    private static CancerStudy getCancerStudy(Properties properties)
    {
        String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
        if (cancerStudyIdentifier == null) {
            throw new IllegalArgumentException("cancer_study_identifier is not specified.");
        }

        String name = properties.getProperty("name");
        if (name == null) {
            throw new IllegalArgumentException("name is not specified.");
        }

        String description = properties.getProperty("description");
        if (description == null) {
            throw new IllegalArgumentException("description is not specified.");
        }

        String typeOfCancer = properties.getProperty("type_of_cancer").toLowerCase();
        if ( typeOfCancer == null) {
            throw new IllegalArgumentException("type of cancer is not specified.");
        }
        
        String shortName = properties.getProperty("short_name");
        if ( typeOfCancer == null) {
            throw new IllegalArgumentException("short_name is not specified.");
        }

        CancerStudy cancerStudy = new CancerStudy(name, description, cancerStudyIdentifier,
                                                  typeOfCancer, publicStudy(properties));
        cancerStudy.setPmid(properties.getProperty("pmid"));
        cancerStudy.setCitation(properties.getProperty("citation"));
        cancerStudy.setGroups(properties.getProperty("groups"));
        cancerStudy.setShortName(shortName);

        return cancerStudy;
    }

    private static boolean publicStudy( Properties properties ) {
        String studyAccess = properties.getProperty("study_access");
        if ( studyAccess != null) {
            if( studyAccess.equals("public") ){
                return true;
            }
            if( studyAccess.equals("private") ){
                return false;
            }
            throw new IllegalArgumentException("study_access must be either 'public' or 'private', but is " + 
                                               studyAccess );
        }
        // studies are public by default
        return true;
    }
}