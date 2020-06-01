/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.dao;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mskcc.cbio.portal.dao.DaoDrugInteraction;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.MySQLbulkLoader;
import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.model.Drug;
import org.mskcc.cbio.portal.model.DrugInteraction;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestDaoDrugInteraction {
	
	@Before 
	public void setUp() throws DaoException {
        DaoDrug daoDrug = DaoDrug.getInstance();
        Drug drug1 = new Drug("DRUG:1", "MyDrug", "description",
                "synonym,synonym2", "this is an xref", "DUMMY", "B01AE02", true, true, false, 25);
        daoDrug.addDrug(drug1);
        Drug drug2 = new Drug("DRUG:2", "MyDrug2", "description2",
                "synonym", "this is an xref2", "BLA", "L01XX29", false, false, true, -1);
        daoDrug.addDrug(drug2);
	}
	
	@Test
    public void testDaoDrugInteraction() throws DaoException {

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
