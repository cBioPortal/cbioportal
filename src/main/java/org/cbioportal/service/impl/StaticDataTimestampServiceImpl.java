package org.cbioportal.service.impl;

import org.cbioportal.model.TableTimestampPair;
import org.cbioportal.service.StaticDataTimestampService;
import org.cbioportal.persistence.StaticDataTimeStampRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StaticDataTimestampServiceImpl implements StaticDataTimestampService {
    @Autowired
    private StaticDataTimeStampRepository staticDataTimeStampRepository;

    @Override
    public Map<String, String> getTimestamps(List<String> tables) {
        List<TableTimestampPair> timestamps = staticDataTimeStampRepository.getTimestamps(tables);
        return timestamps.stream()
                .collect(Collectors.toMap(
                        TableTimestampPair::getTableName,
                        TableTimestampPair::getUpdateTime));
    }
    
    @Override
    public Map<String, Date> getTimestampsAsDates(List<String> tables) {
        List<TableTimestampPair> timestamps = staticDataTimeStampRepository.getTimestamps(tables);
        return timestamps.stream()
            .collect(Collectors.toMap(
                TableTimestampPair::getTableName,
                (pair) -> toDate(pair.getUpdateTime())));
    }
    
    private Date toDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.sss").parse(date);
        } catch (ParseException ignored) {
            return new Date();
        }
    }
}


