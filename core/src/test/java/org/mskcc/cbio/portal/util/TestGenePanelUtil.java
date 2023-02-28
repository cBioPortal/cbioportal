package org.mskcc.cbio.portal.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mskcc.cbio.portal.util.GenePanelUtil.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-dao.xml"})
@Rollback
@Transactional
public class TestGenePanelUtil {

    CanonicalGene otor = makeGene(13321, 56914, "OTOR", makeAliases("FDP", "MIAL1"));
    CanonicalGene cadm2 = makeGene(20001, 253559, "CADM2", makeAliases("IGSF4D", "Necl-3", "NECL3", "SynCAM 2", "synCAM2"));
    CanonicalGene msh3 = makeGene(3573, 4437, "MSH3", makeAliases("MRP1", "DUP", "FAP4"));
    CanonicalGene p2ry10 = makeGene(10455, 27334, "P2RY10", makeAliases("P2Y10", "LYPSR2"));
    CanonicalGene adamts20 = makeGene(15009, 80070, "ADAMTS20", makeAliases("ADAM-TS20", "GON-1", "ADAMTS-20"));
    CanonicalGene kat2a = makeGene(2154, 2648, "KAT2A", makeAliases("hGCN5", "GCN5", "GCN5L2", "PCAF-b"));
    CanonicalGene myb = makeGene(3674, 4602, "MYB", makeAliases("c-myb_CDS", "Cmyb", "c-myb", "efg"));
    CanonicalGene npipb15 = makeGene(24049, 440348, "NPIPB15", makeAliases("A-761H5.4", "NPIPL2"));
    CanonicalGene dtnb = makeGene(1492, 1838, "DTNB", null);
    CanonicalGene ablim1 = makeGene(3239, 3983, "ABLIM1", makeAliases("LIMAB1", "abLIM-1", "ABLIM", "LIMATIN"));
    CanonicalGene piezo1 = makeGene(7609, 9780, "PIEZO1", makeAliases("FAM38A", "Mib", "LMPH3", "DHS"));
    CanonicalGene fgfr3 = makeGene(1827, 2261, "FGFR3", makeAliases("JTK4", "HSFGFR3EX", "ACH", "CEK2", "CD333"));

    @Test
    public void testExtractPropertyValueStableId() {
        Properties p1 = new Properties();
        p1.setProperty("stable_id", "TESTPANEL1");
        String actual = extractPropertyValue("stable_id", p1, true);
        String expected = "TESTPANEL1";
        assertEquals(expected, actual);
    }

    @Test
    public void testExtractPropertyValueDescription() {
        Properties p1 = new Properties();
        p1.setProperty("description", "Example gene panel meta file for testing purposes.");
        String actual = extractPropertyValue("description", p1, false);
        String expected = "Example gene panel meta file for testing purposes.";
        assertEquals(expected, actual);
    }

    @Test
    public void testExtractPropertyValueGeneList() {
        Properties p1 = new Properties();
        p1.setProperty("gene_list", "ABLIM1\tADAMTS20\tCADM2\tDTNB\tKAT2A\tMSH3\tMYB\tNPIPB15\tOTOR\tP2RY10");
        String actual = extractPropertyValue("gene_list", p1, true);
        String expected = "ABLIM1\tADAMTS20\tCADM2\tDTNB\tKAT2A\tMSH3\tMYB\tNPIPB15\tOTOR\tP2RY10";
        assertEquals(expected, actual);
    }

