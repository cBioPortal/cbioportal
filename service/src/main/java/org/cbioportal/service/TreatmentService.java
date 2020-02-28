package org.cbioportal.service;

import org.cbioportal.model.TreatmentRow;

import java.util.List;

public interface TreatmentService {
    public List<TreatmentRow> getAllTreatmentRows(List<String> samples, List<String> studies);
}
