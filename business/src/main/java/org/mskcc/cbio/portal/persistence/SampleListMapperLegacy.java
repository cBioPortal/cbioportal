/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mskcc.cbio.portal.persistence;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.mskcc.cbio.portal.model.DBSample;
import org.mskcc.cbio.portal.model.DBSampleList;

/**
 *
 * @author abeshoua
 */
public interface SampleListMapperLegacy {
    // By 'Incomplete' we mean without the actual sample lists, only the metadata
    List<DBSampleList> getIncompleteSampleLists(
        @Param("sample_list_ids") List<String> sample_list_ids
    );
    List<DBSampleList> getIncompleteSampleListsByStudy(
        @Param("study_id") String study_id
    );
    List<DBSampleList> getAllIncompleteSampleLists();
    List<DBSample> getSampleIds(@Param("list_id") String list_id);
}
