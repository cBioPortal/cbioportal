package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.GenePanelMatrixItem;

import java.util.List;
import java.util.Set;

public interface GenePanelMatrixMapper {

    boolean hasGenePanelMatrix(String studyId, Set<String> sampleIds);

    Cursor<GenePanelMatrixItem> getGenePanelMatrix(String studyId, Set<String> sampleIds);

    List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(String studyId, Set<String> sampleIds);
}
