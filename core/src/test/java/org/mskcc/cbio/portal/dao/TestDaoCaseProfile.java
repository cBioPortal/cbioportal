package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

import java.util.ArrayList;
import junit.framework.TestCase;

/**
 * JUnit test for DaoCase class
 */
public class TestDaoCaseProfile extends TestCase {

    public void testDaoCaseProfile() throws DaoException {
        ResetDatabase.resetDatabase();
        createSamples();

        int num = DaoCaseProfile.addCaseProfile("TCGA-12345", 1);
        assertEquals(1, num);
        boolean exists = DaoCaseProfile.caseExistsInGeneticProfile("TCGA-12345", 1);
        assertTrue(exists);

        assertEquals(1, DaoCaseProfile.getProfileIdForCase( "TCGA-12345" ));
        
        num = DaoCaseProfile.addCaseProfile("TCGA-123456", 1);
        assertEquals(1, num);
        ArrayList<String> caseIds = DaoCaseProfile.getAllCaseIdsInProfile(1);
        assertEquals(2, caseIds.size());
        DaoCaseProfile.deleteAllRecords();
    }

    private void createSamples() throws DaoException {
        CancerStudy study = new CancerStudy("study", "description", "id", "brca", true);
        Patient p = new Patient(study, "TCGA-12345");
        int pId = DaoPatient.addPatient(p);
        Sample s = new Sample("TCGA-12345", pId, "type");
        DaoSample.addSample(s);
        s = new Sample("TCGA-123456", pId, "type");
        DaoSample.addSample(s);
    }
}
