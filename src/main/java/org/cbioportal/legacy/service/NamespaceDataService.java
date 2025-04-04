package org.cbioportal.legacy.service;

import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.model.NamespaceDataCountItem;
import org.cbioportal.legacy.web.parameter.NamespaceDataFilter;
import java.util.List;
    
public interface NamespaceDataService {

    List<NamespaceData> fetchNamespaceData(List<String> studyIds, List<String> sampleIds, List<NamespaceDataFilter> namespaceDataFilters);

    List<NamespaceData> fetchNamespaceDataForComparison(List<String> studyIds, List<String> sampleIds, NamespaceAttribute namespaceAttribute, List <String> values);

    List<NamespaceDataCountItem> fetchNamespaceDataCounts(List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes);

}
