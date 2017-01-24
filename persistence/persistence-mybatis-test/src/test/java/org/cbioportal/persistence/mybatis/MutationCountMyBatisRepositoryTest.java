/*
 * Copyright (c) 2016 Memorial Sloan-Kettering Cancer Center.
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
package org.cbioportal.persistence.mybatis;

import java.util.LinkedList;
import java.util.List;
import org.cbioportal.model.PositionMutationCount;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author abeshoua
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class MutationCountMyBatisRepositoryTest {

	@Autowired
	private MutationCountMyBatisRepository mutationCountMyBatisRepository;
	
	@Test
	public void noPositionsSpecified() {
		String gene = "BRCA1";
		List<Integer> positions = new LinkedList<>();
		List<PositionMutationCount> result = mutationCountMyBatisRepository.getPositionMutationCounts(gene, positions);
		Assert.assertTrue(result.size() == 3);
	}
	@Test
	public void onePositionSpecifiedPresent() {
		String gene = "BRCA1";
		List<Integer> positions = new LinkedList<>();
		positions.add(61);
		List<PositionMutationCount> result = mutationCountMyBatisRepository.getPositionMutationCounts(gene, positions);
		Assert.assertTrue(result.size() == 1);
		Assert.assertTrue(result.get(0).getCodonPosition() == 61);
		Assert.assertTrue(result.get(0).getMutationCount() == 1);
	}
	@Test
	public void onePositionSpecifiedNotPresent() {
		String gene = "BRCA1";
		List<Integer> positions = new LinkedList<>();
		positions.add(62);
		List<PositionMutationCount> result = mutationCountMyBatisRepository.getPositionMutationCounts(gene, positions);
		Assert.assertTrue(result.isEmpty());
	}
	@Test
	public void positionsSpecifiedOnePresent() {
		String gene = "AKT1";
		List<Integer> positions = new LinkedList<>();
		positions.add(61);
		positions.add(63);
		positions.add(121);
		List<PositionMutationCount> result = mutationCountMyBatisRepository.getPositionMutationCounts(gene, positions);
		Assert.assertTrue(result.size() == 1);
		Assert.assertTrue(result.get(0).getCodonPosition() == 61);
		Assert.assertTrue(result.get(0).getMutationCount() == 1);
	}
	@Test
	public void positionsSpecifiedNonePresent() {
		String gene = "AKT1";
		List<Integer> positions = new LinkedList<>();
		positions.add(62);
		positions.add(67);
		positions.add(12341);
		positions.add(233);
		List<PositionMutationCount> result = mutationCountMyBatisRepository.getPositionMutationCounts(gene, positions);
		Assert.assertTrue(result.isEmpty());
	}
}