    @Test(expected = NullPointerException.class)
    public void testExtractPropertyValueUnknownPropertyName() {
        Properties p1 = new Properties();
        extractPropertyValue("stable_id", p1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractPropertyValueNoPropertyValue() {
        Properties p1 = new Properties();
        p1.setProperty("stable_id", "");
        extractPropertyValue("stable_id", p1, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractPropertyValueNoSpaceAllowed() {
        Properties p1 = new Properties();
        p1.setProperty("description", "Example gene panel meta file for testing purposes.");
        extractPropertyValue("description", p1, true);
    }

    @Test
    public void testExtractGenesAllowEmptyGenePanel() {
        Properties p = new Properties();
        p.setProperty("gene_list", "");
        Set<CanonicalGene> expected = makeGeneSet();
        Set<CanonicalGene> actual = extractGenes(p, true);
        assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractGenesDisallowEmptyGenePanel() {
        Properties p = new Properties();
        p.setProperty("gene_list", "");
        extractGenes(p, false);
    }

    @Test
    public void testExtractGenesMissingTab() {
        Properties p1 = new Properties();
        p1.setProperty("gene_list", "ABLIM1 ADAMTS20\tCADM2\tDTNB\tKAT2A\tMSH3\tMYB\tNPIPB15\tOTOR\tP2RY10");
        Set<CanonicalGene> actual = extractGenes(p1, false);
        assertNull(actual);
    }

    @Test
    public void testExtractGenesNonEmptyGenePanel() {
        Properties p1 = new Properties();
        p1.setProperty("gene_list", "ABLIM1\tADAMTS20\tCADM2\tDTNB\tKAT2A\tMSH3\tMYB\tNPIPB15\tOTOR\tP2RY10");
        Set<CanonicalGene> expected = makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1);
        Set<CanonicalGene> actual = extractGenes(p1, false);
        assertEquals(expected, actual);
    }

    @Test
    public void testExtractGenesAliasedGenes() {
        Properties p1 = new Properties();
        p1.setProperty("gene_list", "LIMAB1\tGON-1\tSynCAM 2\tDTNB\tPCAF-b\tDUP\tc-myb\tA-761H5.4\tMIAL1\tLYPSR2");
        Set<CanonicalGene> expected = makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1);
        Set<CanonicalGene> actual = extractGenes(p1, false);
        assertEquals(expected, actual);
    }

    @Test
    public void testExtractGenesUnknownParseableLong() {
        String NOT_A_GENE = "111111111111";
        Properties p1 = new Properties();
        p1.setProperty("gene_list", NOT_A_GENE + "\tADAMTS20\tCADM2\tDTNB\tKAT2A\tMSH3\tMYB\tNPIPB15\tOTOR\tP2RY10");
        Set<CanonicalGene> actual = extractGenes(p1, false);
        assertNull(actual);
    }

    @Test
    public void testExtractGenesUnknownUnparseableLong() {
        String NOT_A_GENE = "HELLOWORLD!";
        Properties p1 = new Properties();
        p1.setProperty("gene_list", NOT_A_GENE + "\tADAMTS20\tCADM2\tDTNB\tKAT2A\tMSH3\tMYB\tNPIPB15\tOTOR\tP2RY10");
        Set<CanonicalGene> actual = extractGenes(p1, false);
        assertNull(actual);
    }

    @Test
    public void testGetAddRemoveAEqualsB() {
        Pair expected = new Pair(
            makeGeneSet(),
            makeGeneSet()
        );
        Pair actual = getAddRemove(
            makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1),
            makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testGetAddRemoveEmptyIntersection() {
        Pair expected = new Pair(
            makeGeneSet(adamts20, ablim1),
            makeGeneSet(piezo1, fgfr3));
        Pair actual = getAddRemove(
            makeGeneSet(adamts20, ablim1),
            makeGeneSet(piezo1, fgfr3));
        assertEquals(expected, actual);
    }

    @Test
    public void testGetAddRemoveAOrBEmpty() {
        Pair expected = new Pair(
            makeGeneSet(),
            makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1)
        );
        Pair actual = getAddRemove(
            makeGeneSet(),
            makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testGetAddRemoveIntersectAndOnlyAdd() {
        Pair expected = new Pair(
            makeGeneSet(otor, cadm2, msh3, p2ry10, kat2a, myb, npipb15, dtnb),
            makeGeneSet()
        );
        Pair actual = getAddRemove(
            makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1),
            makeGeneSet(adamts20, ablim1)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testGetAddRemoveIntersectAndOnlyRemove() {
        Pair expected = new Pair(
            makeGeneSet(),
            makeGeneSet(otor, cadm2, msh3, p2ry10, kat2a, myb, npipb15, dtnb)
        );
        Pair actual = getAddRemove(
            makeGeneSet(adamts20, ablim1),
            makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testGetAddRemoveIntersectAndAndRemove() {
        Pair expected = new Pair(
            makeGeneSet(adamts20, ablim1),
            makeGeneSet(piezo1, fgfr3)
        );
        Pair actual = getAddRemove(
            makeGeneSet(otor, cadm2, msh3, p2ry10, adamts20, kat2a, myb, npipb15, dtnb, ablim1),
            makeGeneSet(otor, cadm2, piezo1, msh3, fgfr3, p2ry10, kat2a, myb, npipb15, dtnb)
        );
        assertEquals(expected, actual);
    }

    @Test
    public void testPair() {
        Pair expected = new Pair(
            makeGeneSet(ablim1, adamts20, fgfr3, piezo1),
            makeGeneSet()
        );
        Pair actual = new Pair(
            makeGeneSet(ablim1, fgfr3, adamts20, piezo1),
            makeGeneSet()
        );
        assertEquals(expected, actual);
    }

    private HashSet<String> makeAliases(String... aliases) {
        return new HashSet<>(Arrays.asList(aliases));
    }

    private HashSet<CanonicalGene> makeGeneSet(CanonicalGene... genes) {
        return new HashSet<>(Arrays.asList(genes));
    }

    private CanonicalGene makeGene(int geneticEntityId, long entrezGeneId, String hugoGeneSymbol, Set<String> aliases) {
        CanonicalGene gene = new CanonicalGene(geneticEntityId, entrezGeneId, hugoGeneSymbol, aliases);
        gene.setType("protein-coding");
        return gene;
    }
}
