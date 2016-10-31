/**
 *
 * @author jiaojiao
 */
package org.cbioportal.persistence.mybatis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.cbioportal.model.DataSet;
import org.cbioportal.persistence.DataSetRepository;

@Repository
public class DataSetMyBatisRepository implements DataSetRepository {

    @Autowired
    DataSetMapper dataSetMapper;
    
    @Override
    public List<DataSet> getDataSets() {

        return dataSetMapper.getDataSets();
    }
}