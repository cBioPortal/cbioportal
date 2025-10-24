package org.cbioportal.application.file.services;

import java.util.Set;
import org.cbioportal.application.file.repositories.MafRecordRepository;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;

public class MafRecordService {

  private final MafRecordRepository mafRecordRepository;

  public MafRecordService(MafRecordRepository mafRecordRepository) {
    this.mafRecordRepository = mafRecordRepository;
  }

  public CloseableIterator<MafRecord> getMafRecords(
      String molecularProfileStableId, Set<String> sampleIds) {
    return mafRecordRepository.getMafRecords(molecularProfileStableId, sampleIds);
  }
}
