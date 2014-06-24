/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.scripts;

import junit.framework.TestCase;
import org.mskcc.cbio.portal.dao.DaoDrug;
import org.mskcc.cbio.portal.dao.DaoDrugInteraction;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Drug;
import org.mskcc.cbio.portal.scripts.drug.DrugDataResource;
import org.mskcc.cbio.portal.scripts.drug.internal.PiHelperImporter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class TestPiHelperDrugImporter extends TestCase {
    public void testImporter() throws Exception {
        ResetDatabase.resetDatabase();

        DaoGeneOptimized daoGeneOptimized = DaoGeneOptimized.getInstance();
        String[] genes = {
                "F2",
                "EGFR",
                "FCGR3B",
                "C1R",
                "C1QA",
                "C1QB",
                "C1QC",
                "FCGR3A",
                "C1S",
                "FCGR1A",
                "FCGR2A",
                "FCGR2B",
                "FCGR2C",
                "IL2RA",
                "IL2RB",
                "IL2RG"
        };

        for (String gene : genes) {
            daoGeneOptimized.addGene(new CanonicalGene(gene));
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        String today = format.format(cal.getTime());
        DrugDataResource pihelper = new DrugDataResource(
                "PiHelper",
                "https://bitbucket.org/armish/pihelper/downloads/pihelper_data_20121107.zip",
                today
        );

        PiHelperImporter importer = new PiHelperImporter(pihelper);

        ClassLoader classLoader = this.getClass().getClassLoader();
        importer.setDrugInfoFile(classLoader.getResourceAsStream("test_pihelper_drugs.tsv"));
        importer.setDrugTargetsFile(classLoader.getResourceAsStream("test_pihelper_drugtargets.tsv"));

        importer.importData();

        DaoDrug daoDrug = DaoDrug.getInstance();
        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        ArrayList<Drug> allDrugs = daoDrug.getAllDrugs();
        int count = allDrugs.size();
        assertEquals(6, count);
        assertEquals(16, daoDrugInteraction.getCount());

        int[] numOfTargets = {1, 12, 0, 3, 0 ,0};
        for(int i=0; i < count; i++) {
            assertEquals(numOfTargets[i], daoDrugInteraction.getTargets(allDrugs.get(i)).size());
        }

        Drug cetuximab = daoDrug.getDrug("33612");
        assertEquals(204, cetuximab.getNumberOfClinicalTrials().intValue());
        assertTrue(cetuximab.isCancerDrug());
        assertFalse(cetuximab.isNutraceuitical());
        assertTrue(cetuximab.isApprovedFDA());

        Drug etanercept = daoDrug.getDrug("33615");
        assertEquals(-1, etanercept.getNumberOfClinicalTrials().intValue());
        assertFalse(etanercept.isCancerDrug());
        assertFalse(etanercept.isNutraceuitical());
        assertTrue(etanercept.isApprovedFDA());
    }
}
