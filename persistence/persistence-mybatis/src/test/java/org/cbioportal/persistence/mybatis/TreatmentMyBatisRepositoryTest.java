/*
 * Copyright (c) 2019 The Hyve B.V.
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

import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.Treatment;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.PersistenceConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabase.xml")
@Configurable
public class TreatmentMyBatisRepositoryTest {

    @Autowired
    private TreatmentMyBatisRepository treatmentMyBatisRepository;

    @Test
    public void getAllTreatments() {

        // String projection, Integer pageSize, Integer pageNumber
        List<Treatment> result = treatmentMyBatisRepository.getAllTreatments(PersistenceConstants.SUMMARY_PROJECTION, 10, 0);
        Assert.assertEquals(2, result.size());

        //expect: ordered ASC by treatment id:
        Treatment treatment = result.get(0);
        Assert.assertEquals("17-AAG", treatment.getStableId());
        Assert.assertEquals("https://en.wikipedia.org/wiki/Tanespimycin", treatment.getRefLink());
        treatment = result.get(1);
        Assert.assertEquals("AEW541", treatment.getStableId());
        Assert.assertEquals("TrkA/B/C inhibitor", treatment.getDescription());
    }

    @Test
    public void getMetaTreatments() {
        BaseMeta result = treatmentMyBatisRepository.getMetaTreatments();
        Assert.assertEquals(2, result.getTotalCount().intValue());
    }

    @Test
    public void getMetaTreatmentsWithTreatmentList() {
        List<String> treatmentIds = Arrays.asList("17-AAG", "AEW541");
        BaseMeta result = treatmentMyBatisRepository.getMetaTreatments(treatmentIds);
        Assert.assertEquals(2, result.getTotalCount().intValue());
    }

    @Test
    public void getMetaTreatmentsWithStudyList() {
        List<String> studyIds = Arrays.asList("study_tcga_pub");
        BaseMeta result = treatmentMyBatisRepository.getMetaTreatmentsInStudies(studyIds);
        Assert.assertEquals(2, result.getTotalCount().intValue());
    }

    @Test
    public void getTreatment() {
        Treatment treatment = treatmentMyBatisRepository.getTreatmentByStableId("17-AAG");
        Assert.assertEquals("https://en.wikipedia.org/wiki/Tanespimycin", treatment.getRefLink());
    }
    
    @Test
    public void fetchTreatments() {

        List<Treatment> result = treatmentMyBatisRepository.fetchTreatments(Arrays.asList("DUMMY"));
        Assert.assertEquals(0, result.size());
        result = treatmentMyBatisRepository.fetchTreatments(Arrays.asList("17-AAG"));
        Assert.assertEquals(1, result.size());
        result = treatmentMyBatisRepository.fetchTreatments(Arrays.asList("17-AAG","AEW541"));
        Assert.assertEquals(2, result.size());

        //test summary and ID projections:
        result = treatmentMyBatisRepository.fetchTreatments(Arrays.asList("AEW541","17-AAG","DUMMY"));
        Assert.assertEquals(2, result.size());

        Treatment treatment = result.get(0);
        //data is sorted ASC on treatment ID, so 17-AAG is first:
        Assert.assertEquals("17-AAG", treatment.getStableId());
    }

    @Test
    public void getTreatmentsWithStudyListAscending() {
        List<String> studyIds = Arrays.asList("study_tcga_pub");
        List<Treatment> result = treatmentMyBatisRepository.getTreatmentsInStudies(studyIds, PersistenceConstants.SUMMARY_PROJECTION, 0, 0, "stableId", "ASC");
        
        //expect: ordered ASC by treatment id:
        Treatment treatment = result.get(0);
        Assert.assertEquals("17-AAG", treatment.getStableId());
        Assert.assertEquals("https://en.wikipedia.org/wiki/Tanespimycin", treatment.getRefLink());
        treatment = result.get(1);
        Assert.assertEquals("AEW541", treatment.getStableId());
        Assert.assertEquals("TrkA/B/C inhibitor", treatment.getDescription());

    }
    @Test

    public void getTreatmentsWithStudyListDescending() {
        List<String> studyIds = Arrays.asList("study_tcga_pub");
        List<Treatment> result = treatmentMyBatisRepository.getTreatmentsInStudies(studyIds, PersistenceConstants.SUMMARY_PROJECTION, 0, 0, "stableId", "DESC");
        
        //expect: ordered ASC by treatment id:
        Treatment treatment = result.get(0);
        Assert.assertEquals("AEW541", treatment.getStableId());
        Assert.assertEquals("TrkA/B/C inhibitor", treatment.getDescription());
        treatment = result.get(1);
        Assert.assertEquals("17-AAG", treatment.getStableId());
        Assert.assertEquals("https://en.wikipedia.org/wiki/Tanespimycin", treatment.getRefLink());

    }

}