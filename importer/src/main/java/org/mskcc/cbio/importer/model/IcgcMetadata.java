/*
 *  Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
  * 
  *  This library is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  *  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  *  documentation provided hereunder is on an "as is" basis, and
  *  Memorial Sloan-Kettering Cancer Center 
  *  has no obligations to provide maintenance, support,
  *  updates, enhancements or modifications.  In no event shall
  *  Memorial Sloan-Kettering Cancer Center
  *  be liable to any party for direct, indirect, special,
  *  incidental or consequential damages, including lost profits, arising
  *  out of the use of this software and its documentation, even if
  *  Memorial Sloan-Kettering Cancer Center 
  *  has been advised of the possibility of such damage.
 */

package org.mskcc.cbio.importer.model;

import com.google.gdata.util.common.base.Preconditions;
import org.apache.commons.beanutils.BeanUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/*
Represents metadata associated with ICGC Cancer studies
Mapped from ICGC panel on Google spreadsheet
*/

public class IcgcMetadata {
    private  String icgcid;
    private  String downloaddirectory;
    private  String tumortype;
    private  String description;
    private  String shortname;
    
    public IcgcMetadata(String[] properties){
        Preconditions.checkArgument(null != properties && properties.length >2, 
                "The properties array is null or invalid");
        this.icgcid = properties[0];
        this.downloaddirectory = properties[1];
        this.tumortype = properties[2];
        this.description = (properties.length >3)?properties[3]:"";
        this.shortname = (properties.length >4)? properties[4]:"";
    }

    /*
    constructor based on row from google worksheet
     */
    public IcgcMetadata(Map<String,String> worksheetRowMap){
        try {
            BeanUtils.populate(this, worksheetRowMap);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public String getIcgcid() {
        return icgcid;
    }

    public String getDownloaddirectory() {
        return downloaddirectory;
    }

    public String getTumortype() {
        return tumortype;
    }

    public String getDescription() {
        return description;
    }

    public String getShortname() {
        return shortname;
    }

    public void setIcgcid(String icgcid) {
        this.icgcid = icgcid;
    }

    public void setDownloaddirectory(String downloaddirectory) {
        this.downloaddirectory = downloaddirectory;
    }

    public void setTumortype(String tumortype) {
        this.tumortype = tumortype;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
}
