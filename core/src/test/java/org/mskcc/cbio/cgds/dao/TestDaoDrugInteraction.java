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

package org.mskcc.cbio.cgds.dao;

import junit.framework.TestCase;
import org.mskcc.cbio.cgds.dao.DaoDrugInteraction;
import org.mskcc.cbio.cgds.dao.DaoException;
import org.mskcc.cbio.cgds.dao.MySQLbulkLoader;
import org.mskcc.cbio.cgds.model.CanonicalGene;
import org.mskcc.cbio.cgds.model.Drug;
import org.mskcc.cbio.cgds.model.DrugInteraction;
import org.mskcc.cbio.cgds.scripts.ResetDatabase;

public class TestDaoDrugInteraction extends TestCase {
    public void testDaoDrugInteraction() throws DaoException {

        ResetDatabase.resetDatabase();

		// save bulkload setting before turning off
		boolean isBulkLoad = MySQLbulkLoader.isBulkLoad();
		MySQLbulkLoader.bulkLoadOff();

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

		// restore bulk setting
		if (isBulkLoad) {
			MySQLbulkLoader.bulkLoadOn();
		}
    }
}
