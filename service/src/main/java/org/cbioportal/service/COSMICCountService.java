package org.cbioportal.service;


import java.util.List;
import org.cbioportal.model.COSMICCount;

public interface COSMICCountService {

    List<COSMICCount> getCOSMICCountsByKeywords(List<String> keywords);
}
