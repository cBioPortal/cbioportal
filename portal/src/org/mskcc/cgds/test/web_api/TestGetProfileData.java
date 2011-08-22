package org.mskcc.cgds.test.web_api;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.web_api.GetProfileData;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.GeneticAlterationType;
import org.mskcc.cgds.model.GeneticProfile;
import org.mskcc.cgds.util.ProgressMonitor;
import org.mskcc.cgds.scripts.ImportTabDelimData;
import org.mskcc.cgds.scripts.ResetDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TestGetProfileData extends TestCase {

    public void testGetProfileData() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        DaoGeneticProfile daoGeneticProfile = new DaoGeneticProfile();
        daoGene.addGene(new CanonicalGene(207, "AKT1"));
        daoGene.addGene(new CanonicalGene(208, "AKT2"));
        daoGene.addGene(new CanonicalGene(10000, "AKT3"));
        daoGene.addGene(new CanonicalGene(369, "ARAF"));
        daoGene.addGene(new CanonicalGene(472, "ATM"));
        daoGene.addGene(new CanonicalGene(673, "BRAF"));
        daoGene.addGene(new CanonicalGene(672, "BRCA1"));
        daoGene.addGene(new CanonicalGene(675, "BRCA2"));

        ProgressMonitor pMonitor = new ProgressMonitor();
        pMonitor.setConsoleMode(false);
        File file = new File("test_data/cna_test.txt");
        ImportTabDelimData parser = new ImportTabDelimData(file, ImportTabDelimData.BARRY_TARGET, 1, pMonitor);
        parser.importData();

        ArrayList <String> targetGeneList = new ArrayList<String> ();
        targetGeneList.add("AKT1");
        targetGeneList.add("AKT2");
        targetGeneList.add("AKT3");
        targetGeneList.add("ATM");
        targetGeneList.add("BRCA1");

        GeneticProfile geneticProfile = new GeneticProfile();
        geneticProfile.setProfileName("GISTIC CNA");
        geneticProfile.setCancerStudyId(1);
        geneticProfile.setStableId("gbm_rae");
        geneticProfile.setGeneticAlterationType(GeneticAlterationType.COPY_NUMBER_ALTERATION);
        daoGeneticProfile.addGeneticProfile(geneticProfile);

        ArrayList <String> geneticProfileIdList = new ArrayList<String>();
        geneticProfileIdList.add("gbm_rae");

        ArrayList <String> caseIdList = new ArrayList <String>();
        caseIdList.add("TCGA-02-0001");
        caseIdList.add("TCGA-02-0003");
        caseIdList.add("TCGA-02-0006");

        String out = GetProfileData.getProfileData(geneticProfileIdList, targetGeneList,
                caseIdList, new Boolean(false));
        String lines[] = out.split("\n");
        assertEquals("# DATA_TYPE\t GISTIC CNA" , lines[0]);
        assertEquals("# COLOR_GRADIENT_SETTINGS\t COPY_NUMBER_ALTERATION", lines[1]);
        assertTrue(lines[2].startsWith("GENE_ID\tCOMMON\tTCGA-02-0001\t" +
                "TCGA-02-0003\tTCGA-02-0006"));
        assertTrue(lines[3].startsWith("207\tAKT1\t0\t0\t0"));
        assertTrue(lines[4].startsWith("208\tAKT2\t0\t0\t0"));
    }
}
