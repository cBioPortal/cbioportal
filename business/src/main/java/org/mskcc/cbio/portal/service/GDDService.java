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

package org.mskcc.cbio.portal.service;

import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.persistence.SampleMapper;
import org.mskcc.cbio.portal.persistence.GDDMapper;


import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
/**
 * Service for importing and retrieving GDD data. 
 */

@Service
public class GDDService {
    @Autowired
    private GDDMapper gddMapper;   
    @Autowired
    private ApiService service;

    
    @Transactional
    public Map<String, Object> insertGddData(String stable_id, String classification) {
        
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("stable_id", stable_id);
        map.put("classification", classification);
        
        gddMapper.insertGddData(map);
        
        return map;
        
    }

    @Transactional
    public List<String> getGddData(String study_id, List<String> sample_ids) {
        List<DBSample> samples = service.getSamplesBySample(study_id, sample_ids);  
        List<String> stable_ids = getStableIds(samples);
        return gddMapper.getGddData(stable_ids);
    }
    
    private List<String> getStableIds(List<DBSample> samples) {
        List<String> stable_ids = new ArrayList<>();
        for (DBSample sp : samples) {
            stable_ids.add(sp.id);            
        }
        return stable_ids;
    }
}
