package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.GenePanelMatrixMapper;
import org.cbioportal.application.file.model.GenePanelMatrixItem;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

import java.util.List;
import java.util.Set;

public class GenePanelMatrixService {
    private final GenePanelMatrixMapper genePanelMatrixMapper;

    public GenePanelMatrixService(GenePanelMatrixMapper genePanelMatrixMapper) {
        this.genePanelMatrixMapper = genePanelMatrixMapper;
    }

    public boolean hasGenePanelMatrix(String studyId, Set<String> sampleIds) {
        return genePanelMatrixMapper.hasGenePanelMatrix(studyId, sampleIds);
    }

    public CloseableIterator<GenePanelMatrixItem> getGenePanelMatrix(String studyId, Set<String> sampleIds) {
       return new CursorAdapter<>(genePanelMatrixMapper.getGenePanelMatrix(studyId, sampleIds));
    }

    public List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(String studyId, Set<String> sampleIds) {
        return genePanelMatrixMapper.getDistinctGeneProfileIdsWithGenePanelMatrix(studyId, sampleIds);
    }
}
