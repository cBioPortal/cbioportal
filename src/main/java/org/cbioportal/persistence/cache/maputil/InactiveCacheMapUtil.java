/*
 * Copyright (c) 2018 - 2019 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.persistence.cache.maputil;

import java.util.Map;
import org.cbioportal.model.CancerStudy;
import org.cbioportal.model.MolecularProfile;
import org.cbioportal.model.SampleList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
// This implementation of the CacheMapUtils is instantiated on portals where all uses can access any study.
@ConditionalOnProperty(name = "security.method_authorization_enabled", havingValue = "false", matchIfMissing = true)
public class InactiveCacheMapUtil implements CacheMapUtil {

	// Since user-permission evaluation is not needed when this bean is present,
	// throw an error when it is accessed.

	@Override
	public Map<String, MolecularProfile> getMolecularProfileMap() {
		throw new RuntimeException(
				"A CacheMapUtils method was called on a portal where studies are accessible to all users.");
	}

	@Override
	public Map<String, SampleList> getSampleListMap() {
		throw new RuntimeException(
				"A CacheMapUtils method was called on a portal where studies are accessible to all users.");
	}

	@Override
	public Map<String, CancerStudy> getCancerStudyMap() {
		throw new RuntimeException(
				"A CacheMapUtils method was called on a portal where studies are accessible to all users.");
	}

	// bean is only instantiated when there is no user authorization
	@Override
	public boolean hasCacheEnabled() {
		return false;
	}

}
