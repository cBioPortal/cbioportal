package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.*;
import org.cbioportal.persistence.AlterationDriverAnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class AlterationDriverAnnotationMyBatisRepository implements AlterationDriverAnnotationRepository {

    @Autowired
    private AlterationDriverAnnotationMapper alterationDriverAnnotationMapper;

    @Override
    public List<AlterationDriverAnnotation> getAlterationDriverAnnotations(
        List<String> molecularProfileIds) {

        if (molecularProfileIds == null || molecularProfileIds.isEmpty()) {
            return Collections.emptyList();
        }

        return alterationDriverAnnotationMapper.getAlterationDriverAnnotations(molecularProfileIds);
    }

}