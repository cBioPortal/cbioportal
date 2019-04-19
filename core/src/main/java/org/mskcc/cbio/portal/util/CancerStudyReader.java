/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoReferenceGenome;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.scripts.TrimmedProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    	TrimmedProperties properties = new TrimmedProperties();
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

    private static Boolean checkSpecies(String studyId, String genomeName) {
        if (genomeName == null || genomeName == "") {
            return true;
        }
        try {
            CancerStudy oldCancerStudy = DaoCancerStudy.getCancerStudyByStableId(studyId);
            ReferenceGenome referenceGenome = DaoReferenceGenome.getReferenceGenomeByGenomeName(
                                oldCancerStudy.getReferenceGenome());
            return referenceGenome.getGenomeName().equalsIgnoreCase(genomeName);
        } catch (DaoException | NullPointerException e) {
            return true;
        }
    }
    
    private static CancerStudy getCancerStudy(TrimmedProperties properties)
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
        if ( shortName == null) {
            throw new IllegalArgumentException("short_name is not specified.");
        }

        
        CancerStudy cancerStudy = new CancerStudy(name, description, cancerStudyIdentifier,
                                                  typeOfCancer, publicStudy(properties));
        cancerStudy.setPmid(properties.getProperty("pmid"));
        cancerStudy.setCitation(properties.getProperty("citation"));
        cancerStudy.setGroupsInUpperCase(properties.getProperty("groups"));
        cancerStudy.setShortName(shortName);
        String referenceGenome = properties.getProperty("reference_genome");
        
        if (referenceGenome == null) {
            referenceGenome = GlobalProperties.getReferenceGenomeName();
        }
        if (!checkSpecies(cancerStudyIdentifier, referenceGenome)) {
            throw new IllegalArgumentException("Species not match with old study");
        }
        cancerStudy.setReferenceGenome(referenceGenome);
        return cancerStudy;
    }

    private static boolean publicStudy( TrimmedProperties properties ) {
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