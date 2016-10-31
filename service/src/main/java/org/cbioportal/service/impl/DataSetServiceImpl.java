/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cbioportal.service.impl;

import java.util.List;
import org.cbioportal.model.DataSet;
import org.cbioportal.persistence.DataSetRepository;
import org.cbioportal.service.DataSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author jiaojiao
 */
@Service
public class DataSetServiceImpl implements DataSetService{
    @Autowired
    private DataSetRepository dataSetRepository;

    @Override
    public List<DataSet> getDataSets() {
        return dataSetRepository.getDataSets();
    }
}
