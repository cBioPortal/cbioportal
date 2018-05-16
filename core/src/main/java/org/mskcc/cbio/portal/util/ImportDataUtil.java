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

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ImportClinicalData;


/**
 * Class to add missing patients and samples "on the fly". 
 * 
 * @deprecated : all patients and samples should be imported via {@link ImportClinicalData}. 
 */
public class ImportDataUtil
{
    /**
	 * @deprecated : this data should be imported via {@link ImportClinicalData}
	 */
    public static void addPatients(String barcodes[], int geneticProfileId) throws DaoException
    {
        addPatients(barcodes, getCancerStudy(geneticProfileId));
    }

    public static CancerStudy getCancerStudy(int geneticProfileId) throws DaoException
    {
        GeneticProfile gp = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
        return DaoCancerStudy.getCancerStudyByInternalId(gp.getCancerStudyId());
    }

    /**
	 * @deprecated : this data should be imported via {@link ImportClinicalData}
	 */
    public static void addPatients(String barcodes[], CancerStudy cancerStudy) throws DaoException
    {
        for (String barcode : barcodes) {
            String patientId = StableIdUtil.getPatientId(barcode);
            if (unknownPatient(cancerStudy, patientId)) {
                addPatient(patientId, cancerStudy);
            }
        }
    }

    private static boolean unknownPatient(CancerStudy cancerStudy, String stableId)
    {
        Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stableId);
        if (p == null) {
        	//this is strange...but at least now it reports it is doing something strange. TODO - review this!
        	ProgressMonitor.logWarning("Couldn't find patient "+stableId+" in study "+cancerStudy.getCancerStudyStableId() + ". Trying to find it in samples table...");
            // genomic data typically has sample ids, check if a sample exists with the id, and if so,
            // that the sample has a patient record associated with it
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableId);
            return (s == null || (s != null && s.getInternalPatientId() <= 0));
        }
        else {
            return false;
        }
    }

    /**
	 * @deprecated : this data should be imported via {@link ImportClinicalData}
	 */
    private static void addPatient(String stableId, CancerStudy cancerStudy) throws DaoException
    {
        DaoPatient.addPatient(new Patient(cancerStudy, stableId));
    }

    /**
	 * @deprecated : this data should be imported via {@link ImportClinicalData}
	 */
    public static int addSamples(String barcodes[], int geneticProfileId) throws DaoException
    {
        return addSamples(barcodes, getCancerStudy(geneticProfileId));
    }

    /**
     * Will check in DB if samples exist and add them if they do not 
     * yet exist (and are NOT a normal sample). 
     * 
     * @return returns the number of missing samples that had to be added to the DB
     * @deprecated : this data should be imported via {@link ImportClinicalData}
     */
    public static int addSamples(String barcodes[], CancerStudy cancerStudy) throws DaoException
    {
    	int nrNewlyAdded = 0;
        for (String barcode : barcodes) {
            String sampleId = StableIdUtil.getSampleId(barcode);
            if (!StableIdUtil.isNormal(barcode) && unknownSample(cancerStudy, sampleId)) {
                addSample(sampleId, cancerStudy);
                nrNewlyAdded++;
            }
        }
        return nrNewlyAdded;
    }

    private static boolean unknownSample(CancerStudy cancerStudy, String stableId)
    {
        Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableId);
        return (s == null);
    }

    /**
	 * @deprecated : this data should be imported via {@link ImportClinicalData}
	 */
    private static void addSample(String sampleId, CancerStudy cancerStudy) throws DaoException
    {
        // if we get here, all we can do is find a patient that owns the sample using the sample id.
        // if we can't find a patient, create a patient using the sample id
        String patientId = StableIdUtil.getPatientId(sampleId);
        Patient p = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patientId);
        int pId = (p == null) ?
            DaoPatient.addPatient(new Patient(cancerStudy, patientId)) : p.getInternalId();
        DaoSample.addSample(new Sample(sampleId, pId,
                                       cancerStudy.getTypeOfCancerId()));
    }

    public static void addSampleProfile(Sample sample, Integer geneticProfileID, String genePanelID) throws DaoException
    {
        if (!DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), geneticProfileID)) {
            if (genePanelID != null) {
                DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileID, GeneticProfileUtil.getGenePanelId(genePanelID));
            } else {
                DaoSampleProfile.addSampleProfile(sample.getInternalId(), geneticProfileID, null);
            }
        }
    }
}
