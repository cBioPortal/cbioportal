package org.cbioportal.application.file.export.mappers;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.GenePanelMatrixItem;

public interface GenePanelMatrixMapper {

  boolean hasGenePanelMatrix(String studyId, Set<String> sampleIds);

  Cursor<GenePanelMatrixItem> getGenePanelMatrix(String studyId, Set<String> sampleIds);

  List<String> getDistinctGeneProfileIdsWithGenePanelMatrix(String studyId, Set<String> sampleIds);
}
