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
        File testFile = new File("test_data/test_drugbank_small.xml");
        drugBankResource.setResourceURL("file://" + testFile.getAbsolutePath());

        DrugBankImporter drugBankImporter = new DrugBankImporter(drugBankResource);
        drugBankImporter.importDrugBankGeneList(new CSVReader(new FileReader("test_data/test_drugbank_targets.csv")));

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
