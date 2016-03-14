
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

import org.mskcc.cbio.portal.model.*;
import org.mskcc.cbio.portal.persistence.ClassifierMapper;
import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.persistence.SampleMapper;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 *
 * @author ochoaa
 */

@Service
public class ClassifierService {
    @Autowired
    private ClassifierMapper classifierMapper;
    @Autowired
    private SampleMapper sampleMapper;
    
    @Transactional
    public Classifier insertClassifierData(String sampleId, String classifierData){
        Map<String, Object> map = new HashMap<String, Object>();
        
        //sampleMapper.getSamplesBySample returns as list, so get the first (only) sample in sampleList to get the sample internalId
        //List<DBSample> sampleList = sampleMapper.getSamplesBySample(studyId, sampleIds);        
        //DBSample sample = sampleList.get(0);
        int internalId = classifierMapper.getInternalIdBySampleId(sampleId);//sample.internal_id;
        
        
        map.put("internal_id", internalId);
        map.put("classifier_data", classifierData);
        classifierMapper.insertClassifierData(map);

        Classifier classifier = new Classifier();
        classifier.internalId = internalId;
        classifier.classifierData = classifierData;

        return classifier;        
    }
    
    public int getInternalIdBySampleId(String sampleId){
        int internalId = classifierMapper.getInternalIdBySampleId(sampleId);
        return internalId;
    }
    
    public Classifier getClassifierData(int internalId){
        //sampleMapper.getSamplesBySample returns as list, so get the first (only) sample in sampleList to get the sample internalId
        //List<DBSample> sampleList = sampleMapper.getSamplesBySample(studyId, sampleIds);        
        //DBSample sample = sampleList.get(0);
        //int internalId = classifierMapper.getInternalIdBySampleId(sampleId);//sample.internal_id;
        
        Classifier classifier = classifierMapper.getClassifierData(internalId);
        return classifier;
    }
        
}
