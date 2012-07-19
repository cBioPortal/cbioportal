package org.mskcc.cgds.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Encapsulates any type of Clinical Parameter.
 */
public class ClinicalParameterMap
{
    public static final String NA = "NA";
    private String name;
    private HashMap <String, String> valueMap;
    private HashSet<String> uniqueCategories = new HashSet<String>();

    public ClinicalParameterMap(String name, HashMap<String, String> valueMap)
    {
        this.name = name;
        this.valueMap = valueMap;

        Iterator<String> keyIterator = valueMap.keySet().iterator();
        
        while (keyIterator.hasNext())
        {
            String caseId = keyIterator.next();
            String value = valueMap.get(caseId);
            uniqueCategories.add(value);
        }
    }

    public String getName() {
        return name;
    }

    public HashSet<String> getDistinctCategories() {
        return this.uniqueCategories;
    }

    public String getValue(String caseId)
    {
        if (valueMap.containsKey(caseId)) {
            return valueMap.get(caseId);
        } else {
            return NA;
        }
    }
}
