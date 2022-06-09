package org.cbioportal.persistence.mybatis.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;

public class TsvParser<T> {
    public LinkedList<T> readTsv(String input, Class<T> t) {
        LinkedList<T> list = new LinkedList<>();
        StringReader reader = new StringReader(input);
        CsvMapper m = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator('\t');
        try {
            MappingIterator<T> r = m.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).readerFor(t).with(schema).readValues(reader);
            while (r.hasNext()) {
                list.add(r.nextValue());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
