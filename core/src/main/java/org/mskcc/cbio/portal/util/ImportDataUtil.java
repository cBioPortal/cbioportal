/** Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.*;

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
        return (DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), stableId) == null);
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
            String patientId = StableIdUtil.getPatientId(barcode);
            Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(cancerStudy.getInternalId(), patientId);
            String sampleId = StableIdUtil.getSampleId(barcode);
            if (unknownSample(patient, sampleId)) {
                addSample(sampleId, patient, cancerStudy);
            }
        }
    }

    private static boolean unknownSample(Patient patient, String stableId)
    {
        for (Sample knownSample : DaoSample.getSamplesByPatientId(patient.getInternalId())) {
          if (knownSample.getStableId().equals(stableId)) {
            return false;
          }
        }
        return true;
    }

    private static void addSample(String sampleId, Patient patient, CancerStudy cancerStudy) throws DaoException
    {
        DaoSample.addSample(new Sample(sampleId,
                                       patient.getInternalId(),
                                       cancerStudy.getTypeOfCancerId()));
    }
}
