package org.cbioportal.service;

import org.cbioportal.model.Mutation;
import org.cbioportal.model.OncoKBDataCount;

import java.util.List;

public interface OncoKBService {

    List<OncoKBDataCount> getDataCounts(List<String> attributes, List<Mutation> mutations);
}
