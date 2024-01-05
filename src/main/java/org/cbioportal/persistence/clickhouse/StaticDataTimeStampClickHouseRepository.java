package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.TableTimestampPair;
import org.cbioportal.persistence.StaticDataTimeStampRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class StaticDataTimeStampClickHouseRepository implements StaticDataTimeStampRepository {

	@Override
	public List<TableTimestampPair> getTimestamps(List<String> tables) {
		// TODO Auto-generated method stub
		return new ArrayList<TableTimestampPair>();
	}

}
