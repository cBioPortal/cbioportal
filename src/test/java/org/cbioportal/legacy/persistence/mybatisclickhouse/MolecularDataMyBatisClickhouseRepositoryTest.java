package org.cbioportal.legacy.persistence.mybatisclickhouse;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

import java.util.*;
import org.cbioportal.legacy.model.MolecularDataRowPerSample;
import org.cbioportal.legacy.model.MolecularProfileSamples;
import org.cbioportal.legacy.model.Sample;
import org.cbioportal.legacy.persistence.mybatis.MolecularDataMyBatisRepository;
import org.cbioportal.legacy.service.SampleService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MolecularDataMyBatisClickhouseRepositoryTest {

  @Mock private MolecularDataMapper mapper;
  @Mock private MolecularDataMyBatisRepository legacyRepo;
  @Mock private SampleService sampleService;

  @InjectMocks private MolecularDataMyBatisClickhouseRepository repository;

  @Before
  public void setup() {
    // nothing
  }

  @Test
  public void getGeneMolecularAlterationsInMultipleMolecularProfilesAggregates() {
    Set<String> profiles = new TreeSet<>(Arrays.asList("profileA", "profileB"));
    List<Integer> genes = Arrays.asList(1);

    // profile samples
    MolecularProfileSamples mpsA = new MolecularProfileSamples();
    mpsA.setMolecularProfileId("profileA");
    mpsA.setCommaSeparatedSampleIds("1,2,");
    MolecularProfileSamples mpsB = new MolecularProfileSamples();
    mpsB.setMolecularProfileId("profileB");
    mpsB.setCommaSeparatedSampleIds("3,");

    when(legacyRepo.commaSeparatedSampleIdsOfMolecularProfilesMap(profiles))
        .thenReturn(Map.of("profileA", mpsA, "profileB", mpsB));

    // samples
    Sample s1 = new Sample();
    s1.setInternalId(1);
    s1.setStableId("s1");
    s1.setCancerStudyIdentifier("study");
    Sample s2 = new Sample();
    s2.setInternalId(2);
    s2.setStableId("s2");
    s2.setCancerStudyIdentifier("study");
    Sample s3 = new Sample();
    s3.setInternalId(3);
    s3.setStableId("s3");
    s3.setCancerStudyIdentifier("study");
    when(sampleService.getSamplesByInternalIds(anyList())).thenReturn(Arrays.asList(s1, s2, s3));

    // clickhouse rows
    MolecularDataRowPerSample r1 = new MolecularDataRowPerSample();
    r1.setEntrezGeneId(1);
    r1.setMolecularProfileId("profileA");
    r1.setSampleUniqueId("study_s1");
    r1.setValue("1");
    MolecularDataRowPerSample r2 = new MolecularDataRowPerSample();
    r2.setEntrezGeneId(1);
    r2.setMolecularProfileId("profileA");
    r2.setSampleUniqueId("study_s2");
    r2.setValue("0");
    MolecularDataRowPerSample r3 = new MolecularDataRowPerSample();
    r3.setEntrezGeneId(1);
    r3.setMolecularProfileId("profileB");
    r3.setSampleUniqueId("study_s3");
    r3.setValue("1");

    when(mapper.getGeneMolecularAlterationsPerSampleInMultipleMolecularProfiles(anySet(), anyList()))
        .thenReturn(Arrays.asList(r1, r2, r3));

    var results = repository.getGeneMolecularAlterationsInMultipleMolecularProfiles(profiles, genes, "projection");
    verify(mapper, times(1)).getGeneMolecularAlterationsPerSampleInMultipleMolecularProfiles(anySet(), anyList());
    Assert.assertEquals(2, results.size());
    // profileA should have 2 sample values: s1=1,s2=0
    // profileB should have 1 sample value: s3=1
  }
}
