package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.SVMapper;
import org.cbioportal.application.file.model.StructuralVariant;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

import java.util.Set;

public class StructuralVariantService {

    private final SVMapper structuralVariantMapper;

    public StructuralVariantService(SVMapper structuralVariantMapper) {
        this.structuralVariantMapper = structuralVariantMapper;
    }

    public CloseableIterator<StructuralVariant> getStructuralVariants(String geneticDatatypeStableId, Set<String> sampleIds) {
        return new CursorAdapter<>(structuralVariantMapper.getStructuralVariants(geneticDatatypeStableId, sampleIds));
    }
}
