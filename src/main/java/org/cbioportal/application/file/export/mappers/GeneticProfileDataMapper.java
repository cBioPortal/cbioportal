package org.cbioportal.application.file.export.mappers;

import java.util.List;
import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.GenericEntityProperty;
import org.cbioportal.application.file.model.GeneticProfileData;

public interface GeneticProfileDataMapper {

  List<String> getSampleStableIds(String molecularProfileStableId);

  Cursor<GeneticProfileData> getData(String molecularProfileStableId);

  List<String> getDistinctGenericEntityMetaPropertyNames(String molecularProfileStableId);

  Cursor<GenericEntityProperty> getGenericEntityMetaProperties(String molecularProfileStableId);
}
