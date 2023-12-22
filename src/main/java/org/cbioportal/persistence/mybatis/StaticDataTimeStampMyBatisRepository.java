package org.cbioportal.persistence.mybatis;

import org.cbioportal.model.TableTimestampPair;
import org.cbioportal.persistence.StaticDataTimeStampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StaticDataTimeStampMyBatisRepository implements StaticDataTimeStampRepository {
    @Value("${db.portal_db_name:}")
    private String dbName;
    
    @Autowired
    private StaticDataTimestampMapper staticDataTimestampMapper;

    @Override
    public List<TableTimestampPair> getTimestamps(List<String> tableNames) {
        return staticDataTimestampMapper.getTimestamps(tableNames, dbName);
    }
}
