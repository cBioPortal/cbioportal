package org.cbioportal.persistence.clickhouse;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.model.Gistic;
import org.cbioportal.model.GisticToGene;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.SignificantCopyNumberRegionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("clickhouse")
public class SignificantCopyNumberRegionClickHouseRepository implements SignificantCopyNumberRegionRepository {

	@Override
	public List<Gistic> getSignificantCopyNumberRegions(String studyId, String projection, Integer pageSize,
			Integer pageNumber, String sortBy, String direction) {
		// TODO Auto-generated method stub
		return new ArrayList<Gistic>();
	}

	@Override
	public BaseMeta getMetaSignificantCopyNumberRegions(String studyId) {
		// TODO Auto-generated method stub
		return new BaseMeta();
	}

	@Override
	public List<GisticToGene> getGenesOfRegions(List<Long> gisticRoiIds) {
		// TODO Auto-generated method stub
		return new ArrayList<GisticToGene>();
	}

}
