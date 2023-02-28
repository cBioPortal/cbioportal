package org.mskcc.portal.test.oncotator;

import junit.framework.TestCase;
import org.mskcc.portal.oncotator.OncotatorParser;
import org.mskcc.portal.oncotator.OncotatorRecord;
import org.mskcc.portal.util.WebFileConnect;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Unit test for OncotatorParser.
 */
public class TestOncotatorParser extends TestCase {

    public void testParser() throws Exception {
        FileReader reader = new FileReader("test_data/oncotator0.json");
        BufferedReader bufReader = new BufferedReader(reader);
        String content = WebFileConnect.readFile(bufReader);
        OncotatorRecord oncotator = OncotatorParser.parseJSON("key", content);
        assertEquals("PTEN", oncotator.getGene());
        assertEquals("g.chr10:89653820G>T", oncotator.getGenomeChange());
        assertEquals("p.E40*", oncotator.getProteinChange());
        assertEquals("Nonsense_Mutation", oncotator.getVariantClassification());
        assertEquals("rs121434568", oncotator.getDbSnpRs());
    }
}
