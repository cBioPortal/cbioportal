package org.cbioportal.application.file.export.repositories.mybatis;

import java.util.Set;
import org.cbioportal.application.file.export.repositories.MafRecordRepository;
import org.cbioportal.application.file.export.repositories.mybatis.utils.CursorAdapter;
import org.cbioportal.application.file.model.MafRecord;
import org.cbioportal.application.file.utils.CloseableIterator;

public class MafRecordMyBatisRepository implements MafRecordRepository {

  private final MafRecordMapper mapper;

  public MafRecordMyBatisRepository(MafRecordMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public CloseableIterator<MafRecord> getMafRecords(
      String molecularProfileStableId, Set<String> sampleIds) {
    return new CursorAdapter<>(mapper.getMafRecords(molecularProfileStableId, sampleIds));
  }
}
