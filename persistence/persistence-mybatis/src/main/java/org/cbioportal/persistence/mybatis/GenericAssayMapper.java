package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.meta.GenericAssayMeta;

public interface GenericAssayMapper {

    List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);

}