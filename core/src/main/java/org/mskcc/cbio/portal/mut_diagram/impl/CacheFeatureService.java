/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
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

package org.mskcc.cbio.portal.mut_diagram.impl;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mskcc.cbio.portal.mut_diagram.FeatureService;
import org.mskcc.cbio.portal.mut_diagram.Sequence;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

/**
 * Implementation of FeatureService based on CacheBuilder.
 */
public final class CacheFeatureService implements FeatureService {
    private static final List<Sequence> EMPTY = Collections.emptyList();
    private static final Logger logger = LogManager.getLogger(CacheFeatureService.class);
    private final Cache<String, List<Sequence>> cache;

    /**
     * Create a new cache feature service with a cache populated by the specified cache loader.
     *
     * @param cacheLoader cache loader, must not be null
     */
    public CacheFeatureService(final CacheLoader<String, List<Sequence>> cacheLoader) {
        checkNotNull(cacheLoader, "cacheLoader must not be null");
        cache = CacheBuilder.newBuilder().build(cacheLoader);
    }

    /** {@inheritDoc} */
    public List<Sequence> getFeatures(final String uniProtId) {
        checkNotNull(uniProtId, "uniProtId must not be null");
        List<Sequence> toReturn = cache.getIfPresent(uniProtId);

        if (toReturn == null) {
            logger.error("could not load features from cache for " + uniProtId);
            return EMPTY;
        } else {
            return toReturn;
        }
    }
}
