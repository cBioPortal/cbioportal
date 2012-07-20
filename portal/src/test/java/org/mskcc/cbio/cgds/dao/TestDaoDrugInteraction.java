package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoDrugInteraction;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Drug;
import org.mskcc.cbio.cgds.model.DrugInteraction;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

public class TestDaoDrugInteraction extends TestCase {
    public void testDaoDrugInteraction() throws DaoException {
        ResetDatabase.resetDatabase();
        DaoDrugInteraction daoDrugInteraction = DaoDrugInteraction.getInstance();

        String type = "TARGETS";
        String dataSource = "Source";

        Drug drug = new Drug();
        drug.setId("DRUG:1");

        Drug drug2 = new Drug();
        drug2.setId("DRUG:2");

        CanonicalGene gene = new CanonicalGene(40, "forty");
        CanonicalGene gene2 = new CanonicalGene(41, "forty-one");
        CanonicalGene gene3 = new CanonicalGene(42, "forty-two");

        assertEquals(1, daoDrugInteraction.addDrugInteraction(drug, gene, type, dataSource, "", ""));
        daoDrugInteraction.addDrugInteraction(drug2, gene, type, dataSource, "", "");
        daoDrugInteraction.addDrugInteraction(drug2, gene2, type, dataSource, "", "");
        daoDrugInteraction.addDrugInteraction(drug, gene3, type, dataSource, "", "");
        daoDrugInteraction.addDrugInteraction(drug, gene2, type, dataSource, "", "");

        assertEquals(5, daoDrugInteraction.getCount());
        assertEquals(2, daoDrugInteraction.getInteractions(gene).size());
        assertEquals(1, daoDrugInteraction.getInteractions(gene3).size());

        DrugInteraction interaction = daoDrugInteraction.getInteractions(gene3).iterator().next();
        assertEquals(type, interaction.getInteractionType());
        assertEquals(dataSource, interaction.getDataSource());
        assertEquals(gene3.getEntrezGeneId(), interaction.getTargetGene());
        assertEquals(drug.getId(), interaction.getDrug());

        assertEquals(2, daoDrugInteraction.getTargets(drug2).size());
        DrugInteraction interaction2 = daoDrugInteraction.getTargets(drug2).iterator().next();
        assertEquals(drug2.getId(), interaction2.getDrug());
        assertEquals(gene.getEntrezGeneId(), interaction2.getTargetGene());
    }
}
