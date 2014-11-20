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
POJO for ICGC attributes used to support ICGC duplicate detection using
a Bllom Filter
*/
package org.mskcc.cbio.importer.icgc.support;

import com.google.common.base.Joiner;
import com.google.inject.internal.Lists;
import com.google.inject.internal.Preconditions;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;


public class IcgcSimpleSomaticRecord {
    
    
    private final String id;
    private final String projectCode;
    private final String sampleId;
    private final String chromosome;
    private final String start;
    private final String end;
    private final String refAllele;
    private final String mutAllele;
    private final String totalReads;
    private final String mutReads;
    private final Set<String> columnNames;
    
    private static final Joiner tabJoiner = Joiner.on('\t').useForNull(" ");
    
    public IcgcSimpleSomaticRecord (Map<String, String> recordMap ){
        Preconditions.checkArgument(null != recordMap, "A map if ICGC column names and values is required");
        Preconditions.checkArgument(!recordMap.isEmpty(), "The supplied Map object is empty");
        this.id = recordMap.get("icgc_mutation_id");
        this.projectCode = recordMap.get("project_code");
        this.sampleId = recordMap.get("submitted_sample_id");
        this.chromosome = recordMap.get("chromosome");
        this.start = recordMap.get("chromosome_start");
        this.end = recordMap.get("chromosome_end");
        this.refAllele = recordMap.get("reference_genome_allele");
        this.mutAllele = recordMap.get("mutated_to_allele");
        this.totalReads = recordMap.get("total_read_count");
        this.mutReads = recordMap.get("mutant_allele_read_count");  
        this.columnNames = recordMap.keySet();
    }
    
      public static String getColumnNamesTSV() {
        return tabJoiner.join(Lists.newArrayList("icgc_mutation_id", "project_code", "submitted_sample_id",
        "chromosome","chromosome_start", "chromosome_end","reference_genome_allele","mutated_to_allele","total_read_count",
        "mutant_allele_read_count"));
        
    }
    public String getId() {
        return id;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public String getSampleId() {
        return sampleId;
    }

    public String getChromosome() {
        return chromosome;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getRefAllele() {
        return refAllele;
    }

    public String getMutAllele() {
        return mutAllele;
    }

    public String getTotalReads() {
        return totalReads;
    }

    public String getMutReads() {
        return mutReads;
    }
   
  
    
    public String toTSV() {
        return tabJoiner.join(Lists.newArrayList(this.getId(), this.getProjectCode(), this.getSampleId(), this.getChromosome(), this.getStart(), this.getEnd(),
                this.getRefAllele(), this.getMutAllele(),this.getTotalReads(),this.getMutReads()));
    }
    
    public boolean equals(Object other) {
        if(this == other) { return true;}
        if (other == null || other.getClass() != this.getClass()) { return false;}
        IcgcSimpleSomaticRecord record2 = (IcgcSimpleSomaticRecord) other;
        return new EqualsBuilder()
                .append(this.getId(),record2.getId())
                .append(this.getChromosome(),record2.getChromosome())
                .append(this.getEnd(), record2.getEnd())
                .append(this.getMutAllele(), record2.getMutAllele())
                .append(this.getProjectCode(), record2.getProjectCode())
                .append(this.getRefAllele(), record2.getRefAllele())
                .append(this.getSampleId(), record2.getSampleId())
                .append(this.getStart(), record2.getStart())
                .append(this.getTotalReads(),record2.getTotalReads())
                .isEquals();
        
        
    }
    
    public String toString() {
        return new ReflectionToStringBuilder(this).toString();
    }

}
