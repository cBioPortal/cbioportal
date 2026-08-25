package org.cbioportal.application.rest.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import org.cbioportal.legacy.model.Mutation;
import org.junit.Test;

public class MutationMapperTest {

  @Test
  public void mapsAnnotationJsonToNamespaceColumns() {
    Mutation mutation = new Mutation();
    mutation.setMolecularProfileId("profile_1");
    mutation.setSampleId("sample_1");
    mutation.setPatientId("patient_1");
    mutation.setStudyId("study_1");
    mutation.setEntrezGeneId(1);
    mutation.setAnnotationJSON("{\"columnName\":{\"fieldName\":\"fieldValue\"}}");

    var dto = MutationMapper.INSTANCE.toMutationDTOO(mutation);

    assertNotNull(dto.namespaceColumns());
    assertEquals(
        "fieldValue", ((Map<?, ?>) dto.namespaceColumns().get("columnName")).get("fieldName"));
  }
}
