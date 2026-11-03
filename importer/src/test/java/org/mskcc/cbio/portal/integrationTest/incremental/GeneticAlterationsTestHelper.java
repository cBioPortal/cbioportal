package org.mskcc.cbio.portal.integrationTest.incremental;

import java.util.*;
import java.util.stream.Collectors;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.dao.DaoGeneticEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeneticAlterationsTestHelper {
    public static Set<Integer> geneStableIdsToEntityIds(Set<String> beforeStableIds) {
        return beforeStableIds.stream().map(stableId -> {
            try {
                return geneStableIdToEntityId(stableId);
            } catch (DaoException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toSet());
    }

    public static int geneStableIdToEntityId(String stableId) throws DaoException {
        return DaoGeneticEntity.getGeneticEntityByStableId(stableId).getId();
    }

    public static <N> void assertPriorDataState(HashMap<N, HashMap<Integer, String>> beforeResult, Set<N> expectedEntityIds, Set<Integer> expectedSampleIds) {
        assertEquals(expectedEntityIds, beforeResult.keySet());
        beforeResult.forEach((entityId, sampleIdToValue) -> {
            assertEquals("Samples for gene with entityId = " + entityId + " have to match expected ones",
                    expectedSampleIds, beforeResult.get(entityId).keySet());
        });
    }

    public static <N> void assertNoChange(HashMap<N, HashMap<Integer, String>> beforeResult,
                                          HashMap<N, HashMap<Integer, String>> afterResult,
                                          Set<N> entityIds,
                                          Set<Integer> sampleIds) {
        entityIds.forEach(entityId -> {
            assertTrue("After result is expected to contain entityId=" + entityId,
                    afterResult.containsKey(entityId));
            sampleIds.forEach(sampleId -> {
                assertTrue("Sample_id=" + sampleId + " expected to be found for gene with entityId=" + entityId,
                        afterResult.get(entityId).containsKey(sampleId));
                assertEquals("The values for sample_id=" + sampleId +
                                " and entityId=" + entityId + " before and after upload have to match.",
                        beforeResult.get(entityId).get(sampleId), afterResult.get(entityId).get(sampleId));
            });
        });
    }

}
