/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.web_api;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.*;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.GeneticAlterationType;
import org.mskcc.cbio.portal.model.GeneticProfile;
import org.mskcc.cbio.portal.util.ProgressMonitor;
import org.mskcc.cbio.portal.scripts.ImportTabDelimData;
import org.mskcc.cbio.portal.scripts.ResetDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JUnit test for GetProfileData class.
 */
public class TestGetProfileData extends TestCase {

    public void testGetProfileData() throws DaoException, IOException {
        ResetDatabase.resetDatabase();
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
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
		// TBD: change this to use getResourceAsStream()
        File file = new File("target/test-classes/cna_test.txt");
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
        geneticProfile.setDatatype("DISCRETE");
        DaoGeneticProfile.addGeneticProfile(geneticProfile);

        ArrayList <String> geneticProfileIdList = new ArrayList<String>();
        geneticProfileIdList.add("gbm_rae");

        ArrayList <String> caseIdList = new ArrayList <String>();
        caseIdList.add("TCGA-02-0001");
        caseIdList.add("TCGA-02-0003");
        caseIdList.add("TCGA-02-0006");

        GetProfileData getProfileData = new GetProfileData(geneticProfileIdList, targetGeneList,
                caseIdList, new Boolean(false));
        String out = getProfileData.getRawContent();
        String lines[] = out.split("\n");
        assertEquals("# DATA_TYPE\t GISTIC CNA" , lines[0]);
        assertEquals("# COLOR_GRADIENT_SETTINGS\t COPY_NUMBER_ALTERATION", lines[1]);
        assertTrue(lines[2].startsWith("GENE_ID\tCOMMON\tTCGA-02-0001\t" +
                "TCGA-02-0003\tTCGA-02-0006"));
        assertTrue(lines[3].startsWith("207\tAKT1\t0\t0\t0"));
        assertTrue(lines[4].startsWith("208\tAKT2\t0\t0\t0"));
    }
}
