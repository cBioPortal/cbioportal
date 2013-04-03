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

import java.util.Collections;
import java.util.Set;

/**
 * Class for protein array information
 * @author jj
 */
public class ProteinArrayInfo {
    private String id;
    private String type;
    private String gene;
    private String residue;
    private Set<Integer> cancerStudies; 

    public ProteinArrayInfo(String id, String type, String gene, 
            String residue, Set<Integer> cancerStudies) {
        this.id = id;
        this.type = type;
        this.gene = gene;
        this.residue = residue;
        this.cancerStudies = cancerStudies;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getResidue() {
        return residue;
    }

    public void setResidue(String residue) {
        this.residue = residue;
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<Integer> getCancerStudies() {
        if (cancerStudies==null) {
            return Collections.emptySet();
        }
        return cancerStudies;
    }

    public void setCancerStudies(Set<Integer> cancerStudies) {
        this.cancerStudies = cancerStudies;
    }
}
