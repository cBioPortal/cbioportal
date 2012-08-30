package org.mskcc.cbio.portal.mut_diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mskcc.cbio.cgds.model.ExtendedMutation;

import com.google.common.collect.ImmutableList;

/**
 * Unit test for Pileup.
 */
public final class TestPileup {

    @Test
    public void testConstructor() {
        Pileup pileup = new Pileup("label", 42, 84);
        assertNotNull(pileup);
        assertEquals("label", pileup.getLabel());
        assertEquals(42, pileup.getLocation());
        assertEquals(84, pileup.getCount());
    }

    @Test
    public void testConstructorEmptyLabel() {
        Pileup pileup = new Pileup("", 42, 84);
        assertEquals("", pileup.getLabel());
    }

    @Test
    public void testConstructorNullLabel() {
        Pileup pileup = new Pileup(null, 42, 84);
        assertNull(pileup.getLabel());
    }

    @Test(expected=NullPointerException.class)
    public void testPileupNullMutations() {
        Pileup.pileup(null);
    }

    @Test
    public void testPileupEmptyMutations() {
        List<Pileup> pileups = Pileup.pileup(Collections.<ExtendedMutation>emptyList());
        assertNotNull(pileups);
        assertTrue(pileups.isEmpty());
    }

    @Test
    public void testPileupSingleMutation() {
        ExtendedMutation mutation = new ExtendedMutation();
        mutation.setProteinChange("A123K");
        mutation.setCaseId("caseId");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(1, pileup.getCount());
    }

    @Test
    public void testPileupMultipleMutationsSameLocationDifferentCaseIds() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange("A123K");
        mutation2.setProteinChange("A123K");
        mutation0.setCaseId("caseId0");
        mutation1.setCaseId("caseId1");
        mutation2.setCaseId("caseId2");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(3, pileup.getCount());
    }

    @Test
    public void testPileupMultipleMutationsSameLocationSameCaseIds() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange("A123K");
        mutation2.setProteinChange("A123K");
        mutation0.setCaseId("caseId");
        mutation1.setCaseId("caseId");
        mutation2.setCaseId("caseId");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(1, pileup.getCount());
    }

    @Test
    public void testPileupMultipleMutationsSameLocationNullCaseId() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange("A123K");
        mutation2.setProteinChange("A123K");
        mutation0.setCaseId("caseId0");
        mutation1.setCaseId(null);
        mutation2.setCaseId("caseId2");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(3, pileup.getCount());
    }

    @Test
    public void testPileupMultipleMutationsSameLocationDifferentAminoAcidChange() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange("A123K");
        mutation2.setProteinChange("A123G");
        mutation0.setCaseId("caseId0");
        mutation1.setCaseId("caseId1");
        mutation2.setCaseId("caseId2");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123G/A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(3, pileup.getCount());
    }

    @Test
    // not sure this could happen, unless one caseId can have different phases
    public void testPileupMultipleMutationsSameLocationDifferentAminoAcidChangeSameCaseIds() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange("A123K");
        mutation2.setProteinChange("A123G");
        mutation0.setCaseId("caseId");
        mutation1.setCaseId("caseId");
        mutation2.setCaseId("caseId");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123G/A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(2, pileup.getCount());
    }

    @Test
    public void testPileupMultipleMutationsDifferentLocations() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange("A123K");
        mutation2.setProteinChange("K234G");
        mutation0.setCaseId("caseId0");
        mutation1.setCaseId("caseId1");
        mutation2.setCaseId("caseId2");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(2, pileups.size());
        Pileup pileup = pileups.get(0);
        if (pileup.getLocation() == 123) {
            assertEquals("A123K", pileup.getLabel());
            assertEquals(2, pileup.getCount());
        }
        else if (pileup.getLocation() == 234) {
            assertEquals("K234G", pileup.getLabel());
            assertEquals(1, pileup.getCount());
        }
        else {
            fail();
        }
    }

    @Test
    public void testPileupMultipleMutationsNumberFormatException() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange("missense");
        mutation2.setProteinChange("A123K");
        mutation0.setCaseId("caseId0");
        mutation1.setCaseId("caseId1");
        mutation2.setCaseId("caseId2");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(2, pileup.getCount());
    }

    @Test
    public void testPileupMultipleMutationsNullLabel() {
        ExtendedMutation mutation0 = new ExtendedMutation();
        ExtendedMutation mutation1 = new ExtendedMutation();
        ExtendedMutation mutation2 = new ExtendedMutation();
        mutation0.setProteinChange("A123K");
        mutation1.setProteinChange(null);
        mutation2.setProteinChange("A123K");
        mutation0.setCaseId("caseId0");
        mutation1.setCaseId("caseId1");
        mutation2.setCaseId("caseId2");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation0, mutation1, mutation2));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("A123K", pileup.getLabel());
        assertEquals(123, pileup.getLocation());
        assertEquals(2, pileup.getCount());
    }

    @Test
    public void testPileupsLowercasePDotSAnimoAcidPosition() {
        ExtendedMutation mutation = new ExtendedMutation();
        mutation.setProteinChange("p.S310F");
        mutation.setCaseId("caseId");

        List<Pileup> pileups = Pileup.pileup(ImmutableList.of(mutation));
        assertNotNull(pileups);
        assertEquals(1, pileups.size());
        Pileup pileup = pileups.get(0);

        assertEquals("p.S310F", pileup.getLabel());
        assertEquals(310, pileup.getLocation());
        assertEquals(1, pileup.getCount());
    }
}
