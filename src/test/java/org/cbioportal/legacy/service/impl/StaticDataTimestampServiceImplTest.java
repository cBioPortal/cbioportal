package org.cbioportal.legacy.service.impl;

import java.util.Collections;
import java.util.HashMap;
import org.cbioportal.legacy.model.TableTimestampPair;
import org.cbioportal.legacy.persistence.StaticDataTimeStampRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StaticDataTimestampServiceImplTest extends BaseServiceImplTest {
  @InjectMocks private StaticDataTimestampServiceImpl infoService;

  @Mock private StaticDataTimeStampRepository repository;

  @Test
  public void TestGetTimestamps() {
    HashMap<String, String> pairs = new HashMap<>();
    pairs.put("gene", "2019-11-11 08:41:15");
    TableTimestampPair pair = new TableTimestampPair();
    pair.setTableName("gene");
    pair.setUpdateTime("2019-11-11 08:41:15");

    Mockito.when(repository.getTimestamps(Mockito.anyList()))
        .thenReturn(Collections.singletonList(pair));

    Assert.assertEquals(infoService.getTimestamps(Collections.singletonList("gene")), pairs);
  }
}
