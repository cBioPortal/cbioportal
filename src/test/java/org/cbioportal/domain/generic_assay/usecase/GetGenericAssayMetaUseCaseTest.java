package org.cbioportal.domain.generic_assay.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
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

  /** Collects everything the use case streams to its consumer. */
  private List<GenericAssayMeta> collect(
      List<String> stableIds, List<String> molecularProfileIds, String projection) {
    List<GenericAssayMeta> collected = new ArrayList<>();
    useCase.execute(stableIds, molecularProfileIds, projection, collected::add);
    return collected;
  }

  /** Stubs the repository's profile-based streaming method to emit the given items. */
  private void streamProfileMeta(List<GenericAssayMeta> items) {
    doAnswer(
            invocation -> {
              Consumer<GenericAssayMeta> consumer = invocation.getArgument(2);
              items.forEach(consumer);
              return null;
            })
        .when(repository)
        .getGenericAssayMetaByProfileIds(any(), any(), any());
  }

  /** Stubs the repository's stable-id-based streaming method to emit the given items. */
  private void streamStableIdMeta(List<GenericAssayMeta> items) {
    doAnswer(
            invocation -> {
              Consumer<GenericAssayMeta> consumer = invocation.getArgument(1);
              items.forEach(consumer);
              return null;
            })
        .when(repository)
        .getGenericAssayMetaByStableIds(any(), any());
  }

  @Test
  public void execute_profilesAndStableIds_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    streamProfileMeta(mockList);

    List<GenericAssayMeta> result =
        collect(ID_LIST, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(mockList.get(0).getStableId(), result.get(0).getStableId());
    Assert.assertEquals(mockList.get(1).getStableId(), result.get(1).getStableId());
    verify(repository).getGenericAssayMetaByProfileIds(eq(PROFILE_ID_LIST), eq(ID_LIST), any());
  }

  @Test
  public void execute_profilesWithNullElement_filtersNullsWithoutThrowing() {
    // A null element previously NPE'd in sorted() -> 500. It must now be filtered out.
    streamProfileMeta(createMockMetaList());

    collect(null, Arrays.asList(null, PROFILE_ID), PersistenceConstants.SUMMARY_PROJECTION);

    verify(repository).getGenericAssayMetaByProfileIds(eq(PROFILE_ID_LIST), eq(null), any());
  }

  @Test
  public void execute_profilesOnly_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    streamProfileMeta(mockList);

    List<GenericAssayMeta> result =
        collect(null, PROFILE_ID_LIST, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(mockList.get(0).getStableId(), result.get(0).getStableId());
    Assert.assertEquals(mockList.get(1).getStableId(), result.get(1).getStableId());
    verify(repository).getGenericAssayMetaByProfileIds(eq(PROFILE_ID_LIST), eq(null), any());
  }

  @Test
  public void execute_profilesOnly_idProjection() {
    when(repository.getGenericAssayStableIdsByProfileIds(PROFILE_ID_LIST)).thenReturn(ID_LIST);

    List<GenericAssayMeta> result = collect(null, PROFILE_ID_LIST, "ID");

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(GENERIC_ASSAY_ID_1, result.get(0).getStableId());
    Assert.assertEquals(GENERIC_ASSAY_ID_2, result.get(1).getStableId());
    Assert.assertNull(result.get(0).getEntityType());
    verify(repository).getGenericAssayStableIdsByProfileIds(PROFILE_ID_LIST);
    verify(repository, never()).getGenericAssayMetaByProfileIds(any(), any(), any());
  }

  @Test
  public void execute_stableIdsOnly_summaryProjection() {
    List<GenericAssayMeta> mockList = createMockMetaList();
    streamStableIdMeta(mockList);

    List<GenericAssayMeta> result = collect(ID_LIST, null, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(mockList.get(0).getStableId(), result.get(0).getStableId());
    Assert.assertEquals(
        mockList.get(0).getGenericEntityMetaProperties(),
        result.get(0).getGenericEntityMetaProperties());
    verify(repository).getGenericAssayMetaByStableIds(eq(ID_LIST), any());
  }

  @Test
  public void execute_stableIdsOnly_idProjection() {
    List<GenericAssayMeta> result = collect(ID_LIST, null, "ID");

    Assert.assertEquals(2, result.size());
    Assert.assertEquals(GENERIC_ASSAY_ID_1, result.get(0).getStableId());
    Assert.assertEquals(GENERIC_ASSAY_ID_2, result.get(1).getStableId());
    verifyNoInteractions(repository);
  }

  @Test
  public void execute_bothNull_returnsEmpty() {
    List<GenericAssayMeta> result = collect(null, null, PersistenceConstants.SUMMARY_PROJECTION);

    Assert.assertTrue(result.isEmpty());
    verifyNoInteractions(repository);
  }
}
