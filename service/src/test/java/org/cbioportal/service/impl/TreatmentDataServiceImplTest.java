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

import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.Sample;
import org.cbioportal.model.TreatmentMolecularAlteration;
import org.cbioportal.model.TreatmentMolecularData;
import org.cbioportal.persistence.MolecularDataRepository;
import org.cbioportal.service.MolecularProfileService;
import org.cbioportal.service.SampleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TreatmentDataServiceImplTest extends BaseServiceImplTest {

    @InjectMocks
    private TreatmentDataServiceImpl treatmentDataService;

    @Mock
    private MolecularDataRepository geneticDataRepository;
    @Mock
    private SampleService sampleService;
    @Mock
    private MolecularProfileService geneticProfileService;

    /**
     * This is executed n times, for each of the n test methods below:
     * @throws Exception 
     * @throws DaoException
     */
    @Before 
    public void setUp() throws Exception {
        //stub for samples
        Mockito.when(geneticDataRepository.getCommaSeparatedSampleIdsOfMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(
                "1,2,");

        List<Sample> sampleList1 = new ArrayList<>();
        Sample sample = new Sample();
        sample.setInternalId(1);
        sample.setStableId(SAMPLE_ID1);
        sampleList1.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID), Arrays.asList(SAMPLE_ID1), "ID"))
            .thenReturn(sampleList1);
        List<Sample> sampleListAll = new ArrayList<>(sampleList1);
        sample = new Sample();
        sample.setInternalId(2);
        sample.setStableId(SAMPLE_ID2);
        sampleListAll.add(sample);
        Mockito.when(sampleService.fetchSamples(Arrays.asList(STUDY_ID, STUDY_ID), Arrays.asList(SAMPLE_ID1, SAMPLE_ID2), "ID"))
            .thenReturn(sampleListAll);

        //stub for genetic profile
        MolecularProfile geneticProfile = new MolecularProfile();
        geneticProfile.setCancerStudyIdentifier(STUDY_ID);
        Mockito.when(geneticProfileService.getMolecularProfile(MOLECULAR_PROFILE_ID)).thenReturn(geneticProfile);

        //stub for repository data
        List<TreatmentMolecularAlteration> treatmentGeneticAlterationList = new ArrayList<>();

        TreatmentMolecularAlteration treatmentGeneticAlteration1 = new TreatmentMolecularAlteration();
        treatmentGeneticAlteration1.setTreatmentId(GENESET_ID1);
        treatmentGeneticAlteration1.setValues("0.2,0.499");

        TreatmentMolecularAlteration treatmentGeneticAlteration2 = new TreatmentMolecularAlteration();
        treatmentGeneticAlteration2.setTreatmentId(GENESET_ID2);
        treatmentGeneticAlteration2.setValues("0.89,-0.509");

        treatmentGeneticAlterationList.add(treatmentGeneticAlteration1);
        treatmentGeneticAlterationList.add(treatmentGeneticAlteration2);

        List<Integer> entrezGeneIds = new ArrayList<>();
        entrezGeneIds.add(ENTREZ_GENE_ID_1);
        Mockito.when(geneticDataRepository.getTreatmentMolecularAlterations(MOLECULAR_PROFILE_ID, Arrays.asList(GENESET_ID1, GENESET_ID2), "SUMMARY"))
            .thenReturn(treatmentGeneticAlterationList);
    }


    @Test
    public void fetchTreatmentData() throws Exception {

        List<TreatmentMolecularData> result = treatmentDataService.fetchTreatmentData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1, SAMPLE_ID2),
                Arrays.asList(GENESET_ID1, GENESET_ID2));

        //what we expect: 2 samples x 2 treatment items = 4 TreatmentData items:
        //SAMPLE_1:
        //   treatment1 value: 0.2
        //   treatment2 value: 0.89
        //SAMPLE_2:
        //   treatment1 value: 0.499
        //   treatment2 value: -0.509
        Assert.assertEquals(4, result.size());
        TreatmentMolecularData item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getTreatmentId(), GENESET_ID1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        TreatmentMolecularData item2 = result.get(1);
        Assert.assertEquals(item2.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item2.getTreatmentId(), GENESET_ID2);
        Assert.assertEquals(item2.getValue(), "0.89");
        Assert.assertEquals(item2.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        TreatmentMolecularData item4 = result.get(3);
        Assert.assertEquals(item4.getSampleId(), SAMPLE_ID2);
        Assert.assertEquals(item4.getTreatmentId(), GENESET_ID2);
        Assert.assertEquals(item4.getValue(), "-0.509");
        Assert.assertEquals(item4.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
        
        //check when selecting only 1 sample:
        result = treatmentDataService.fetchTreatmentData(MOLECULAR_PROFILE_ID, Arrays.asList(SAMPLE_ID1),
        Arrays.asList(GENESET_ID1, GENESET_ID2));
        Assert.assertEquals(2, result.size());
        item1 = result.get(0);
        Assert.assertEquals(item1.getSampleId(), SAMPLE_ID1);
        Assert.assertEquals(item1.getTreatmentId(), GENESET_ID1);
        Assert.assertEquals(item1.getValue(), "0.2");
        Assert.assertEquals(item1.getMolecularProfileId(), MOLECULAR_PROFILE_ID);
    }
}