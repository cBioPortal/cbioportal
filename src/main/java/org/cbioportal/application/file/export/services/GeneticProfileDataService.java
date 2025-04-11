package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.GeneticProfileDataMapper;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticProfileData;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

import java.util.List;

public class GeneticProfileDataService {
    private final GeneticProfileDataMapper geneticProfileDataMapper;

    public GeneticProfileDataService(GeneticProfileDataMapper geneticProfileDataMapper) {
        this.geneticProfileDataMapper = geneticProfileDataMapper;
    }

    public List<String> getSampleStableIds(String molecularProfileStableId) {
        return geneticProfileDataMapper.getSampleStableIds(molecularProfileStableId);
    }

    public CloseableIterator<GeneticProfileData> getData(String molecularProfileStableId) {
        return new CursorAdapter<>(geneticProfileDataMapper.getData(molecularProfileStableId));
    }

    public List<String> getDistinctGenericEntityMetaPropertyNames(String molecularProfileStableId) {
        return geneticProfileDataMapper.getDistinctGenericEntityMetaPropertyNames(molecularProfileStableId);
    }

    public CloseableIterator<GenericEntityProperty> getGenericEntityMetaProperties(String molecularProfileStableId) {
        return new CursorAdapter<>(geneticProfileDataMapper.getGenericEntityMetaProperties(molecularProfileStableId));
    }
}
