package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.SVMapper;
import org.cbioportal.application.file.model.StructuralVariant;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

public class StructuralVariantService {

    private final SVMapper structuralVariantMapper;

    public StructuralVariantService(SVMapper structuralVariantMapper) {
        this.structuralVariantMapper = structuralVariantMapper;
    }

    public CloseableIterator<StructuralVariant> getStructuralVariants(String geneticDatatypeStableId) {
        return new CursorAdapter<>(structuralVariantMapper.getStructuralVariants(geneticDatatypeStableId));
    }
}
