/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.util;

import java.io.*;
import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoReferenceGenome;
import org.mskcc.cbio.portal.dao.DaoTypeOfCancer;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.ReferenceGenome;
import org.mskcc.cbio.portal.scripts.TrimmedProperties;

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
        if (genomeName == null || genomeName.equals("")) {
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
        

        
        CancerStudy cancerStudy = new CancerStudy(name, description, cancerStudyIdentifier,
                                                  typeOfCancer, publicStudy(properties));
        cancerStudy.setPmid(properties.getProperty("pmid"));
        cancerStudy.setCitation(properties.getProperty("citation"));
        cancerStudy.setGroupsInUpperCase(properties.getProperty("groups"));
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
