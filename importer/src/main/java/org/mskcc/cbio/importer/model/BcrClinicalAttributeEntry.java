/*
 * Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
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
 * has been advised of the possibility of such damage.  See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.mskcc.cbio.importer.model;

import org.mskcc.cbio.portal.model.ClinicalAttributeAbstract;
import org.mskcc.cbio.importer.converter.internal.ClinicalDataConverterImpl;

import java.util.HashMap;
import java.util.Map;

public class BcrClinicalAttributeEntry extends ClinicalAttributeAbstract {
    private String diseaseSpecificity;
    private String id;

    private HashMap<String, String> propertiesMap = null;

    public BcrClinicalAttributeEntry(String diseaseSpecificity,
                                     String id,
                                     String displayName,
                                     String description) {
        super(displayName, description);
        this.diseaseSpecificity = diseaseSpecificity;
        this.id = id;
    }

    public BcrClinicalAttributeEntry() {
        this("", "", "", "");
    }

    public String getDiseaseSpecificity() {
        return diseaseSpecificity;
    }

    public void setDiseaseSpecificity(String diseaseSpecificity) {
        this.diseaseSpecificity = diseaseSpecificity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getPropertiesMap() {
        if (propertiesMap == null) {
            propertiesMap = makePropertiesMap();
        }
        return propertiesMap;
    }

    public HashMap<String, String> makePropertiesMap() {
        HashMap<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put("COLUMNHEADER", id.toUpperCase());
        propertiesMap.put("DISPLAYNAME", super.getDisplayName());
        propertiesMap.put("DESCRIPTION", super.getDescription());
        propertiesMap.put("DATATYPE", "");
        propertiesMap.put("ALIASES", id.replaceAll("_",""));
        propertiesMap.put("ANNOTATIONSTATUS", ClinicalDataConverterImpl.UNANNOTATED);
        propertiesMap.put("DISEASESPECIFICITY", this.diseaseSpecificity);

        return propertiesMap;
    }
}
