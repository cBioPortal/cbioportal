package org.cbioportal.application.file.export;

import org.cbioportal.application.file.export.mappers.MafRecordMapper;
import org.cbioportal.application.file.model.MafRecord;

import java.util.Iterator;

public class MafRecordService {

    private final MafRecordMapper mafRecordMapper;

    public MafRecordService(MafRecordMapper mafRecordMapper) {
        this.mafRecordMapper = mafRecordMapper;
    }

    public Iterator<MafRecord> getMafRecords(String molecularProfileStableId) {
        return mafRecordMapper.getMafRecords(molecularProfileStableId).iterator();
    }
}
