package org.cbioportal.domain.alteration.util;

import org.apache.commons.math3.util.Pair;
import org.cbioportal.legacy.model.AlterationCountByGene;
import org.cbioportal.legacy.model.AlterationEnrichment;
import org.cbioportal.legacy.model.CountSummary;
import org.cbioportal.legacy.model.Gene;
import org.cbioportal.legacy.model.MolecularProfileCaseIdentifier;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AlterationEnrichmentScoreUtilTest {

    @Test
    public void calculateEnrichmentScore() {
        // create molecularProfileCaseSet1, molecularProfileCaseSet2 list of entities

        AlterationEnrichment alterationEnrichment = new AlterationEnrichment();

        List<CountSummary> countSummaries = new ArrayList<>();
        CountSummary countSummary1 = new CountSummary();
        CountSummary countSummary2 = new CountSummary();
        CountSummary countSummary3 = new CountSummary();
        CountSummary countSummary4 = new CountSummary();
        countSummary1.setAlteredCount(0);
        countSummary1.setProfiledCount(943);
        countSummary1.setName("groupD");

        countSummary2.setAlteredCount(1);
        countSummary2.setProfiledCount(2103);
        countSummary2.setName("groupB");

        countSummary3.setAlteredCount(0);
        countSummary3.setProfiledCount(680);
        countSummary3.setName("groupC");

        countSummary4.setAlteredCount(0);
        countSummary4.setProfiledCount(6144);
        countSummary4.setName("groupD");

        countSummaries.add(countSummary1);
        countSummaries.add(countSummary2);
        countSummaries.add(countSummary3);
        countSummaries.add(countSummary4);
        alterationEnrichment.setEntrezGeneId(2);
        alterationEnrichment.setCounts(countSummaries);

        var pValue = AlterationEnrichmentScoreUtil.calculateEnrichmentScore(alterationEnrichment);
        assertEquals(0.2964987551514857, pValue.doubleValue(), 1e-10);
    }
}