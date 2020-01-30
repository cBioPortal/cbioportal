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

package org.mskcc.cbio.portal.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates any type of Clinical Parameter.
 */
public class ClinicalParameterMap {
    public static final String NA = "NA";
    private String name;
    private Map<String, String> valueMap;
    private Set<String> uniqueCategories = new HashSet<String>();

    public ClinicalParameterMap(String name, Map<String, String> valueMap) {
        this.name = name;
        this.valueMap = valueMap;

        Iterator<String> keyIterator = valueMap.keySet().iterator();

        while (keyIterator.hasNext()) {
            String caseId = keyIterator.next();
            String value = valueMap.get(caseId);
            uniqueCategories.add(value);
        }
    }

    public String getName() {
        return name;
    }

    public Set<String> getDistinctCategories() {
        return this.uniqueCategories;
    }

    public String getValue(String caseId) {
        if (valueMap.containsKey(caseId)) {
            return valueMap.get(caseId);
        } else {
            return NA;
        }
    }
}
