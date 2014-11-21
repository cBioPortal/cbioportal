package org.mskcc.cbio.importer.cvr.darwin.util.deid;

/**
 * Copyright (c) 2014 Memorial Sloan-Kettering Cancer Center.
 * <p/>
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
 * <p/>
 * Created by criscuof on 11/14/14.
 */
public class Deid {
    private Integer deidentificationid;
    private String sampleid;

    public Deid(){}

    public Integer getDeidentificationid() {
        return deidentificationid;
    }

    public void setDeidentificationid(Integer deidentificationid) {
        this.deidentificationid = deidentificationid;
    }

    public String getSampleid() {
        return sampleid;
    }

    public void setSampleid(String sampleid) {
        this.sampleid = sampleid;
    }

    public String toString() {
        return ("Darwin ID: " +this.getDeidentificationid() +"  DMP ID: " +this.getSampleid());
    }
}
