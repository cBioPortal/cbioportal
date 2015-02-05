package org.mskcc.cbio.importer.cvr.darwin.util.deid;

import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
 * <p/>
 * Created by criscuof on 11/14/14.
 */
public interface DeidMapper {

    @Select("SELECT deidentification_id  as deidentificationid, sample_id as sampleid FROM dvcbio.DEID_TO_SAMPLE_ID WHERE deidentification_id = #{id}")
      @Results({
           @Result(id=true, property="deidentificationid", column="DEIDENTIFICATION_ID"),
              @Result(property="sampleid", column="SAMPLE_ID")
      })
    public Deid getDeidById(Integer id);



    @Select("SELECT * FROM dvcbio.DEID_TO_SAMPLE_ID ")
    @Results({
            @Result(id=true, property="deidentificationid", column="DEIDENTIFICATION_ID"),
            @Result(property="sampleid", column="SAMPLE_ID")
    })
    public List<Deid> getAllDeids();

}
