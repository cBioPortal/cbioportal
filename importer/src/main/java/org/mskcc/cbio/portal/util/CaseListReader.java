package org.mskcc.cbio.portal.util;

import org.mskcc.cbio.portal.scripts.TrimmedProperties;

import java.io.*;
import java.util.*;

public class CaseListReader {

    public static CaseList readFile(File caseListFile) {
        Properties properties = new TrimmedProperties();
        try {
            properties.load(new FileReader(caseListFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String stableId = properties.getProperty("stable_id");
        String cancerStudyIdentifier = properties.getProperty("cancer_study_identifier");
        String caseListName = properties.getProperty("case_list_name");
        String caseListDescription = properties.getProperty("case_list_description");
        String caseListCategory = properties.getProperty("case_list_category");
        String caseListIds = properties.getProperty("case_list_ids");
        List<String> sampleIds = caseListIds == null ? List.of()
                : Arrays.stream(caseListIds.split("\t")).toList();

        return new CaseList(
                stableId,
                cancerStudyIdentifier,
                caseListName,
                caseListDescription,
                caseListCategory,
                sampleIds
        );
    }


}
