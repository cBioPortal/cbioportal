package org.cbioportal.persistence.mysql;

import org.cbioportal.model.*;
import org.cbioportal.persistence.AlterationDriverAnnotationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@Profile("mysql")
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