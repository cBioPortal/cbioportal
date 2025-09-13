package org.cbioportal.infrastructure.repository.clickhouse.mutation;

import java.util.List;

public record ProfileSamplePair(String molecularProfileId, List<String> sampleIds) {
}
