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

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

import java.util.ArrayList;
import junit.framework.TestCase;

/**
 * JUnit test for DaoSample class
 */
public class TestDaoSampleProfile extends TestCase {

    public void testDaoSampleProfile() throws DaoException {
        ResetDatabase.resetDatabase();
        createSamples();

        Patient patient = DaoPatient.getPatientByCancerStudyAndPatientId(CancerStudy.NO_SUCH_STUDY, "TCGA-12345");
        Sample sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-12345-01");

        int num = DaoSampleProfile.addSampleProfile(sample.getInternalId(), 1);
        assertEquals(1, num);

        boolean exists = DaoSampleProfile.sampleExistsInGeneticProfile(sample.getInternalId(), 1);
        assertTrue(exists);

        assertEquals(1, DaoSampleProfile.getProfileIdForSample(sample.getInternalId()));

        sample = DaoSample.getSampleByPatientAndSampleId(patient.getInternalId(), "TCGA-123456-01");
        num = DaoSampleProfile.addSampleProfile(sample.getInternalId(), 1);
        assertEquals(1, num);

        ArrayList<Integer> sampleIds = DaoSampleProfile.getAllSampleIdsInProfile(1);
        assertEquals(2, sampleIds.size());
        DaoSampleProfile.deleteAllRecords();
    }

    private void createSamples() throws DaoException {
        CancerStudy study = new CancerStudy("study", "description", "id", "brca", true);
        Patient p = new Patient(study, "TCGA-12345");
        int pId = DaoPatient.addPatient(p);
        Sample s = new Sample("TCGA-12345-01", pId, "type");
        DaoSample.addSample(s);
        s = new Sample("TCGA-123456-01", pId, "type");
        DaoSample.addSample(s);
    }
}
