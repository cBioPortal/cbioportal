package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.GeneMolecularAlteration;
import org.cbioportal.model.GenesetMolecularAlteration;
import org.cbioportal.model.meta.GenericAssayMeta;
import org.cbioportal.persistence.GenericAssayRepository;
import org.cbioportal.persistence.MolecularDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.Arrays;
import java.util.List;

@Repository
public class GenericAssayMyBatisRepository implements GenericAssayRepository {

    @Autowired
    private GenericAssayMapper genericAssayMapper;

    @Override
    public List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds) {

        return genericAssayMapper.getGenericAssayMeta(stableIds);
    }
}