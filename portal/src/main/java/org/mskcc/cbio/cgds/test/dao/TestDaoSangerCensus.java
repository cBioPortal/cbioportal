package org.mskcc.cgds.test.dao;

import junit.framework.TestCase;
import org.mskcc.cgds.dao.*;
import org.mskcc.cgds.scripts.ResetDatabase;
import org.mskcc.cgds.model.ExtendedMutation;
import org.mskcc.cgds.model.CanonicalGene;
import org.mskcc.cgds.model.SangerCancerGene;

import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;

/**
 * JUnit tests for DaoSangerCensus class.
 */
public class TestDaoSangerCensus extends TestCase {

    public void testDaoMutation() throws DaoException {
        DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
        CanonicalGene brca1 = new CanonicalGene(675, "BRCA1");
        daoGene.addGene(brca1);

        DaoSangerCensus daoCensus = DaoSangerCensus.getInstance();
        daoCensus.deleteAllRecords();
        daoCensus.addGene(brca1, true, true, "CML, ALL, T-ALL", "AML, leukemia, breast",
                "Familial neuroblastoma", "L, E, M", "T, Mis, A", "BCR, ETV6, NUP214", true,
                "Cardio-facio-cutaneous syndrome");

        HashMap<String, SangerCancerGene> cancerCensus = daoCensus.getCancerGeneSet();
        SangerCancerGene cancerGene = cancerCensus.get("BRCA1");
        assertEquals (true, cancerGene.isCancerGermlineMutation());
        assertEquals (true, cancerGene.isCancerSomaticMutation());

        assertEquals ("CML, ALL, T-ALL", cancerGene.getTumorTypesSomaticMutation());
        assertEquals (3, cancerGene.getTumorTypesSomaticMutationList().size());
        assertEquals ("chronic myeloid leukemia", cancerGene.getTumorTypesSomaticMutationList().get(0));
        assertEquals ("AML, leukemia, breast", cancerGene.getTumorTypesGermlineMutation());
        assertEquals (3, cancerGene.getTumorTypesGermlineMutationList().size());
        assertEquals ("acute myelogenous leukemia", cancerGene.getTumorTypesGermlineMutationList().get(0));
        assertEquals ("Familial neuroblastoma", cancerGene.getCancerSyndrome());
        assertEquals ("L, E, M", cancerGene.getTissueType());
        assertEquals (3, cancerGene.getTissueTypeList().size());
        assertEquals ("leukaemia/lymphoma", cancerGene.getTissueTypeList().get(0));
        assertEquals ("T, Mis, A", cancerGene.getMutationType());
        assertEquals (3, cancerGene.getMutationTypeList().size());
        assertEquals ("translocation", cancerGene.getMutationTypeList().get(0));
        assertEquals ("BCR, ETV6, NUP214", cancerGene.getTranslocationPartner());
        assertEquals (true, cancerGene.getOtherGermlineMut());
        assertEquals ("Cardio-facio-cutaneous syndrome", cancerGene.getOtherDisease());
    }
}