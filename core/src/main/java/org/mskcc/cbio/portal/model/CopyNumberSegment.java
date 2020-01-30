/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mskcc.cbio.portal.model;

/**
 *
 * @author jgao
 */
public class CopyNumberSegment {
    private long segId;
    private int cancerStudyId;
    private int sampleId;
    private String chr; // 1-22,X/Y,M
    private long start;
    private long end;
    private int numProbes;
    private double segMean;

    public CopyNumberSegment(
        int cancerStudyId,
        int sampleId,
        String chr,
        long start,
        long end,
        int numProbes,
        double segMean
    ) {
        this.cancerStudyId = cancerStudyId;
        this.sampleId = sampleId;
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.numProbes = numProbes;
        this.segMean = segMean;
    }

    public int getCancerStudyId() {
        return cancerStudyId;
    }

    public void setCancerStudyId(int cancerStudyId) {
        this.cancerStudyId = cancerStudyId;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getNumProbes() {
        return numProbes;
    }

    public void setNumProbes(int numProbes) {
        this.numProbes = numProbes;
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public long getSegId() {
        return segId;
    }

    public void setSegId(long segId) {
        this.segId = segId;
    }

    public double getSegMean() {
        return segMean;
    }

    public void setSegMean(double segMean) {
        this.segMean = segMean;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }
}
