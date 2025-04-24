package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.GenePanelMatrixMapper;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

import java.util.List;

public class GenePanelMatrixService {
    private final GenePanelMatrixMapper genePanelMatrixMapper;

    public GenePanelMatrixService(GenePanelMatrixMapper genePanelMatrixMapper) {
        this.genePanelMatrixMapper = genePanelMatrixMapper;
    }

    public boolean hasGenePanelMatrix(String studyId) {
        return genePanelMatrixMapper.hasGenePanelMatrix(studyId);
    }

    public CloseableIterator<GenePanelMatrixItem> getGenePanelMatrix(String studyId) {
       return new CursorAdapter<>(genePanelMatrixMapper.getGenePanelMatrix(studyId));
    }

    public List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(String studyId) {
        return genePanelMatrixMapper.getDistinctGeneProfileIdsWithGenePanelMatrix(studyId);
    }
}
