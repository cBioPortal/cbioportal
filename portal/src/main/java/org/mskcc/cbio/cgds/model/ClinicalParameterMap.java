/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
**
** This library is free software; you can redistribute it and/or modify it
** under the terms of the GNU Lesser General Public License as published
** by the Free Software Foundation; either version 2.1 of the License, or
** any later version.
**
** This library is distributed in the hope that it will be useful, but
** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
** documentation provided hereunder is on an "as is" basis, and
** Memorial Sloan-Kettering Cancer Center 
** has no obligations to provide maintenance, support,
** updates, enhancements or modifications.  In no event shall
** Memorial Sloan-Kettering Cancer Center
** be liable to any party for direct, indirect, special,
** incidental or consequential damages, including lost profits, arising
** out of the use of this software and its documentation, even if
** Memorial Sloan-Kettering Cancer Center 
** has been advised of the possibility of such damage.  See
** the GNU Lesser General Public License for more details.
**
** You should have received a copy of the GNU Lesser General Public License
** along with this library; if not, write to the Free Software Foundation,
** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
**/

package org.mskcc.cbio.cgds.model;

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
