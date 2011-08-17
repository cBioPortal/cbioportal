package org.mskcc.cgds.test.util;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.DaoCancerStudy;
import org.mskcc.cgds.dao.DaoGeneticProfile;
import org.mskcc.cgds.model.CancerStudy;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.scripts.ImportTypesOfCancers;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.util.GeneticProfileReader;
import org.mskcc.cgds.util.ProgressMonitor;

import java.io.File;
import java.util.ArrayList;

public class TestGeneticProfileReader extends TestCase {

    public void testGeneticProfileReader() throws Exception {
        ResetDatabase.resetDatabase();
        // load cancers
        ImportTypesOfCancers.load(new ProgressMonitor(), new File("test_data/cancers.txt"));

        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        daoGeneticProfile.deleteAllRecords();

        DaoCancerStudy.deleteAllRecords();

        CancerStudy cancerStudy = new CancerStudy("GBM", "GBM Description",
                "gbm", "gbm", true);
        DaoCancerStudy.addCancerStudy(cancerStudy);

        File file = new File("test_data/genetic_profile_test.txt");
        GeneticProfile geneticProfile = GeneticProfileReader.loadGeneticProfile(file);
        assertEquals("Barry", geneticProfile.getTargetLine());
        assertEquals("Blah Blah.", geneticProfile.getProfileDescription());

        cancerStudy = DaoCancerStudy.getCancerStudyByStableId("gbm");
        ArrayList<GeneticProfile> list = daoGeneticProfile.getAllGeneticProfiles
                (cancerStudy.getInternalId());
        geneticProfile = list.get(0);

        assertEquals(cancerStudy.getInternalId(), geneticProfile.getCancerStudyId());
        assertEquals("Barry's CNA Data", geneticProfile.getProfileName());
        assertEquals(GeneticAlterationType.COPY_NUMBER_ALTERATION,
                geneticProfile.getGeneticAlterationType());

        geneticProfile = GeneticProfileReader.loadGeneticProfile(file);
        assertEquals("Barry", geneticProfile.getTargetLine());
        assertEquals("Blah Blah.", geneticProfile.getProfileDescription());
    }
}
