/*
 * Copyright (c) 2019 The Hyve B.V.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.cbioportal.persistence.mybatis;

import java.util.List;

import org.cbioportal.model.Treatment;
import org.cbioportal.model.meta.BaseMeta;
import org.cbioportal.persistence.TreatmentRepository;
import org.cbioportal.persistence.mybatis.util.OffsetCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.backoff.ThreadWaitSleeper;
import org.springframework.stereotype.Repository;

@Repository
public class TreatmentMyBatisRepository implements TreatmentRepository {

    @Autowired
    TreatmentMapper treatmentMapper;
    @Autowired
    private OffsetCalculator offsetCalculator;

	@Override
	public List<Treatment> getAllTreatments(String projection, Integer pageSize, Integer pageNumber) {
		return this.getTreatmentsInStudies(null, projection, pageSize, offsetCalculator.calculate(pageSize, pageNumber), "stableId", "ASC");
	}

	@Override
	public BaseMeta getMetaTreatments() {
		return treatmentMapper.getMetaTreatments(null);
	}

	@Override
	public BaseMeta getMetaTreatments(List<String> treatmentIds) {
		return treatmentMapper.getMetaTreatments(treatmentIds);
	}

	@Override
	public BaseMeta getMetaTreatmentsInStudies(List<String> studyIds) {
		return treatmentMapper.getMetaTreatmentsInStudies(studyIds);
	}
	
	@Override
	public Treatment getTreatmentByStableId(String treatmentId) {
		return treatmentMapper.getTreatmentByStableId(treatmentId);
	}
	
	@Override
	public List<Treatment> fetchTreatments(List<String> treatmentIds) {
		return treatmentMapper.fetchTreatments(treatmentIds);
	}
	
	@Override
	public List<Treatment> getTreatmentsInStudies(List<String> studyIds, String projection, Integer limit, Integer offset, String sortBy, String direction) {
		return treatmentMapper.getTreatmentsInStudies(studyIds, projection, limit, offset, sortBy, direction);
	}

	@Override
	public List<Treatment> getTreatments(List<String> treatmentIds, String projection) {
		return null;
	}

}