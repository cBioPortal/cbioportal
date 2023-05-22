package org.cbioportal.persistence.mybatiscolumnstore;

import org.cbioportal.model.AlterationCountByGene;
import org.cbioportal.model.Sample;
import org.cbioportal.webparam.StudyViewFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/testContextDatabaseClickhouse.xml")
@Configurable
public class StudyViewMyBatisRepositoryTest {

    @Autowired
    private StudyViewMyBatisRepository studyViewMyBatisRepository;

    @Test
    public void getFilteredSamples() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();

        List<String> studyIds = new ArrayList<>();
        studyIds.add("msk_ch_2020");
        studyViewFilter.setStudyIds(studyIds);
        
        List<Sample> samples = studyViewMyBatisRepository.getFilteredSamplesFromColumnstore(studyViewFilter);
        Assert.assertEquals(3, samples.size());
    }

    @Test
    public void getMutatedGenes() {
        StudyViewFilter studyViewFilter = new StudyViewFilter();

        List<String> studyIds = new ArrayList<>();
        studyIds.add("msk_ch_2020");
        studyViewFilter.setStudyIds(studyIds);

        List<AlterationCountByGene> mutations = studyViewMyBatisRepository.getMutatedGenes(studyViewFilter);
        Assert.assertEquals(2, mutations.size());
    }

}