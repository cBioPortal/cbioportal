package org.cbioportal.shared.util;

import java.util.List;
import java.util.stream.Collectors;
import org.cbioportal.legacy.model.ClinicalDataCount;
import org.cbioportal.legacy.model.ClinicalDataCountItem;
import org.cbioportal.legacy.service.util.StudyViewColumnarServiceUtil;

public abstract class ClinicalDataCountItemUtil {
  private ClinicalDataCountItemUtil() {}

  public static List<ClinicalDataCountItem> generateDataCountItems(
      List<ClinicalDataCount> dataCounts) {
    return dataCounts.stream()
        .collect(Collectors.groupingBy(ClinicalDataCount::getAttributeId))
        .entrySet()
        .parallelStream()
        .map(
            e -> {
              ClinicalDataCountItem item = new ClinicalDataCountItem();
              item.setAttributeId(e.getKey());
              item.setCounts(StudyViewColumnarServiceUtil.normalizeDataCounts(e.getValue()));
              return item;
            })
        .toList();
  }
}
