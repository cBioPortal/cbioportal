package org.cbioportal.persistence;

import java.util.List;

import org.cbioportal.model.meta.GenericAssayMeta;

public interface GenericAssayRepository {

    List<GenericAssayMeta> getGenericAssayMeta(List<String> stableIds);
}
