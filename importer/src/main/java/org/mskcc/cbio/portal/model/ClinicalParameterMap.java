/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import java.util.*;

/**
 * Encapsulates any type of Clinical Parameter.
 */
public class ClinicalParameterMap
{
    public static final String NA = "NA";
    private String name;
    private Map <String, String> valueMap;
    private Set<String> uniqueCategories = new HashSet<String>();

    public ClinicalParameterMap(String name, Map<String, String> valueMap)
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

    public Set<String> getDistinctCategories() {
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
