/*
 * Copyright (c) 2019-2020 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.model;

import java.io.Serializable;
import java.util.*;
import static org.mskcc.cbio.maf.ValueTypeUtil.toFloat;
import static org.mskcc.cbio.maf.ValueTypeUtil.toInt;

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

    public AlleleSpecificCopyNumber(Map<String,Object> ascnData) {
        this.ascnIntegerCopyNumber = toInt(ascnData.getOrDefault(ASCN_INT_COPY_NUMBER, null));
        this.ascnMethod = (String) ascnData.getOrDefault(ASCN_METHOD, null);
        this.ccfExpectedCopiesUpper = toFloat(ascnData.getOrDefault(CCF_EXPECTED_COPIES_UPPER, null));
        this.ccfExpectedCopies = toFloat(ascnData.getOrDefault(CCF_EXPECTED_COPIES, null));
        this.clonal = normalizeClonalValue((String) ascnData.getOrDefault(CLONAL, null));
        this.minorCopyNumber = toInt(ascnData.getOrDefault(MINOR_COPY_NUMBER, null));
        this.expectedAltCopies = toInt(ascnData.getOrDefault(EXPECTED_ALT_COPIES, null));
        this.totalCopyNumber = toInt(ascnData.getOrDefault(TOTAL_COPY_NUMBER, null));
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
        if (clonal == null) {
            return null;
        }
        String upperCaseClonal = clonal.toUpperCase();
        if (ACCEPTED_CLONAL_VALUES.contains(upperCaseClonal)) {
            return upperCaseClonal;
        }
        return "NA";
    }

}
