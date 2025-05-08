package org.cbioportal.legacy.persistence.mybatis;

import java.util.List;
import org.cbioportal.legacy.model.VariantCount;

public interface VariantCountMapper {

  List<VariantCount> fetchVariantCounts(
      String molecularProfileId, List<Integer> entrezGeneIds, List<String> keywords);
}
