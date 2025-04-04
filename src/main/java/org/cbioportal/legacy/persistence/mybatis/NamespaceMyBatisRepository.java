package org.cbioportal.legacy.persistence.mybatis;

import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;
import org.cbioportal.legacy.model.NamespaceData;
import org.cbioportal.legacy.model.NamespaceDataCount;
import org.cbioportal.legacy.persistence.NamespaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class NamespaceMyBatisRepository implements NamespaceRepository {

    private final NamespaceMapper namespaceMapper;

    @Autowired
    public NamespaceMyBatisRepository(NamespaceMapper namespaceMapper){
        this.namespaceMapper = namespaceMapper;
    }

    @Override
    public List<NamespaceAttribute> getNamespaceOuterKey(List<String> studyIds) {

        return namespaceMapper.getNamespaceOuterKey(studyIds);
    }

    @Override
    public List<NamespaceAttribute> getNamespaceInnerKey(String outerKey, List<String> studyIds) {

        return namespaceMapper.getNamespaceInnerKey(outerKey,studyIds);
    }

    @Override
    public List<NamespaceAttributeCount> getNamespaceAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes) {

        return namespaceMapper.getNamespaceAttributeCountsBySampleIds(studyIds, sampleIds, namespaceAttributes);
    }

    @Override
    public List<NamespaceDataCount> getNamespaceDataCounts(List<String> studyIds, List<String> sampleIds, String outerKey, String innerKey) {

        return namespaceMapper.getNamespaceDataCounts(studyIds, sampleIds, outerKey, innerKey);
    }

    @Override
    public List<NamespaceData> getNamespaceData(List<String> studyIds, List<String> sampleIds, String outerKey, String innerKey) {

        return namespaceMapper.getNamespaceData(studyIds, sampleIds, outerKey, innerKey);
    }

    @Override
    public List<NamespaceData> getNamespaceDataForComparison(List<String> studyIds, List<String> sampleIds, String outerKey, String innerKey, String value) {

        return namespaceMapper.getNamespaceDataForComparison(studyIds, sampleIds, outerKey, innerKey, value);
    }
}
