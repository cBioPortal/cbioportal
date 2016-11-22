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
package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.cbioportal.model.PositionMutationCount;
import org.cbioportal.persistence.MutationCountRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author abeshoua
 */
@RunWith(MockitoJUnitRunner.class)
public class MutationCountServiceImplTest {
	@InjectMocks
    private MutationCountServiceImpl mutationCountService;

    @Mock
    private MutationCountRepository mutationCountRepository;
    
    @Test
    public void getPositionMutationCounts() throws Exception {

        ArrayList<String> testGenes = new ArrayList<>();
	testGenes.add("BRAF");
	testGenes.add("KRAS");
        ArrayList<List<Integer>> testPositions = new ArrayList<>();
	ArrayList<Integer> brafPositions = new ArrayList<>();
	ArrayList<Integer> krasPositions = new ArrayList<>();
	brafPositions.add(1);
	brafPositions.add(2);
	krasPositions.add(3);
	testPositions.add(brafPositions);
	testPositions.add(krasPositions);

	PositionMutationCount pmc1 = new PositionMutationCount();
	PositionMutationCount pmc2 = new PositionMutationCount();
        List<PositionMutationCount> brafResult = new ArrayList<>();
	List<PositionMutationCount> krasResult = new ArrayList<>();
	brafResult.add(pmc1);
	krasResult.add(pmc2);

        Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("BRAF"), org.mockito.Matchers.anyList())).thenReturn(brafResult);
	Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("KRAS"), org.mockito.Matchers.anyList())).thenReturn(krasResult);
	
	List<PositionMutationCount> expectedResult = new ArrayList<>();
	expectedResult.add(pmc1);
	expectedResult.add(pmc2);
	List<PositionMutationCount> result = mutationCountService.getPositionMutationCounts(testGenes, testPositions);
	Assert.assertEquals(result, expectedResult);
    }
    
    @Test
    public void getPositionMutationCountsOnePositionListMissing() throws Exception {

        ArrayList<String> testGenes = new ArrayList<>();
	testGenes.add("BRAF");
	testGenes.add("KRAS");
        ArrayList<List<Integer>> testPositions = new ArrayList<>();
	ArrayList<Integer> brafPositions = new ArrayList<>();
	brafPositions.add(1);
	brafPositions.add(2);
	testPositions.add(brafPositions);

	PositionMutationCount pmc1 = new PositionMutationCount();
	PositionMutationCount pmc2 = new PositionMutationCount();
        List<PositionMutationCount> brafResult = new ArrayList<>();
	List<PositionMutationCount> krasResult = new ArrayList<>();
	brafResult.add(pmc1);
	krasResult.add(pmc2);

        Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("BRAF"), org.mockito.Matchers.anyList())).thenReturn(brafResult);
	Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("KRAS"), org.mockito.Matchers.anyList())).thenReturn(krasResult);
	
	List<PositionMutationCount> expectedResult = new ArrayList<>();
	expectedResult.add(pmc1);
	List<PositionMutationCount> result = mutationCountService.getPositionMutationCounts(testGenes, testPositions);
	Assert.assertEquals(result, expectedResult);
    }
    
     @Test
    public void getPositionMutationCountsNoPositions() throws Exception {

        ArrayList<String> testGenes = new ArrayList<>();
	testGenes.add("BRAF");
	testGenes.add("KRAS");
        ArrayList<List<Integer>> testPositions = new ArrayList<>();

	PositionMutationCount pmc1 = new PositionMutationCount();
	PositionMutationCount pmc2 = new PositionMutationCount();
        List<PositionMutationCount> brafResult = new ArrayList<>();
	List<PositionMutationCount> krasResult = new ArrayList<>();
	brafResult.add(pmc1);
	krasResult.add(pmc2);

        Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("BRAF"), org.mockito.Matchers.anyList())).thenReturn(brafResult);
	Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("KRAS"), org.mockito.Matchers.anyList())).thenReturn(krasResult);
	
	List<PositionMutationCount> expectedResult = new ArrayList<>();
	List<PositionMutationCount> result = mutationCountService.getPositionMutationCounts(testGenes, testPositions);
	Assert.assertEquals(result, expectedResult);
    }
    @Test
    public void getPositionMutationCountsNoGenes() throws Exception {

        ArrayList<String> testGenes = new ArrayList<>();
        ArrayList<List<Integer>> testPositions = new ArrayList<>();
	ArrayList<Integer> brafPositions = new ArrayList<>();
	ArrayList<Integer> krasPositions = new ArrayList<>();
	brafPositions.add(1);
	brafPositions.add(2);
	krasPositions.add(3);
	testPositions.add(brafPositions);
	testPositions.add(krasPositions);

	PositionMutationCount pmc1 = new PositionMutationCount();
	PositionMutationCount pmc2 = new PositionMutationCount();
        List<PositionMutationCount> brafResult = new ArrayList<>();
	List<PositionMutationCount> krasResult = new ArrayList<>();
	brafResult.add(pmc1);
	krasResult.add(pmc2);

        Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("BRAF"), org.mockito.Matchers.anyList())).thenReturn(brafResult);
	Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("KRAS"), org.mockito.Matchers.anyList())).thenReturn(krasResult);
	
	List<PositionMutationCount> expectedResult = new ArrayList<>();
	List<PositionMutationCount> result = mutationCountService.getPositionMutationCounts(testGenes, testPositions);
	Assert.assertEquals(result, expectedResult);
    }
    @Test
    public void getPositionMutationCountsNoGenesNoPositions() throws Exception {

        ArrayList<String> testGenes = new ArrayList<>();
        ArrayList<List<Integer>> testPositions = new ArrayList<>();

	PositionMutationCount pmc1 = new PositionMutationCount();
	PositionMutationCount pmc2 = new PositionMutationCount();
        List<PositionMutationCount> brafResult = new ArrayList<>();
	List<PositionMutationCount> krasResult = new ArrayList<>();
	brafResult.add(pmc1);
	krasResult.add(pmc2);

        Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("BRAF"), org.mockito.Matchers.anyList())).thenReturn(brafResult);
	Mockito.when(mutationCountRepository.getPositionMutationCounts(org.mockito.Matchers.eq("KRAS"), org.mockito.Matchers.anyList())).thenReturn(krasResult);
	
	List<PositionMutationCount> expectedResult = new ArrayList<>();
	List<PositionMutationCount> result = mutationCountService.getPositionMutationCounts(testGenes, testPositions);
	Assert.assertEquals(result, expectedResult);
    }
}
