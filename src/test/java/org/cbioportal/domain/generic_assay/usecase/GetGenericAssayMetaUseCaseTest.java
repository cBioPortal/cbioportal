package org.cbioportal.domain.generic_assay.usecase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.cbioportal.domain.generic_assay.repository.GenericAssayRepository;
import org.cbioportal.legacy.model.meta.GenericAssayMeta;
import org.cbioportal.legacy.persistence.PersistenceConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetGenericAssayMetaUseCaseTest {

  private static final String PROFILE_ID = "test_profile_id";
  private static final List<String> PROFILE_ID_LIST = Arrays.asList(PROFILE_ID);
  private static final String GENERIC_ASSAY_ID_1 = "generic_assay_id_1";
  private static final String GENERIC_ASSAY_ID_2 = "generic_assay_id_2";
  private static final List<String> ID_LIST = Arrays.asList(GENERIC_ASSAY_ID_1, GENERIC_ASSAY_ID_2);
  private static final String ENTITY_TYPE = "GENERIC_ASSAY";
  private static final String PROPERTY_NAME_1 = "property_name_1";
  private static final String PROPERTY_VALUE_1 = "property_value_1";
  private static final String PROPERTY_NAME_2 = "property_name_2";
  private static final String PROPERTY_VALUE_2 = "property_value_2";

  @InjectMocks private GetGenericAssayMetaUseCase useCase;

  @Mock private GenericAssayRepository repository;

  private List<GenericAssayMeta> createMockMetaList() {
    GenericAssayMeta meta1 = new GenericAssayMeta(ENTITY_TYPE, GENERIC_ASSAY_ID_1);
    HashMap<String, String> map1 = new HashMap<>();
    map1.put(PROPERTY_NAME_1, PROPERTY_VALUE_1);
    meta1.setGenericEntityMetaProperties(map1);

    GenericAssayMeta meta2 = new GenericAssayMeta(ENTITY_TYPE, GENERIC_ASSAY_ID_2);
    HashMap<String, String> map2 = new HashMap<>();
    map2.put(PROPERTY_NAME_2, PROPERTY_VALUE_2);
    meta2.setGenericEntityMetaProperties(map2);

    return Arrays.asList(meta1, meta2);
  }

  @Test
  public void execute_profilesAndStableIds_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    when(repository.getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, ID_LIST)).thenReturn(mockList);

    List<GenericAssayMeta> result =
        useCase.execute(ID_LIST, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(mockList.get(0).getStableId(), result.get(0).getStableId());
    Assert.assertEquals(mockList.get(1).getStableId(), result.get(1).getStableId());
    verify(repository).getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, ID_LIST);
  }

  @Test
  public void execute_profilesOnly_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    when(repository.getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, null)).thenReturn(mockList);

    List<GenericAssayMeta> result =
        useCase.execute(null, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(mockList.get(0).getStableId(), result.get(0).getStableId());
    Assert.assertEquals(mockList.get(1).getStableId(), result.get(1).getStableId());
  }

  @Test
  public void execute_profilesOnly_idProjection() {
    when(repository.getGenericAssayStableIdsByProfileIds(PROFILE_ID_LIST)).thenReturn(ID_LIST);

    List<GenericAssayMeta> result = useCase.execute(null, PROFILE_ID_LIST, "ID");

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(GENERIC_ASSAY_ID_1, result.get(0).getStableId());
    Assert.assertEquals(GENERIC_ASSAY_ID_2, result.get(1).getStableId());
    Assert.assertNull(result.get(0).getEntityType());
    verify(repository).getGenericAssayStableIdsByProfileIds(PROFILE_ID_LIST);
    verify(repository, org.mockito.Mockito.never())
        .getGenericAssayMetaByProfileIds(org.mockito.Mockito.any(), org.mockito.Mockito.any());
  }

  @Test
  public void execute_stableIdsOnly_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    when(repository.getGenericAssayMetaByStableIds(ID_LIST)).thenReturn(mockList);

    List<GenericAssayMeta> result =
        useCase.execute(ID_LIST, null, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(mockList.get(0).getStableId(), result.get(0).getStableId());
    Assert.assertEquals(
        mockList.get(0).getGenericEntityMetaProperties(),
        result.get(0).getGenericEntityMetaProperties());
  }

  @Test
  public void execute_stableIdsOnly_idProjection() {
    List<GenericAssayMeta> result = useCase.execute(ID_LIST, null, "ID");

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(GENERIC_ASSAY_ID_1, result.get(0).getStableId());
    Assert.assertEquals(GENERIC_ASSAY_ID_2, result.get(1).getStableId());
    verifyNoInteractions(repository);
  }

  @Test
  public void execute_bothNull_returnsEmpty() {
    List<GenericAssayMeta> result =
        useCase.execute(null, null, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(Collections.emptyList(), result);
    verifyNoInteractions(repository);
  }
}
