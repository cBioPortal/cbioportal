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

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

import java.util.List;

public class ImportDataUtil
{
    public static void addPatients(String barcodes[], int geneticProfileId) throws DaoException
    {
        addPatients(barcodes, getCancerStudy(geneticProfileId));
    }

    public static CancerStudy getCancerStudy(int geneticProfileId)
    {
        GeneticProfile gp = DaoGeneticProfile.getGeneticProfileById(geneticProfileId);
        return DaoCancerStudy.getCancerStudyByInternalId(gp.getCancerStudyId());
    }

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
            // genomic data typically has sample ids, check if a sample exists with the id, and if so,
            // that the sample has a patient record associated with it
            Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableId);
            return (s == null || (s != null && s.getInternalPatientId() <= 0));
        }
        else {
            return false;
        }
    }

    private static void addPatient(String stableId, CancerStudy cancerStudy) throws DaoException
    {
        DaoPatient.addPatient(new Patient(cancerStudy, stableId));
    }

    public static void addSamples(String barcodes[], int geneticProfileId) throws DaoException
    {
        addSamples(barcodes, getCancerStudy(geneticProfileId));
    }

    public static void addSamples(String barcodes[], CancerStudy cancerStudy) throws DaoException
    {
        for (String barcode : barcodes) {
            String sampleId = StableIdUtil.getSampleId(barcode);
            if (!StableIdUtil.isNormal(barcode) && unknownSample(cancerStudy, sampleId)) {
                addSample(sampleId, cancerStudy);
            }
        }
    }

    private static boolean unknownSample(CancerStudy cancerStudy, String stableId)
    {
        Sample s = DaoSample.getSampleByCancerStudyAndSampleId(cancerStudy.getInternalId(), stableId);
        return (s == null);
    }

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
}
