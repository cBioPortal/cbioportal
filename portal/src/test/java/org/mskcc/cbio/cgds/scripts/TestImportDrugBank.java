/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.cgds.scripts;

import au.com.bytecode.opencsv.CSVReader;
import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoDrug;
import org.mskcc.cbio.cgds.dao.DaoDrugInteraction;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Drug;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;
import org.mskcc.cbio.cgds.scripts.drug.internal.DrugBankImporter;
import org.mskcc.cbio.cgds.scripts.drug.internal.DrugBankResource;

import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestImportDrugBank extends TestCase {
    public void testImportDrugBank() throws Exception {
        ResetDatabase.resetDatabase();

        DrugBankResource drugBankResource = new DrugBankResource();
        drugBankResource.setName("DrugBank");
        drugBankResource.setVersion("201101");
		// TBD: change this to use getResourceAsStream()
        File testFile = new File("target/test-classes/test_drugbank_small.xml");
        drugBankResource.setResourceURL("file://" + testFile.getAbsolutePath());

        DrugBankImporter drugBankImporter = new DrugBankImporter(drugBankResource);
		// TBD: change this to use getResourceAsStream()
        drugBankImporter.importDrugBankGeneList(new CSVReader(new FileReader("target/test-classes/test_drugbank_targets.csv")));

        assertEquals(92, drugBankImporter.getGeneMap().size());
        // We don't have all the genes loaded up yet, so we will just fake that

        Map<BigInteger,List<CanonicalGene>> geneMap = drugBankImporter.getGeneMap();
        for (BigInteger bigInteger : geneMap.keySet()) {
            List<CanonicalGene> geneList = new ArrayList<CanonicalGene>();
            geneList.add(new CanonicalGene(bigInteger.intValue(), bigInteger.toString()));
            geneMap.put(bigInteger, geneList);
        }

        drugBankImporter.importData();

        DaoDrug daoDrug = DaoDrug.getInstance();
        Drug drug = daoDrug.getDrug("DB00001");
        assertNotNull(drug);
        assertEquals("DrugBank", drug.getResource());

        assertNotNull(daoDrug.getDrug("DB00002"));
        assertNotNull(daoDrug.getDrug("DB00003"));
        assertNotNull(daoDrug.getDrug("DB00004"));

        assertEquals(4, daoDrug.getAllDrugs().size());

        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();
        assertEquals(17, daoDrugInteraction.getCount());

    }

}
