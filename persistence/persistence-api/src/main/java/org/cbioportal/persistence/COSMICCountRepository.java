package org.cbioportal.persistence;

import java.util.List;
import org.cbioportal.model.COSMICCount;

public interface COSMICCountRepository {
	List<COSMICCount> getCOSMICCountsByKeywords(List<String> keywords);
}
