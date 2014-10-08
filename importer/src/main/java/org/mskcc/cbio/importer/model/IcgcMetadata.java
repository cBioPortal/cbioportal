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

/*
Represents metadata associated with ICGC Cancer studies
Mapped from ICGC panel on Google spreadsheet
*/

public class IcgcMetadata {
    private final String icgcId;
    private final String downloadDirectory;
    private final String tumorType;
    private final String description;
    private final String shortName;
    
    public IcgcMetadata(String[] properties){
        Preconditions.checkArgument(null != properties && properties.length >2, 
                "The properties array is null or invalid");
        this.icgcId = properties[0];
        this.downloadDirectory = properties[1];
        this.tumorType = properties[2];
        this.description = (properties.length >3)?properties[3]:"";
        this.shortName = (properties.length >4)? properties[4]:"";
    }
   
    public String getIcgcId() { return this.icgcId; }
    public String getDownloadDirectory() { return this.downloadDirectory; }
    public String getTumorType() { return this.tumorType;}
    public String getDescription() { return this.description;}
    public String getShortName() { return this.shortName;}
    
   
}
