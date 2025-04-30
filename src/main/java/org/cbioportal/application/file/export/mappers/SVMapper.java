package org.cbioportal.application.file.export.mappers;

import org.apache.ibatis.cursor.Cursor;
import org.cbioportal.application.file.model.StructuralVariant;

import java.util.Set;

public interface SVMapper {
    Cursor<StructuralVariant> getStructuralVariants(String molecularProfileStableId, Set<String> sampleIds);
}
