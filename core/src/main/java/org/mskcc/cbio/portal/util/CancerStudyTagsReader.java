package org.mskcc.cbio.portal.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.mskcc.cbio.portal.dao.DaoCancerStudy;
import org.mskcc.cbio.portal.dao.DaoException;
import org.mskcc.cbio.portal.model.CancerStudy;
import org.mskcc.cbio.portal.model.CancerStudyTags;
import org.mskcc.cbio.portal.scripts.TrimmedProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class CancerStudyTagsReader {

    public static CancerStudyTags loadCancerStudyTags(File file, CancerStudy cancerStudy) 
        throws FileNotFoundException, IOException, DaoException {

        TrimmedProperties properties = new TrimmedProperties();
        properties.load(new FileInputStream(file));

        String tags_filename = properties.getProperty("tags_file");
        String tags = null;
        CancerStudyTags cancerStudyTags = null;
        if (tags_filename != null) {        
            String content = new Scanner(new File(file.getParent()+"/"+tags_filename)).useDelimiter("\\Z").next();
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(content, Object.class);

            ObjectMapper jsonWriter = new ObjectMapper();
            tags = jsonWriter.writeValueAsString(obj);

            cancerStudyTags = new CancerStudyTags();
            cancerStudyTags.setCancerStudyId(cancerStudy.getInternalId());
            cancerStudyTags.setTags(tags);
            DaoCancerStudy.addCancerStudyTags(cancerStudyTags);
        }

        return cancerStudyTags;
    }
}