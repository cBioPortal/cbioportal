package org.cbioportal.domain.generic_assay.usecase;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
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
    when(repository.getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, ID_LIST, null, null, null))
        .thenReturn(mockList);

    List<GenericAssayMeta> result =
        useCase.execute(ID_LIST, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(mockList.get(0).getStableId(), result.get(0).getStableId());
    Assert.assertEquals(mockList.get(1).getStableId(), result.get(1).getStableId());
    verify(repository).getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, ID_LIST, null, null, null);
  }

  @Test
  public void execute_profilesOnly_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    when(repository.getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, null, null, null, null))
        .thenReturn(mockList);

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
    verify(repository, never()).getGenericAssayMetaByProfileIds(any(), any());
  }

  @Test
  public void execute_stableIdsOnly_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    when(repository.getGenericAssayMetaByStableIds(ID_LIST, null, null, null)).thenReturn(mockList);

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

  @Test
  public void execute_profilesOnly_summaryProjection_withSearchAndPaging() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    when(repository.getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, null, "tp53", 100, 100))
        .thenReturn(mockList);

    List<GenericAssayMeta> result =
        useCase.execute(
            null, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION, "tp53", 100, 1);

    Assert.assertEquals(2, result.size());
    verify(repository).getGenericAssayMetaByProfileIds(PROFILE_ID_LIST, null, "tp53", 100, 100);
  }

  @Test
  public void count_profilesOnly_summaryProjection_usesRepositoryCount() {
    when(repository.countGenericAssayMetaByProfileIds(PROFILE_ID_LIST, null, "tp53"))
        .thenReturn(42);

    Integer result =
        useCase.count(null, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION, "tp53");

    Assert.assertEquals(Integer.valueOf(42), result);
    verify(repository).countGenericAssayMetaByProfileIds(PROFILE_ID_LIST, null, "tp53");
  }

  @Test
  public void count_profilesOnly_idProjection_matchesStableIdOnly() {
    // GENERIC_ASSAY_ID_2 does not contain "generic_assay_id_1", so only ID_1 should match;
    // if this incorrectly delegated to the repository's name/description-aware count, this
    // distinction would be lost.
    when(repository.getGenericAssayStableIdsByProfileIds(PROFILE_ID_LIST)).thenReturn(ID_LIST);

    Integer result = useCase.count(null, PROFILE_ID_LIST, "ID", GENERIC_ASSAY_ID_1);

    Assert.assertEquals(Integer.valueOf(1), result);
    verify(repository, never()).countGenericAssayMetaByProfileIds(any(), any(), any());
  }

  @Test
  public void count_stableIdsOnly_idProjection_matchesStableIdOnly() {
    Integer result = useCase.count(ID_LIST, null, "ID", GENERIC_ASSAY_ID_2);

    Assert.assertEquals(Integer.valueOf(1), result);
    verifyNoInteractions(repository);
  }

  @Test
  public void execute_stableIdsOnly_idProjection_withSearchTerm_ignoresNullStableId() {
    // a malformed request can legally contain a null entry in genericAssayStableIds
    // (GenericAssayMetaFilter only validates list size, not element nullability); it
    // must be filtered out rather than throwing when a searchTerm is also supplied
    List<String> idsWithNull = Arrays.asList(GENERIC_ASSAY_ID_1, null, GENERIC_ASSAY_ID_2);

    List<GenericAssayMeta> result = useCase.execute(idsWithNull, null, "ID", "id_1", null, null);

    Assert.assertEquals(1, result.size());
    Assert.assertEquals(GENERIC_ASSAY_ID_1, result.get(0).getStableId());
  }

  @Test
  public void count_stableIdsOnly_idProjection_withSearchTerm_ignoresNullStableId() {
    List<String> idsWithNull = Arrays.asList(GENERIC_ASSAY_ID_1, null, GENERIC_ASSAY_ID_2);

    Integer result = useCase.count(idsWithNull, null, "ID", "generic_assay_id");

    Assert.assertEquals(Integer.valueOf(2), result);
  }
}
