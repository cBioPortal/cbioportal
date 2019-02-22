/**
 *
 * @author jiaojiao
 */
package org.mskcc.cbio.portal.service;

import org.mskcc.cbio.portal.model.CNSegmentData;
import org.mskcc.cbio.portal.repository.CNSegmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CNSegmentServiceImpl implements CNSegmentService {

    @Autowired
    private CNSegmentRepository cnSegmentRepository;

    @Override
    @PreAuthorize("hasPermission(#cancerStudyId, 'CancerStudyId', 'read')")
    public List<CNSegmentData> getCNSegmentData(String cancerStudyId, List<String> chromosomes, List<String> sampleIds) {
        return cnSegmentRepository.getCNSegmentData(cancerStudyId, chromosomes, sampleIds);
    }
    
    @Override
    @PreAuthorize("hasPermission(#cancerStudyId, 'CancerStudyId', 'read')")
    public String getCNSegmentFile(String cancerStudyId, List<String> sampleIds) {
        List<CNSegmentData> results = cnSegmentRepository.getCNSegmentData(cancerStudyId, null, sampleIds);
        StringBuilder fileContent = new StringBuilder();
        //append file header
        fileContent.append("ID\tchrom\tloc.start\tloc.end\tnum.mark\tseg.mean");
        fileContent.append(System.getProperty("line.separator"));
        //append file content, sperate columns with tab
        for(CNSegmentData item: results){
            fileContent.append(item.getSample());
            fileContent.append("\t");
            fileContent.append(item.getChr());
            fileContent.append("\t");
            fileContent.append(item.getStart());
            fileContent.append("\t");
            fileContent.append(item.getEnd());
            fileContent.append("\t");
            fileContent.append(item.getNumProbes());
            fileContent.append("\t");
            fileContent.append(item.getValue());
            fileContent.append(System.getProperty("line.separator"));
        }
        return fileContent.toString();
    }

}

     
