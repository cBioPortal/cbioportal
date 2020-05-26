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

package org.mskcc.cbio.portal.scripts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import org.mskcc.cbio.portal.model.CanonicalGene;
import org.mskcc.cbio.portal.dao.DaoGeneOptimized;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-dao.xml" })
@Rollback
@Transactional
public class TestNormalizeExpressionLevels {

	private String validationFile;
	private String[] args;

	@Before
	public void initialize() {
		URL url = this.getClass().getResource("/correct_data_mRNA_ZbyNorm.txt");
		validationFile = url.getFile().toString();

		URL url1 = TestNormalizeExpressionLevels.class.getResource("/test_all_thresholded.by_genes.txt");
		URL url2 = TestNormalizeExpressionLevels.class.getResource("/test_PR_GDAC_CANCER.medianexp.txt");
		URL url3 = TestNormalizeExpressionLevels.class.getResource("/data_mRNA_ZbyNorm.txt");

		args = new String[] { 
				url1.getFile().toString(),
				url2.getFile().toString(), 
				url3.getFile().toString(),
				NormalizeExpressionLevels.TCGA_NORMAL_SUFFIX, "4" };
	}

	// TBD: change this to use getResourceAsStream()

	@Test
	public void testNormalizeExpressionLevels() {

		try {

			DaoGeneOptimized daoGene = DaoGeneOptimized.getInstance();
			daoGene.addGene(new CanonicalGene(65985, "AACS"));
			daoGene.addGene(new CanonicalGene(63916, "ELMO2"));
			daoGene.addGene(new CanonicalGene(9240, "PNMA1"));
			daoGene.addGene(new CanonicalGene(6205, "RPS11"));
			daoGene.addGene(new CanonicalGene(7157, "TP53"));
			daoGene.addGene(new CanonicalGene(367, "AR"));
			NormalizeExpressionLevels.main(args);
			// compare with correct
			String line;
			Process p = Runtime.getRuntime().exec(
					"diff" + " " + validationFile + " " + args[2]);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			while ((line = input.readLine()) != null) {
				assertEquals("", line);
			}
			input.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertTrue(NormalizeExpressionLevels.isNormal("TCGA-A7-A0CG-11A-11D-A011-01"));
		assertFalse(NormalizeExpressionLevels.isNormal("TCGA-A7-A0CG-01A-11D-A011-01"));
	}

	@Test
	public void testJoin() {
		ArrayList<String> l = new ArrayList<String>();
		l.add("out");
		l.add("of");
		l.add("order");
		assertEquals("out-of-order", NormalizeExpressionLevels.join(l, "-"));
	}
}
