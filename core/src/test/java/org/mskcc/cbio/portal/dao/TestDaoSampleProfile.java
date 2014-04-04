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
