package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.GenePanelMatrixItem;

import java.util.List;

public interface GenePanelMatrixMapper {

    boolean hasGenePanelMatrix(String studyId);

    Cursor<GenePanelMatrixItem> getGenePanelMatrix(String studyId);

    List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(String studyId);
}
