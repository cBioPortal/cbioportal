package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.CnaSegmentMapper;
import org.cbioportal.application.file.model.CnaSegment;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

public class CnaSegmentService {
    private final CnaSegmentMapper cnaSegmentMapper;

    public CnaSegmentService(CnaSegmentMapper cnaSegmentMapper) {
        this.cnaSegmentMapper = cnaSegmentMapper;
    }

    public CloseableIterator<CnaSegment> getCnaSegments(String studyId) {
        return new CursorAdapter<>(cnaSegmentMapper.getCnaSegments(studyId));
    }

    public boolean hasCnaSegments(String studyId) {
        return cnaSegmentMapper.hasCnaSegments(studyId);
    }
}
