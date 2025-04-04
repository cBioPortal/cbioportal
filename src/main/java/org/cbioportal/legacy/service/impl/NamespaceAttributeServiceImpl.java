package org.cbioportal.legacy.service.impl;

import org.cbioportal.legacy.model.NamespaceAttribute;
import org.cbioportal.legacy.model.NamespaceAttributeCount;
import org.cbioportal.legacy.persistence.NamespaceRepository;
import org.cbioportal.legacy.service.NamespaceAttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NamespaceAttributeServiceImpl implements NamespaceAttributeService {

    private final NamespaceRepository namespaceRepository;

    @Autowired
    public NamespaceAttributeServiceImpl(NamespaceRepository namespaceRepository){
        this.namespaceRepository = namespaceRepository;
    }

    @Override
    public List<NamespaceAttribute> fetchNamespaceAttributes(List<String> studyIds) {

        List<NamespaceAttribute> outerNamespaceKeys = namespaceRepository.getNamespaceOuterKey(studyIds);

        return outerNamespaceKeys.stream()
            .flatMap(outerNamespaceKey -> namespaceRepository.getNamespaceInnerKey(outerNamespaceKey.getOuterKey(), studyIds).stream())
            .toList();
    }

    @Override
    public List<NamespaceAttributeCount> fetchNamespaceAttributeCountsBySampleIds(List<String> studyIds, List<String> sampleIds, List<NamespaceAttribute> namespaceAttributes) {

        return namespaceRepository.getNamespaceAttributeCountsBySampleIds(studyIds, sampleIds, namespaceAttributes);
    }

}
