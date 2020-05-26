/*
 * Copyright (c) 2019-2020 Memorial Sloan-Kettering Cancer Center.
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

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author averyniceday.
 */
public class AlleleSpecificCopyNumber implements Serializable {
    private final String ASCN_INT_COPY_NUMBER = "ascn_integer_copy_number";
    private final String ASCN_METHOD = "ascn_method";
    private final String CCF_EXPECTED_COPIES_UPPER = "ccf_expected_copies_upper";
    private final String CCF_EXPECTED_COPIES = "ccf_expected_copies";
    private final String CLONAL = "clonal";
    private final String MINOR_COPY_NUMBER = "minor_copy_number";
    private final String EXPECTED_ALT_COPIES = "expected_alt_copies";
    private final String TOTAL_COPY_NUMBER = "total_copy_number";

    private final Set<String> ACCEPTED_CLONAL_VALUES = new HashSet<String>(Arrays.asList("CLONAL", "SUBCLONAL", "INDETERMINATE", "NA"));
    
    private long mutationEventId;
    private int geneticProfileId;
    private int sampleId;
    private Integer ascnIntegerCopyNumber;
    private String ascnMethod;
    private Float ccfExpectedCopiesUpper;
    private Float ccfExpectedCopies;
    private String clonal;
    private Integer minorCopyNumber;
    private Integer expectedAltCopies;
    private Integer totalCopyNumber;

    public AlleleSpecificCopyNumber(Map<String,String> ascnData) {
        this.ascnIntegerCopyNumber = mapContainsValueForKey(ascnData, ASCN_INT_COPY_NUMBER) ? Integer.parseInt(ascnData.get(ASCN_INT_COPY_NUMBER)) : null;
        this.ascnMethod = mapContainsValueForKey(ascnData, ASCN_METHOD) ? ascnData.get(ASCN_METHOD) : null;
        this.ccfExpectedCopiesUpper = mapContainsValueForKey(ascnData, CCF_EXPECTED_COPIES_UPPER) ? Float.parseFloat(ascnData.get(CCF_EXPECTED_COPIES_UPPER)) : null;
        this.ccfExpectedCopies = mapContainsValueForKey(ascnData, CCF_EXPECTED_COPIES) ? Float.parseFloat(ascnData.get(CCF_EXPECTED_COPIES)) : null;
        this.clonal = mapContainsValueForKey(ascnData, CLONAL) ? normalizeClonalValue(ascnData.get(CLONAL)) : null;
        this.minorCopyNumber = mapContainsValueForKey(ascnData, MINOR_COPY_NUMBER) ? Integer.parseInt(ascnData.get(MINOR_COPY_NUMBER)) : null;
        this.expectedAltCopies = mapContainsValueForKey(ascnData, EXPECTED_ALT_COPIES) ? Integer.parseInt(ascnData.get(EXPECTED_ALT_COPIES)) : null;
        this.totalCopyNumber = mapContainsValueForKey(ascnData, TOTAL_COPY_NUMBER) ? Integer.parseInt(ascnData.get(TOTAL_COPY_NUMBER)) : null;
    }

    public void updateAscnUniqueKeyDetails(ExtendedMutation mutation) {
        this.mutationEventId = mutation.getMutationEventId();
        this.geneticProfileId = mutation.getGeneticProfileId();
        this.sampleId = mutation.getSampleId();
    }

    public long getMutationEventId() {
        return mutationEventId;
    }

    public void setMutationEventId(long mutationEventId) {
        this.mutationEventId = mutationEventId;
    }

    public int getGeneticProfileId() {
        return geneticProfileId;
    }

    public void setGeneticProfileId(int geneticProfileId) {
        this.geneticProfileId = geneticProfileId;
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public Integer getAscnIntegerCopyNumber() {
        return ascnIntegerCopyNumber;
    }

    public void setAscnIntegerCopyNumber(Integer ascnIntegerCopyNumber) {
        this.ascnIntegerCopyNumber = ascnIntegerCopyNumber;
    }

    public String getAscnMethod() {
        return ascnMethod;
    }

    public void setAscnMethod(String ascnMethod) {
        this.ascnMethod = ascnMethod;
    }

    public Float getCcfExpectedCopiesUpper() {
        return ccfExpectedCopiesUpper;
    }

    public void setCcfExpectedCopiesUpper(Float ccfExpectedCopiesUpper) {
        this.ccfExpectedCopiesUpper = ccfExpectedCopiesUpper;
    }

    public Float getCcfExpectedCopies() {
        return ccfExpectedCopies;
    }

    public void setCcfExpectedCopies(Float ccfExpectedCopies) {
        this.ccfExpectedCopies = ccfExpectedCopies;
    }

    public String getClonal() {
        return clonal;
    }

    public void setClonal(String clonal) {
        this.clonal = clonal;
    }

    public Integer getMinorCopyNumber() {
        return minorCopyNumber;
    }

    public void setMinorCopyNumber(Integer minorCopyNumber) {
        this.minorCopyNumber = minorCopyNumber;
    }

    public Integer getExpectedAltCopies() {
        return expectedAltCopies;
    }

    public void setExpectedAltCopies(Integer expectedAltCopies) {
        this.expectedAltCopies = expectedAltCopies;
    }

    public Integer getTotalCopyNumber() {
        return totalCopyNumber;
    }

    public void setTotalCopyNumber(Integer totalCopyNumber) {
        this.totalCopyNumber = totalCopyNumber;
    }

    private String normalizeClonalValue(String clonal) {
        String upperCaseClonal = clonal.toUpperCase();
        if (ACCEPTED_CLONAL_VALUES.contains(upperCaseClonal)) {
            return upperCaseClonal;
        }
        return "NA";
    }

    private boolean mapContainsValueForKey(Map<String, String> data, String key) {
        return data.containsKey(key) && !data.get(key).isEmpty();
    }
}
