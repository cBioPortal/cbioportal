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
package org.cbioportal.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cbioportal.model.Treatment;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.cbioportal.service.exception.TreatmentNotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TreatmentServiceImplTest extends BaseServiceImplTest {

    public static final String TREATMENT_ID_1 = "treatment_id_1";
    public static final String STUDY_ID_1 = "study_id_1";
    private static final int INTERNAL_ID_1 = 1;
    public static final String TREATMENT_ID_2 = "treatment_id_2";
    public static final String STUDY_ID_2 = "study_id_2";
    private static final int INTERNAL_ID_2 = 2;

    private static final List<String> idList = Arrays.asList(TREATMENT_ID_1, TREATMENT_ID_2);
    private static final  List<Treatment> mockTreatmentList = createTreatmentList();

    @InjectMocks
    private TreatmentServiceImpl treatmentService;

    @Mock
    private TreatmentRepository treatmentRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService geneticProfileService;

    @Test
    public void getAllTreatments() {

        List<Treatment> treatmentList = createTreatmentList();
        Mockito.when(treatmentRepository.getAllTreatments(PROJECTION, PAGE_SIZE, PAGE_NUMBER))
            .thenReturn(treatmentList);

        List<Treatment> result = treatmentService.getAllTreatments(PROJECTION, PAGE_SIZE, PAGE_NUMBER);

        Assert.assertEquals(treatmentList, result);
    }

    @Test
    public void getMetaTreatments() {

        BaseMeta expectedBaseMeta = new BaseMeta();
        Mockito.when(treatmentRepository.getMetaTreatments()).thenReturn(expectedBaseMeta);
        BaseMeta result = treatmentService.getMetaTreatments();

        Assert.assertEquals(expectedBaseMeta, result);
    }

    @Test
    public void getTreatment() throws TreatmentNotFoundException {

        Treatment treatment = mockTreatmentList.get(0);
        Mockito.when(treatmentRepository.getTreatmentByStableId(TREATMENT_ID_1))
            .thenReturn(treatment);

        Treatment result = treatmentService.getTreatment(TREATMENT_ID_1);
        Assert.assertEquals(treatment, result);
    }
    
    @Test(expected = TreatmentNotFoundException.class)
    public void getTreatmentByStableIdNotFound() throws TreatmentNotFoundException {

        Treatment treatment = mockTreatmentList.get(0);
        Mockito.when(treatmentRepository.getTreatmentByStableId(TREATMENT_ID_1))
            .thenReturn(treatment);
        //expect TreatmentNotFoundException here:
        treatmentService.getTreatment("wrongId");
    }

    @Test
    public void fetchTreatments() {
        Mockito.when(treatmentRepository.fetchTreatments(Mockito.anyList()))
            .thenReturn(mockTreatmentList);
        Assert.assertEquals(treatmentService.fetchTreatments(idList), mockTreatmentList );
    }

    @Test
    public void getMetaTreatmentsInStudies() {
        BaseMeta baseMetaStudy = new BaseMeta();
        baseMetaStudy.setTotalCount(1);
        Mockito.when(treatmentRepository.getMetaTreatmentsInStudies(Mockito.anyList()))
            .thenReturn(baseMetaStudy);
        Assert.assertEquals(treatmentService.getMetaTreatmentsInStudies(idList).getTotalCount(), (Integer) 1 );
    }

    @Test
    public void getTreatments() {
        Mockito.when(treatmentRepository.getTreatments(Mockito.anyList(), Mockito.anyString()))
            .thenReturn(mockTreatmentList);
        Assert.assertEquals(treatmentService.getTreatments(idList, "SUMMARY"), mockTreatmentList );
    }

    private static List<Treatment> createTreatmentList() {

        List<Treatment> treatmentList = new ArrayList<>();

        Treatment treatment1 = new Treatment();
        treatment1.setId(INTERNAL_ID_1);
        treatment1.setStableId(TREATMENT_ID_1);
        treatmentList.add(treatment1);

        Treatment treatment2 = new Treatment();
        treatment2.setId(INTERNAL_ID_2);
        treatment2.setStableId(TREATMENT_ID_2);
        treatmentList.add(treatment2);
        return treatmentList;
    }

}
