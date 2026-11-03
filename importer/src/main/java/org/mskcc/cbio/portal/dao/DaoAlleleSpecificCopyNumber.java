/*
 * Copyright (c) 2019 - 2020 Memorial Sloan-Kettering Cancer Center.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.mskcc.cbio.portal.dao;

import org.mskcc.cbio.portal.model.*;

/**
 * Data access object for Mutation table
 */
public final class DaoAlleleSpecificCopyNumber {

    public static int addAlleleSpecificCopyNumber(AlleleSpecificCopyNumber ascn) throws DaoException {
        if (!ClickHouseBulkLoader.isBulkLoad()) {
            throw new DaoException("You have to turn on ClickHouseBulkLoader in order to insert allele specific copy numbers");
        } else {
            int result = 1;
            ClickHouseBulkLoader.getClickHouseBulkLoader("allele_specific_copy_number").insertRecord(
                resolveValueToString(ascn.getMutationEventId()),
                resolveValueToString(ascn.getGeneticProfileId()),
                resolveValueToString(ascn.getSampleId()),
                resolveValueToString(ascn.getAscnIntegerCopyNumber()),
                ascn.getAscnMethod(),
                resolveValueToString(ascn.getCcfExpectedCopiesUpper()),
                resolveValueToString(ascn.getCcfExpectedCopies()),
                ascn.getClonal(),
                resolveValueToString(ascn.getMinorCopyNumber()),
                resolveValueToString(ascn.getExpectedAltCopies()),
                resolveValueToString(ascn.getTotalCopyNumber()));
            return result;
        }
    }

    private static String resolveValueToString(Object value) {
        if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }
}
