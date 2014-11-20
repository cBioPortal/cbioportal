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

/*
POJO for ICGC clinicalattributes used to support duplicate detection 
 of clinical data records using a Bloom Filter
just use identifier columns
*/
package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Preconditions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;


public class IcgcClinicalRecord {
    
    
    private final String donorId;
    private final String submitted_donor_id;
    private final String icgc_specimen_id;
    private  final String submitted_specimen_id;
    
    private final Set<String> columnNames;
   
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    private static final Splitter usSplitter = Splitter.on('_'); 
    
    public IcgcClinicalRecord (Map<String, String> recordMap ){
        Preconditions.checkArgument(null != recordMap, "A map of ICGC column names and values is required");
        Preconditions.checkArgument(!recordMap.isEmpty(), "The supplied Map object is empty");
        this.donorId = recordMap.get("icgc_donor_id"); 
        this.submitted_donor_id = recordMap.get("submitted_donor_id");
        this.icgc_specimen_id = recordMap.get("icgc_specimen_id");
        this.submitted_specimen_id = recordMap.get("submitted_specimen_id");   
        this.columnNames = recordMap.keySet();
    }
    
      public static String getColumnNamesTSV() {
        return tabJoiner.join(Lists.newArrayList("icgc_donor_id", "submitted_donor_id", "icgc_specimen_id",
        "submitted_specimen_id"));  
    }
   
    public String getDonorId() {
        return donorId;
    }
    
    public String getSubmittedDonorId() { return this.submitted_donor_id; }


    public String getIcgcSpecimenId() {
        return icgc_specimen_id;
    }

    public String getSubmittedSpecimenId() { return this.submitted_specimen_id;} 
  
    
    public String toTSV() {
        return tabJoiner.join(Lists.newArrayList(this.getDonorId(),this.getSubmittedDonorId(),this.getIcgcSpecimenId(), this.getSubmittedSpecimenId()));
    }
    
    public boolean equals(Object other) {
        if(this == other) { return true;}
        if (other == null || other.getClass() != this.getClass()) { return false;}
        IcgcClinicalRecord record2 = (IcgcClinicalRecord) other;
        return new EqualsBuilder()
                .append(this.getDonorId(),record2.getDonorId())
                .append(this.getIcgcSpecimenId(), record2.getIcgcSpecimenId())
                .append(this.getSubmittedDonorId(), record2.getSubmittedDonorId())
                .append(this.getSubmittedSpecimenId(), record2.getSubmittedSpecimenId())
                .isEquals();
        
        
    }
    
    public String toString() {
        return new ReflectionToStringBuilder(this).toString();
    }

}
