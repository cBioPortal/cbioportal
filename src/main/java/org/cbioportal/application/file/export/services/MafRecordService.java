package org.cbioportal.application.file.export.services;

import org.cbioportal.application.file.export.mappers.MafRecordMapper;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;
import org.cbioportal.application.file.utils.CursorAdapter;

public class MafRecordService {

    private final MafRecordMapper mafRecordMapper;

    public MafRecordService(MafRecordMapper mafRecordMapper) {
        this.mafRecordMapper = mafRecordMapper;
    }

    public CloseableIterator<MafRecord> getMafRecords(String molecularProfileStableId) {
        return new CursorAdapter<>(mafRecordMapper.getMafRecords(molecularProfileStableId));
    }
}
