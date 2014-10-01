/** Copyright (c) 2012 Memorial Sloan-Kettering Cancer Center.
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
 * has been advised of the possibility of such damage.
*/
package org.mskcc.cbio.portal.model;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jgao
 */
public class Coexpression {
    private long gene1;
    private long gene2;
    private int profileId;
    private double pearson;
    private double spearman;

    public Coexpression(long gene1, long gene2, int profileId, double pearson, double spearman) {
        this.gene1 = gene1;
        this.gene2 = gene2;
        this.profileId = profileId;
        this.pearson = pearson;
        this.spearman = spearman;
    }

    public long getGene1() {
        return gene1;
    }

    public void setGene1(long gene1) {
        this.gene1 = gene1;
    }

    public long getGene2() {
        return gene2;
    }

    public void setGene2(long gene2) {
        this.gene2 = gene2;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public double getPearson() {
        return pearson;
    }

    public void setPearson(double pearson) {
        this.pearson = pearson;
    }

    public double getSpearman() {
        return spearman;
    }

    public void setSpearman(double spearman) {
        this.spearman = spearman;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.profileId;
        Set<Long> genes = new HashSet<Long>(2);
        genes.add(gene1);
        genes.add(gene2);
        hash = 97 * hash + (genes != null ? genes.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Coexpression other = (Coexpression) obj;
        if (this.profileId != other.profileId) {
            return false;
        }
        
        Set<Long> genes = new HashSet<Long>(2);
        genes.add(gene1);
        genes.add(gene2);
        
        Set<Long> otherGenes = new HashSet<Long>(2);
        genes.add(other.gene1);
        genes.add(other.gene2);
        
        if (!genes.equals(otherGenes)) {
            return false;
        }
        return true;
    }
    
    
    
    
}
